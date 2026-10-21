-- Carica il dataset sintetico montato nel container in /data (vedi docker-compose.yml)
COPY sales (store_id, sku, sale_date, quantity, unit_price)
FROM '/data/sales.csv' WITH (FORMAT csv, HEADER true, DELIMITER ';');
