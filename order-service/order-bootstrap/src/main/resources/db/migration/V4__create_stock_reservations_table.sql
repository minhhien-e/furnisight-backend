CREATE TABLE IF NOT EXISTS stock_reservations (
    id UUID PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL,
    product_variant_id UUID,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_stock_res_order_code FOREIGN KEY (order_code) REFERENCES orders(order_code)
);

CREATE INDEX idx_stock_res_order_code ON stock_reservations(order_code);
