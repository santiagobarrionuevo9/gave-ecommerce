-- src/main/resources/db/migration/V7__order_tables_and_reserved.sql
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      buyer_email VARCHAR(180) NOT NULL,
    buyer_name  VARCHAR(180) NOT NULL,
    buyer_phone VARCHAR(60),
    status VARCHAR(32) NOT NULL,
    items_total NUMERIC(14,2) NOT NULL,
    grand_total NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
    );

CREATE TABLE IF NOT EXISTS order_item (
                                          id BIGSERIAL PRIMARY KEY,
                                          order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity INT NOT NULL,
    unit_price NUMERIC(14,2) NOT NULL,
    line_total NUMERIC(14,2) NOT NULL
    );

-- campo de reservas en product
ALTER TABLE product ADD COLUMN IF NOT EXISTS reserved INT NOT NULL DEFAULT 0;

-- índices útiles
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_product_reserved ON product(reserved);

-- V7__order_tables_and_reserved.sql (parte de reserved)
ALTER TABLE product ADD COLUMN IF NOT EXISTS reserved INT NOT NULL DEFAULT 0;
