-- ============================================================
-- SEED DATA
-- Chay thu cong sau khi Docker stack va Flyway migrations da san sang.
--v 
-- Cach chay:
--   docker exec -i furnisight_user_postgres psql -v ON_ERROR_STOP=1 -U postgres < seed-data.sql
-- ============================================================

SELECT 'CREATE DATABASE furnisight_promotion_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_promotion_db')\gexec

-- ============================================================
-- furnisight_user_db
-- Schema tham chieu:
--   - user-service/.../V1__init_identity_schema.sql
--   - user-service/.../V2__init_profile_schema.sql
--   - user-service/.../V3__init_favorite_product_schema.sql
--   - user-service/.../V4__create_user_addresses_table.sql
--   - user-service/.../V5__add_avatar_url_to_user_profiles.sql
-- ============================================================
\connect furnisight_user_db;

BEGIN;

-- Roles
INSERT INTO roles (id, name, permissions, position, created_at, updated_at) VALUES
  (gen_random_uuid(), 'ADMIN', -1, 100, NOW(), NOW()),
  (gen_random_uuid(), 'USER', 0, 1, NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET
  permissions = EXCLUDED.permissions,
  position = EXCLUDED.position,
  updated_at = NOW();

-- Xoa lien ket/profile cua tap account seed de script co the chay lap lai.
DELETE FROM account_roles
WHERE account_id IN (
  '52379d96-5238-4fd9-8383-bae82736bb3b',
  'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
  '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
  '7c22e6d3-1111-4aab-b999-aabbcc001122',
  '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
);

DELETE FROM user_profiles
WHERE account_id IN (
  '52379d96-5238-4fd9-8383-bae82736bb3b',
  'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
  '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
  '7c22e6d3-1111-4aab-b999-aabbcc001122',
  '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
);

DELETE FROM user_addresses
WHERE account_id IN (
  '52379d96-5238-4fd9-8383-bae82736bb3b',
  'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
  '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
  '7c22e6d3-1111-4aab-b999-aabbcc001122',
  '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
);

DELETE FROM favorite_products
WHERE account_id IN (
  '52379d96-5238-4fd9-8383-bae82736bb3b',
  'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
  '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
  '7c22e6d3-1111-4aab-b999-aabbcc001122',
  '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
);


DELETE FROM accounts
WHERE username IN ('minhhien', 'admin', 'user01', 'user02', 'user03')
   OR email IN (
     'minhhien7840@gmail.com',
     'admin@furnisight.store',
     '22130080@st.hcmuaf.edu.vn',
     'user02@furnisight.store',
     'user03@furnisight.store'
   );

-- Accounts (password_hash = bcrypt('Password123!'))
INSERT INTO accounts (
  id,
  username,
  email,
  password_hash,
  status,
  failed_login_attempts,
  lockout_end,
  created_at,
  updated_at
) VALUES
  ('52379d96-5238-4fd9-8383-bae82736bb3b', 'minhhien', 'minhhien7840@gmail.com', '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NULL, NOW(), NOW()),
  ('f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'admin', 'admin@furnisight.store', '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NULL, NOW(), NOW()),
  ('4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'user01', '22130080@st.hcmuaf.edu.vn', '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NULL, NOW(), NOW()),
  ('7c22e6d3-1111-4aab-b999-aabbcc001122', 'user02', 'user02@furnisight.store', '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NULL, NOW(), NOW()),
  ('8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'user03', 'user03@furnisight.store', '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a', 'ACTIVE', 0, NULL, NOW(), NOW());

INSERT INTO account_roles (id, account_id, role_id, created_at, updated_at) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', (SELECT id FROM roles WHERE name = 'USER'), NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', (SELECT id FROM roles WHERE name = 'ADMIN'), NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', (SELECT id FROM roles WHERE name = 'USER'), NOW(), NOW()),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', (SELECT id FROM roles WHERE name = 'USER'), NOW(), NOW()),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', (SELECT id FROM roles WHERE name = 'USER'), NOW(), NOW()),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', (SELECT id FROM roles WHERE name = 'USER'), NOW(), NOW());

INSERT INTO user_profiles (
  id,
  account_id,
  display_name,
  first_name,
  last_name,
  avatar_url,
  email,
  date_of_birth,
  gender,
  created_at,
  updated_at
) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hien', 'Hien', 'Minh', 'https://api.dicebear.com/7.x/avataaars/svg?seed=minhhien', 'minhhien7840@gmail.com', '2000-01-15', 'MALE', NOW(), NOW()),
  (gen_random_uuid(), 'f85b5fd8-d60e-4c7e-87ae-5912796d668e', 'Admin', 'Admin', 'FurniSight', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'admin@furnisight.store', NULL, 'OTHER', NOW(), NOW()),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01', 'Van', 'An', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user01', '22130080@st.hcmuaf.edu.vn', '1999-05-20', 'MALE', NOW(), NOW()),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02', 'Thi', 'Binh', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user02', 'user02@furnisight.store', '2001-08-10', 'FEMALE', NOW(), NOW()),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03', 'Quoc', 'Cuong', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user03', 'user03@furnisight.store', '1998-12-03', 'MALE', NOW(), NOW());

