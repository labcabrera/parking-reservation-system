-- V3: Add daily_rate and currency to parking_facility
ALTER TABLE catalog.parking_facility
    ADD COLUMN IF NOT EXISTS daily_rate NUMERIC(10, 2) NOT NULL DEFAULT 25.00,
    ADD COLUMN IF NOT EXISTS currency   VARCHAR(3)     NOT NULL DEFAULT 'EUR';

-- Set realistic seed values for existing test data
UPDATE catalog.parking_facility
SET daily_rate = 30.00, currency = 'EUR'
WHERE id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567891'; -- Madrid Centro

UPDATE catalog.parking_facility
SET daily_rate = 28.00, currency = 'EUR'
WHERE id = 'b2c3d4e5-f6a7-8901-bcde-f12345678902'; -- Barcelona Gracia

UPDATE catalog.parking_facility
SET daily_rate = 45.00, currency = 'EUR'
WHERE id = 'c3d4e5f6-a7b8-9012-cdef-123456789013'; -- Madrid Barajas Airport
