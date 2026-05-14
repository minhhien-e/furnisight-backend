-- Seed data for Categories
-- Added image_url, icon_url, and product_count
INSERT INTO categories (id, name, slug, parent_id, path, product_count, image_url, icon_url, created_at, updated_at) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Living Room', 'living-room', NULL, 'living-room', 2, 'https://images.unsplash.com/photo-1583847268964-b28dc8f51f92?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Sofa', 'sofa', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'living-room/sofa', 1, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800', '🛋️', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Coffee Table', 'coffee-table', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'living-room/coffee-table', 1, 'https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800', '☕', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Bedroom', 'bedroom', NULL, 'bedroom', 1, 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Bed', 'bed', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'bedroom/bed', 1, 'https://images.unsplash.com/photo-1505693413171-293669746a57?auto=format&fit=crop&q=80&w=800', '🛏️', NOW(), NOW());

-- Seed data for Products
INSERT INTO products (id, shop_id, category_id, name, description, product_status, attributes, weight, length, width, height, view_count, created_at, updated_at) VALUES 
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Modern Leather Sofa', 'A premium leather sofa for your living room.', 'ACTIVE', '{"material": "Leather", "color": "Brown", "image": "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800"}', 50000.0, 200.0, 90.0, 85.0, 150, NOW(), NOW()),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Minimalist Coffee Table', 'Wooden coffee table with a minimalist design.', 'ACTIVE', '{"material": "Oak Wood", "color": "Natural", "image": "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&q=80&w=800"}', 15000.0, 100.0, 60.0, 45.0, 85, NOW(), NOW()),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'King Size Bed Frame', 'Spacious and comfortable king size bed frame.', 'ACTIVE', '{"material": "Metal", "size": "King", "image": "https://images.unsplash.com/photo-1505691938895-1758d7eaa511?auto=format&fit=crop&q=80&w=800"}', 40000.0, 210.0, 190.0, 35.0, 120, NOW(), NOW());

-- Seed data for Product Variants
INSERT INTO product_variants (id, product_id, sku, price, stock_quantity) VALUES 
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'SOFA-LTR-BRW', 12000000.00, 10),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', 'TAB-MIN-OAK', 3500000.00, 25),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'BED-KNG-MET', 8500000.00, 5);
