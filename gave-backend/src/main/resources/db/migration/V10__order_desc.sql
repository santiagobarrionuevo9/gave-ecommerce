-- =========================================
-- DESCUENTOS POR PRODUCTO
-- =========================================

-- cantidad mínima para aplicar descuento (ej: 10 unidades)
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS discount_threshold INTEGER;

-- porcentaje de descuento (ej: 10.00 = 10 %)
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS discount_percent NUMERIC(5,2);

-- =========================================
-- DESCUENTO APLICADO POR ÍTEM DE PEDIDO
-- =========================================

-- monto total de descuento aplicado en ese renglón
ALTER TABLE order_item
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
