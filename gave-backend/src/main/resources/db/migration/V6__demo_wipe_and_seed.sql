-- V6__demo_filters_seed.sql
-- Catálogo de Filtros de Agua y Accesorios (12+ productos)
-- Compatible con tu esquema unificado: product_type / product / product_image

BEGIN;

-- Limpio catálogo (resetea IDs)
TRUNCATE TABLE product_image RESTART IDENTITY CASCADE;
TRUNCATE TABLE product       RESTART IDENTITY CASCADE;
TRUNCATE TABLE product_type  RESTART IDENTITY CASCADE;

-- =========================
-- 1) TIPOS / CATEGORÍAS
-- =========================
INSERT INTO product_type (name, slug, description) VALUES
                                                       ('Filtros de Sedimento',   'filtros-sedimento',   'Cartuchos de polipropileno (PP), varios micrajes'),
                                                       ('Carbón Activado',        'carbon-activado',     'CTO/GAC para mejorar sabor y olor'),
                                                       ('Carcasas y Portafiltros','carcasas-portafiltros','Carcasas 10\"/20\" y portafiltros simples/dobles'),
                                                       ('Ósmosis Inversa',        'osmosis-inversa',     'Membranas RO y postfiltros'),
                                                       ('Accesorios y Repuestos', 'accesorios-repuestos','Llaves, válvulas, manómetros y conectores');

-- =========================
-- 2) PRODUCTOS
-- =========================

-- SEDIMENTO (PP)
INSERT INTO product (type_id, name, slug, short_desc, description, is_active, sku, price, stock) VALUES
                                                                                                     ((SELECT id FROM product_type WHERE slug='filtros-sedimento'),
                                                                                                      'Cartucho PP 10\" 5µm', 'cartucho-pp-10-5-micras',
                                                                                                      'Polipropileno 5 micras', 'Cartucho de sedimento para equipos domésticos. Medida estándar 10\".', TRUE,
                                                                                                      'GAVE-PP-10-5', 1899.00, 120),

                                                                                                     ((SELECT id FROM product_type WHERE slug='filtros-sedimento'),
                                                                                                      'Cartucho PP 10\" 1µm', 'cartucho-pp-10-1-micra',
                                                                                                      'Polipropileno 1 micra', 'Para retención fina de sólidos. Medida 10\" estándar.', TRUE,
                                                                                                      'GAVE-PP-10-1', 2199.00, 80),

                                                                                                     ((SELECT id FROM product_type WHERE slug='filtros-sedimento'),
                                                                                                      'Cartucho PP 20\" 5µm', 'cartucho-pp-20-5-micras',
                                                                                                      'Polipropileno 20\" 5µm', 'Mayor capacidad por longitud 20\". Para portafiltros altos.', TRUE,
                                                                                                      'GAVE-PP-20-5', 3599.00, 40);

-- CARBÓN
INSERT INTO product (type_id, name, slug, short_desc, description, is_active, sku, price, stock) VALUES
                                                                                                     ((SELECT id FROM product_type WHERE slug='carbon-activado'),
                                                                                                      'Cartucho CTO 10\"', 'cartucho-cto-10',
                                                                                                      'Block de carbón 10\"', 'Carbón block (CTO) para mejorar sabor y olor. Estándar 10\".', TRUE,
                                                                                                      'GAVE-CTO-10', 3299.00, 95),

                                                                                                     ((SELECT id FROM product_type WHERE slug='carbon-activado'),
                                                                                                      'Cartucho GAC 10\"', 'cartucho-gac-10',
                                                                                                      'Carbón granular 10\"', 'GAC (Granular) para cloro/sabor/olor. Estándar 10\".', TRUE,
                                                                                                      'GAVE-GAC-10', 3099.00, 70);

-- CARCASAS / PORTAFILTROS
INSERT INTO product (type_id, name, slug, short_desc, description, is_active, sku, price, stock) VALUES
                                                                                                     ((SELECT id FROM product_type WHERE slug='carcasas-portafiltros'),
                                                                                                      'Carcasa 10\" Transparente 1/2\"', 'carcasa-10-transparente-media',
                                                                                                      'Rosca 1/2\"', 'Carcasa transparente para cartuchos 10\". Incluye o-ring.', TRUE,
                                                                                                      'GAVE-HSG-10-12', 8999.00, 22),

                                                                                                     ((SELECT id FROM product_type WHERE slug='carcasas-portafiltros'),
                                                                                                      'Portafiltro Doble 10\"', 'portafiltro-doble-10',
                                                                                                      'Estructura doble', 'Soporte doble para dos carcasas 10\" (sedimento+carbón).', TRUE,
                                                                                                      'GAVE-HSG-DBL-10', 17999.00, 12),

                                                                                                     ((SELECT id FROM product_type WHERE slug='carcasas-portafiltros'),
                                                                                                      'Llave para Carcasa 10\"', 'llave-carcasa-10',
                                                                                                      'Llave plástica', 'Llave universal para aflojar carcasas 10\". Imprescindible para mantenimiento.', TRUE,
                                                                                                      'GAVE-KEY-10', 2499.00, 60);

