CREATE SCHEMA IF NOT EXISTS catalog;

ALTER TABLE IF EXISTS catalog.parking_facility
    ADD COLUMN IF NOT EXISTS short_term_spots INTEGER,
    ADD COLUMN IF NOT EXISTS long_term_spots INTEGER;

UPDATE catalog.parking_facility
   SET short_term_spots = COALESCE(short_term_spots, total_spots),
       long_term_spots = COALESCE(long_term_spots, 0)
 WHERE short_term_spots IS NULL
    OR long_term_spots IS NULL;

ALTER TABLE IF EXISTS catalog.inventory_slot
    ADD COLUMN IF NOT EXISTS block_type VARCHAR(20);

UPDATE catalog.inventory_slot
   SET block_type = COALESCE(block_type, 'SHORT_TERM')
 WHERE block_type IS NULL;

ALTER TABLE IF EXISTS catalog.inventory_slot
    ALTER COLUMN block_type SET NOT NULL;

ALTER TABLE IF EXISTS catalog.inventory_slot
    DROP CONSTRAINT IF EXISTS uk_inventory_slot;

ALTER TABLE IF EXISTS catalog.inventory_slot
    ADD CONSTRAINT uk_inventory_slot UNIQUE (facility_id, slot_start, block_type);

CREATE INDEX IF NOT EXISTS ix_inventory_slot_facility_start
    ON catalog.inventory_slot (facility_id, slot_start, block_type);
