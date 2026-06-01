-- init.sql — PostgreSQL initialization script for Parking Reservation System
-- Creates the three service databases if they do not already exist.
-- Executed by the postgres container on first start via /docker-entrypoint-initdb.d/
-- catalog_db is already created by POSTGRES_DB env var; create the others here.

CREATE DATABASE reservation_db OWNER parking;
CREATE DATABASE payment_db OWNER parking;
