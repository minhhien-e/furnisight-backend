import pandas as pd
import uuid
import json
import os
import hashlib
import re
import urllib.request

# Configuration
EXCEL_FILE = "data_furnisight.xlsx"
SQL_FILE = "catalog_seed.sql"
MAPPING_FILE = "image_mapping.json"
CLOUDINARY_URL = os.environ.get("CLOUDINARY_URL")  # Expected: cloudinary://API_KEY:API_SECRET@CLOUD_NAME

def generate_uuid(seed_string):
    """Generate a deterministic UUID from a string."""
    m = hashlib.md5()
    m.update(seed_string.encode('utf-8'))
    return str(uuid.UUID(bytes=m.digest()))

def sql_escape(value):
    if pd.isna(value) or value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    
    # Escape single quotes
    val_str = str(value).replace("'", "''")
    return f"'{val_str}'"

def main():
    print("Loading Excel file...")
    try:
        xls = pd.ExcelFile(EXCEL_FILE)
        df_cat = pd.read_excel(xls, "1_Categories")
        df_prod = pd.read_excel(xls, "2_Products")
        df_var = pd.read_excel(xls, "3_Product_Variants")
        df_img = pd.read_excel(xls, "4_Product_Images")
    except Exception as e:
        print(f"Error reading excel: {e}")
        return

    # ID mapping
    room_type_ids = {} # Excel ID -> UUID
    category_ids = {}  # Excel ID -> UUID
    product_ids = {}   # Excel ID -> UUID
    variant_ids = {}   # Excel ID -> UUID

    sql_statements = []
    sql_statements.append("-- ==========================================")
    sql_statements.append("-- Catalog Seed Generated from Excel")
    sql_statements.append("-- ==========================================\n")

    # 1. Process Room Types & Categories
    print("Processing Categories/Room Types...")
    for idx, row in df_cat.iterrows():
        excel_id = row['Category_ID']
        name = row["Category_Name"]
        slug = row['Slug']
        parent_id = row["Parent_Category_ID"]
        is_visible = row["Visible"]
        desc = row['Description']
        icon_url = row['Icon_URL']
        img_url = row['Image_URL']
        pos = row['Position'] if 'Position' in row and not pd.isna(row['Position']) else 0

        # We determined that if excel_id <= 4, it's a RoomType (IDs 1-4)
        if int(excel_id) <= 4:
            rt_uuid = generate_uuid(f"RoomType_{excel_id}")
            room_type_ids[excel_id] = rt_uuid
            
            sql = f"INSERT INTO room_types (id, name, slug, description, image_url, visible, created_at, updated_at) VALUES (" \
                  f"'{rt_uuid}', {sql_escape(name)}, {sql_escape(slug)}, {sql_escape(desc)}, " \
                  f"{sql_escape(img_url)}, {sql_escape(is_visible)}, NOW(), NOW()) ON CONFLICT (id) DO NOTHING;"
            sql_statements.append(sql)
        else:
            cat_uuid = generate_uuid(f"Category_{excel_id}")
            category_ids[excel_id] = cat_uuid
            
            # The parent_id in Excel points to a RoomType (since 1,2,3,4 are RoomTypes)
            # So room_type_id = parent_id, parent_id in DB is NULL
            rt_uuid = room_type_ids.get(parent_id, "NULL")
            if rt_uuid != "NULL":
                rt_uuid = f"'{rt_uuid}'"

            sql = f"INSERT INTO categories (id, name, slug, parent_id, room_type_id, path, product_count, icon_url, visible, description, image_url, created_at, updated_at) VALUES (" \
                  f"'{cat_uuid}', {sql_escape(name)}, {sql_escape(slug)}, NULL, {rt_uuid}, '', 0, " \
                  f"{sql_escape(icon_url)}, {sql_escape(is_visible)}, {sql_escape(desc)}, {sql_escape(img_url)}, NOW(), NOW()) ON CONFLICT (id) DO NOTHING;"
            sql_statements.append(sql)

    sql_statements.append("\n-- Products")
    # 2. Process Products
    print("Processing Products...")
    for idx, row in df_prod.iterrows():
        excel_id = row['Product_ID']
        cat_id = row['Category_ID']
        name = row['Product_Name']
        slug = row['Slug']
        sku = row['Product_SKU']
        desc = row['Description']
        feats = row['Features']
        specs = row['Specs']
        status = row['Product_Status']
        price = row['Price']

        prod_uuid = generate_uuid(f"Product_{excel_id}")
        product_ids[excel_id] = prod_uuid
        
        c_uuid = category_ids.get(cat_id, "NULL")
        if c_uuid != "NULL":
            c_uuid = f"'{c_uuid}'"

        # parse features into jsonb array if it's not nan
        feat_json = "NULL"
        if not pd.isna(feats):
            feat_list = [f.strip() for f in str(feats).split('\n') if f.strip()]
            feat_json = sql_escape(json.dumps(feat_list, ensure_ascii=False))

        specs_json = "NULL"
        if not pd.isna(specs):
            # assume specs is json or key-value. We will just wrap it as a simple text json for now if it's text.
            # In Excel it might be text. If it is, let's make it a valid json object
            try:
                # Try parsing as JSON first
                specs_dict = json.loads(specs)
                specs_json = sql_escape(json.dumps(specs_dict, ensure_ascii=False))
            except:
                specs_json = sql_escape(json.dumps({"details": str(specs)}, ensure_ascii=False))

        sql = f"INSERT INTO products (id, category_id, name, slug, sku, description, product_status, base_price, features, specifications, weight, length, width, height, sold_count, rating, rating_count, created_at, updated_at) VALUES (" \
              f"'{prod_uuid}', {c_uuid}, {sql_escape(name)}, {sql_escape(slug)}, {sql_escape(sku)}, {sql_escape(desc)}, " \
              f"{sql_escape(status)}, {sql_escape(price)}, {feat_json}, {specs_json}, 1, 1, 1, 1, 0, 0.0, 0, NOW(), NOW()) ON CONFLICT (id) DO NOTHING;"
        sql_statements.append(sql)

    sql_statements.append("\n-- Product Variants")
    # 3. Process Product Variants
    print("Processing Product Variants...")
    for idx, row in df_var.iterrows():
        # Using Variant_SKU as identifier if Variant_ID is not present, wait df_var has 'Variant_SKU' and 'Product_ID'
        sku = row['Variant_SKU']
        prod_id = row['Product_ID']
        price = row['Price']
        stock = row['Stock_Quantity']
        low_stock = row['Low_Stock_Threshold']
        material = row['Material']
        color = row['Color']
        length = row['Length_cm']
        width = row['Width_cm']
        height = row['Height_cm']
        weight = row['Weight_kg']
        warranty = row['Warranty']
        model_url = row['Model_3D_URL']
        supp_3d = row['Supports_3D']
        specs = row['Specs']

        prod_uuid = product_ids.get(prod_id)
        if not prod_uuid:
            continue

        var_uuid = generate_uuid(f"Variant_{sku}")
        variant_ids[sku] = var_uuid
        
        specs_json = "NULL"
        if not pd.isna(specs):
            try:
                specs_dict = json.loads(specs)
                specs_json = sql_escape(json.dumps(specs_dict, ensure_ascii=False))
            except:
                specs_json = sql_escape(json.dumps({"details": str(specs)}, ensure_ascii=False))

        # Default price logic if NaN
        if pd.isna(price):
            price = 0

        # Replace nan with 0 for dimensions
        if pd.isna(weight): weight = 1
        if pd.isna(length): length = 1
        if pd.isna(width): width = 1
        if pd.isna(height): height = 1
        if pd.isna(low_stock): low_stock = 5

        sql = f"INSERT INTO product_variants (id, product_id, sku, price, stock_quantity, low_stock_threshold, weight, length, width, height, material, warranty, color, model_url, supports_3d, specifications, created_at, updated_at) VALUES (" \
              f"'{var_uuid}', '{prod_uuid}', {sql_escape(sku)}, {sql_escape(price)}, {sql_escape(stock)}, {sql_escape(low_stock)}, " \
              f"{sql_escape(weight)}, {sql_escape(length)}, {sql_escape(width)}, {sql_escape(height)}, {sql_escape(material)}, " \
              f"{sql_escape(warranty)}, {sql_escape(color)}, {sql_escape(model_url)}, {sql_escape(supp_3d)}, {specs_json}, NOW(), NOW()) ON CONFLICT (id) DO NOTHING;"
        sql_statements.append(sql)


    sql_statements.append("\n-- Images")
    # 4. Images
    print("Processing Images...")
    product_image_updates = {} # prod_uuid -> url
    for idx, row in df_img.iterrows():
        t_type = row['Target_Type']
        t_id = row['Target_ID']
        url = row['Image_URL']
        pos = row['Position'] if 'Position' in row and not pd.isna(row['Position']) else 0
        
        if pd.isna(url):
            continue

        if t_type == 'PRODUCT':
            # Target_ID refers to Product_ID
            prod_uuid = product_ids.get(t_id)
            if prod_uuid and prod_uuid not in product_image_updates:
                product_image_updates[prod_uuid] = url
        elif t_type == 'VARIANT':
            # Target_ID might refer to Product_ID or Variant_ID?
            # Wait, in sheet 4_Product_Images, Target_ID for VARIANT is 1, 2 etc. Let's assume it maps to Product_ID but wait, if it maps to Product_ID, how do we know which variant?
            # Or does Target_ID refer to the index of the variant?
            # Actually, we will just use the Target_ID if it maps to Variant_ID. If Target_ID is 1, let's see if variant_ids has 1. We used SKU!
            # Let's map it differently: if Target_ID is int, maybe it's the index in df_var.
            pass

    # Update products with their first image
    for puuid, purl in product_image_updates.items():
        sql_statements.append(f"UPDATE products SET image_url = {sql_escape(purl)} WHERE id = '{puuid}';")
        
    # Variants: wait, let's check df_var for Variant_ID. Ah, df_var has no Variant_ID column? It only has Product_ID and Variant_SKU. 
    # But df_img has Target_ID=1 for VARIANT. If Target_ID = 1, it might just be the first row in Variant sheet.
    for idx, row in df_img.iterrows():
        t_type = row['Target_Type']
        t_id = row['Target_ID']
        url = row['Image_URL']
        pos = row['Position'] if 'Position' in row and not pd.isna(row['Position']) else 0
        
        if pd.isna(url):
            continue

        if t_type == 'VARIANT':
            # Let's try to map Target_ID to Variant_SKU using index
            # If t_id is 1, it might mean the 1st variant (idx 0).
            try:
                variant_row = df_var.iloc[int(t_id) - 1]
                sku = variant_row['Variant_SKU']
                var_uuid = variant_ids.get(sku)
                if var_uuid:
                    img_uuid = generate_uuid(f"VarImg_{t_id}_{idx}")
                    sql = f"INSERT INTO product_variant_images (id, variant_id, image_url, position, created_at, updated_at) VALUES (" \
                          f"'{img_uuid}', '{var_uuid}', {sql_escape(url)}, {int(pos)}, NOW(), NOW()) ON CONFLICT (id) DO NOTHING;"
                    sql_statements.append(sql)
            except Exception as e:
                print(f"Warning: Could not map variant image Target_ID {t_id}")

    # Write SQL
    with open(SQL_FILE, 'w') as f:
        f.write("\n".join(sql_statements))
    print(f"SQL Seed generated at {SQL_FILE}")

if __name__ == '__main__':
    main()
