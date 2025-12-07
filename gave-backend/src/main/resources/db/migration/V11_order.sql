-- V11__add_stock_thresholds.sql (o el número que siga)
ALTER TABLE product
    ADD COLUMN stock_low_threshold    INT DEFAULT 5,
    ADD COLUMN stock_medium_threshold INT DEFAULT 15;
