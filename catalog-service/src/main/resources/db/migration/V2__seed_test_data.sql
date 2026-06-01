-- V2: Seed test data for catalog service
-- 3 parking facilities + 5 spots each

INSERT INTO catalog.parking_facility
    (id, name, city, address, latitude, longitude, total_spots, tags, status,
     free_cancel_hours, penalty_cancel_minutes, version, created_at, updated_at)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567891',
     'Madrid Centro Parking',
     'Madrid',
     'Calle Gran Vía 45, 28013 Madrid',
     40.4200, -3.7050,
     5,
     ARRAY['COVERED', 'EXPRESS_ENTRY'],
     'ACTIVE',
     24, 60, 0, now(), now()),

    ('b2c3d4e5-f6a7-8901-bcde-f12345678902',
     'Barcelona Gràcia Parking',
     'Barcelona',
     'Carrer de Gràcia 12, 08012 Barcelona',
     41.3980, 2.1591,
     5,
     ARRAY['FREE_CANCELLATION', 'GUARDED'],
     'ACTIVE',
     48, 30, 0, now(), now()),

    ('c3d4e5f6-a7b8-9012-cdef-123456789013',
     'Madrid Barajas Airport Parking',
     'Madrid',
     'Av. de la Hispanidad, s/n, 28042 Madrid',
     40.4719, -3.5626,
     5,
     ARRAY['EXPRESS_ENTRY', 'COVERED', 'EV_CHARGING'],
     'ACTIVE',
     72, 0, 0, now(), now());

-- Spots for Madrid Centro
INSERT INTO catalog.parking_spot
    (id, facility_id, spot_number, type, availability_status, version, created_at, updated_at)
VALUES
    ('d4e5f6a7-b8c9-0123-defa-234567890124', 'a1b2c3d4-e5f6-7890-abcd-ef1234567891', 'A01', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('e5f6a7b8-c9d0-1234-efab-345678901235', 'a1b2c3d4-e5f6-7890-abcd-ef1234567891', 'A02', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('f6a7b8c9-d0e1-2345-fabc-456789012346', 'a1b2c3d4-e5f6-7890-abcd-ef1234567891', 'A03', 'COMPACT',   'AVAILABLE', 0, now(), now()),
    ('a7b8c9d0-e1f2-3456-abcd-567890123457', 'a1b2c3d4-e5f6-7890-abcd-ef1234567891', 'A04', 'DISABLED',  'AVAILABLE', 0, now(), now()),
    ('b8c9d0e1-f2a3-4567-bcde-678901234568', 'a1b2c3d4-e5f6-7890-abcd-ef1234567891', 'A05', 'EV',        'AVAILABLE', 0, now(), now());

-- Spots for Barcelona Gràcia
INSERT INTO catalog.parking_spot
    (id, facility_id, spot_number, type, availability_status, version, created_at, updated_at)
VALUES
    ('c9d0e1f2-a3b4-5678-cdef-789012345679', 'b2c3d4e5-f6a7-8901-bcde-f12345678902', 'B01', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('d0e1f2a3-b4c5-6789-defa-890123456780', 'b2c3d4e5-f6a7-8901-bcde-f12345678902', 'B02', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('e1f2a3b4-c5d6-7890-efab-901234567891', 'b2c3d4e5-f6a7-8901-bcde-f12345678902', 'B03', 'COMPACT',  'AVAILABLE', 0, now(), now()),
    ('f2a3b4c5-d6e7-8901-fabc-012345678902', 'b2c3d4e5-f6a7-8901-bcde-f12345678902', 'B04', 'COMPACT',  'AVAILABLE', 0, now(), now()),
    ('a3b4c5d6-e7f8-9012-abcd-123456789013', 'b2c3d4e5-f6a7-8901-bcde-f12345678902', 'B05', 'EV',       'AVAILABLE', 0, now(), now());

-- Spots for Madrid Barajas Airport
INSERT INTO catalog.parking_spot
    (id, facility_id, spot_number, type, availability_status, version, created_at, updated_at)
VALUES
    ('b4c5d6e7-f8a9-0123-bcde-234567890124', 'c3d4e5f6-a7b8-9012-cdef-123456789013', 'C01', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('c5d6e7f8-a9b0-1234-cdef-345678901235', 'c3d4e5f6-a7b8-9012-cdef-123456789013', 'C02', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('d6e7f8a9-b0c1-2345-defa-456789012346', 'c3d4e5f6-a7b8-9012-cdef-123456789013', 'C03', 'STANDARD', 'AVAILABLE', 0, now(), now()),
    ('e7f8a9b0-c1d2-3456-efab-567890123457', 'c3d4e5f6-a7b8-9012-cdef-123456789013', 'C04', 'EV',       'AVAILABLE', 0, now(), now()),
    ('f8a9b0c1-d2e3-4567-fabc-678901234568', 'c3d4e5f6-a7b8-9012-cdef-123456789013', 'C05', 'DISABLED', 'AVAILABLE', 0, now(), now());
