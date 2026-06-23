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
--   - user-service/.../V6__drop_district_from_user_addresses.sql
-- ============================================================
\connect furnisight_user_db;

BEGIN;

-- Roles
INSERT INTO
    roles (
        id,
        name,
        permissions,
        position,
        created_at,
        updated_at
    )
VALUES (
        gen_random_uuid (),
        'ADMIN',
        -1,
        100,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'USER',
        0,
        1,
        NOW(),
        NOW()
    ) ON CONFLICT (name) DO
UPDATE
SET
    permissions = EXCLUDED.permissions,
    position = EXCLUDED.position,
    updated_at = NOW();

-- Xoa lien ket/profile cua tap account seed de script co the chay lap lai.
DELETE FROM account_roles
WHERE
    account_id IN (
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
    );

DELETE FROM user_profiles
WHERE
    account_id IN (
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
    );

DELETE FROM user_addresses
WHERE
    account_id IN (
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
    );

DELETE FROM favorite_products
WHERE
    account_id IN (
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233'
    );

DELETE FROM accounts
WHERE
    username IN (
        'minhhien',
        'admin',
        'user01',
        'user02',
        'user03'
    )
    OR email IN (
        'minhhien7840@gmail.com',
        'admin@furnisight.store',
        '22130080@st.hcmuaf.edu.vn',
        'user02@furnisight.store',
        'user03@furnisight.store'
    );

-- Accounts (password_hash = bcrypt('Password123!'))
INSERT INTO
    accounts (
        id,
        username,
        email,
        password_hash,
        status,
        failed_login_attempts,
        lockout_end,
        created_at,
        updated_at
    )
VALUES (
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'minhhien',
        'minhhien7840@gmail.com',
        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a',
        'ACTIVE',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        'admin',
        'admin@furnisight.store',
        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a',
        'ACTIVE',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'user01',
        '22130080@st.hcmuaf.edu.vn',
        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a',
        'ACTIVE',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'user02',
        'user02@furnisight.store',
        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a',
        'ACTIVE',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'user03',
        'user03@furnisight.store',
        '$2b$10$MvTGAhLM9CISs.j9l9kNie733z5HhDKZZ3UBxhEH21uaP8VSIZT7a',
        'ACTIVE',
        0,
        NULL,
        NOW(),
        NOW()
    );

INSERT INTO
    account_roles (
        id,
        account_id,
        role_id,
        created_at,
        updated_at
    )
VALUES (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'USER'
        ),
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'ADMIN'
        ),
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'USER'
        ),
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'USER'
        ),
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'USER'
        ),
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        (
            SELECT id
            FROM roles
            WHERE
                name = 'USER'
        ),
        NOW(),
        NOW()
    );

INSERT INTO
    user_profiles (
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
    )
VALUES (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'Minh Hien',
        'Hien',
        'Minh',
        'https://api.dicebear.com/7.x/avataaars/svg?seed=minhhien',
        'minhhien7840@gmail.com',
        '2000-01-15',
        'MALE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'f85b5fd8-d60e-4c7e-87ae-5912796d668e',
        'Admin',
        'Admin',
        'FurniSight',
        'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
        'admin@furnisight.store',
        NULL,
        'OTHER',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'User 01',
        'Van',
        'An',
        'https://api.dicebear.com/7.x/avataaars/svg?seed=user01',
        '22130080@st.hcmuaf.edu.vn',
        '1999-05-20',
        'MALE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'User 02',
        'Thi',
        'Binh',
        'https://api.dicebear.com/7.x/avataaars/svg?seed=user02',
        'user02@furnisight.store',
        '2001-08-10',
        'FEMALE',
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'User 03',
        'Quoc',
        'Cuong',
        'https://api.dicebear.com/7.x/avataaars/svg?seed=user03',
        'user03@furnisight.store',
        '1998-12-03',
        'MALE',
        NOW(),
        NOW()
    );

INSERT INTO
    user_addresses (
        id,
        account_id,
        full_name,
        phone,
        province_code,
        province_name,
        ward_code,
        ward_name,
        detail,
        type,
        is_default,
        created_at,
        updated_at
    )
VALUES (
        '61000000-0000-0000-0000-000000000001',
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'Minh Hiền',
        '0901234567',
        '79',
        'Thành phố Hồ Chí Minh',
        '26824',
        'Phường Thủ Đức',
        'Khu phố 6',
        'HOME',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        '61000000-0000-0000-0000-000000000002',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'Văn An',
        '0902345678',
        '79',
        'Thành phố Hồ Chí Minh',
        '26737',
        'Phường Tân Định',
        '12 Nguyễn Huệ',
        'HOME',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        '61000000-0000-0000-0000-000000000003',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'Thị Bình',
        '0903456789',
        '79',
        'Thành phố Hồ Chí Minh',
        '26905',
        'Phường Bình Lợi Trung',
        '45 Nguyễn Gia Trí',
        'OFFICE',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        '61000000-0000-0000-0000-000000000004',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'Quốc Cường',
        '0904567890',
        '79',
        'Thành phố Hồ Chí Minh',
        '27142',
        'Phường Nhiêu Lộc',
        '89 Võ Văn Tần',
        'HOME',
        TRUE,
        NOW(),
        NOW()
    );

INSERT INTO
    favorite_products (
        id,
        account_id,
        product_id,
        created_at,
        updated_at
    )
VALUES (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '4 days',
        NOW() - INTERVAL '4 days'
    ),
    (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'e0000000-0000-0000-0000-000000000010',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    );

COMMIT;

-- ============================================================
-- furnisight_catalog_db
-- Schema tham chieu:
--   - catalog-service/.../V1__init_schema.sql den V7
-- ============================================================
\connect furnisight_catalog_db;

BEGIN;

-- Don seed cu de script co the chay lap lai.
CREATE TEMP TABLE seed_extra_products ON COMMIT DROP AS
SELECT ('e0000000-0000-0000-0000-' || lpad(product_no::text, 12, '0'))::uuid AS id
FROM generate_series(11, 40) AS product_no;

DELETE FROM reviews
WHERE
    product_id IN (
        SELECT id
        FROM seed_extra_products
    );

DELETE FROM product_favorite_logs
WHERE
    product_id IN (
        SELECT id
        FROM seed_extra_products
    );

DELETE FROM product_images
WHERE
    product_id IN (
        SELECT id
        FROM seed_extra_products
    );

DELETE FROM product_variants
WHERE
    product_id IN (
        SELECT id
        FROM seed_extra_products
    );

DELETE FROM products
WHERE
    id IN (
        SELECT id
        FROM seed_extra_products
    );

