import pandas as pd
import json
import os
import requests
import re
import cloudinary
import cloudinary.uploader
import urllib.parse
from io import BytesIO

# Configuration
EXCEL_FILE = "data_furnisight.xlsx"
MAPPING_FILE = "image_mapping.json"
SQL_FILE = "catalog_seed.sql"
NEW_SQL_FILE = "catalog_seed_cloudinary.sql"

# Make sure CLOUDINARY_URL is set in your environment
# Example: export CLOUDINARY_URL="cloudinary://API_KEY:API_SECRET@CLOUD_NAME"
cloudinary.config()

def convert_gdrive_url_to_direct_download(url):
    """
    Converts a Google Drive view/share URL to a direct download URL.
    Example: https://drive.google.com/file/d/1yUQsvCu402pctoQy8pmDbI-aHPYCjLFp/view?usp=drive_link
    -> https://drive.google.com/uc?export=download&id=1yUQsvCu402pctoQy8pmDbI-aHPYCjLFp
    """
    if "drive.google.com/file/d/" in url:
        match = re.search(r"/d/([a-zA-Z0-9_-]+)/", url)
        if match:
            file_id = match.group(1)
            return f"https://drive.google.com/uc?export=download&id={file_id}"
    return url

def upload_image(url):
    """
    Downloads image from URL and uploads to Cloudinary.
    Returns the Cloudinary secure URL.
    """
    direct_url = convert_gdrive_url_to_direct_download(url)
    try:
        print(f"Downloading {url} ...")
        response = requests.get(direct_url, stream=True)
        response.raise_for_status()
        
        print("Uploading to Cloudinary...")
        result = cloudinary.uploader.upload(
            response.content,
            folder="furnisight/catalog",
            use_filename=True,
            unique_filename=True
        )
        return result.get("secure_url")
    except Exception as e:
        print(f"Error uploading {url}: {e}")
        return None

def main():
    if not os.environ.get("CLOUDINARY_URL"):
        print("ERROR: CLOUDINARY_URL environment variable is not set.")
        print("Please set it before running: export CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME")
        return

    print("Loading Excel file to extract image URLs...")
    xls = pd.ExcelFile(EXCEL_FILE)
    
    # 1. Categories
    df_cat = pd.read_excel(xls, "1_Categories")
    # 4. Product Images
    df_img = pd.read_excel(xls, "4_Product_Images")

    urls_to_process = set()
    for _, row in df_cat.iterrows():
        url = row.get('Image_URL')
        if pd.notna(url) and str(url).strip():
            urls_to_process.add(str(url).strip())
            
    for _, row in df_img.iterrows():
        url = row.get('Image_URL')
        if pd.notna(url) and str(url).strip():
            urls_to_process.add(str(url).strip())

    print(f"Found {len(urls_to_process)} unique image URLs.")
    
    mapping = {}
    if os.path.exists(MAPPING_FILE):
        with open(MAPPING_FILE, 'r') as f:
            mapping = json.load(f)
            
    # Process uploads
    for url in urls_to_process:
        if url in mapping:
            print(f"Already uploaded: {url}")
            continue
        
        # Only process google drive links if we want (or any http link)
        if url.startswith("http"):
            cld_url = upload_image(url)
            if cld_url:
                mapping[url] = cld_url
                print(f"Success -> {cld_url}")
                # Save incrementally
                with open(MAPPING_FILE, 'w') as f:
                    json.dump(mapping, f, indent=4)

    # Rewrite SQL file if it exists
    if os.path.exists(SQL_FILE):
        print(f"Rewriting {SQL_FILE} with Cloudinary URLs -> {NEW_SQL_FILE}")
        with open(SQL_FILE, 'r') as f:
            sql_content = f.read()
            
        for old_url, new_url in mapping.items():
            # Be careful with replacements, SQL might have it inside quotes
            sql_content = sql_content.replace(old_url, new_url)
            
        with open(NEW_SQL_FILE, 'w') as f:
            f.write(sql_content)
        print("Done!")
    else:
        print(f"Could not find {SQL_FILE} to rewrite. Make sure to run generate_catalog_seed.py first.")

if __name__ == '__main__':
    main()
