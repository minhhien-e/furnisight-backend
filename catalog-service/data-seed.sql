-- Seed data for Categories
-- Level 1: Rooms
INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES 
('c0000000-0000-0000-0000-000000000001', 'Living Room', 'living-room', NULL, 'living-room', 3, 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000002', 'Bedroom', 'bedroom', NULL, 'bedroom', 3, 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000003', 'Dining Room', 'dining-room', NULL, 'dining-room', 2, 'https://images.unsplash.com/photo-1617806118233-18e1c0945594?auto=format&fit=crop&q=80&w=800', '🍽️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000004', 'Workspace', 'workspace', NULL, 'workspace', 2, 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=800', '💻', NOW(), NOW());

-- Level 2: Subcategories
INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES 
('d0000000-0000-0000-0000-000000000001', 'Sofa', 'sofa', 'c0000000-0000-0000-0000-000000000001', 'living-room/sofa', 2, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000002', 'Coffee Table', 'coffee-table', 'c0000000-0000-0000-0000-000000000001', 'living-room/coffee-table', 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800', '☕', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000003', 'Bed', 'bed', 'c0000000-0000-0000-0000-000000000002', 'bedroom/bed', 2, 'https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000004', 'Wardrobe', 'wardrobe', 'c0000000-0000-0000-0000-000000000002', 'bedroom/wardrobe', 1, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800', '🚪', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000005', 'Dining Table', 'dining-table', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-table', 1, 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800', '🪑', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000006', 'Dining Chair', 'dining-chair', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-chair', 1, 'https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800', '🪑', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000007', 'Desk', 'desk', 'c0000000-0000-0000-0000-000000000004', 'workspace/desk', 1, 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=800', '🖥️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000008', 'Office Chair', 'office-chair', 'c0000000-0000-0000-0000-000000000004', 'workspace/office-chair', 1, 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=800', '💺', NOW(), NOW());

-- Seed data for Products (10 Products) - product.attributes chỉ còn thumbnailUrl
INSERT INTO products (id, shop_id, category_id, name, description, product_status, attributes, weight, length, width, height, view_count, created_at, updated_at) VALUES 
-- Living Room (Sofa & Coffee Table)
('e0000000-0000-0000-0000-000000000001', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000001', 'Modern Leather Sofa', 'Ghế sofa da cao cấp phong cách hiện đại cho phòng khách của bạn.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800"}', 50000.0, 200.0, 90.0, 85.0, 150, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000002', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000001', 'Fabric Sectional Sofa', 'Sofa vải hình chữ L thoải mái với nhiều màu sắc đa dạng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=800"}', 65000.0, 250.0, 150.0, 85.0, 200, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000003', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000002', 'Minimalist Oak Coffee Table', 'Bàn cà phê gỗ sồi phong cách tối giản.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800"}', 15000.0, 100.0, 60.0, 45.0, 85, NOW(), NOW()),

-- Bedroom (Bed & Wardrobe)
('e0000000-0000-0000-0000-000000000004', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000003', 'King Size Metal Bed', 'Khung giường kim loại king size chắc chắn và rộng rãi.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800"}', 40000.0, 210.0, 190.0, 35.0, 120, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000005', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000003', 'Queen Size Wooden Bed', 'Giường queen size cổ điển làm từ gỗ thông.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800"}', 35000.0, 200.0, 160.0, 40.0, 95, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000006', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000004', 'Sliding Door Wardrobe', 'Tủ quần áo rộng rãi với cửa trượt có gương.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800"}', 80000.0, 220.0, 180.0, 60.0, 110, NOW(), NOW()),

-- Dining Room (Dining Table & Chair)
('e0000000-0000-0000-0000-000000000007', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000005', 'Marble Top Dining Table', 'Bàn ăn sang trọng với mặt bàn đá cẩm thạch thật.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800"}', 60000.0, 180.0, 90.0, 75.0, 180, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000008', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000006', 'Velvet Dining Chair', 'Ghế ăn nhung mềm mại với chân vàng sang trọng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800"}', 8000.0, 50.0, 55.0, 90.0, 140, NOW(), NOW()),

-- Workspace (Desk & Office Chair)
('e0000000-0000-0000-0000-000000000009', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000007', 'Ergonomic Standing Desk', 'Bàn đứng điều chỉnh chiều cao giúp làm việc thoải mái hơn.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=800"}', 30000.0, 120.0, 60.0, 70.0, 250, NOW(), NOW()),
('e0000000-0000-0000-0000-000000000010', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'd0000000-0000-0000-0000-000000000008', 'Mesh Ergonomic Office Chair', 'Ghế công thái học lưới thoáng khí với đệm thắt lưng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=800"}', 12000.0, 65.0, 65.0, 110.0, 220, NOW(), NOW());


-- Seed data for Product Variants (20 Variants) - attributes gồm color và material của từng variant
INSERT INTO product_variants (id, product_id, sku, price, stock_quantity, attributes) VALUES 
-- Sofa 1 (Leather) - 2 màu
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'SOFA-LTR-BRW-STD', 12000000.00, 10, '{"material": "leather", "color": "brown", "colorLabel": "Nâu Da Bò", "materialLabel": "Da Thật", "size": "Standard (2 chỗ)"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'SOFA-LTR-BLK-PRM', 15000000.00, 5, '{"material": "leather", "color": "black", "colorLabel": "Đen", "materialLabel": "Da Thật", "size": "Premium (3 chỗ)"}'),
-- Sofa 2 (Fabric) - 2 màu
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'SOFA-FAB-GRY-2S', 8500000.00, 15, '{"material": "fabric", "color": "gray", "colorLabel": "Xám Tro", "materialLabel": "Vải", "size": "2 chỗ"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'SOFA-FAB-BEG-3S', 10500000.00, 8, '{"material": "fabric", "color": "beige", "colorLabel": "Kem", "materialLabel": "Vải", "size": "3 chỗ"}'),
-- Coffee Table - 2 kích thước
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'TAB-OAK-NAT-SML', 3500000.00, 20, '{"material": "oak-wood", "color": "natural", "colorLabel": "Gỗ Tự Nhiên", "materialLabel": "Gỗ Sồi", "size": "Nhỏ 80x50cm"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'TAB-OAK-NAT-LRG', 4500000.00, 12, '{"material": "oak-wood", "color": "natural", "colorLabel": "Gỗ Tự Nhiên", "materialLabel": "Gỗ Sồi", "size": "Lớn 100x60cm"}'),
-- Bed 1 (Metal King) - 2 màu
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000004', 'BED-MET-BLK-KNG', 6500000.00, 8, '{"material": "metal", "color": "black", "colorLabel": "Đen", "materialLabel": "Kim Loại", "size": "King (200x200cm)"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000004', 'BED-MET-GLD-KNG', 7000000.00, 4, '{"material": "metal", "color": "gold", "colorLabel": "Vàng Gold", "materialLabel": "Kim Loại", "size": "King (200x200cm)"}'),
-- Bed 2 (Wood Queen) - 2 màu
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000005', 'BED-PNE-WHT-QEN', 5500000.00, 10, '{"material": "pine-wood", "color": "white", "colorLabel": "Trắng", "materialLabel": "Gỗ Thông", "size": "Queen (160x200cm)"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000005', 'BED-PNE-NAT-QEN', 5500000.00, 10, '{"material": "pine-wood", "color": "natural", "colorLabel": "Gỗ Tự Nhiên", "materialLabel": "Gỗ Thông", "size": "Queen (160x200cm)"}'),
-- Wardrobe - 2 cấu hình cửa
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000006', 'WAR-MDF-WHT-2D', 8000000.00, 12, '{"material": "mdf", "color": "white", "colorLabel": "Trắng", "materialLabel": "MDF", "size": "2 cánh cửa"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000006', 'WAR-MDF-WHT-3D', 11000000.00, 6, '{"material": "mdf", "color": "white", "colorLabel": "Trắng", "materialLabel": "MDF", "size": "3 cánh cửa"}'),
-- Dining Table - 2 màu
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000007', 'TAB-MAR-WHT-180', 12500000.00, 5, '{"material": "marble", "color": "white", "colorLabel": "Đá Trắng", "materialLabel": "Đá Cẩm Thạch", "size": "180x90cm"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000007', 'TAB-MAR-BLK-180', 13000000.00, 3, '{"material": "marble", "color": "black", "colorLabel": "Đá Đen", "materialLabel": "Đá Cẩm Thạch", "size": "180x90cm"}'),
-- Dining Chair - 2 màu nhung
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000008', 'CHR-VEL-GRN-1', 1200000.00, 30, '{"material": "velvet", "color": "green", "colorLabel": "Xanh Lá", "materialLabel": "Nhung", "size": "Tiêu Chuẩn"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000008', 'CHR-VEL-BLU-1', 1200000.00, 25, '{"material": "velvet", "color": "blue", "colorLabel": "Xanh Dương", "materialLabel": "Nhung", "size": "Tiêu Chuẩn"}'),
-- Standing Desk - thủ công và điện
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000009', 'DSK-STL-BLK-MAN', 4500000.00, 15, '{"material": "steel", "color": "black", "colorLabel": "Đen", "materialLabel": "Thép", "size": "Cơ (120cm)"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000009', 'DSK-STL-WHT-ELC', 7500000.00, 10, '{"material": "steel", "color": "white", "colorLabel": "Trắng", "materialLabel": "Thép", "size": "Điện (140cm)"}'),
-- Office Chair - 2 cấp độ
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'CHR-MSH-GRY-STD', 2500000.00, 40, '{"material": "mesh", "color": "gray", "colorLabel": "Xám", "materialLabel": "Lưới Mesh", "size": "Standard"}'),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'CHR-MSH-BLK-PRM', 3500000.00, 20, '{"material": "mesh", "color": "black", "colorLabel": "Đen", "materialLabel": "Lưới Mesh", "size": "Premium"}');