-- ÓSMOSIS INVERSA
INSERT INTO product (type_id, name, slug, short_desc, description, is_active, sku, price, stock) VALUES
                                                                                                     ((SELECT id FROM product_type WHERE slug='osmosis-inversa'),
                                                                                                      'Membrana RO 50 GPD', 'membrana-ro-50-gpd',
                                                                                                      'Ósmosis 50 GPD', 'Membrana de ósmosis inversa 50 gal/día. Hogar/mesada.', TRUE,
                                                                                                      'GAVE-RO-50', 29999.00, 18),

                                                                                                     ((SELECT id FROM product_type WHERE slug='osmosis-inversa'),
                                                                                                      'Membrana RO 75 GPD', 'membrana-ro-75-gpd',
                                                                                                      'Ósmosis 75 GPD', 'Mayor caudal. Compatible con sistemas estándar.', TRUE,
                                                                                                      'GAVE-RO-75', 33999.00, 10),

                                                                                                     ((SELECT id FROM product_type WHERE slug='osmosis-inversa'),
                                                                                                      'Postfiltro Inline Carbón 10\"', 'postfiltro-inline-carbon-10',
                                                                                                      'Inline 10\"', 'Postfiltro de carbón para pulido final en equipos RO.', TRUE,
                                                                                                      'GAVE-INL-CARB-10', 4599.00, 35);

-- ACCESORIOS / REPUESTOS
INSERT INTO product (type_id, name, slug, short_desc, description, is_active, sku, price, stock) VALUES
                                                                                                     ((SELECT id FROM product_type WHERE slug='accesorios-repuestos'),
                                                                                                      'Válvula Corte Rápido 1/4\"', 'valvula-corte-rapido-14',
                                                                                                      'Push-fit 1/4\"', 'Válvula plástica para tubería 1/4\". Sistema push-fit.', TRUE,
                                                                                                      'GAVE-VAL-14', 1999.00, 140),

                                                                                                     ((SELECT id FROM product_type WHERE slug='accesorios-repuestos'),
                                                                                                      'Manómetro 1/4\"', 'manometro-14',
                                                                                                      '0–160 psi', 'Manómetro para línea de agua 1/4\". Útil en RO.', TRUE,
                                                                                                      'GAVE-MAN-14', 11999.00, 9);

-- =========================
-- 3) IMÁGENES (opcionales)
-- Si no existen archivos en ./uploads con esos nombres, tu front mostrará el placeholder.
INSERT INTO product_image (product_id, url, alt_text, sort_order) VALUES
                                                                      ((SELECT id FROM product WHERE slug='cartucho-pp-10-5-micras'), '/files/pp-10-5.webp', 'PP 10\" • 5µm', 0),
                                                                      ((SELECT id FROM product WHERE slug='cartucho-pp-10-1-micra'),  '/files/pp-10-1.webp', 'PP 10\" • 1µm', 0),
                                                                      ((SELECT id FROM product WHERE slug='cartucho-cto-10'),         '/files/cto-10.webp',  'CTO 10\"',     0),
                                                                      ((SELECT id FROM product WHERE slug='carcasa-10-transparente-media'), '/files/housing-10.webp', 'Carcasa 10\"', 0),
                                                                      ((SELECT id FROM product WHERE slug='membrana-ro-50-gpd'),      '/files/ro-50.webp',  'RO 50 GPD',    0),
                                                                      ((SELECT id FROM product WHERE slug='postfiltro-inline-carbon-10'), '/files/inline-carbon.webp', 'Postfiltro inline', 0);

COMMIT;

-- Índices útiles (idempotentes por si ya existen)
CREATE INDEX IF NOT EXISTS idx_product_sku   ON product(sku);
CREATE INDEX IF NOT EXISTS idx_product_price ON product(price);
CREATE INDEX IF NOT EXISTS idx_product_name  ON product((lower(name)));
