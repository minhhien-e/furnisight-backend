UPDATE categories
SET image_url = 'https://res.cloudinary.com/demo/image/upload/f_auto,q_auto,w_800,h_600,c_fill/sample.jpg',
    updated_at = NOW()
WHERE image_url LIKE 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511%';

UPDATE categories
SET image_url = 'https://res.cloudinary.com/demo/image/upload/f_auto,q_auto,w_800,h_600,c_fill/cld-sample-2.jpg',
    updated_at = NOW()
WHERE image_url LIKE 'https://images.unsplash.com/photo-1505693413171-293669746a57%';

UPDATE product_images
SET image_url = 'https://res.cloudinary.com/demo/image/upload/f_auto,q_auto,w_1200,h_900,c_fill/sample.jpg',
    updated_at = NOW()
WHERE image_url LIKE 'https://images.unsplash.com/photo-1505691938895-1758d7eaa511%';

UPDATE product_images
SET image_url = 'https://res.cloudinary.com/demo/image/upload/f_auto,q_auto,w_1200,h_900,c_fill/cld-sample-2.jpg',
    updated_at = NOW()
WHERE image_url LIKE 'https://images.unsplash.com/photo-1505693413171-293669746a57%';