INSERT INTO user_addresses (
  id,
  account_id,
  full_name,
  phone,
  province_code,
  province_name,
  district_code,
  district_name,
  ward_code,
  ward_name,
  detail,
  type,
  is_default,
  created_at,
  updated_at
) VALUES
  ('61000000-0000-0000-0000-000000000001', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hiền', '0901234567', '79', 'Thành phố Hồ Chí Minh', '769', 'Thành phố Thủ Đức', '26824', 'Phường Linh Trung', 'Khu phố 6', 'HOME', TRUE, NOW(), NOW()),
  ('61000000-0000-0000-0000-000000000002', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'Văn An', '0902345678', '79', 'Thành phố Hồ Chí Minh', '760', 'Quận 1', '26734', 'Phường Bến Nghé', '12 Nguyễn Huệ', 'HOME', TRUE, NOW(), NOW()),
  ('61000000-0000-0000-0000-000000000003', '7c22e6d3-1111-4aab-b999-aabbcc001122', 'Thị Bình', '0903456789', '79', 'Thành phố Hồ Chí Minh', '765', 'Quận Bình Thạnh', '26905', 'Phường 25', '45 Nguyễn Gia Trí', 'OFFICE', TRUE, NOW(), NOW()),
  ('61000000-0000-0000-0000-000000000004', '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'Quốc Cường', '0904567890', '79', 'Thành phố Hồ Chí Minh', '770', 'Quận 3', '27142', 'Phường Võ Thị Sáu', '89 Võ Văn Tần', 'HOME', TRUE, NOW(), NOW());

INSERT INTO favorite_products (id, account_id, product_id, created_at, updated_at) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000010', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

COMMIT;

-- ============================================================
-- furnisight_catalog_db
-- Schema tham chieu:
--   - catalog-service/.../V1__init_schema.sql den V7
-- ============================================================
\connect furnisight_catalog_db;

BEGIN;

-- Don seed cu de script co the chay lap lai.
DELETE FROM reviews
WHERE product_id IN (
  'e0000000-0000-0000-0000-000000000001',
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004',
  'e0000000-0000-0000-0000-000000000005',
  'e0000000-0000-0000-0000-000000000006',
  'e0000000-0000-0000-0000-000000000007',
  'e0000000-0000-0000-0000-000000000008',
  'e0000000-0000-0000-0000-000000000009',
  'e0000000-0000-0000-0000-000000000010'
);

DELETE FROM product_favorite_logs
WHERE product_id IN (
  'e0000000-0000-0000-0000-000000000001',
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004',
  'e0000000-0000-0000-0000-000000000005',
  'e0000000-0000-0000-0000-000000000006',
  'e0000000-0000-0000-0000-000000000007',
  'e0000000-0000-0000-0000-000000000008',
  'e0000000-0000-0000-0000-000000000009',
  'e0000000-0000-0000-0000-000000000010'
);

DELETE FROM product_images
WHERE product_id IN (
  'e0000000-0000-0000-0000-000000000001',
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004',
  'e0000000-0000-0000-0000-000000000005',
  'e0000000-0000-0000-0000-000000000006',
  'e0000000-0000-0000-0000-000000000007',
  'e0000000-0000-0000-0000-000000000008',
  'e0000000-0000-0000-0000-000000000009',
  'e0000000-0000-0000-0000-000000000010'
);

DELETE FROM product_variants
WHERE product_id IN (
  'e0000000-0000-0000-0000-000000000001',
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004',
  'e0000000-0000-0000-0000-000000000005',
  'e0000000-0000-0000-0000-000000000006',
  'e0000000-0000-0000-0000-000000000007',
  'e0000000-0000-0000-0000-000000000008',
  'e0000000-0000-0000-0000-000000000009',
  'e0000000-0000-0000-0000-000000000010'
);

DELETE FROM products
WHERE id IN (
  'e0000000-0000-0000-0000-000000000001',
  'e0000000-0000-0000-0000-000000000002',
  'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004',
  'e0000000-0000-0000-0000-000000000005',
  'e0000000-0000-0000-0000-000000000006',
  'e0000000-0000-0000-0000-000000000007',
  'e0000000-0000-0000-0000-000000000008',
  'e0000000-0000-0000-0000-000000000009',
  'e0000000-0000-0000-0000-000000000010'
);

DELETE FROM collections
WHERE id IN (
  'f0000000-0000-0000-0000-000000000001',
  'f0000000-0000-0000-0000-000000000002',
  'f0000000-0000-0000-0000-000000000003',
  'f0000000-0000-0000-0000-000000000004'
);

DELETE FROM categories
WHERE id IN (
  'c0000000-0000-0000-0000-000000000001',
  'c0000000-0000-0000-0000-000000000002',
  'c0000000-0000-0000-0000-000000000003',
  'c0000000-0000-0000-0000-000000000004',
  'd0000000-0000-0000-0000-000000000001',
  'd0000000-0000-0000-0000-000000000002',
  'd0000000-0000-0000-0000-000000000003',
  'd0000000-0000-0000-0000-000000000004',
  'd0000000-0000-0000-0000-000000000005',
  'd0000000-0000-0000-0000-000000000006',
  'd0000000-0000-0000-0000-000000000007',
  'd0000000-0000-0000-0000-000000000008'
);

INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES
  ('c0000000-0000-0000-0000-000000000001', 'Phòng khách', 'living-room', NULL, 'living-room', 3, 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800', 'living-room', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000002', 'Phòng ngủ', 'bedroom', NULL, 'bedroom', 3, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=800', 'bedroom', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000003', 'Phòng bếp', 'kitchen', NULL, 'kitchen', 2, 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&q=80&w=800', 'kitchen', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000004', 'Phòng tắm', 'bathroom', NULL, 'bathroom', 2, 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=800', 'bathroom', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000001', 'Ghế sofa', 'sofa', 'c0000000-0000-0000-0000-000000000001', 'living-room/sofa', 2, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800', 'sofa', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000002', 'Bàn trà', 'coffee-table', 'c0000000-0000-0000-0000-000000000001', 'living-room/coffee-table', 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800', 'coffee-table', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000003', 'Giường ngủ', 'bed', 'c0000000-0000-0000-0000-000000000002', 'bedroom/bed', 2, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=800', 'bed', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000004', 'Tủ quần áo', 'wardrobe', 'c0000000-0000-0000-0000-000000000002', 'bedroom/wardrobe', 1, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800', 'wardrobe', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000005', 'Bàn ăn', 'dining-table', 'c0000000-0000-0000-0000-000000000003', 'kitchen/dining-table', 1, 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800', 'dining-table', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000006', 'Tủ bếp', 'kitchen-cabinet', 'c0000000-0000-0000-0000-000000000003', 'kitchen/kitchen-cabinet', 1, 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=800', 'kitchen-cabinet', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000007', 'Tủ lavabo', 'bathroom-vanity', 'c0000000-0000-0000-0000-000000000004', 'bathroom/bathroom-vanity', 1, 'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=800', 'bathroom-vanity', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000008', 'Gương phòng tắm', 'bathroom-mirror', 'c0000000-0000-0000-0000-000000000004', 'bathroom/bathroom-mirror', 1, 'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=800', 'bathroom-mirror', NOW(), NOW());

INSERT INTO collections (id, name, description, slug, created_at, updated_at) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Phòng khách hiện đại', 'Bộ sưu tập nội thất phòng khách gọn gàng, ấm cúng và dễ phối màu.', 'modern-living-room', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000002', 'Phòng ngủ thư giãn', 'Bộ sưu tập phòng ngủ ưu tiên sự thoải mái, lưu trữ tốt và chất liệu bền.', 'relaxing-bedroom', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000003', 'Góc bếp ấm cúng', 'Bộ sưu tập bàn ăn và tủ bếp phù hợp căn hộ gia đình hiện đại.', 'cozy-kitchen', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000004', 'Phòng tắm tinh gọn', 'Bộ sưu tập tủ lavabo và gương giúp phòng tắm sáng, sạch và dễ sử dụng.', 'clean-bathroom', NOW(), NOW());

INSERT INTO products (
  id,
  category_id,
  collection_id,
  name,
  slug,
  description,
  product_status,
  model_url,
  model_media_id,
  supports_3d,
  features,
  sold_count,
  created_at,
  updated_at
) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', 'Sofa da bò hiện đại', 'modern-leather-sofa', 'Ghế sofa da bò cao cấp, dáng gọn và sang trọng cho phòng khách hiện đại.', 'ACTIVE', NULL, NULL, FALSE, '["Da bò thật","Khung gỗ sồi","Dễ vệ sinh","Đệm ngồi êm"]', 24, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', 'Sofa vải chữ L êm ái', 'fabric-sectional-sofa', 'Sofa vải chữ L rộng rãi, phù hợp phòng khách gia đình và không gian mở.', 'ACTIVE', NULL, NULL, FALSE, '["Vải nỉ cao cấp","Thiết kế chữ L","Đệm mút dày","Có thể tháo vỏ"]', 17, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'Bàn trà gỗ sồi tối giản', 'minimalist-oak-coffee-table', 'Bàn trà gỗ sồi phong cách tối giản, dễ phối với sofa và thảm phòng khách.', 'ACTIVE', NULL, NULL, FALSE, '["Gỗ sồi tự nhiên","Mặt bàn chống trầy","Kiểu dáng tối giản"]', 31, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002', 'Giường king khung kim loại', 'king-size-metal-bed', 'Giường king size khung kim loại chắc chắn, rộng rãi và dễ vệ sinh gầm giường.', 'ACTIVE', NULL, NULL, FALSE, '["Sơn tĩnh điện","Khung thép chịu lực","Dễ lắp ráp","Không gây tiếng kêu"]', 12, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002', 'Giường queen gỗ tự nhiên', 'queen-size-wooden-bed', 'Giường queen size bằng gỗ tự nhiên, tông ấm và phù hợp phòng ngủ thư giãn.', 'ACTIVE', NULL, NULL, FALSE, '["Gỗ thông tự nhiên","Phong cách ấm áp","Nan giường chắc chắn"]', 20, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000002', 'Tủ quần áo cửa trượt', 'sliding-door-wardrobe', 'Tủ quần áo cửa trượt rộng rãi, tích hợp gương và chia ngăn khoa học.', 'ACTIVE', NULL, NULL, FALSE, '["Cửa trượt tiết kiệm diện tích","Tích hợp gương lớn","Gỗ MDF phủ Melamine"]', 9, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000003', 'Bàn ăn mặt đá cẩm thạch', 'marble-top-dining-table', 'Bàn ăn mặt đá cẩm thạch sang trọng, phù hợp phòng bếp và khu vực ăn gia đình.', 'ACTIVE', NULL, NULL, FALSE, '["Mặt đá cẩm thạch","Chân bàn kim loại","Dễ lau chùi","Phong cách hiện đại"]', 8, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000003', 'Tủ bếp gỗ sáng màu', 'light-wood-kitchen-cabinet', 'Tủ bếp gỗ sáng màu có nhiều ngăn lưu trữ, giúp khu bếp gọn gàng và sạch sẽ.', 'ACTIVE', NULL, NULL, FALSE, '["Gỗ công nghiệp chống ẩm","Tay nắm âm","Dễ lau dầu mỡ","Nhiều khoang chứa"]', 6, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000004', 'Tủ lavabo treo tường', 'wall-mounted-bathroom-vanity', 'Tủ lavabo treo tường chống ẩm, giúp phòng tắm thoáng và dễ vệ sinh sàn.', 'ACTIVE', NULL, NULL, TRUE, '["Chống ẩm tốt","Thiết kế treo tường","Ngăn kéo giảm chấn","Mặt lavabo dễ lau"]', 15, NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000010', 'd0000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000004', 'Gương phòng tắm có đèn LED', 'led-bathroom-mirror', 'Gương phòng tắm tích hợp đèn LED, ánh sáng dịu và phù hợp khu vực lavabo.', 'ACTIVE', NULL, NULL, TRUE, '["Đèn LED tiết kiệm điện","Chống mờ nhẹ","Ánh sáng trung tính","Dễ lắp đặt"]', 27, NOW(), NOW());

INSERT INTO product_images (id, product_id, image_url, position, created_at, updated_at) VALUES
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000006', 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000007', 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000008', 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000009', 'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW());

INSERT INTO product_variants (
  id,
  product_id,
  price,
  stock_quantity,
  weight,
  length,
  width,
  height,
  material,
  warranty,
  color,
  sku,
  low_stock_threshold,
  created_at,
  updated_at
) VALUES
  ('a0000001-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 12000000.00, 10, 50.0, 200.0, 90.0, 85.0, 'Da bò tự nhiên', '2 năm', 'Nâu đỏ', 'SOFA-DA-NAU-200', 3, NOW(), NOW()),
  ('a0000001-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 15000000.00, 5, 60.0, 220.0, 95.0, 90.0, 'Da bò cao cấp', '2 năm', 'Đen tuyền', 'SOFA-DA-DEN-220', 3, NOW(), NOW()),
  ('a0000002-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000002', 18000000.00, 8, 70.0, 250.0, 150.0, 85.0, 'Vải nỉ Hàn Quốc', '1 năm', 'Xám', 'SOFA-VAI-L-XAM', 4, NOW(), NOW()),
  ('a0000003-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003', 4500000.00, 15, 15.0, 100.0, 60.0, 45.0, 'Gỗ sồi trắng', '1 năm', 'Màu gỗ tự nhiên', 'BAN-TRA-SOI-100', 5, NOW(), NOW()),
  ('a0000004-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000004', 9500000.00, 12, 40.0, 210.0, 190.0, 35.0, 'Thép sơn tĩnh điện', '3 năm', 'Trắng tĩnh điện', 'GIUONG-KING-KL', 4, NOW(), NOW()),
  ('a0000005-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000005', 8500000.00, 14, 35.0, 200.0, 160.0, 40.0, 'Gỗ thông New Zealand', '2 năm', 'Nâu đậm', 'GIUONG-QUEEN-GO', 5, NOW(), NOW()),
  ('a0000006-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000006', 13500000.00, 7, 80.0, 220.0, 180.0, 60.0, 'MDF phủ Melamine', '1 năm', 'Trắng ngà', 'TU-AO-TRUOT-220', 3, NOW(), NOW()),
  ('a0000007-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 24000000.00, 6, 60.0, 180.0, 90.0, 75.0, 'Đá cẩm thạch thật', '5 năm', 'Trắng vân mây', 'BAN-AN-DA-180', 3, NOW(), NOW()),
  ('a0000008-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000008', 18500000.00, 9, 95.0, 240.0, 60.0, 220.0, 'Gỗ công nghiệp chống ẩm', '2 năm', 'Sồi sáng', 'TU-BEP-SOI-240', 4, NOW(), NOW()),
  ('a0000009-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000009', 7200000.00, 18, 30.0, 90.0, 48.0, 55.0, 'MDF chống ẩm / mặt lavabo sứ', '3 năm', 'Trắng', 'LAVABO-TREO-90', 5, NOW(), NOW()),
  ('a0000010-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000010', 3600000.00, 25, 8.0, 80.0, 4.0, 70.0, 'Gương bạc / khung nhôm', '1 năm', 'Đen', 'GUONG-LED-80-DEN', 5, NOW(), NOW()),
  ('a0000010-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000010', 4200000.00, 12, 8.5, 90.0, 4.0, 75.0, 'Gương bạc / khung nhôm', '1 năm', 'Xám than', 'GUONG-LED-90-XAM', 5, NOW(), NOW());

INSERT INTO product_favorite_logs (id, user_id, product_id, created_at, updated_at) VALUES
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000001', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
  (gen_random_uuid(), '52379d96-5238-4fd9-8383-bae82736bb3b', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  (gen_random_uuid(), '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  (gen_random_uuid(), '7c22e6d3-1111-4aab-b999-aabbcc001122', 'e0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  (gen_random_uuid(), '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'e0000000-0000-0000-0000-000000000010', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

INSERT INTO reviews (
  id,
  title,
  user_id,
  user_name,
  product_id,
  order_item_id,
  content_text,
  content_hash,
  rating,
  status,
  trust_score,
  created_at,
  updated_at
) VALUES
  ('b0000001-0000-0000-0000-000000000001', 'Sofa da rất xịn!', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hiền', 'e0000000-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 'Mình hài lòng với chiếc sofa này. Chất da mềm, màu sắc đẹp, giao hàng cẩn thận.', md5('Mình hài lòng với chiếc sofa này.'), 5, 'VISIBLE', 0.90, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
  ('b0000001-0000-0000-0000-000000000002', 'Chất lượng ổn, giá hơi cao', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01', 'e0000000-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000002', 'Sofa đẹp, ngồi thoải mái. Giá hơi cao nhưng hoàn thiện tốt.', md5('Sofa đẹp, ngồi thoải mái.'), 4, 'VISIBLE', 0.75, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  ('b0000002-0000-0000-0000-000000000001', 'Sofa vải siêu thoải mái', '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02', 'e0000000-0000-0000-0000-000000000002', 'c0000002-0000-0000-0000-000000000001', 'Mua cho phòng khách nhà, gia đình rất thích. Vải mềm, đệm dày, dễ vệ sinh.', md5('Mua cho phòng khách nhà, gia đình rất thích.'), 5, 'VISIBLE', 0.85, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
  ('b0000003-0000-0000-0000-000000000001', 'Bàn trà đẹp, lắp dễ', '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03', 'e0000000-0000-0000-0000-000000000003', 'c0000003-0000-0000-0000-000000000001', 'Bàn trà chắc chắn, hướng dẫn lắp ráp rõ ràng, bề mặt đẹp.', md5('Bàn trà chắc chắn, hướng dẫn lắp ráp rõ ràng.'), 5, 'VISIBLE', 0.95, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
  ('b0000004-0000-0000-0000-000000000001', 'Giường king chắc, không tiếng kêu', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hiền', 'e0000000-0000-0000-0000-000000000004', 'c0000004-0000-0000-0000-000000000001', 'Dùng được 2 tháng, khung thép chắc chắn, sơn không bị tróc.', md5('Dùng được 2 tháng, khung thép chắc chắn.'), 5, 'VISIBLE', 0.88, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  ('b0000009-0000-0000-0000-000000000001', 'Tủ lavabo gọn và đẹp', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01', 'e0000000-0000-0000-0000-000000000009', 'c0000009-0000-0000-0000-000000000001', 'Tủ treo tường giúp phòng tắm thoáng hơn, ngăn kéo đóng êm và mặt lavabo dễ lau.', md5('Tủ treo tường giúp phòng tắm thoáng hơn.'), 5, 'VISIBLE', 0.92, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  ('b0000009-0000-0000-0000-000000000002', 'Tốt nhưng giao hàng chậm', '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02', 'e0000000-0000-0000-0000-000000000009', 'c0000009-0000-0000-0000-000000000002', 'Sản phẩm chất lượng tốt, đúng mô tả. Khâu giao hàng cần cải thiện.', md5('Sản phẩm chất lượng tốt, đúng mô tả.'), 3, 'VISIBLE', 0.65, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
  ('b0000010-0000-0000-0000-000000000001', 'Gương LED sáng dịu, rất tiện', '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03', 'e0000000-0000-0000-0000-000000000010', 'c0000010-0000-0000-0000-000000000001', 'Đèn LED sáng vừa đủ, soi rõ mặt nhưng không chói. Lắp ở khu lavabo rất hợp.', md5('Đèn LED sáng vừa đủ, soi rõ mặt nhưng không chói.'), 5, 'VISIBLE', 0.87, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

COMMIT;

-- ============================================================
-- furnisight_promotion_db
-- Schema tham chieu:
--   - promotion-service/.../V1__create_promotion_tables.sql
-- ============================================================
\connect furnisight_promotion_db;

BEGIN;

DELETE FROM user_vouchers
WHERE promotion_id IN (
  '81000000-0000-0000-0000-000000000001',
  '81000000-0000-0000-0000-000000000002',
  '81000000-0000-0000-0000-000000000003',
  '81000000-0000-0000-0000-000000000004',
  '81000000-0000-0000-0000-000000000005',
  '81000000-0000-0000-0000-000000000006'
);

DELETE FROM promotions
WHERE id IN (
  '81000000-0000-0000-0000-000000000001',
  '81000000-0000-0000-0000-000000000002',
  '81000000-0000-0000-0000-000000000003',
  '81000000-0000-0000-0000-000000000004',
  '81000000-0000-0000-0000-000000000005',
  '81000000-0000-0000-0000-000000000006'
)
OR code IN (
  'WELCOME10',
  'FREESHIP',
  'FURNI500K',
  'MINHHIEN15',
  'VIPSHIP',
  'SUMMER25'
);

INSERT INTO promotions (
  id,
  code,
  name,
  description,
  icon,
  voucher_type,
  discount_type,
  discount_value,
  max_discount,
  min_order,
  start_date,
  end_date,
  active,
  created_at,
  updated_at
) VALUES
  (
    '81000000-0000-0000-0000-000000000001',
    'WELCOME10',
    'Welcome 10%',
    'Public voucher: giam 10% cho don tu 3.000.000, toi da 500.000.',
    'badgePercent',
    'PUBLIC',
    'PERCENT',
    10,
    500000,
    3000000,
    NOW() - INTERVAL '30 days',
    NOW() + INTERVAL '180 days',
    TRUE,
    NOW(),
    NOW()
  ),
  (
    '81000000-0000-0000-0000-000000000002',
    'FREESHIP',
    'Free shipping',
    'Public shipping voucher: giam phi van chuyen toi da 100.000.',
    'truck',
    'PUBLIC',
    'SHIPPING_CAP',
    100000,
    NULL,
    1000000,
    NOW() - INTERVAL '30 days',
    NOW() + INTERVAL '180 days',
    TRUE,
    NOW(),
    NOW()
  ),
  (
    '81000000-0000-0000-0000-000000000003',
    'FURNI500K',
    'Furni 500K',
    'Public voucher: giam truc tiep 500.000 cho don tu 10.000.000.',
    'ticket',
    'PUBLIC',
    'FIXED',
    500000,
    NULL,
    10000000,
    NOW() - INTERVAL '30 days',
    NOW() + INTERVAL '90 days',
    TRUE,
    NOW(),
    NOW()
  ),
  (
    '81000000-0000-0000-0000-000000000004',
    'MINHHIEN15',
    'Personal 15%',
    'Personal voucher gan san cho account minhhien.',
    'sparkles',
    'PERSONAL',
    'PERCENT',
    15,
    750000,
    5000000,
    NOW() - INTERVAL '7 days',
    NOW() + INTERVAL '60 days',
    TRUE,
    NOW(),
    NOW()
  ),
  (
    '81000000-0000-0000-0000-000000000005',
    'VIPSHIP',
    'VIP shipping',
    'Personal shipping voucher gan san cho account admin de test saved voucher.',
    'truck',
    'PERSONAL',
    'SHIPPING_CAP',
    200000,
    NULL,
    2000000,
    NOW() - INTERVAL '7 days',
    NOW() + INTERVAL '60 days',
    TRUE,
    NOW(),
    NOW()
  ),
  (
    '81000000-0000-0000-0000-000000000006',
    'SUMMER25',
    'Summer marketing preview',
    'Marketing voucher mau cho admin list/stats, chua dung de phat hang loat o v1.',
    'megaphone',
    'MARKETING',
    'PERCENT',
    25,
    1000000,
    15000000,
    NOW() + INTERVAL '7 days',
    NOW() + INTERVAL '45 days',
    FALSE,
    NOW(),
    NOW()
  );

INSERT INTO user_vouchers (
  id,
  user_id,
  promotion_id,
  is_used,
  used_at,
  saved_at
) VALUES
  (
    '82000000-0000-0000-0000-000000000001',
    '52379d96-5238-4fd9-8383-bae82736bb3b',
    '81000000-0000-0000-0000-000000000004',
    FALSE,
    NULL,
    NOW() - INTERVAL '2 days'
  ),
  (
    '82000000-0000-0000-0000-000000000002',
    'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
    '81000000-0000-0000-0000-000000000005',
    FALSE,
    NULL,
    NOW() - INTERVAL '1 day'
  );

DELETE FROM marketing_dispatch_logs
WHERE source_id IN (
  '83000000-0000-0000-0000-000000000001',
  '85000000-0000-0000-0000-000000000001'
);

DELETE FROM marketing_campaigns
WHERE id IN (
  '83000000-0000-0000-0000-000000000001',
  '83000000-0000-0000-0000-000000000002'
);

DELETE FROM marketing_notifications
WHERE id IN (
  '85000000-0000-0000-0000-000000000001',
  '85000000-0000-0000-0000-000000000002'
);

DELETE FROM promotion_combo_items
WHERE combo_id IN (
  '84000000-0000-0000-0000-000000000001',
  '84000000-0000-0000-0000-000000000002'
);

DELETE FROM promotion_combos
WHERE id IN (
  '84000000-0000-0000-0000-000000000001',
  '84000000-0000-0000-0000-000000000002'
);

INSERT INTO marketing_campaigns (
  id, name, voucher_id, target_type, target_user_ids, segment_key, channels,
  schedule_type, scheduled_at, notification_title, notification_body, status,
  sent_count, active, dispatched_at, created_at, updated_at
) VALUES
  ('83000000-0000-0000-0000-000000000001', 'Phat WELCOME10 cho khach moi', '81000000-0000-0000-0000-000000000001', 'SEGMENT', '', 'NEW_USERS', 'NOTIFICATION,EMAIL', 'SCHEDULED', NOW() + INTERVAL '1 day', 'Ban vua nhan voucher WELCOME10', 'Dung voucher WELCOME10 de giam 10% cho don hang dau tien.', 'SCHEDULED', 0, TRUE, NULL, NOW(), NOW()),
  ('83000000-0000-0000-0000-000000000002', 'Nhac gio hang bo quen', '81000000-0000-0000-0000-000000000003', 'SEGMENT', '', 'ABANDONED_CART', 'NOTIFICATION', 'DRAFT', NULL, 'Uu dai cho gio hang cua ban', 'Hoan tat don hang hom nay de nhan uu dai noi that.', 'DRAFT', 0, TRUE, NULL, NOW(), NOW());

INSERT INTO promotion_combos (
  id, name, description, discount_type, discount_value, start_date, end_date,
  active, placements, original_amount, final_amount, saved_amount, used_count,
  created_at, updated_at
) VALUES
  ('84000000-0000-0000-0000-000000000001', 'Combo phong ngu LuxNest', 'Giuong, tu ao va ban trang diem cho phong ngu.', 'PERCENTAGE', 15, NOW() - INTERVAL '7 days', NOW() + INTERVAL '60 days', TRUE, 'PRODUCT_DETAIL,CART,CHECKOUT', 11000000, 9350000, 1650000, 24, NOW(), NOW()),
  ('84000000-0000-0000-0000-000000000002', 'Combo phong khach tinh gon', 'Sofa va ban tra cho phong khach hien dai.', 'FIXED_AMOUNT', 1300000, NOW() - INTERVAL '7 days', NOW() + INTERVAL '45 days', TRUE, 'PRODUCT_DETAIL,CART', 10300000, 9000000, 1300000, 11, NOW(), NOW());

INSERT INTO promotion_combo_items (
  id, combo_id, product_id, variant_id, product_name, sku, category_name, image,
  price, quantity, snapshot_missing, created_at, updated_at
) VALUES
  ('84100000-0000-0000-0000-000000000001', '84000000-0000-0000-0000-000000000001', 'product-bed-001', 'variant-bed-001', 'Giuong go LuxBed 01', 'BED-LUX-001', 'Phong ngu', 'bedDouble', 5000000, 1, FALSE, NOW(), NOW()),
  ('84100000-0000-0000-0000-000000000002', '84000000-0000-0000-0000-000000000001', 'product-wardrobe-003', 'variant-wardrobe-003', 'Tu ao 3 canh LuxWardrobe', 'WAR-LUX-003', 'Phong ngu', 'box', 4000000, 1, FALSE, NOW(), NOW()),
  ('84100000-0000-0000-0000-000000000003', '84000000-0000-0000-0000-000000000001', 'product-makeup-002', 'variant-makeup-002', 'Ban trang diem LuxMakeup', 'MAKEUP-LUX-002', 'Phong ngu', 'sparkles', 2000000, 1, FALSE, NOW(), NOW()),
  ('84100000-0000-0000-0000-000000000004', '84000000-0000-0000-0000-000000000002', 'product-sofa-004', 'variant-sofa-004', 'Sofa goc LuxSofa', 'SOFA-LUX-004', 'Phong khach', 'sofa', 8500000, 1, FALSE, NOW(), NOW()),
  ('84100000-0000-0000-0000-000000000005', '84000000-0000-0000-0000-000000000002', 'product-table-005', 'variant-table-005', 'Ban tra LuxTable', 'TABLE-LUX-005', 'Phong khach', 'table', 1800000, 1, FALSE, NOW(), NOW());

INSERT INTO marketing_notifications (
  id, title, body, target_type, target_user_ids, segment_key, channels, send_type,
  scheduled_at, related_voucher_id, status, sent_count, active, dispatched_at,
  created_at, updated_at
) VALUES
  ('85000000-0000-0000-0000-000000000001', 'Ban vua nhan voucher WELCOME10', 'Kiem tra vi voucher va dung uu dai trong checkout.', 'ALL', '', NULL, 'NOTIFICATION', 'DRAFT', NULL, '81000000-0000-0000-0000-000000000001', 'DRAFT', 0, TRUE, NULL, NOW(), NOW()),
  ('85000000-0000-0000-0000-000000000002', 'Uu dai noi that cuoi tuan', 'Khach VIP nhan uu dai dac biet cho bo suu tap moi.', 'SEGMENT', '', 'VIP', 'NOTIFICATION,EMAIL', 'SCHEDULED', NOW() + INTERVAL '3 days', NULL, 'SCHEDULED', 0, TRUE, NULL, NOW(), NOW());

COMMIT;

-- ============================================================
-- furnisight_order_db
-- Schema tham chieu:
--   - order-service/.../V1__init_order_schema.sql den V8
-- ============================================================
\connect furnisight_order_db;

BEGIN;

DELETE FROM stock_reservations
WHERE order_code IN ('ORD-A1B2C3D4', 'ORD-X9Y8Z7W6');

DELETE FROM order_items
WHERE order_id IN (
  'c5379d96-1111-4fd9-8383-bae82736bb11',
  'd5379d96-2222-4fd9-8383-bae82736bb22'
);

DELETE FROM orders
WHERE id IN (
  'c5379d96-1111-4fd9-8383-bae82736bb11',
  'd5379d96-2222-4fd9-8383-bae82736bb22'
)
OR order_code IN ('ORD-A1B2C3D4', 'ORD-X9Y8Z7W6');

DELETE FROM user_vouchers
WHERE promotion_id IN (
  '71000000-0000-0000-0000-000000000001',
  '71000000-0000-0000-0000-000000000002',
  '71000000-0000-0000-0000-000000000003'
);

DELETE FROM promotions
WHERE id IN (
  '71000000-0000-0000-0000-000000000001',
  '71000000-0000-0000-0000-000000000002',
  '71000000-0000-0000-0000-000000000003'
)
OR code IN ('WELCOME10', 'FREESHIP', 'FURNI500K');

INSERT INTO promotions (
  id, code, name, description, icon, discount_type, discount_value,
  max_discount, min_order, start_date, end_date, active, created_at, updated_at
) VALUES
  ('71000000-0000-0000-0000-000000000001', 'WELCOME10', 'Ưu đãi khách hàng mới', 'Giảm 10% cho đơn hàng đầu tiên.', 'percent', 'PERCENT', 10, 500000, 3000000, NOW() - INTERVAL '30 days', NOW() + INTERVAL '180 days', TRUE, NOW(), NOW()),
  ('71000000-0000-0000-0000-000000000002', 'FREESHIP', 'Miễn phí vận chuyển', 'Giảm tối đa 100.000đ phí vận chuyển.', 'truck', 'SHIPPING_CAP', 100000, 100000, 1000000, NOW() - INTERVAL '30 days', NOW() + INTERVAL '180 days', TRUE, NOW(), NOW()),
  ('71000000-0000-0000-0000-000000000003', 'FURNI500K', 'Giảm 500.000đ', 'Giảm trực tiếp 500.000đ cho đơn nội thất từ 10 triệu.', 'ticket', 'FIXED', 500000, 500000, 10000000, NOW() - INTERVAL '30 days', NOW() + INTERVAL '90 days', TRUE, NOW(), NOW());

INSERT INTO user_vouchers (id, user_id, promotion_id, is_used, used_at, saved_at) VALUES
  ('72000000-0000-0000-0000-000000000001', '52379d96-5238-4fd9-8383-bae82736bb3b', '71000000-0000-0000-0000-000000000001', FALSE, NULL, NOW() - INTERVAL '3 days'),
  ('72000000-0000-0000-0000-000000000002', '52379d96-5238-4fd9-8383-bae82736bb3b', '71000000-0000-0000-0000-000000000002', FALSE, NULL, NOW() - INTERVAL '2 days');

-- Mock Orders cho minhhien7840@gmail.com
INSERT INTO orders (
  id, order_code, user_id, status, sub_total, total_amount, shipping_fee,
  shipping_discount, discount_amount, insurance_fee, saved_amount,
  shipping_address_name, shipping_address_phone, shipping_address_detail,
  shipping_method, customer_note, payment_method, payment_status, paid_amount,
  paid_at, order_created_at, payment_initiated_at, payment_completed_at,
  payment_failed_at, shop_voucher_code, shipping_voucher_code,
  created_at, updated_at, created_by, updated_by
) VALUES
  ('c5379d96-1111-4fd9-8383-bae82736bb11', 'ORD-A1B2C3D4', '52379d96-5238-4fd9-8383-bae82736bb3b', 'UNPAID', 4500000, 4500000, 100000, 100000, 0, 0, 100000, 'Minh Hiền', '0901234567', 'Khu phố 6, Phường Linh Trung, Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 'STANDARD', 'Giao hàng trong giờ hành chính', 'VNPAY', 'PENDING', 0, NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NULL, NULL, NULL, 'FREESHIP', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', 'seed-data', 'seed-data'),
  ('d5379d96-2222-4fd9-8383-bae82736bb22', 'ORD-X9Y8Z7W6', '52379d96-5238-4fd9-8383-bae82736bb3b', 'DELIVERED', 12000000, 11500000, 100000, 100000, 500000, 0, 600000, 'Minh Hiền', '0901234567', 'Khu phố 6, Phường Linh Trung, Thành phố Thủ Đức, Thành phố Hồ Chí Minh', 'STANDARD', NULL, 'VNPAY', 'PAID', 11500000, NOW() - INTERVAL '7 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', NULL, 'FURNI500K', 'FREESHIP', NOW() - INTERVAL '8 days', NOW() - INTERVAL '2 days', 'seed-data', 'seed-data');

INSERT INTO order_items (
  id, order_id, product_id, variant_id, category_name, product_name, color,
  material, warranty, weight, length, width, height, price, quantity,
  image_url, created_at, updated_at, created_by, updated_by
) VALUES
  ('e5379d96-3333-4fd9-8383-bae82736bb33', 'c5379d96-1111-4fd9-8383-bae82736bb11', 'e0000000-0000-0000-0000-000000000003', 'a0000003-0000-0000-0000-000000000001', 'Bàn trà', 'Bàn trà gỗ sồi tối giản', 'Màu gỗ tự nhiên', 'Gỗ sồi trắng', '1 năm', 15, 100, 60, 45, 4500000, 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', 'seed-data', 'seed-data'),
  ('f5379d96-4444-4fd9-8383-bae82736bb44', 'd5379d96-2222-4fd9-8383-bae82736bb22', 'e0000000-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001', 'Ghế sofa', 'Sofa da bò hiện đại', 'Nâu đỏ', 'Da bò tự nhiên', '2 năm', 50, 200, 90, 85, 12000000, 1, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', 'seed-data', 'seed-data');

INSERT INTO stock_reservations (
  id, order_code, product_id, product_variant_id, quantity, created_at
) VALUES
  ('73000000-0000-0000-0000-000000000001', 'ORD-A1B2C3D4', 'e0000000-0000-0000-0000-000000000003', 'a0000003-0000-0000-0000-000000000001', 1, NOW() - INTERVAL '2 hours');

COMMIT;
