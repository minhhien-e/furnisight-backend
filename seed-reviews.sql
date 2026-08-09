BEGIN;

-- Homepage testimonials seed.
-- Run against furnisight_review_db after catalog data is seeded.

INSERT INTO reviews (
  id,
  user_id,
  user_name,
  user_avatar_media_id,
  product_id,
  order_item_id,
  title,
  content_text,
  content_hash,
  rating,
  status,
  trust_score,
  sentiment,
  sentiment_confidence,
  sentiment_status,
  sentiment_analyzed_at,
  created_at,
  updated_at
)
VALUES
  (
    '91000000-0000-0000-0000-000000000001',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Minh Anh',
    NULL,
    'fd342f1c-932d-1693-4cd9-13d2ac1ca354',
    '92000000-0000-0000-0000-000000000001',
    'Sản phẩm đẹp hơn mong đợi',
    'Ghế lên hình rất sang, chất liệu chắc chắn và màu ngoài đời đúng như ảnh. Mình thích nhất là có thể xem trước mẫu 3D nên dễ hình dung khi đặt vào phòng ăn.',
    'homepage-review-001',
    5,
    'VISIBLE',
    0.95,
    'POSITIVE',
    0.9820,
    'COMPLETED',
    NOW(),
    NOW() - INTERVAL '5 days',
    NOW()
  ),
  (
    '91000000-0000-0000-0000-000000000002',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Quốc Huy',
    NULL,
    'b4205fc2-9983-37f7-35c0-e6d2cc9b9066',
    '92000000-0000-0000-0000-000000000002',
    'Trải nghiệm mua hàng rất ổn',
    'Trang chi tiết rõ ràng, ảnh sản phẩm đẹp và thông số đầy đủ. Mình chọn bàn ăn nhanh hơn vì có phân loại theo phòng và giá hiển thị dễ so sánh.',
    'homepage-review-002',
    5,
    'VISIBLE',
    0.93,
    'POSITIVE',
    0.9710,
    'COMPLETED',
    NOW(),
    NOW() - INTERVAL '3 days',
    NOW()
  ),
  (
    '91000000-0000-0000-0000-000000000003',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Thanh Trúc',
    NULL,
    '3688826c-32f6-afbc-4d4d-056d39ed50ea',
    '92000000-0000-0000-0000-000000000003',
    'Dễ chọn đồ cho không gian nhỏ',
    'Mình mua giỏ đựng đồ cho phòng tắm, sản phẩm gọn và đúng phong cách đang tìm. Bộ lọc theo phòng giúp tìm món phù hợp nhanh hơn nhiều.',
    'homepage-review-003',
    4,
    'VISIBLE',
    0.91,
    'POSITIVE',
    0.9440,
    'COMPLETED',
    NOW(),
    NOW() - INTERVAL '1 day',
    NOW()
  )
ON CONFLICT (id) DO UPDATE
SET
  user_name = EXCLUDED.user_name,
  title = EXCLUDED.title,
  content_text = EXCLUDED.content_text,
  content_hash = EXCLUDED.content_hash,
  rating = EXCLUDED.rating,
  status = EXCLUDED.status,
  trust_score = EXCLUDED.trust_score,
  sentiment = EXCLUDED.sentiment,
  sentiment_confidence = EXCLUDED.sentiment_confidence,
  sentiment_status = EXCLUDED.sentiment_status,
  sentiment_analyzed_at = EXCLUDED.sentiment_analyzed_at,
  updated_at = NOW();

COMMIT;