DELETE FROM reviews
WHERE
    product_id IN (
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
WHERE
    product_id IN (
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
WHERE
    product_id IN (
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
WHERE
    product_id IN (
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
WHERE
    id IN (
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

DELETE FROM categories
WHERE
    id IN (
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

INSERT INTO
    categories (
        id,
        name,
        slug,
        parent_id,
        path,
        product_count,
        image_url,
        icon_url,
        created_at,
        updated_at
    )
VALUES (
        'c0000000-0000-0000-0000-000000000001',
        'Phòng khách',
        'living-room',
        NULL,
        'living-room',
        10,
        'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800',
        'living-room',
        NOW(),
        NOW()
    ),
    (
        'c0000000-0000-0000-0000-000000000002',
        'Phòng ngủ',
        'bedroom',
        NULL,
        'bedroom',
        10,
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=800',
        'bedroom',
        NOW(),
        NOW()
    ),
    (
        'c0000000-0000-0000-0000-000000000003',
        'Phòng bếp',
        'kitchen',
        NULL,
        'kitchen',
        10,
        'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&q=80&w=800',
        'kitchen',
        NOW(),
        NOW()
    ),
    (
        'c0000000-0000-0000-0000-000000000004',
        'Phòng tắm',
        'bathroom',
        NULL,
        'bathroom',
        10,
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=800',
        'bathroom',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000001',
        'Ghế sofa',
        'sofa',
        'c0000000-0000-0000-0000-000000000001',
        'living-room/sofa',
        5,
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800',
        'sofa',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà',
        'coffee-table',
        'c0000000-0000-0000-0000-000000000001',
        'living-room/coffee-table',
        5,
        'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800',
        'coffee-table',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000003',
        'Giường ngủ',
        'bed',
        'c0000000-0000-0000-0000-000000000002',
        'bedroom/bed',
        5,
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=800',
        'bed',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000004',
        'Tủ quần áo',
        'wardrobe',
        'c0000000-0000-0000-0000-000000000002',
        'bedroom/wardrobe',
        5,
        'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=800',
        'wardrobe',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000005',
        'Bàn ăn',
        'dining-table',
        'c0000000-0000-0000-0000-000000000003',
        'kitchen/dining-table',
        5,
        'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=800',
        'dining-table',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp',
        'kitchen-cabinet',
        'c0000000-0000-0000-0000-000000000003',
        'kitchen/kitchen-cabinet',
        5,
        'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=800',
        'kitchen-cabinet',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo',
        'bathroom-vanity',
        'c0000000-0000-0000-0000-000000000004',
        'bathroom/bathroom-vanity',
        5,
        'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=800',
        'bathroom-vanity',
        NOW(),
        NOW()
    ),
    (
        'd0000000-0000-0000-0000-000000000008',
        'Gương phòng tắm',
        'bathroom-mirror',
        'c0000000-0000-0000-0000-000000000004',
        'bathroom/bathroom-mirror',
        5,
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=800',
        'bathroom-mirror',
        NOW(),
        NOW()
    );

INSERT INTO
    products (
        id,
        category_id,
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
    )
VALUES (
        'e0000000-0000-0000-0000-000000000001',
        'd0000000-0000-0000-0000-000000000001',
        'Sofa da bò hiện đại',
        'modern-leather-sofa',
        'Ghế sofa da bò cao cấp, dáng gọn và sang trọng cho phòng khách hiện đại.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Da bò thật","Khung gỗ sồi","Dễ vệ sinh","Đệm ngồi êm"]',
        24,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000002',
        'd0000000-0000-0000-0000-000000000001',
        'Sofa vải chữ L êm ái',
        'fabric-sectional-sofa',
        'Sofa vải chữ L rộng rãi, phù hợp phòng khách gia đình và không gian mở.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Vải nỉ cao cấp","Thiết kế chữ L","Đệm mút dày","Có thể tháo vỏ"]',
        17,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000003',
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà gỗ sồi tối giản',
        'minimalist-oak-coffee-table',
        'Bàn trà gỗ sồi phong cách tối giản, dễ phối với sofa và thảm phòng khách.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Gỗ sồi tự nhiên","Mặt bàn chống trầy","Kiểu dáng tối giản"]',
        31,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000004',
        'd0000000-0000-0000-0000-000000000003',
        'Giường king khung kim loại',
        'king-size-metal-bed',
        'Giường king size khung kim loại chắc chắn, rộng rãi và dễ vệ sinh gầm giường.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Sơn tĩnh điện","Khung thép chịu lực","Dễ lắp ráp","Không gây tiếng kêu"]',
        12,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000005',
        'd0000000-0000-0000-0000-000000000003',
        'Giường queen gỗ tự nhiên',
        'queen-size-wooden-bed',
        'Giường queen size bằng gỗ tự nhiên, tông ấm và phù hợp phòng ngủ thư giãn.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Gỗ thông tự nhiên","Phong cách ấm áp","Nan giường chắc chắn"]',
        20,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000006',
        'd0000000-0000-0000-0000-000000000004',
        'Tủ quần áo cửa trượt',
        'sliding-door-wardrobe',
        'Tủ quần áo cửa trượt rộng rãi, tích hợp gương và chia ngăn khoa học.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Cửa trượt tiết kiệm diện tích","Tích hợp gương lớn","Gỗ MDF phủ Melamine"]',
        9,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000007',
        'd0000000-0000-0000-0000-000000000005',
        'Bàn ăn mặt đá cẩm thạch',
        'marble-top-dining-table',
        'Bàn ăn mặt đá cẩm thạch sang trọng, phù hợp phòng bếp và khu vực ăn gia đình.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Mặt đá cẩm thạch","Chân bàn kim loại","Dễ lau chùi","Phong cách hiện đại"]',
        8,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000008',
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp gỗ sáng màu',
        'light-wood-kitchen-cabinet',
        'Tủ bếp gỗ sáng màu có nhiều ngăn lưu trữ, giúp khu bếp gọn gàng và sạch sẽ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Gỗ công nghiệp chống ẩm","Tay nắm âm","Dễ lau dầu mỡ","Nhiều khoang chứa"]',
        6,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000009',
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo treo tường',
        'wall-mounted-bathroom-vanity',
        'Tủ lavabo treo tường chống ẩm, giúp phòng tắm thoáng và dễ vệ sinh sàn.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Chống ẩm tốt","Thiết kế treo tường","Ngăn kéo giảm chấn","Mặt lavabo dễ lau"]',
        15,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000010',
        'd0000000-0000-0000-0000-000000000008',
        'Gương phòng tắm có đèn LED',
        'led-bathroom-mirror',
        'Gương phòng tắm tích hợp đèn LED, ánh sáng dịu và phù hợp khu vực lavabo.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Đèn LED tiết kiệm điện","Chống mờ nhẹ","Ánh sáng trung tính","Dễ lắp đặt"]',
        27,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000011',
        'd0000000-0000-0000-0000-000000000001',
        'Sofa văng Bắc Âu',
        'nordic-loveseat-sofa',
        'Sofa văng nhỏ gọn phong cách Bắc Âu, phù hợp căn hộ và phòng khách vừa.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Vải bố thoáng khí","Chân gỗ cao su","Đệm rời dễ vệ sinh"]',
        18,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000012',
        'd0000000-0000-0000-0000-000000000001',
        'Sofa module màu be',
        'beige-modular-sofa',
        'Sofa module linh hoạt, có thể ghép nhiều cấu hình cho không gian sinh hoạt chung.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Module linh hoạt","Màu be dễ phối","Đệm lưng lớn","Khung gỗ chắc chắn"]',
        14,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000013',
        'd0000000-0000-0000-0000-000000000001',
        'Sofa đơn thư giãn',
        'relaxing-armchair-sofa',
        'Ghế sofa đơn có tựa rộng, dùng làm ghế đọc sách hoặc ghế thư giãn.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Tựa lưng êm","Chân kim loại sơn tĩnh điện","Phù hợp góc đọc sách"]',
        22,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000014',
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà mặt kính khói',
        'smoked-glass-coffee-table',
        'Bàn trà mặt kính khói hiện đại, tạo điểm nhấn nhẹ cho phòng khách.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Mặt kính cường lực","Khung thép mảnh","Dễ lau chùi"]',
        11,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000015',
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà tròn đá trắng',
        'round-white-stone-coffee-table',
        'Bàn trà tròn mặt đá trắng, kiểu dáng mềm mại và sang trọng.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Mặt đá nhân tạo","Chân kim loại vàng","Bo cạnh an toàn"]',
        16,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000016',
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà ngăn kéo đôi',
        'double-drawer-coffee-table',
        'Bàn trà có hai ngăn kéo, giúp lưu trữ remote, sách và phụ kiện nhỏ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Hai ngăn kéo","Gỗ MDF phủ veneer","Ray kéo êm"]',
        9,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000017',
        'd0000000-0000-0000-0000-000000000002',
        'Bàn trà oval gỗ óc chó',
        'walnut-oval-coffee-table',
        'Bàn trà oval gỗ óc chó, đường nét mềm và màu gỗ ấm.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Gỗ óc chó veneer","Dáng oval","Chân bàn vát cạnh"]',
        13,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000018',
        'd0000000-0000-0000-0000-000000000003',
        'Giường bọc nệm đầu cao',
        'upholstered-high-headboard-bed',
        'Giường bọc nệm với đầu giường cao, tạo cảm giác êm và sang cho phòng ngủ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Đầu giường bọc nệm","Khung gỗ chịu lực","Vải nhung mềm"]',
        19,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000019',
        'd0000000-0000-0000-0000-000000000003',
        'Giường có hộc kéo',
        'storage-drawer-bed',
        'Giường ngủ tích hợp hộc kéo dưới gầm, tối ưu lưu trữ chăn ga.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Hộc kéo rộng","Nan giường chắc","Tối ưu diện tích"]',
        15,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000020',
        'd0000000-0000-0000-0000-000000000003',
        'Giường gỗ thấp kiểu Nhật',
        'japanese-low-platform-bed',
        'Giường platform thấp kiểu Nhật, tối giản và tạo cảm giác phòng rộng hơn.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Thiết kế thấp","Gỗ cao su ghép","Phong cách tối giản"]',
        21,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000021',
        'd0000000-0000-0000-0000-000000000004',
        'Tủ quần áo cánh kính',
        'glass-door-wardrobe',
        'Tủ quần áo cánh kính hiện đại, có đèn hắt nhẹ và khoang treo rộng.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Cánh kính khung nhôm","Đèn LED hắt","Khoang treo dài"]',
        8,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000022',
        'd0000000-0000-0000-0000-000000000004',
        'Tủ áo ba cánh gỗ sồi',
        'three-door-oak-wardrobe',
        'Tủ áo ba cánh gỗ sồi sáng màu, chia ngăn đơn giản và dễ dùng.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Ba cánh mở","Gỗ sồi veneer","Ngăn kéo dưới"]',
        10,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000023',
        'd0000000-0000-0000-0000-000000000004',
        'Tủ áo âm tường module',
        'modular-built-in-wardrobe',
        'Tủ áo module thiết kế âm tường, phù hợp phòng ngủ cần tối ưu diện tích.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Module lắp ghép","Tay nắm âm","Tối ưu không gian"]',
        7,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000024',
        'd0000000-0000-0000-0000-000000000004',
        'Tủ áo trẻ em pastel',
        'pastel-kids-wardrobe',
        'Tủ áo trẻ em màu pastel, chiều cao vừa tầm và bo góc an toàn.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Màu pastel","Bo góc an toàn","Thanh treo thấp"]',
        12,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000025',
        'd0000000-0000-0000-0000-000000000005',
        'Bàn ăn gỗ sồi sáu ghế',
        'six-seat-oak-dining-table',
        'Bàn ăn gỗ sồi cho sáu người, bề mặt rộng và vân gỗ tự nhiên.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Gỗ sồi tự nhiên","Sáu chỗ ngồi","Mặt bàn phủ dầu"]',
        17,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000026',
        'd0000000-0000-0000-0000-000000000005',
        'Bàn ăn tròn xoay',
        'round-rotating-dining-table',
        'Bàn ăn tròn có mâm xoay giữa, tiện cho bữa ăn gia đình.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Mâm xoay tiện dụng","Chân trụ chắc","Mặt bàn chống thấm"]',
        6,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000027',
        'd0000000-0000-0000-0000-000000000005',
        'Bàn đảo bếp mini',
        'mini-kitchen-island-table',
        'Bàn đảo bếp mini kết hợp mặt chuẩn bị đồ ăn và kệ lưu trữ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Kệ mở tiện dụng","Bánh xe khóa được","Mặt bàn chống nước"]',
        11,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000028',
        'd0000000-0000-0000-0000-000000000005',
        'Bàn ăn mở rộng thông minh',
        'extendable-smart-dining-table',
        'Bàn ăn có thể mở rộng khi cần, phù hợp nhà nhỏ và gia đình đông khách.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Mặt bàn mở rộng","Ray trượt chắc","Thiết kế gọn"]',
        13,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000029',
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp chữ L chống ẩm',
        'moisture-resistant-l-kitchen-cabinet',
        'Tủ bếp chữ L chống ẩm, chia khoang hợp lý cho căn bếp gia đình.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Chống ẩm tốt","Thiết kế chữ L","Bản lề giảm chấn"]',
        5,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000030',
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp trên kính mờ',
        'frosted-glass-upper-cabinet',
        'Tủ bếp trên cánh kính mờ, giúp khu bếp nhẹ và dễ tìm đồ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Cánh kính mờ","Khung nhôm nhẹ","Kệ chia tầng"]',
        8,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000031',
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp đảo cao cấp',
        'premium-island-kitchen-cabinet',
        'Hệ tủ bếp kèm đảo bếp cao cấp, phù hợp không gian bếp mở.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Đảo bếp rộng","Mặt đá chống thấm","Khoang máy rửa chén"]',
        4,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000032',
        'd0000000-0000-0000-0000-000000000006',
        'Tủ bếp mini căn hộ',
        'apartment-mini-kitchen-cabinet',
        'Tủ bếp mini cho căn hộ, đầy đủ khoang rửa, khoang nấu và lưu trữ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Kích thước gọn","Khoang lưu trữ thông minh","Dễ vệ sinh"]',
        10,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000033',
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo gỗ óc chó',
        'walnut-bathroom-vanity',
        'Tủ lavabo gỗ óc chó chống ẩm, tông trầm sang trọng cho phòng tắm.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Gỗ óc chó veneer","Chống ẩm","Ngăn kéo giảm chấn"]',
        9,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000034',
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo đôi',
        'double-sink-bathroom-vanity',
        'Tủ lavabo đôi cho phòng tắm lớn, hai khoang rửa tiện dụng.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Hai lavabo","Mặt đá nhân tạo","Ngăn kéo rộng"]',
        6,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000035',
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo chân đứng',
        'floor-standing-bathroom-vanity',
        'Tủ lavabo chân đứng chắc chắn, dễ lắp đặt và có nhiều ngăn chứa.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Chân đứng chắc","Nhiều ngăn chứa","Mặt sứ dễ lau"]',
        14,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000036',
        'd0000000-0000-0000-0000-000000000007',
        'Tủ lavabo tối giản',
        'minimalist-bathroom-vanity',
        'Tủ lavabo tối giản màu trắng, phù hợp phòng tắm nhỏ và sáng.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Màu trắng sạch","Thiết kế gọn","Tay nắm âm"]',
        18,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000037',
        'd0000000-0000-0000-0000-000000000008',
        'Gương tròn viền đồng',
        'round-brass-bathroom-mirror',
        'Gương tròn viền đồng tạo điểm nhấn ấm áp cho khu lavabo.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Viền đồng mảnh","Dáng tròn mềm","Móc treo chắc"]',
        20,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000038',
        'd0000000-0000-0000-0000-000000000008',
        'Gương soi toàn thân chống ẩm',
        'moisture-resistant-full-length-mirror',
        'Gương soi toàn thân chống ẩm, dùng được cho phòng tắm hoặc phòng thay đồ.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Chống ẩm","Kích thước lớn","Khung nhôm nhẹ"]',
        11,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000039',
        'd0000000-0000-0000-0000-000000000008',
        'Gương LED cảm ứng',
        'touch-led-bathroom-mirror',
        'Gương LED cảm ứng với ba chế độ sáng, tiện cho trang điểm và chăm sóc da.',
        'ACTIVE',
        NULL,
        NULL,
        TRUE,
        '["Cảm ứng chạm","Ba chế độ sáng","Chống mờ nhẹ"]',
        23,
        NOW(),
        NOW()
    ),
    (
        'e0000000-0000-0000-0000-000000000040',
        'd0000000-0000-0000-0000-000000000008',
        'Gương chữ nhật bo góc',
        'rounded-rectangle-bathroom-mirror',
        'Gương chữ nhật bo góc, kiểu dáng hiện đại và dễ phối nội thất phòng tắm.',
        'ACTIVE',
        NULL,
        NULL,
        FALSE,
        '["Bo góc an toàn","Khung mảnh","Dễ lắp đặt"]',
        15,
        NOW(),
        NOW()
    );

INSERT INTO
    product_images (
        id,
        product_id,
        image_url,
        position,
        created_at,
        updated_at
    )
VALUES (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000001',
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000001',
        'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=1200',
        2,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000002',
        'https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000002',
        'https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&q=80&w=1200',
        2,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000003',
        'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000004',
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000005',
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000006',
        'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000007',
        'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000008',
        'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000009',
        'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000010',
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000010',
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=1200',
        2,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000011',
        'https://images.unsplash.com/photo-1550254478-ead40cc54513?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000012',
        'https://images.unsplash.com/photo-1550581190-9c1c48d21d6c?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000013',
        'https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000014',
        'https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000015',
        'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000016',
        'https://images.unsplash.com/photo-1618220179428-22790b461013?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000017',
        'https://images.unsplash.com/photo-1615874694520-474822394e73?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000018',
        'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000019',
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000020',
        'https://images.unsplash.com/photo-1617325247661-675ab4b64ae2?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000021',
        'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000022',
        'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000023',
        'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000024',
        'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000025',
        'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000026',
        'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000027',
        'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000028',
        'https://images.unsplash.com/photo-1600585152220-90363fe7e115?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000029',
        'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000030',
        'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000031',
        'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000032',
        'https://images.unsplash.com/photo-1556909212-d5b604d0c90d?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000033',
        'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000034',
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000035',
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000036',
        'https://images.unsplash.com/photo-1595514535415-dae8970c255d?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000037',
        'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000038',
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000039',
        'https://images.unsplash.com/photo-1600566752734-9a2f331aabe1?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    ),
    (
        gen_random_uuid (),
        'e0000000-0000-0000-0000-000000000040',
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200',
        1,
        NOW(),
        NOW()
    );

INSERT INTO
    product_variants (
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
    )
VALUES (
        'a0000001-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000001',
        12000000.00,
        10,
        50.0,
        200.0,
        90.0,
        85.0,
        'Da bò tự nhiên',
        '2 năm',
        'Nâu đỏ',
        'SOFA-DA-NAU-200',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000001-0000-0000-0000-000000000002',
        'e0000000-0000-0000-0000-000000000001',
        15000000.00,
        5,
        60.0,
        220.0,
        95.0,
        90.0,
        'Da bò cao cấp',
        '2 năm',
        'Đen tuyền',
        'SOFA-DA-DEN-220',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000002-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000002',
        18000000.00,
        8,
        70.0,
        250.0,
        150.0,
        85.0,
        'Vải nỉ Hàn Quốc',
        '1 năm',
        'Xám',
        'SOFA-VAI-L-XAM',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000003-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000003',
        4500000.00,
        15,
        15.0,
        100.0,
        60.0,
        45.0,
        'Gỗ sồi trắng',
        '1 năm',
        'Màu gỗ tự nhiên',
        'BAN-TRA-SOI-100',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000004-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000004',
        9500000.00,
        12,
        40.0,
        210.0,
        190.0,
        35.0,
        'Thép sơn tĩnh điện',
        '3 năm',
        'Trắng tĩnh điện',
        'GIUONG-KING-KL',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000005-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000005',
        8500000.00,
        14,
        35.0,
        200.0,
        160.0,
        40.0,
        'Gỗ thông New Zealand',
        '2 năm',
        'Nâu đậm',
        'GIUONG-QUEEN-GO',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000006-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000006',
        13500000.00,
        7,
        80.0,
        220.0,
        180.0,
        60.0,
        'MDF phủ Melamine',
        '1 năm',
        'Trắng ngà',
        'TU-AO-TRUOT-220',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000007-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000007',
        24000000.00,
        6,
        60.0,
        180.0,
        90.0,
        75.0,
        'Đá cẩm thạch thật',
        '5 năm',
        'Trắng vân mây',
        'BAN-AN-DA-180',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000008-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000008',
        18500000.00,
        9,
        95.0,
        240.0,
        60.0,
        220.0,
        'Gỗ công nghiệp chống ẩm',
        '2 năm',
        'Sồi sáng',
        'TU-BEP-SOI-240',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000009-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000009',
        7200000.00,
        18,
        30.0,
        90.0,
        48.0,
        55.0,
        'MDF chống ẩm / mặt lavabo sứ',
        '3 năm',
        'Trắng',
        'LAVABO-TREO-90',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000010-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000010',
        3600000.00,
        25,
        8.0,
        80.0,
        4.0,
        70.0,
        'Gương bạc / khung nhôm',
        '1 năm',
        'Đen',
        'GUONG-LED-80-DEN',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000010-0000-0000-0000-000000000002',
        'e0000000-0000-0000-0000-000000000010',
        4200000.00,
        12,
        8.5,
        90.0,
        4.0,
        75.0,
        'Gương bạc / khung nhôm',
        '1 năm',
        'Xám than',
        'GUONG-LED-90-XAM',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000011-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000011',
        9800000.00,
        16,
        42.0,
        160.0,
        82.0,
        78.0,
        'Vải bố / gỗ cao su',
        '1 năm',
        'Xanh rêu',
        'SOFA-VANG-BACAU',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000012-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000012',
        21500000.00,
        7,
        72.0,
        280.0,
        165.0,
        82.0,
        'Vải nỉ cao cấp',
        '2 năm',
        'Be',
        'SOFA-MODULE-BE',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000013-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000013',
        5200000.00,
        20,
        22.0,
        92.0,
        88.0,
        95.0,
        'Vải nhung / thép sơn',
        '1 năm',
        'Cam đất',
        'SOFA-DON-THUGIAN',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000014-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000014',
        3200000.00,
        18,
        18.0,
        110.0,
        55.0,
        42.0,
        'Kính cường lực / thép',
        '1 năm',
        'Kính khói',
        'BAN-TRA-KINH-KHOI',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000015-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000015',
        4100000.00,
        14,
        25.0,
        85.0,
        85.0,
        38.0,
        'Đá nhân tạo / kim loại',
        '2 năm',
        'Trắng vân mây',
        'BAN-TRA-TRON-DA',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000016-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000016',
        5600000.00,
        12,
        32.0,
        120.0,
        60.0,
        42.0,
        'MDF phủ veneer',
        '1 năm',
        'Óc chó',
        'BAN-TRA-NGAN-KEO',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000017-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000017',
        6900000.00,
        10,
        28.0,
        125.0,
        65.0,
        40.0,
        'Veneer óc chó',
        '2 năm',
        'Nâu óc chó',
        'BAN-TRA-OVAL-OCCHO',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000018-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000018',
        7900000.00,
        11,
        38.0,
        210.0,
        170.0,
        110.0,
        'Vải nhung / gỗ thông',
        '2 năm',
        'Xám khói',
        'GIUONG-NEM-DAUCAO',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000019-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000019',
        10200000.00,
        9,
        48.0,
        205.0,
        165.0,
        45.0,
        'MDF phủ Melamine',
        '2 năm',
        'Trắng sữa',
        'GIUONG-HOC-KEO',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000020-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000020',
        7400000.00,
        13,
        34.0,
        200.0,
        160.0,
        28.0,
        'Gỗ cao su ghép',
        '2 năm',
        'Gỗ tự nhiên',
        'GIUONG-KIEU-NHAT',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000021-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000021',
        11500000.00,
        6,
        82.0,
        240.0,
        180.0,
        62.0,
        'Kính cường lực / khung nhôm',
        '2 năm',
        'Đen khói',
        'TU-AO-CANH-KINH',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000022-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000022',
        8800000.00,
        10,
        70.0,
        180.0,
        58.0,
        210.0,
        'Veneer sồi',
        '1 năm',
        'Sồi sáng',
        'TU-AO-BA-CANH-SOI',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000023-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000023',
        16800000.00,
        5,
        110.0,
        260.0,
        60.0,
        240.0,
        'MDF lõi xanh',
        '3 năm',
        'Trắng ngà',
        'TU-AO-AM-TUONG',
        2,
        NOW(),
        NOW()
    ),
    (
        'a0000024-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000024',
        6200000.00,
        15,
        45.0,
        120.0,
        50.0,
        150.0,
        'MDF phủ Melamine',
        '1 năm',
        'Hồng pastel',
        'TU-AO-TRE-PASTEL',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000025-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000025',
        12800000.00,
        8,
        55.0,
        180.0,
        90.0,
        75.0,
        'Gỗ sồi tự nhiên',
        '3 năm',
        'Sồi tự nhiên',
        'BAN-AN-SOI-6GHE',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000026-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000026',
        9600000.00,
        7,
        50.0,
        135.0,
        135.0,
        75.0,
        'Đá nhân tạo / gỗ',
        '2 năm',
        'Nâu sáng',
        'BAN-AN-TRON-XOAY',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000027-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000027',
        6800000.00,
        12,
        40.0,
        110.0,
        55.0,
        88.0,
        'Gỗ cao su / thép',
        '1 năm',
        'Trắng gỗ',
        'BAN-DAO-BEP-MINI',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000028-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000028',
        14200000.00,
        6,
        62.0,
        160.0,
        90.0,
        75.0,
        'MDF phủ veneer',
        '2 năm',
        'Nâu walnut',
        'BAN-AN-MO-RONG',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000029-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000029',
        16900000.00,
        5,
        105.0,
        280.0,
        60.0,
        220.0,
        'MDF lõi xanh chống ẩm',
        '3 năm',
        'Trắng phối gỗ',
        'TU-BEP-L-CHONG-AM',
        2,
        NOW(),
        NOW()
    ),
    (
        'a0000030-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000030',
        7200000.00,
        10,
        38.0,
        160.0,
        35.0,
        70.0,
        'Kính mờ / nhôm',
        '1 năm',
        'Trắng mờ',
        'TU-BEP-TREN-KINH',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000031-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000031',
        28500000.00,
        4,
        130.0,
        300.0,
        90.0,
        220.0,
        'MDF chống ẩm / mặt đá',
        '5 năm',
        'Xám đá',
        'TU-BEP-DAO-CAOCAP',
        2,
        NOW(),
        NOW()
    ),
    (
        'a0000032-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000032',
        9800000.00,
        9,
        70.0,
        180.0,
        55.0,
        210.0,
        'MDF phủ Melamine',
        '2 năm',
        'Sồi sáng',
        'TU-BEP-MINI-CANHO',
        3,
        NOW(),
        NOW()
    ),
    (
        'a0000033-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000033',
        6800000.00,
        10,
        32.0,
        90.0,
        48.0,
        55.0,
        'Veneer óc chó / lavabo sứ',
        '3 năm',
        'Óc chó',
        'LAVABO-OCCHO-90',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000034-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000034',
        12800000.00,
        5,
        55.0,
        150.0,
        50.0,
        58.0,
        'MDF chống ẩm / mặt đá',
        '3 năm',
        'Trắng đá',
        'LAVABO-DOI-150',
        2,
        NOW(),
        NOW()
    ),
    (
        'a0000035-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000035',
        5900000.00,
        12,
        34.0,
        80.0,
        46.0,
        82.0,
        'MDF chống ẩm / sứ',
        '2 năm',
        'Xám nhạt',
        'LAVABO-CHAN-DUNG',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000036-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000036',
        4300000.00,
        18,
        28.0,
        75.0,
        45.0,
        52.0,
        'MDF chống ẩm',
        '2 năm',
        'Trắng',
        'LAVABO-TOI-GIAN',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000037-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000037',
        2400000.00,
        22,
        6.0,
        70.0,
        3.0,
        70.0,
        'Gương bạc / viền đồng',
        '1 năm',
        'Đồng',
        'GUONG-TRON-DONG',
        5,
        NOW(),
        NOW()
    ),
    (
        'a0000038-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000038',
        3100000.00,
        15,
        12.0,
        60.0,
        3.0,
        170.0,
        'Gương bạc / khung nhôm',
        '1 năm',
        'Đen',
        'GUONG-TOANTHAN-CHONGAM',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000039-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000039',
        4800000.00,
        16,
        9.0,
        90.0,
        4.0,
        70.0,
        'Gương bạc / LED',
        '2 năm',
        'Trắng',
        'GUONG-LED-CAMUNG',
        4,
        NOW(),
        NOW()
    ),
    (
        'a0000040-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000040',
        2800000.00,
        20,
        7.0,
        80.0,
        3.0,
        60.0,
        'Gương bạc / khung nhôm',
        '1 năm',
        'Bạc',
        'GUONG-BO-GOC',
        5,
        NOW(),
        NOW()
    );

INSERT INTO
    product_favorite_logs (
        id,
        user_id,
        product_id,
        created_at,
        updated_at
    )
VALUES (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'e0000000-0000-0000-0000-000000000001',
        NOW() - INTERVAL '4 days',
        NOW() - INTERVAL '4 days'
    ),
    (
        gen_random_uuid (),
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        gen_random_uuid (),
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        gen_random_uuid (),
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'e0000000-0000-0000-0000-000000000009',
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        gen_random_uuid (),
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'e0000000-0000-0000-0000-000000000010',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    );

INSERT INTO
    reviews (
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
    )
VALUES (
        'b0000001-0000-0000-0000-000000000001',
        'Sofa da rất xịn!',
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'Minh Hiền',
        'e0000000-0000-0000-0000-000000000001',
        'c0000001-0000-0000-0000-000000000001',
        'Mình hài lòng với chiếc sofa này. Chất da mềm, màu sắc đẹp, giao hàng cẩn thận.',
        md5(
            'Mình hài lòng với chiếc sofa này.'
        ),
        5,
        'VISIBLE',
        0.90,
        NOW() - INTERVAL '5 days',
        NOW() - INTERVAL '5 days'
    ),
    (
        'b0000001-0000-0000-0000-000000000002',
        'Chất lượng ổn, giá hơi cao',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'User 01',
        'e0000000-0000-0000-0000-000000000001',
        'c0000001-0000-0000-0000-000000000002',
        'Sofa đẹp, ngồi thoải mái. Giá hơi cao nhưng hoàn thiện tốt.',
        md5('Sofa đẹp, ngồi thoải mái.'),
        4,
        'VISIBLE',
        0.75,
        NOW() - INTERVAL '3 days',
        NOW() - INTERVAL '3 days'
    ),
    (
        'b0000002-0000-0000-0000-000000000001',
        'Sofa vải siêu thoải mái',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'User 02',
        'e0000000-0000-0000-0000-000000000002',
        'c0000002-0000-0000-0000-000000000001',
        'Mua cho phòng khách nhà, gia đình rất thích. Vải mềm, đệm dày, dễ vệ sinh.',
        md5(
            'Mua cho phòng khách nhà, gia đình rất thích.'
        ),
        5,
        'VISIBLE',
        0.85,
        NOW() - INTERVAL '7 days',
        NOW() - INTERVAL '7 days'
    ),
    (
        'b0000003-0000-0000-0000-000000000001',
        'Bàn trà đẹp, lắp dễ',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'User 03',
        'e0000000-0000-0000-0000-000000000003',
        'c0000003-0000-0000-0000-000000000001',
        'Bàn trà chắc chắn, hướng dẫn lắp ráp rõ ràng, bề mặt đẹp.',
        md5(
            'Bàn trà chắc chắn, hướng dẫn lắp ráp rõ ràng.'
        ),
        5,
        'VISIBLE',
        0.95,
        NOW() - INTERVAL '10 days',
        NOW() - INTERVAL '10 days'
    ),
    (
        'b0000004-0000-0000-0000-000000000001',
        'Giường king chắc, không tiếng kêu',
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'Minh Hiền',
        'e0000000-0000-0000-0000-000000000004',
        'c0000004-0000-0000-0000-000000000001',
        'Dùng được 2 tháng, khung thép chắc chắn, sơn không bị tróc.',
        md5(
            'Dùng được 2 tháng, khung thép chắc chắn.'
        ),
        5,
        'VISIBLE',
        0.88,
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '2 days'
    ),
    (
        'b0000009-0000-0000-0000-000000000001',
        'Tủ lavabo gọn và đẹp',
        '4b33e5c1-cae1-458d-b4b1-e568ddd766f6',
        'User 01',
        'e0000000-0000-0000-0000-000000000009',
        'c0000009-0000-0000-0000-000000000001',
        'Tủ treo tường giúp phòng tắm thoáng hơn, ngăn kéo đóng êm và mặt lavabo dễ lau.',
        md5(
            'Tủ treo tường giúp phòng tắm thoáng hơn.'
        ),
        5,
        'VISIBLE',
        0.92,
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day'
    ),
    (
        'b0000009-0000-0000-0000-000000000002',
        'Tốt nhưng giao hàng chậm',
        '7c22e6d3-1111-4aab-b999-aabbcc001122',
        'User 02',
        'e0000000-0000-0000-0000-000000000009',
        'c0000009-0000-0000-0000-000000000002',
        'Sản phẩm chất lượng tốt, đúng mô tả. Khâu giao hàng cần cải thiện.',
        md5(
            'Sản phẩm chất lượng tốt, đúng mô tả.'
        ),
        3,
        'VISIBLE',
        0.65,
        NOW() - INTERVAL '4 days',
        NOW() - INTERVAL '4 days'
    ),
    (
        'b0000010-0000-0000-0000-000000000001',
        'Gương LED sáng dịu, rất tiện',
        '8d33f7e4-2222-4bbc-caaa-bbccdd002233',
        'User 03',
        'e0000000-0000-0000-0000-000000000010',
        'c0000010-0000-0000-0000-000000000001',
        'Đèn LED sáng vừa đủ, soi rõ mặt nhưng không chói. Lắp ở khu lavabo rất hợp.',
        md5(
            'Đèn LED sáng vừa đủ, soi rõ mặt nhưng không chói.'
        ),
        5,
        'VISIBLE',
        0.87,
        NOW() - INTERVAL '6 days',
        NOW() - INTERVAL '6 days'
    );

COMMIT;

-- ============================================================
-- furnisight_promotion_db
-- Schema tham chieu:
--   - promotion-service/.../V1__create_promotion_tables.sql
-- ============================================================
\connect furnisight_promotion_db;

BEGIN;

DELETE FROM user_vouchers
WHERE
    promotion_id IN (
        '81000000-0000-0000-0000-000000000001',
        '81000000-0000-0000-0000-000000000002',
        '81000000-0000-0000-0000-000000000003',
        '81000000-0000-0000-0000-000000000004',
        '81000000-0000-0000-0000-000000000005',
        '81000000-0000-0000-0000-000000000006'
    );

DELETE FROM promotions
WHERE
    id IN (
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

INSERT INTO
    promotions (
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
    )
VALUES (
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

INSERT INTO
    user_vouchers (
        id,
        user_id,
        promotion_id,
        is_used,
        used_at,
        saved_at
    )
VALUES (
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
WHERE
    source_id IN (
        '83000000-0000-0000-0000-000000000001',
        '85000000-0000-0000-0000-000000000001'
    );

DELETE FROM marketing_campaigns
WHERE
    id IN (
        '83000000-0000-0000-0000-000000000001',
        '83000000-0000-0000-0000-000000000002'
    );

DELETE FROM marketing_notifications
WHERE
    id IN (
        '85000000-0000-0000-0000-000000000001',
        '85000000-0000-0000-0000-000000000002'
    );

DELETE FROM promotion_combo_items
WHERE
    combo_id IN (
        '84000000-0000-0000-0000-000000000001',
        '84000000-0000-0000-0000-000000000002',
        '84000000-0000-0000-0000-000000000003',
        '84000000-0000-0000-0000-000000000004',
        '84000000-0000-0000-0000-000000000005',
        '84000000-0000-0000-0000-000000000006'
    );

DELETE FROM promotion_combos
WHERE
    id IN (
        '84000000-0000-0000-0000-000000000001',
        '84000000-0000-0000-0000-000000000002',
        '84000000-0000-0000-0000-000000000003',
        '84000000-0000-0000-0000-000000000004',
        '84000000-0000-0000-0000-000000000005',
        '84000000-0000-0000-0000-000000000006'
    );

INSERT INTO
    marketing_campaigns (
        id,
        name,
        voucher_id,
        target_type,
        target_user_ids,
        segment_key,
        channels,
        schedule_type,
        scheduled_at,
        notification_title,
        notification_body,
        status,
        sent_count,
        active,
        dispatched_at,
        created_at,
        updated_at
    )
VALUES (
        '83000000-0000-0000-0000-000000000001',
        'Phat WELCOME10 cho khach moi',
        '81000000-0000-0000-0000-000000000001',
        'SEGMENT',
        '',
        'NEW_USERS',
        'NOTIFICATION,EMAIL',
        'SCHEDULED',
        NOW() + INTERVAL '1 day',
        'Ban vua nhan voucher WELCOME10',
        'Dung voucher WELCOME10 de giam 10% cho don hang dau tien.',
        'SCHEDULED',
        0,
        TRUE,
        NULL,
        NOW(),
        NOW()
    ),
    (
        '83000000-0000-0000-0000-000000000002',
        'Nhac gio hang bo quen',
        '81000000-0000-0000-0000-000000000003',
        'SEGMENT',
        '',
        'ABANDONED_CART',
        'NOTIFICATION',
        'DRAFT',
        NULL,
        'Uu dai cho gio hang cua ban',
        'Hoan tat don hang hom nay de nhan uu dai noi that.',
        'DRAFT',
        0,
        TRUE,
        NULL,
        NOW(),
        NOW()
    );

INSERT INTO
    promotion_combos (
        id,
        name,
        description,
        image_url,
        discount_type,
        discount_value,
        start_date,
        end_date,
        active,
        original_amount,
        final_amount,
        saved_amount,
        used_count,
        created_at,
        updated_at
    )
VALUES (
        '84000000-0000-0000-0000-000000000001',
        'Combo phong ngu LuxNest',
        'Giuong king, giuong queen va tu quan ao cho phong ngu.',
        'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=1400&q=85',
        'PERCENTAGE',
        15,
        NOW() - INTERVAL '7 days',
        NOW() + INTERVAL '60 days',
        TRUE,
        31500000,
        26775000,
        4725000,
        24,
        NOW(),
        NOW()
    ),
    (
        '84000000-0000-0000-0000-000000000002',
        'Combo phong khach tinh gon',
        'Sofa da va ban tra go soi cho phong khach hien dai.',
        'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1400&q=85',
        'FIXED_AMOUNT',
        1300000,
        NOW() - INTERVAL '7 days',
        NOW() + INTERVAL '45 days',
        TRUE,
        16500000,
        15200000,
        1300000,
        11,
        NOW(),
        NOW()
    ),
    (
        '84000000-0000-0000-0000-000000000003',
        'Combo phong khach Bac Au',
        'Sofa vang, ban tra kinh khoi va ban tra da trang cho phong khach sang nhe.',
        'https://images.unsplash.com/photo-1550581190-9c1c48d21d6c?auto=format&fit=crop&w=1400&q=85',
        'FIXED_AMOUNT',
        1800000,
        NOW() - INTERVAL '3 days',
        NOW() + INTERVAL '75 days',
        TRUE,
        17100000,
        15300000,
        1800000,
        8,
        NOW(),
        NOW()
    ),
    (
        '84000000-0000-0000-0000-000000000004',
        'Combo bep am cung',
        'Bo ban an, ban tron xoay va tu bep chu L cho khong gian bep tien nghi.',
        'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1400&q=85',
        'PERCENTAGE',
        12,
        NOW() - INTERVAL '3 days',
        NOW() + INTERVAL '75 days',
        TRUE,
        39300000,
        34584000,
        4716000,
        5,
        NOW(),
        NOW()
    ),
    (
        '84000000-0000-0000-0000-000000000005',
        'Combo phong tam spa',
        'Tu lavabo go oc cho ket hop guong tron va guong soi toan than.',
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=1400&q=85',
        'FIXED_AMOUNT',
        900000,
        NOW() - INTERVAL '3 days',
        NOW() + INTERVAL '60 days',
        TRUE,
        12300000,
        11400000,
        900000,
        7,
        NOW(),
        NOW()
    ),
    (
        '84000000-0000-0000-0000-000000000006',
        'Combo phong ngu toi gian',
        'Giuong boc nem dau cao va hai mau tu ao hien dai cho phong ngu moi.',
        'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=1400&q=85',
        'PERCENTAGE',
        10,
        NOW() - INTERVAL '3 days',
        NOW() + INTERVAL '90 days',
        TRUE,
        28200000,
        25380000,
        2820000,
        4,
        NOW(),
        NOW()
    );

INSERT INTO
    promotion_combo_items (
        id,
        combo_id,
        product_id,
        variant_id,
        product_name,
        sku,
        category_name,
        image,
        price,
        quantity,
        snapshot_missing,
        created_at,
        updated_at
    )
VALUES (
        '84100000-0000-0000-0000-000000000001',
        '84000000-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000004',
        'a0000004-0000-0000-0000-000000000001',
        'Giường king khung kim loại',
        'GIUONG-KING-KL',
        'Giường ngủ',
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200',
        9500000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000002',
        '84000000-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000005',
        'a0000005-0000-0000-0000-000000000001',
        'Giường queen gỗ tự nhiên',
        'GIUONG-QUEEN-GO',
        'Giường ngủ',
        'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&q=80&w=1200',
        8500000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000003',
        '84000000-0000-0000-0000-000000000001',
        'e0000000-0000-0000-0000-000000000006',
        'a0000006-0000-0000-0000-000000000001',
        'Tủ quần áo cửa trượt',
        'TU-AO-TRUOT-220',
        'Tủ quần áo',
        'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200',
        13500000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000004',
        '84000000-0000-0000-0000-000000000002',
        'e0000000-0000-0000-0000-000000000001',
        'a0000001-0000-0000-0000-000000000001',
        'Sofa da bò hiện đại',
        'SOFA-DA-NAU-200',
        'Ghế sofa',
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200',
        12000000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000005',
        '84000000-0000-0000-0000-000000000002',
        'e0000000-0000-0000-0000-000000000003',
        'a0000003-0000-0000-0000-000000000001',
        'Bàn trà gỗ sồi tối giản',
        'BAN-TRA-SOI-100',
        'Bàn trà',
        'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200',
        4500000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000006',
        '84000000-0000-0000-0000-000000000003',
        'e0000000-0000-0000-0000-000000000011',
        'a0000011-0000-0000-0000-000000000001',
        'Sofa văng Bắc Âu',
        'SOFA-VANG-BACAU',
        'Ghế sofa',
        'https://images.unsplash.com/photo-1550254478-ead40cc54513?auto=format&fit=crop&q=80&w=1200',
        9800000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000007',
        '84000000-0000-0000-0000-000000000003',
        'e0000000-0000-0000-0000-000000000014',
        'a0000014-0000-0000-0000-000000000001',
        'Bàn trà mặt kính khói',
        'BAN-TRA-KINH-KHOI',
        'Bàn trà',
        'https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&q=80&w=1200',
        3200000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000008',
        '84000000-0000-0000-0000-000000000003',
        'e0000000-0000-0000-0000-000000000015',
        'a0000015-0000-0000-0000-000000000001',
        'Bàn trà tròn đá trắng',
        'BAN-TRA-TRON-DA',
        'Bàn trà',
        'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&q=80&w=1200',
        4100000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000009',
        '84000000-0000-0000-0000-000000000004',
        'e0000000-0000-0000-0000-000000000025',
        'a0000025-0000-0000-0000-000000000001',
        'Bàn ăn gỗ sồi sáu ghế',
        'BAN-AN-SOI-6GHE',
        'Bàn ăn',
        'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&q=80&w=1200',
        12800000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000010',
        '84000000-0000-0000-0000-000000000004',
        'e0000000-0000-0000-0000-000000000026',
        'a0000026-0000-0000-0000-000000000001',
        'Bàn ăn tròn xoay',
        'BAN-AN-TRON-XOAY',
        'Bàn ăn',
        'https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&q=80&w=1200',
        9600000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000011',
        '84000000-0000-0000-0000-000000000004',
        'e0000000-0000-0000-0000-000000000029',
        'a0000029-0000-0000-0000-000000000001',
        'Tủ bếp chữ L chống ẩm',
        'TU-BEP-L-CHONG-AM',
        'Tủ bếp',
        'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?auto=format&fit=crop&q=80&w=1200',
        16900000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000012',
        '84000000-0000-0000-0000-000000000005',
        'e0000000-0000-0000-0000-000000000033',
        'a0000033-0000-0000-0000-000000000001',
        'Tủ lavabo gỗ óc chó',
        'LAVABO-OCCHO-90',
        'Tủ lavabo',
        'https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=1200',
        6800000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000013',
        '84000000-0000-0000-0000-000000000005',
        'e0000000-0000-0000-0000-000000000037',
        'a0000037-0000-0000-0000-000000000001',
        'Gương tròn viền đồng',
        'GUONG-TRON-DONG',
        'Gương phòng tắm',
        'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&q=80&w=1200',
        2400000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000014',
        '84000000-0000-0000-0000-000000000005',
        'e0000000-0000-0000-0000-000000000038',
        'a0000038-0000-0000-0000-000000000001',
        'Gương soi toàn thân chống ẩm',
        'GUONG-TOANTHAN-CHONGAM',
        'Gương phòng tắm',
        'https://images.unsplash.com/photo-1600566752355-35792bedcfea?auto=format&fit=crop&q=80&w=1200',
        3100000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000015',
        '84000000-0000-0000-0000-000000000006',
        'e0000000-0000-0000-0000-000000000018',
        'a0000018-0000-0000-0000-000000000001',
        'Giường bọc nệm đầu cao',
        'GIUONG-NEM-DAUCAO',
        'Giường ngủ',
        'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&q=80&w=1200',
        7900000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000016',
        '84000000-0000-0000-0000-000000000006',
        'e0000000-0000-0000-0000-000000000021',
        'a0000021-0000-0000-0000-000000000001',
        'Tủ quần áo cánh kính',
        'TU-AO-CANH-KINH',
        'Tủ quần áo',
        'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&q=80&w=1200',
        11500000,
        1,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        '84100000-0000-0000-0000-000000000017',
        '84000000-0000-0000-0000-000000000006',
        'e0000000-0000-0000-0000-000000000022',
        'a0000022-0000-0000-0000-000000000001',
        'Tủ áo ba cánh gỗ sồi',
        'TU-AO-BA-CANH-SOI',
        'Tủ quần áo',
        'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=1200',
        8800000,
        1,
        FALSE,
        NOW(),
        NOW()
    );

INSERT INTO
    marketing_notifications (
        id,
        title,
        body,
        target_type,
        target_user_ids,
        segment_key,
        channels,
        send_type,
        scheduled_at,
        related_voucher_id,
        status,
        sent_count,
        active,
        dispatched_at,
        created_at,
        updated_at
    )
VALUES (
        '85000000-0000-0000-0000-000000000001',
        'Ban vua nhan voucher WELCOME10',
        'Kiem tra vi voucher va dung uu dai trong checkout.',
        'ALL',
        '',
        NULL,
        'NOTIFICATION',
        'DRAFT',
        NULL,
        '81000000-0000-0000-0000-000000000001',
        'DRAFT',
        0,
        TRUE,
        NULL,
        NOW(),
        NOW()
    ),
    (
        '85000000-0000-0000-0000-000000000002',
        'Uu dai noi that cuoi tuan',
        'Khach VIP nhan uu dai dac biet cho bo suu tap moi.',
        'SEGMENT',
        '',
        'VIP',
        'NOTIFICATION,EMAIL',
        'SCHEDULED',
        NOW() + INTERVAL '3 days',
        NULL,
        'SCHEDULED',
        0,
        TRUE,
        NULL,
        NOW(),
        NOW()
    );

COMMIT;

-- ============================================================
-- furnisight_order_db
-- Schema tham chieu:
--   - order-service/.../V1__init_order_schema.sql den V8
-- ============================================================
\connect furnisight_order_db;

BEGIN;

DELETE FROM stock_reservations
WHERE
    order_code IN (
        'ORD-A1B2C3D4',
        'ORD-X9Y8Z7W6'
    );

DELETE FROM order_items
WHERE
    order_id IN (
        'c5379d96-1111-4fd9-8383-bae82736bb11',
        'd5379d96-2222-4fd9-8383-bae82736bb22'
    );

DELETE FROM orders
WHERE
    id IN (
        'c5379d96-1111-4fd9-8383-bae82736bb11',
        'd5379d96-2222-4fd9-8383-bae82736bb22'
    )
    OR order_code IN (
        'ORD-A1B2C3D4',
        'ORD-X9Y8Z7W6'
    );

-- Mock Orders cho minhhien7840@gmail.com
INSERT INTO
    orders (
        id,
        order_code,
        user_id,
        status,
        sub_total,
        total_amount,
        shipping_fee,
        shipping_discount,
        discount_amount,
        insurance_fee,
        saved_amount,
        shipping_address_name,
        shipping_address_phone,
        shipping_address_detail,
        shipping_method,
        customer_note,
        payment_method,
        payment_status,
        paid_amount,
        paid_at,
        order_created_at,
        payment_initiated_at,
        payment_completed_at,
        payment_failed_at,
        shop_voucher_code,
        shipping_voucher_code,
        created_at,
        updated_at,
        created_by,
        updated_by
    )
VALUES (
        'c5379d96-1111-4fd9-8383-bae82736bb11',
        'ORD-A1B2C3D4',
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'UNPAID',
        4500000,
        4600000,
        100000,
        0,
        0,
        0,
        0,
        'Minh Hiền',
        '0901234567',
        'Khu phố 6, Phường Linh Trung, Thành phố Thủ Đức, Thành phố Hồ Chí Minh',
        'STANDARD',
        'Giao hàng trong giờ hành chính',
        'VNPAY',
        'PENDING',
        0,
        NULL,
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours',
        NULL,
        NULL,
        NULL,
        NULL,
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours',
        'seed-data',
        'seed-data'
    ),
    (
        'd5379d96-2222-4fd9-8383-bae82736bb22',
        'ORD-X9Y8Z7W6',
        '52379d96-5238-4fd9-8383-bae82736bb3b',
        'DELIVERED',
        12000000,
        12100000,
        100000,
        0,
        0,
        0,
        0,
        'Minh Hiền',
        '0901234567',
        'Khu phố 6, Phường Linh Trung, Thành phố Thủ Đức, Thành phố Hồ Chí Minh',
        'STANDARD',
        NULL,
        'VNPAY',
        'PAID',
        12100000,
        NOW() - INTERVAL '7 days',
        NOW() - INTERVAL '8 days',
        NOW() - INTERVAL '8 days',
        NOW() - INTERVAL '7 days',
        NULL,
        NULL,
        NULL,
        NOW() - INTERVAL '8 days',
        NOW() - INTERVAL '2 days',
        'seed-data',
        'seed-data'
    );

INSERT INTO
    order_items (
        id,
        order_id,
        product_id,
        variant_id,
        category_name,
        product_name,
        color,
        material,
        warranty,
        weight,
        length,
        width,
        height,
        price,
        quantity,
        image_url,
        created_at,
        updated_at,
        created_by,
        updated_by
    )
VALUES (
        'e5379d96-3333-4fd9-8383-bae82736bb33',
        'c5379d96-1111-4fd9-8383-bae82736bb11',
        'e0000000-0000-0000-0000-000000000003',
        'a0000003-0000-0000-0000-000000000001',
        'Bàn trà',
        'Bàn trà gỗ sồi tối giản',
        'Màu gỗ tự nhiên',
        'Gỗ sồi trắng',
        '1 năm',
        15,
        100,
        60,
        45,
        4500000,
        1,
        'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=1200',
        NOW() - INTERVAL '2 hours',
        NOW() - INTERVAL '2 hours',
        'seed-data',
        'seed-data'
    ),
    (
        'f5379d96-4444-4fd9-8383-bae82736bb44',
        'd5379d96-2222-4fd9-8383-bae82736bb22',
        'e0000000-0000-0000-0000-000000000001',
        'a0000001-0000-0000-0000-000000000001',
        'Ghế sofa',
        'Sofa da bò hiện đại',
        'Nâu đỏ',
        'Da bò tự nhiên',
        '2 năm',
        50,
        200,
        90,
        85,
        12000000,
        1,
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=1200',
        NOW() - INTERVAL '8 days',
        NOW() - INTERVAL '8 days',
        'seed-data',
        'seed-data'
    );

INSERT INTO
    stock_reservations (
        id,
        order_code,
        product_id,
        product_variant_id,
        quantity,
        created_at
    )
VALUES (
        '73000000-0000-0000-0000-000000000001',
        'ORD-A1B2C3D4',
        'e0000000-0000-0000-0000-000000000003',
        'a0000003-0000-0000-0000-000000000001',
        1,
        NOW() - INTERVAL '2 hours'
    );

COMMIT;