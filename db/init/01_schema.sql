-- Schema v1 (giorno 18): minimo ma con vincoli
CREATE TABLE stores (
    store_id  VARCHAR(10)  PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    city      VARCHAR(100) NOT NULL,
    sqm       NUMERIC(7,1) NOT NULL CHECK (sqm > 0)
);

CREATE TABLE products (
    sku        VARCHAR(20)   PRIMARY KEY,
    name       VARCHAR(150)  NOT NULL,
    category   VARCHAR(50)   NOT NULL,
    list_price NUMERIC(10,2) NOT NULL CHECK (list_price > 0),
    unit_cost  NUMERIC(10,2) NOT NULL CHECK (unit_cost >= 0)
);

CREATE TABLE sales (
    sale_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_id   VARCHAR(10)   NOT NULL REFERENCES stores (store_id),
    sku        VARCHAR(20)   NOT NULL REFERENCES products (sku),
    sale_date  DATE          NOT NULL,
    quantity   INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0)
);

CREATE INDEX ix_sales_store_date ON sales (store_id, sale_date);
CREATE INDEX ix_sales_sku ON sales (sku);
