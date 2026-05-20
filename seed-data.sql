-- ============================================================
-- SEED DATA — chạy thủ công sau khi các service đã khởi động
-- và Flyway migrations đã tạo xong schema.
--
-- Cách chạy:
--   docker exec -i furnisight_user_postgres psql -U postgres < seed-data.sql
-- ============================================================


-- ============================================================
-- furnisight_user_db
-- ============================================================
\connect furnisight_user_db;

-- Roles
-- ADMIN UUID = 9b6eca13-0b92-466a-bbf5-141e6676edab (do Flyway V1 seed với gen_random_uuid())
-- USER  UUID = a0000000-0000-0000-0000-000000000002
INSERT INTO roles (id, name, permissions, position, created_at, updated_at) VALUES
  ('9b6eca13-0b92-466a-bbf5-141e6676edab', 'ADMIN',  -1, 100, NOW(), NOW()),
  ('a0000000-0000-0000-0000-000000000002', 'USER',    0,   1, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Accounts (password_hash = bcrypt('Password123!'))
INSERT INTO accounts (id, username, email, password_hash, status, failed_login_attempts, created_at, updated_at) VALUES
  ('52379d96-5238-4fd9-8383-bae82736bb3b', 'minhhien', 'minhhien7840@gmail.com',       '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NOW(), NOW()),
  ('f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'admin',    'admin@furnisight.store',        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NOW(), NOW()),
  ('4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'user01',  '22130080@st.hcmuaf.edu.vn',     '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NOW(), NOW()),
  ('7c22e6d3-1111-4aab-b999-aabbcc001122', 'user02',  'user02@furnisight.store',        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NOW(), NOW()),
  ('8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'user03',  'user03@furnisight.store',        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
  username = EXCLUDED.username,
  email    = EXCLUDED.email,
  status   = EXCLUDED.status;

-- Account ↔ Roles
INSERT INTO account_roles (id, account_id, role_id, created_at, updated_at) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'a0000000-0000-0000-0000-000000000002', NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', '9b6eca13-0b92-466a-bbf5-141e6676edab', NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'a0000000-0000-0000-0000-000000000002', NOW(), NOW()),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'a0000000-0000-0000-0000-000000000002', NOW(), NOW()),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'a0000000-0000-0000-0000-000000000002', NOW(), NOW()),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'a0000000-0000-0000-0000-000000000002', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- User profiles
INSERT INTO user_profiles (id, account_id, display_name, first_name, last_name, avatar_url, email, phone_number, date_of_birth, gender, created_at, updated_at) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hiền', 'Hiền',  'Minh',      'https://api.dicebear.com/7.x/avataaars/svg?seed=minhhien', 'minhhien7840@gmail.com',       '0901234567', '2000-01-15', 'MALE',   NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'Admin',     'Admin', 'FurniSight', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',    'admin@furnisight.store',        NULL,         NULL,         'OTHER',  NOW(), NOW()),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01',  'Văn',   'An',         'https://api.dicebear.com/7.x/avataaars/svg?seed=user01',   '22130080@st.hcmuaf.edu.vn',     '0912345678', '1999-05-20', 'MALE',   NOW(), NOW()),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02',  'Thị',   'Bình',       'https://api.dicebear.com/7.x/avataaars/svg?seed=user02',   'user02@furnisight.store',        '0923456789', '2001-08-10', 'FEMALE', NOW(), NOW()),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03',  'Quốc',  'Cường',      'https://api.dicebear.com/7.x/avataaars/svg?seed=user03',   'user03@furnisight.store',        '0934567890', '1998-12-03', 'MALE',   NOW(), NOW())
ON CONFLICT DO NOTHING;


-- ============================================================
-- furnisight_review_db
-- ============================================================
\connect furnisight_review_db;

-- Reviews
-- UUID prefix key: b00000{product_no}-... cho review, c00000{product_no}-... cho order_item
INSERT INTO reviews (id, title, user_id, product_id, order_item_id, content_text, content_hash, rating, status, trust_score, created_at, updated_at) VALUES
  -- Modern Leather Sofa (product 1)
  ('b0000001-0000-0000-0000-000000000001', 'Sofa da rất xịn!',
   '52379d96-5238-4fd9-8383-bae82736bb3b',
   'e0000000-0000-0000-0000-000000000001',
   'c0000001-0000-0000-0000-000000000001',
   'Mình rất hài lòng với chiếc sofa này. Chất da mềm mại, màu sắc đẹp, đúng như hình. Giao hàng nhanh, đóng gói cẩn thận. Sẽ mua lại!',
   md5('Mình rất hài lòng với chiếc sofa này.'), 5, 'VISIBLE', 0.90, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),

  ('b0000001-0000-0000-0000-000000000002', 'Chất lượng ổn, giá hơi cao',
   '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
   'e0000000-0000-0000-0000-000000000001',
   'c0000001-0000-0000-0000-000000000002',
   'Sofa đẹp, ngồi thoải mái. Tuy nhiên giá hơi cao so với mặt bằng chung. Chất da có vẻ dễ trầy xước nếu không bảo quản kỹ.',
   md5('Sofa đẹp, ngồi thoải mái.'), 4, 'VISIBLE', 0.75, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),

  -- Fabric Sectional Sofa (product 2)
  ('b0000002-0000-0000-0000-000000000001', 'Sofa vải siêu thoải mái',
   '7c22e6d3-1111-4aab-b999-aabbcc001122',
   'e0000000-0000-0000-0000-000000000002',
   'c0000002-0000-0000-0000-000000000001',
   'Mua cho phòng khách nhà, gia đình rất thích. Vải mềm, đệm dày, dễ tháo ra giặt. Rất tiện cho nhà có trẻ nhỏ.',
   md5('Mua cho phòng khách nhà, gia đình rất thích.'), 5, 'VISIBLE', 0.85, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),

  -- Minimalist Oak Coffee Table (product 3)
  ('b0000003-0000-0000-0000-000000000001', 'Bàn trà đẹp, lắp dễ',
   '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
   'e0000000-0000-0000-0000-000000000003',
   'c0000003-0000-0000-0000-000000000001',
   'Bàn trà gỗ sồi rất đẹp và chắc chắn. Hướng dẫn lắp ráp rõ ràng, tự lắp trong 20 phút. Bề mặt mịn, không bị xước khi mới nhận.',
   md5('Bàn trà gỗ sồi rất đẹp và chắc chắn.'), 5, 'VISIBLE', 0.95, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

  -- King Size Metal Bed (product 4)
  ('b0000004-0000-0000-0000-000000000001', 'Giường king đỉnh, không tiếng ọt ét',
   '52379d96-5238-4fd9-8383-bae82736bb3b',
   'e0000000-0000-0000-0000-000000000004',
   'c0000004-0000-0000-0000-000000000001',
   'Dùng được 2 tháng, không có tiếng kêu khi xoay trở. Khung thép chắc chắn, sơn không bị tróc. Rất hài lòng với sản phẩm.',
   md5('Dùng được 2 tháng, không có tiếng kêu khi xoay trở.'), 5, 'VISIBLE', 0.88, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

  -- Ergonomic Standing Desk (product 9)
  ('b0000009-0000-0000-0000-000000000001', 'Bàn đứng thay đổi cuộc sống làm việc',
   '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
   'e0000000-0000-0000-0000-000000000009',
   'c0000009-0000-0000-0000-000000000001',
   'Từ khi có bàn đứng điều chỉnh điện, lưng đỡ đau hẳn. Motor êm, điều chỉnh mượt. Bề mặt bàn rộng, đủ để 2 màn hình. Mua lại lần 2 rồi!',
   md5('Từ khi có bàn đứng điều chỉnh điện, lưng đỡ đau hẳn.'), 5, 'VISIBLE', 0.92, NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day'),

  ('b0000009-0000-0000-0000-000000000002', 'Tốt nhưng giao hàng chậm',
   '7c22e6d3-1111-4aab-b999-aabbcc001122',
   'e0000000-0000-0000-0000-000000000009',
   'c0000009-0000-0000-0000-000000000002',
   'Sản phẩm chất lượng tốt, đúng mô tả. Nhưng giao hàng mất đến 10 ngày mới nhận được. Cần cải thiện khâu vận chuyển.',
   md5('Sản phẩm chất lượng tốt, đúng mô tả.'), 3, 'VISIBLE', 0.65, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

  -- Mesh Ergonomic Office Chair (product 10)
  ('b0000010-0000-0000-0000-000000000001', 'Ghế lưới cực thoáng, ngồi lâu không mỏi',
   '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
   'e0000000-0000-0000-0000-000000000010',
   'c0000010-0000-0000-0000-000000000001',
   'Lưới thoáng khí thật sự rất thoải mái cho mùa hè. Đệm thắt lưng đỡ đúng điểm. Ngồi 8 tiếng không thấy mỏi lưng. Recommend!',
   md5('Lưới thoáng khí thật sự rất thoải mái cho mùa hè.'), 5, 'VISIBLE', 0.87, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days')
ON CONFLICT DO NOTHING;

-- Review votes
INSERT INTO review_votes (user_id, review_id, vote_type, created_at) VALUES
  ('4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'b0000001-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('7c22e6d3-1111-4aab-b999-aabbcc001122', 'b0000001-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'b0000001-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('52379d96-5238-4fd9-8383-bae82736bb3b', 'b0000003-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'b0000009-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'b0000009-0000-0000-0000-000000000001', 'UP',   NOW()),
  ('52379d96-5238-4fd9-8383-bae82736bb3b', 'b0000009-0000-0000-0000-000000000002', 'DOWN', NOW()),
  ('7c22e6d3-1111-4aab-b999-aabbcc001122', 'b0000010-0000-0000-0000-000000000001', 'UP',   NOW())
ON CONFLICT DO NOTHING;


-- ============================================================
-- furnisight_catalog_db
-- ============================================================
\connect furnisight_catalog_db;

-- Level 1: Rooms
INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES 
('c0000000-0000-0000-0000-000000000001', 'Living Room', 'living-room', NULL, 'living-room', 3, 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000002', 'Bedroom', 'bedroom', NULL, 'bedroom', 3, 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000003', 'Dining Room', 'dining-room', NULL, 'dining-room', 2, 'https://images.unsplash.com/photo-1617806118233-18e1c0945594?auto=format&fit=crop&q=80&w=800', '🍽️', NOW(), NOW()),
('c0000000-0000-0000-0000-000000000004', 'Workspace', 'workspace', NULL, 'workspace', 2, 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=800', '💻', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Level 2: Subcategories
INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES 
('d0000000-0000-0000-0000-000000000001', 'Sofa', 'sofa', 'c0000000-0000-0000-0000-000000000001', 'living-room/sofa', 2, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000002', 'Coffee Table', 'coffee-table', 'c0000000-0000-0000-0000-000000000001', 'living-room/coffee-table', 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800', '☕', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000003', 'Bed', 'bed', 'c0000000-0000-0000-0000-000000000002', 'bedroom/bed', 2, 'https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000004', 'Wardrobe', 'wardrobe', 'c0000000-0000-0000-0000-000000000002', 'bedroom/wardrobe', 1, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800', '🚪', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000005', 'Dining Table', 'dining-table', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-table', 1, 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800', '🪑', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000006', 'Dining Chair', 'dining-chair', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-chair', 1, 'https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800', '🪑', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000007', 'Desk', 'desk', 'c0000000-0000-0000-0000-000000000004', 'workspace/desk', 1, 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=800', '🖥️', NOW(), NOW()),
('d0000000-0000-0000-0000-000000000008', 'Office Chair', 'office-chair', 'c0000000-0000-0000-0000-000000000004', 'workspace/office-chair', 1, 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=800', '💺', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed data for Products
INSERT INTO products (id, category_id, name, slug, description, product_status, attributes, created_at, updated_at) VALUES 
('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'Modern Leather Sofa', 'modern-leather-sofa', 'Ghế sofa da cao cấp phong cách hiện đại cho phòng khách của bạn.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'Fabric Sectional Sofa', 'fabric-sectional-sofa', 'Sofa vải hình chữ L thoải mái với nhiều màu sắc đa dạng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', 'Minimalist Oak Coffee Table', 'minimalist-oak-coffee-table', 'Bàn cà phê gỗ sồi phong cách tối giản.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000003', 'King Size Metal Bed', 'king-size-metal-bed', 'Khung giường kim loại king size chắc chắn và rộng rãi.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000003', 'Queen Size Wooden Bed', 'queen-size-wooden-bed', 'Giường queen size cổ điển làm từ gỗ thông.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000004', 'Sliding Door Wardrobe', 'sliding-door-wardrobe', 'Tủ quần áo rộng rãi với cửa trượt có gương.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000005', 'Marble Top Dining Table', 'marble-top-dining-table', 'Bàn ăn sang trọng với mặt bàn đá cẩm thạch thật.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000006', 'Velvet Dining Chair', 'velvet-dining-chair', 'Ghế ăn nhung mềm mại với chân vàng sang trọng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000007', 'Ergonomic Standing Desk', 'ergonomic-standing-desk', 'Bàn đứng điều chỉnh chiều cao giúp làm việc thoải mái hơn.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW()),
('e0000000-0000-0000-0000-000000000010', 'd0000000-0000-0000-0000-000000000008', 'Mesh Ergonomic Office Chair', 'mesh-ergonomic-office-chair', 'Ghế công thái học lưới thoáng khí với đệm thắt lưng.', 'ACTIVE', '{"image": "https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=800"}', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed data for Product Variants
INSERT INTO product_variants (id, product_id, price, stock_quantity, weight, length, width, height) VALUES 
('a0000001-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 12000000.00, 10, 50.0, 200.0, 90.0, 85.0),
('a0000001-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 15000000.00, 5, 60.0, 220.0, 95.0, 90.0),
('a0000002-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000002', 18000000.00, 8, 70.0, 250.0, 150.0, 85.0),
('a0000003-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003', 4500000.00, 15, 15.0, 100.0, 60.0, 45.0),
('a0000004-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000004', 9500000.00, 12, 40.0, 210.0, 190.0, 35.0),
('a0000005-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000005', 8500000.00, 14, 35.0, 200.0, 160.0, 40.0),
('a0000006-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000006', 13500000.00, 7, 80.0, 220.0, 180.0, 60.0),
('a0000007-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 24000000.00, 6, 60.0, 180.0, 90.0, 75.0),
('a0000008-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000008', 2500000.00, 20, 8.0, 50.0, 55.0, 90.0),
('a0000009-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000009', 11500000.00, 18, 30.0, 120.0, 60.0, 70.0),
('a0000010-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000010', 5800000.00, 25, 12.0, 65.0, 65.0, 110.0)
ON CONFLICT (id) DO NOTHING;

-- Seed data for Product Favorite Logs (User likes products this week)
INSERT INTO product_favorite_logs (id, user_id, product_id, created_at) VALUES
(gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '4 days'),
(gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '1 day'),
(gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '2 days'),
(gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '3 days'),
(gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000010', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

