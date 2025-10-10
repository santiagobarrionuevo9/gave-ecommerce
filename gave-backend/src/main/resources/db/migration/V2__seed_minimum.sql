-- Tipos
INSERT INTO product_type (name, slug, description) VALUES
                                                       ('Filtros de Agua','filtros-agua','Filtros y cartuchos para agua'),
                                                       ('Comida para Perro','comida-perro','Alimentos balanceados y snacks'),
                                                       ('Llantas','llantas','Llantas para autos');

-- Producto: Filtro
INSERT INTO product (type_id, name, slug, short_desc, description)
VALUES (
           (SELECT id FROM product_type WHERE slug='filtros-agua'),
           'Filtro 10" Polipropileno 5µm',
           'filtro-10-pp-5',
           'Cartucho 10" 5 micras',
           'Filtro de sedimentos para equipos domésticos'
       );

INSERT INTO product_variant (product_id, sku, title, price, stock)
VALUES (
           (SELECT id FROM product WHERE slug='filtro-10-pp-5'),
           'GAVE-FLT-10-5',
           '10" • 5µm',
           12999.00,
           50
       );

-- Producto: Comida perro (2 variantes por peso)
INSERT INTO product (type_id, name, slug, short_desc)
VALUES (
           (SELECT id FROM product_type WHERE slug='comida-perro'),
           'Alimento Adulto Pollo',
           'alimento-adulto-pollo',
           'Sabor pollo, adultos'
       );

INSERT INTO product_variant (product_id, sku, title, price, stock) VALUES
                                                                       ((SELECT id FROM product WHERE slug='alimento-adulto-pollo'),'CF-ADULT-3KG','3kg',15999,20),
                                                                       ((SELECT id FROM product WHERE slug='alimento-adulto-pollo'),'CF-ADULT-15KG','15kg',59999,10);

-- Producto: Llanta (1 variante)
INSERT INTO product (type_id, name, slug, short_desc)
VALUES (
           (SELECT id FROM product_type WHERE slug='llantas'),
           'Llanta ACME Sport Negra',
           'llanta-acme-sport-negra',
           'Deportiva terminación negra'
       );

INSERT INTO product_variant (product_id, sku, title, price, stock)
VALUES (
           (SELECT id FROM product WHERE slug='llanta-acme-sport-negra'),
           'AW-SPRT-17x7.5-5x114',
           '17x7.5 • 5x114.3',
           119999.00,
           8
       );
