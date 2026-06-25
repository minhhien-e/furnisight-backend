-- Seed catalog products and variants
INSERT INTO products (id, category_id, name, slug, description, product_status, features, sold_count, sku, created_at, updated_at) VALUES
('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'Sofa vải chữ L êm ái', 'fabric-sectional-sofa', 'Sofa vải chữ L rộng rãi, phù hợp phòng khách gia đình và không gian mở.', 'ACTIVE', '["Vải nỉ cao cấp","Thiết kế chữ L","Đệm mút dày","Có thể tháo vỏ"]', 18, 'SKU-0002', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', 'Bàn cà phê mặt đá cẩm thạch', 'marble-coffee-table', 'Bàn cà phê mặt đá cẩm thạch tự nhiên, khung inox mạ vàng sang trọng.', 'ACTIVE', '["Đá cẩm thạch tự nhiên","Khung inox mạ vàng","Chân côn thanh mảnh"]', 15, 'SKU-0003', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000002', 'Kệ TV tối giản Bắc Âu', 'minimalist-tv-stand', 'Kệ TV phong cách Bắc Âu với chân gỗ côn tinh tế, phù hợp TV 55-75 inch.', 'ACTIVE', '["Gỗ MDF phủ veneer","Ngăn kéo ẩn","Chân gỗ côn","2 ngăn tủ"]', 42, 'SKU-0004', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000003', 'Giường đôi gỗ sồi cao cấp', 'oak-queen-bed', 'Giường đôi gỗ sồi tự nhiên, thiết kế đầu giường cao bo tròn tinh tế.', 'ACTIVE', '["Gỗ sồi tự nhiên","Đầu giường bo tròn","Ngăn kéo dưới gầm","Kích thước 160x200"]', 31, 'SKU-0005', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000004', 'Tủ quần áo cánh lùa gương', 'sliding-mirror-wardrobe', 'Tủ quần áo cánh lùa toàn gương, tiết kiệm không gian và tạo cảm giác rộng rãi.', 'ACTIVE', '["Cánh lùa gương toàn phần","Thanh trượt êm","Ngăn kéo trong","Kích thước 2m"]', 27, 'SKU-0006', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000003', 'Tủ đầu giường nhỏ gọn', 'bedside-table', 'Tủ đầu giường thiết kế nhỏ gọn, 2 ngăn kéo, chân gỗ côn dễ phối đồ.', 'ACTIVE', '["Gỗ MDF sơn","2 ngăn kéo","Chân gỗ côn","Tay nắm tròn đồng"]', 56, 'SKU-0007', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000005', 'Bộ bàn ăn 6 ghế hiện đại', 'modern-dining-set-6', 'Bộ bàn ăn 6 ghế phong cách hiện đại, mặt bàn gỗ sần tự nhiên, chân kim loại.', 'ACTIVE', '["Mặt gỗ tự nhiên","Chân kim loại","6 ghế kèm theo","Kích thước 180x90"]', 19, 'SKU-0008', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000005', 'Ghế ăn bọc da PU', 'pu-leather-dining-chair', 'Ghế ăn bọc da PU cao cấp, lưng dựa thấp phong cách mid-century modern.', 'ACTIVE', '["Da PU nhân tạo","Khung kim loại đen","Chân gỗ sồi","Nhiều màu"]', 84, 'SKU-0009', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000010', 'd0000000-0000-0000-0000-000000000001', 'Kệ sách đứng phong cách công nghiệp', 'industrial-bookshelf', 'Kệ sách đứng khung thép và gỗ thông, phong cách công nghiệp mạnh mẽ.', 'ACTIVE', '["Khung thép đen matte","Kệ gỗ thông","5 tầng","Kích thước 80x180cm"]', 45, 'SKU-0010', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000011', 'd0000000-0000-0000-0000-000000000001', 'Ghế thư giãn đọc sách', 'reading-armchair', 'Ghế thư giãn bọc vải cao cấp, kiểu dáng Bắc Âu ấm cúng, chân gỗ sồi.', 'ACTIVE', '["Vải chenille","Đệm lông vũ","Chân gỗ sồi","Quay 360 độ"]', 33, 'SKU-0011', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000012', 'd0000000-0000-0000-0000-000000000003', 'Bàn trang điểm LED gương lớn', 'led-vanity-desk', 'Bàn trang điểm tích hợp đèn LED quanh gương, nhiều ngăn đựng đồ trang điểm.', 'ACTIVE', '["Đèn LED 3 chế độ","Gương lớn","Nhiều ngăn kéo","Mặt gỗ rộng"]', 22, 'SKU-0012', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_variants (id, product_id, sku, price, stock, attributes, created_at, updated_at) VALUES
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'SKU-0001-V1', 18500000, 10, '{"color":"Nâu"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'SKU-0002-V1', 22000000, 8, '{"color":"Xám"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'SKU-0003-V1', 12800000, 6, '{"color":"Trắng"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000004', 'SKU-0004-V1', 5500000, 15, '{"color":"Walnut"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000005', 'SKU-0005-V1', 15200000, 12, '{"color":"Gỗ tự nhiên"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000006', 'SKU-0006-V1', 11900000, 9, '{"color":"Gương"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000007', 'SKU-0007-V1', 2200000, 20, '{"color":"Trắng"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000008', 'SKU-0008-V1', 28000000, 5, '{"color":"Gỗ tự nhiên"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000009', 'SKU-0009-V1', 1850000, 30, '{"color":"Đen"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'SKU-0010-V1', 3200000, 18, '{"color":"Đen-Nâu"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000011', 'SKU-0011-V1', 7600000, 14, '{"color":"Xanh rêu"}', NOW(), NOW()),
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000012', 'SKU-0012-V1', 4800000, 11, '{"color":"Trắng"}', NOW(), NOW());
