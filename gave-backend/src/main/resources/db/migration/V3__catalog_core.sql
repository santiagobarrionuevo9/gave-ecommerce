-- V3__unify_and_migrate_variants_to_products.sql
-- Objetivo:
-- 1) Agregar columnas de venta a product (si no existen).
-- 2) Crear un product NUEVO por cada variant (copiando sku/price/stock y componiendo name/slug).
-- 3) Reapuntar imágenes de variant → nuevo product (por SKU).
-- 4) Desactivar los products "base" que tengan variants (para no duplicar en el catálogo).
-- 5) Quitar dependencia de variantes en product_image y eliminar product_variant.

-- 1) Campos de venta en PRODUCT (idempotente)
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS sku   VARCHAR(64),
    ADD COLUMN IF NOT EXISTS price NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS stock INT DEFAULT 0;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_product_sku') THEN
ALTER TABLE product ADD CONSTRAINT uk_product_sku UNIQUE (sku);
END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);

-- 2) Insertar NUEVOS productos a partir de variantes (uno por cada variant)
--    name = name_base + ' ' + title_variant
--    slug = slug_base + '-' + slugify(title_variant)
--    sku/price/stock = los de la variant
-- NOTA: usamos una "slugificación" simple con REPLACE/REGEXP_REPLACE.
WITH ins AS (
INSERT INTO product (
    type_id, name, slug, short_desc, description, is_active,
    sku, price, stock, created_at, updated_at
)
SELECT
    p.type_id,
    COALESCE(p.name, '') || CASE WHEN v.title IS NOT NULL AND v.title <> '' THEN ' ' || v.title ELSE '' END AS name,
    -- slug compuesto: base + '-' + titulo variant "slugificado"
    (
        p.slug || '-' ||
        REGEXP_REPLACE(
                REPLACE(REPLACE(REPLACE(LOWER(COALESCE(v.title,'')), ' ', '-'), '"',''), '•','-'),
                '[^a-z0-9\-]+', '-', 'g'
        )
        ) AS slug,
    p.short_desc,
    p.description,
    TRUE,
    v.sku,
    v.price,
    v.stock,
    NOW(),
    NOW()
FROM product p
         JOIN product_variant v ON v.product_id = p.id
-- Evitar reinsertar si ya existe un product con ese SKU (idempotente)
WHERE NOT EXISTS (SELECT 1 FROM product x WHERE x.sku = v.sku)
    RETURNING sku
)
SELECT COUNT(*) AS inserted_from_variants
FROM ins;

-- 3) Reapuntar IMÁGENES de variantes al nuevo producto por SKU
--    product_image.product_id = product.id del product creado con ese SKU
UPDATE product_image pi
SET product_id = np.id
    FROM product np
JOIN product_variant v ON v.sku = np.sku
WHERE pi.variant_id = v.id
  AND pi.product_id IS DISTINCT FROM np.id;

-- 4) Desactivar los products "base" que tenían variants (para que no dupliquen en el catálogo)
UPDATE product p
SET is_active = FALSE
WHERE EXISTS (SELECT 1 FROM product_variant v WHERE v.product_id = p.id)
  AND (p.sku IS NULL OR p.price IS NULL);  -- sólo los base sin datos de venta

-- 5) Eliminar dependencia de variant en IMÁGENES y borrar product_variant
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_name='product_image' AND column_name='variant_id') THEN
ALTER TABLE product_image DROP CONSTRAINT IF EXISTS fk_img_variant;
DROP INDEX IF EXISTS idx_image_variant;
ALTER TABLE product_image DROP COLUMN variant_id;
END IF;
END$$;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='product_variant') THEN
DROP TABLE product_variant;
END IF;
END$$;
