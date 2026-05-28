ALTER TABLE orders 
ADD COLUMN order_created_at TIMESTAMP,
ADD COLUMN payment_initiated_at TIMESTAMP,
ADD COLUMN payment_completed_at TIMESTAMP;
