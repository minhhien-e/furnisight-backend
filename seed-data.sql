-- ============================================================
-- SEED DATA
-- Chay thu cong sau khi Docker stack va Flyway migrations da san sang.
--
-- Cach chay:
--   docker exec -i furnisight_user_postgres psql -U postgres < seed-data.sql
-- ============================================================

-- ============================================================
-- furnisight_user_db
-- Schema tham chieu:
--   - user-service/.../V1__init_identity_schema.sql
--   - user-service/.../V2__init_profile_schema.sql
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
--   - catalog-service/.../V1__init_schema.sql
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
  ('c0000000-0000-0000-0000-000000000001', 'Living Room', 'living-room', NULL, 'living-room', 3, 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800', 'living-room', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000002', 'Bedroom', 'bedroom', NULL, 'bedroom', 3, 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800', 'bedroom', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000003', 'Dining Room', 'dining-room', NULL, 'dining-room', 2, 'https://images.unsplash.com/photo-1617806118233-18e1c0945594?auto=format&fit=crop&q=80&w=800', 'dining-room', NOW(), NOW()),
  ('c0000000-0000-0000-0000-000000000004', 'Workspace', 'workspace', NULL, 'workspace', 2, 'https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&q=80&w=800', 'workspace', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000001', 'Sofa', 'sofa', 'c0000000-0000-0000-0000-000000000001', 'living-room/sofa', 2, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800', 'sofa', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000002', 'Coffee Table', 'coffee-table', 'c0000000-0000-0000-0000-000000000001', 'living-room/coffee-table', 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800', 'coffee-table', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000003', 'Bed', 'bed', 'c0000000-0000-0000-0000-000000000002', 'bedroom/bed', 2, 'https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800', 'bed', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000004', 'Wardrobe', 'wardrobe', 'c0000000-0000-0000-0000-000000000002', 'bedroom/wardrobe', 1, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800', 'wardrobe', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000005', 'Dining Table', 'dining-table', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-table', 1, 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800', 'dining-table', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000006', 'Dining Chair', 'dining-chair', 'c0000000-0000-0000-0000-000000000003', 'dining-room/dining-chair', 1, 'https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800', 'dining-chair', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000007', 'Desk', 'desk', 'c0000000-0000-0000-0000-000000000004', 'workspace/desk', 1, 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=800', 'desk', NOW(), NOW()),
  ('d0000000-0000-0000-0000-000000000008', 'Office Chair', 'office-chair', 'c0000000-0000-0000-0000-000000000004', 'workspace/office-chair', 1, 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=800', 'office-chair', NOW(), NOW());

INSERT INTO collections (id, name, description, slug, created_at, updated_at) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Urban Comfort', 'Bo suu tap phong khach hien dai va toi gian.', 'urban-comfort', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000002', 'Sleep Sanctuary', 'Bo suu tap phong ngu toi uu su thoai mai.', 'sleep-sanctuary', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000003', 'Dining Luxe', 'Noi that phong an trang nha va ben bi.', 'dining-luxe', NOW(), NOW()),
  ('f0000000-0000-0000-0000-000000000004', 'Work Smart', 'Noi that workspace cong thai hoc va hieu qua.', 'work-smart', NOW(), NOW());

INSERT INTO products (
  id,
  category_id,
  collection_id,
  name,
  slug,
  description,
  product_status,
  model_url,
  supports_3d,
  features,
  created_at,
  updated_at
) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', 'Modern Leather Sofa', 'modern-leather-sofa', 'Ghe sofa da cao cap phong cach hien dai cho phong khach cua ban.', 'ACTIVE', NULL, FALSE, '["Da that","Khung go soi","Chong tham nuoc"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', 'Fabric Sectional Sofa', 'fabric-sectional-sofa', 'Sofa vai chu L thoai mai cho khong gian song rong mo.', 'ACTIVE', NULL, FALSE, '["Vai ni cao cap","Thiet ke chu L","Dem mut xop 3 lop"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', 'Minimalist Oak Coffee Table', 'minimalist-oak-coffee-table', 'Ban ca phe go soi phong cach toi gian, de phoi noi that.', 'ACTIVE', NULL, FALSE, '["Go soi nguyen khoi","Phong cach toi gian"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002', 'King Size Metal Bed', 'king-size-metal-bed', 'Khung giuong kim loai king size chac chan va rong rai.', 'ACTIVE', NULL, FALSE, '["Son tinh dien","Khung thep chiu luc","De lap rap"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002', 'Queen Size Wooden Bed', 'queen-size-wooden-bed', 'Giuong queen size co dien lam tu go thong.', 'ACTIVE', NULL, FALSE, '["Go thong tu nhien","Phong cach co dien"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000006', 'd0000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000002', 'Sliding Door Wardrobe', 'sliding-door-wardrobe', 'Tu quan ao rong rai voi cua truot tich hop guong lon.', 'ACTIVE', NULL, FALSE, '["Cua truot tien loi","Tich hop guong lon","Go cong nghiep MDF"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000003', 'Marble Top Dining Table', 'marble-top-dining-table', 'Ban an sang trong voi mat da cam thach cho khong gian tiep khach.', 'ACTIVE', NULL, FALSE, '["Mat da cam thach","Chan ban boc dong","Phong cach chau Au"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000008', 'd0000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000003', 'Velvet Dining Chair', 'velvet-dining-chair', 'Ghe an nhung mem mai voi ve ngoai sang trong.', 'ACTIVE', NULL, FALSE, '["Boc vai nhung","Chan ma vang","Nem mut em ai"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000009', 'd0000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000004', 'Ergonomic Standing Desk', 'ergonomic-standing-desk', 'Ban dung dieu chinh chieu cao giup lam viec thoai mai hon.', 'ACTIVE', NULL, TRUE, '["Dieu chinh do cao bang dien","Mat ban chong xuoc","Ghi nho 3 vi tri"]', NOW(), NOW()),
  ('e0000000-0000-0000-0000-000000000010', 'd0000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000004', 'Mesh Ergonomic Office Chair', 'mesh-ergonomic-office-chair', 'Ghe cong thai hoc luoi thoang khi voi dem that lung.', 'ACTIVE', NULL, TRUE, '["Luoi thoang khi 3D","Ho tro cot song","Tua dau dieu chinh"]', NOW(), NOW());

INSERT INTO product_images (id, product_id, image_url, position, created_at, updated_at) VALUES
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000006', 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000007', 'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000008', 'https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000009', 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&q=80&w=1200', 1, NOW(), NOW()),
  (gen_random_uuid(), 'e0000000-0000-0000-0000-000000000010', 'https://images.unsplash.com/photo-1580480055273-228ff5388ef8?auto=format&fit=crop&q=80&w=1200', 2, NOW(), NOW());

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
  created_at,
  updated_at
) VALUES
  ('a0000001-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 12000000.00, 10, 50.0, 200.0, 90.0, 85.0, 'Da bo tu nhien', '2 nam', 'Nau do', NOW(), NOW()),
  ('a0000001-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 15000000.00, 5, 60.0, 220.0, 95.0, 90.0, 'Da bo cao cap', '2 nam', 'Den tuyen', NOW(), NOW()),
  ('a0000002-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000002', 18000000.00, 8, 70.0, 250.0, 150.0, 85.0, 'Vai ni Han Quoc', '1 nam', 'Xam', NOW(), NOW()),
  ('a0000003-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003', 4500000.00, 15, 15.0, 100.0, 60.0, 45.0, 'Go soi trang', '1 nam', 'Mau go tu nhien', NOW(), NOW()),
  ('a0000004-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000004', 9500000.00, 12, 40.0, 210.0, 190.0, 35.0, 'Thep khong gi', '3 nam', 'Trang tinh dien', NOW(), NOW()),
  ('a0000005-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000005', 8500000.00, 14, 35.0, 200.0, 160.0, 40.0, 'Go thong New Zealand', '2 nam', 'Nau dam', NOW(), NOW()),
  ('a0000006-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000006', 13500000.00, 7, 80.0, 220.0, 180.0, 60.0, 'MDF phu Melamine', '1 nam', 'Trang nga', NOW(), NOW()),
  ('a0000007-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 24000000.00, 6, 60.0, 180.0, 90.0, 75.0, 'Da cam thach that', '5 nam', 'Trang van may', NOW(), NOW()),
  ('a0000008-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000008', 2500000.00, 20, 8.0, 50.0, 55.0, 90.0, 'Nhung / Chan sat', '1 nam', 'Hong nhat', NOW(), NOW()),
  ('a0000009-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000009', 11500000.00, 18, 30.0, 120.0, 60.0, 70.0, 'Mat go MDF / Chan thep', '3 nam', 'Den', NOW(), NOW()),
  ('a0000010-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000010', 5800000.00, 25, 12.0, 65.0, 65.0, 110.0, 'Luoi thoang khi / Khung nhua', '1 nam', 'Den', NOW(), NOW()),
  ('a0000010-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000010', 6200000.00, 12, 12.5, 65.0, 65.0, 115.0, 'Luoi thoang khi / Khung nhua', '1 nam', 'Xam than', NOW(), NOW());

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
  ('b0000001-0000-0000-0000-000000000001', 'Sofa da rat xin!', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hien', 'e0000000-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000001', 'Minh hai long voi chiec sofa nay. Chat da mem, mau sac dep, giao hang can than.', md5('Minh hai long voi chiec sofa nay.'), 5, 'VISIBLE', 0.90, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
  ('b0000001-0000-0000-0000-000000000002', 'Chat luong on, gia hoi cao', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01', 'e0000000-0000-0000-0000-000000000001', 'c0000001-0000-0000-0000-000000000002', 'Sofa dep, ngoi thoai mai. Gia hoi cao nhung hoan thien tot.', md5('Sofa dep, ngoi thoai mai.'), 4, 'VISIBLE', 0.75, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
  ('b0000002-0000-0000-0000-000000000001', 'Sofa vai sieu thoai mai', '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02', 'e0000000-0000-0000-0000-000000000002', 'c0000002-0000-0000-0000-000000000001', 'Mua cho phong khach nha, gia dinh rat thich. Vai mem, dem day, de ve sinh.', md5('Mua cho phong khach nha, gia dinh rat thich.'), 5, 'VISIBLE', 0.85, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
  ('b0000003-0000-0000-0000-000000000001', 'Ban tra dep, lap de', '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03', 'e0000000-0000-0000-0000-000000000003', 'c0000003-0000-0000-0000-000000000001', 'Ban tra chac chan, huong dan lap rap ro rang, be mat dep.', md5('Ban tra chac chan, huong dan lap rap ro rang.'), 5, 'VISIBLE', 0.95, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
  ('b0000004-0000-0000-0000-000000000001', 'Giuong king dinh, khong tieng keu', '52379d96-5238-4fd9-8383-bae82736bb3b', 'Minh Hien', 'e0000000-0000-0000-0000-000000000004', 'c0000004-0000-0000-0000-000000000001', 'Dung duoc 2 thang, khung thep chac chan, son khong bi troc.', md5('Dung duoc 2 thang, khung thep chac chan.'), 5, 'VISIBLE', 0.88, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
  ('b0000009-0000-0000-0000-000000000001', 'Ban dung thay doi cuoc song lam viec', '4b33e5c1-cae1-458d-b4b1-e568ddd766f6', 'User 01', 'e0000000-0000-0000-0000-000000000009', 'c0000009-0000-0000-0000-000000000001', 'Motor em, dieu chinh muot, be mat rong, rat hop de 2 man hinh.', md5('Motor em, dieu chinh muot, be mat rong.'), 5, 'VISIBLE', 0.92, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
  ('b0000009-0000-0000-0000-000000000002', 'Tot nhung giao hang cham', '7c22e6d3-1111-4aab-b999-aabbcc001122', 'User 02', 'e0000000-0000-0000-0000-000000000009', 'c0000009-0000-0000-0000-000000000002', 'San pham chat luong tot, dung mo ta. Khau giao hang can cai thien.', md5('San pham chat luong tot, dung mo ta.'), 3, 'VISIBLE', 0.65, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
  ('b0000010-0000-0000-0000-000000000001', 'Ghe luoi cuc thoang, ngoi lau khong moi', '8d33f7e4-2222-4bbc-caaa-bbccdd002233', 'User 03', 'e0000000-0000-0000-0000-000000000010', 'c0000010-0000-0000-0000-000000000001', 'Luoi thoang khi rat de chiu, dem that lung do dung diem, ngoi van thoai mai.', md5('Luoi thoang khi rat de chiu, dem that lung do dung diem.'), 5, 'VISIBLE', 0.87, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days');

COMMIT;

-- ============================================================
-- furnisight_order_db
-- ============================================================
\connect furnisight_order_db;

BEGIN;

DELETE FROM order_items;
DELETE FROM orders;

-- Mock Orders cho minhhien7840@gmail.com
INSERT INTO orders (id, order_code, user_id, status, sub_total, total_amount, shipping_fee, shipping_discount, discount_amount, insurance_fee, saved_amount, shipping_address_name, shipping_address_phone, shipping_address_detail, shipping_method, customer_note, payment_method, payment_status, paid_amount, created_at, updated_at) VALUES 
('c5379d96-1111-4fd9-8383-bae82736bb11', 'ORD-A1B2C3D4', '52379d96-5238-4fd9-8383-bae82736bb3b', 'UNPAID', 5000000, 5015000, 15000, 0, 0, 0, 0, 'Minh Hiền', '0123456789', '123 Đường A, Quận B, TP C', 'Giao hàng tận nơi', 'Giao sáng sớm', 'COD', 'UNPAID', 0, NOW(), NOW()),
('d5379d96-2222-4fd9-8383-bae82736bb22', 'ORD-X9Y8Z7W6', '52379d96-5238-4fd9-8383-bae82736bb3b', 'DELIVERED', 12000000, 12015000, 15000, 0, 0, 0, 0, 'Minh Hiền', '0123456789', '123 Đường A, Quận B, TP C', 'Giao hàng tận nơi', '', 'BANK_TRANSFER', 'PAID', 12015000, NOW() - INTERVAL '5 DAYS', NOW());

INSERT INTO order_items (id, order_id, product_id, variant_id, category_name, product_name, color, material, warranty, weight, length, width, height, price, old_price, quantity, image_url, created_at, updated_at) VALUES 
('e5379d96-3333-4fd9-8383-bae82736bb33', 'c5379d96-1111-4fd9-8383-bae82736bb11', 'prod-1', 'var-1', 'Sofa', 'Sofa Gỗ Xoan Đào', 'Nâu', 'Gỗ Xoan Đào', '12 tháng', 50000, 120, 60, 40, 5000000, 5500000, 1, 'https://example.com/sofa.jpg', NOW(), NOW()),
('f5379d96-4444-4fd9-8383-bae82736bb44', 'd5379d96-2222-4fd9-8383-bae82736bb22', 'prod-2', 'var-2', 'Giường', 'Giường Thông Minh', 'Trắng', 'Gỗ Công Nghiệp', '24 tháng', 80000, 200, 160, 30, 12000000, 15000000, 1, 'https://example.com/bed.jpg', NOW() - INTERVAL '5 DAYS', NOW() - INTERVAL '5 DAYS');

COMMIT;
