-- ---------------------------------------------------------------------------
-- PostgreSQL initialization script
-- Runs once on first container startup (docker-entrypoint-initdb.d).
-- ---------------------------------------------------------------------------

SELECT 'CREATE DATABASE keycloak_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db')\gexec
