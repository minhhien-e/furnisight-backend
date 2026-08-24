-- ============================================================
-- furnisight_promotion_db
-- Schema tham chieu:
--   - promotion-service/.../V1__create_promotion_tables.sql

-- 1. Seed data for promotions
INSERT INTO promotions (id, code, name, description, icon, voucher_type, discount_type, discount_value, max_discount, min_order, start_date, end_date, active) VALUES
('11111111-1111-1111-1111-111111111111', 'WELCOME20', 'Welcome 20%', 'Giảm giá 20% cho khách hàng mới', 'mdi-gift', 'PUBLIC', 'PERCENT', 20, 500000, 1000000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('22222222-2222-2222-2222-222222222222', 'FREESHIP', 'Free Shipping', 'Miễn phí vận chuyển lên tới 50k', 'mdi-truck-fast', 'PUBLIC', 'SHIPPING_CAP', 50000, 50000, 500000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('33333333-3333-3333-3333-333333333333', 'TET2026', 'Tết Nguyên Đán', 'Giảm 1 triệu cho đơn từ 10 triệu', 'mdi-firework', 'PUBLIC', 'FIXED', 1000000, 1000000, 10000000, '2026-01-01 00:00:00', '2026-02-28 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777771', 'SUMMER2026', 'Chào Hè Rực Rỡ', 'Giảm 15% tối đa 1 triệu cho đơn từ 3 triệu', 'mdi-white-balance-sunny', 'PUBLIC', 'PERCENT', 15, 1000000, 3000000, '2026-05-01 00:00:00', '2026-08-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777772', 'NEWYEAR2026', 'Đón Năm Mới', 'Giảm 200k cho đơn từ 2 triệu', 'mdi-pine-tree', 'PUBLIC', 'FIXED', 200000, 200000, 2000000, '2025-12-01 00:00:00', '2026-02-15 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777773', 'LUXURYVIP', 'Ưu đãi Đặc Quyền', 'Giảm 5 triệu cho đơn siêu cấp từ 50 triệu', 'mdi-crown', 'PUBLIC', 'FIXED', 5000000, 5000000, 50000000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777774', 'MIDNIGHT', 'Giờ Vàng Giá Sốc', 'Giảm 30% tối đa 500k', 'mdi-weather-night', 'PUBLIC', 'PERCENT', 30, 500000, 1000000, '2026-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777775', 'STUDENT10', 'Ưu đãi Sinh viên', 'Giảm 10% tối đa 100k', 'mdi-school', 'PUBLIC', 'PERCENT', 10, 100000, 200000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777776', 'HAPPYWEEKEND', 'Cuối Tuần Vui Vẻ', 'Giảm trực tiếp 150k', 'mdi-calendar-weekend', 'PUBLIC', 'FIXED', 150000, 150000, 1500000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777777', 'FLASH50', 'Flash Sale Nửa Giá', 'Giảm 50% tối đa 500k', 'mdi-flash', 'PUBLIC', 'PERCENT', 50, 500000, 1000000, '2026-06-06 00:00:00', '2026-06-06 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777778', 'PRIVATE500', 'Voucher Bí Mật', 'Giảm 500k cho khách VIP', 'mdi-lock', 'PERSONAL', 'FIXED', 500000, 500000, 5000000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE),
('77777777-7777-7777-7777-777777777779', 'LOYALTY20', 'Tri Ân Khách Hàng', 'Giảm 20% tối đa 2 triệu', 'mdi-heart', 'PERSONAL', 'PERCENT', 20, 2000000, 5000000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 2. Seed data for promotion_combos
INSERT INTO promotion_combos (id, name, description, discount_type, discount_value, start_date, end_date, active, image_url, original_amount, final_amount, saved_amount) VALUES
('44444444-4444-4444-4444-444444444444', 'Combo Phòng Ăn Gia Đình', 'Bàn ăn Davison và 4 ghế ăn Norrie', 'PERCENTAGE', 15, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800', 133998500, 113898725, 20099775),
('55555555-5555-5555-5555-555555555555', 'Combo Phòng Khách Hiện Đại', 'Ghế bành da Loop, bàn phụ Koa và Bàn console', 'FIXED_AMOUNT', 10000000, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=800', 87500100, 77500100, 10000000),
('88888888-8888-8888-8888-888888888888', 'Combo Phòng Ngủ Ấm Áp', 'Giường bệt Andes và ghế băng bọc lông cừu Socca', 'PERCENTAGE', 10, '2023-01-01 00:00:00', '2026-12-31 23:59:59', TRUE, 'https://images.unsplash.com/photo-1505693314120-0d443867891c?auto=format&fit=crop&q=80&w=800', 68327400, 61494660, 6832740)
ON CONFLICT (id) DO NOTHING;

-- 3. Seed data for promotion_combo_items
INSERT INTO promotion_combo_items (id, combo_id, product_id, variant_id, product_slug, product_name, sku, category_name, price, quantity) VALUES
('66666666-6666-6666-6666-666666666661', '44444444-4444-4444-4444-444444444444', 'b4205fc2-9983-37f7-35c0-e6d2cc9b9066', '0b46f9ae-8962-1466-5ad7-551532344086', 'ban-an-hinh-chu-nhat-davison', 'Bàn ăn hình chữ nhật Davison', 'DT-DAV', 'Bàn ăn', 65723700, 1),
('66666666-6666-6666-6666-666666666662', '44444444-4444-4444-4444-444444444444', 'fd342f1c-932d-1693-4cd9-13d2ac1ca354', '27ca9cce-2516-39eb-72c1-d2b05099c859', 'ghe-an-khong-tay-vin-norrie', 'Ghế ăn không tay vịn Norrie', 'DC-NOR', 'Ghế ăn', 17068700, 4),
('66666666-6666-6666-6666-666666666663', '55555555-5555-5555-5555-555555555555', 'df66c802-87aa-6ee7-389a-341b9f576b10', 'c643b71b-6d26-946d-c16b-b8dbd86ad0d2', 'ghe-banh-da-loop-mau-nau-chocolate', 'Ghế bành da Loop màu nâu chocolate', 'AC-LOOP', 'Ghế bành', 44683700, 1),
('66666666-6666-6666-6666-666666666664', '55555555-5555-5555-5555-555555555555', '6e6900d0-6ef7-d0f2-d9d0-4540f59ef6c6', '9979af9f-3cb0-8a78-506d-e3fe06cbd886', 'ban-phu-koa', 'Bàn phụ Koa', 'ST-KOA', 'Bàn phụ', 9178700, 1),
('66666666-6666-6666-6666-666666666665', '55555555-5555-5555-5555-555555555555', '9dbad847-7a4b-91d4-02e4-27d29209918b', 'd32f34f8-4183-472a-6daa-808e3e72ba9a', 'ban-console-bodene-82-inch', 'Bàn console Bodene 82 inch', 'CT-BODENE', 'Bàn console', 33637700, 1),
('66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', '4a4c0c70-0e62-59f8-b1d9-f2dc44ed95d5', 'ab073fc7-bfc1-1ae7-16a9-151c22bca665', 'giuong-bet-andes-kem-tu-dau-giuong', 'Giường bệt Andes cỡ Queen kèm tủ đầu giường', 'BED-AND', 'Giường bệt', 21013700, 1),
('66666666-6666-6666-6666-666666666667', '88888888-8888-8888-8888-888888888888', '00694f9f-53ca-3ddf-ca05-6353651695a0', '0603b758-29d9-ae6d-6a38-9be445cfe0c0', 'ghe-bang-boc-long-cuu-socca', 'Ghế băng bọc lông cừu Socca', 'BN-SOCCA', 'Ghế băng', 47313700, 1)
ON CONFLICT (id) DO NOTHING;
