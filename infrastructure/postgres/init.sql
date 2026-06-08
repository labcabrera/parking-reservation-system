-- ---------------------------------------------------------------------------
-- PostgreSQL initialization script
-- Runs once on first container startup (docker-entrypoint-initdb.d).
-- ---------------------------------------------------------------------------

SELECT 'CREATE DATABASE keycloak_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db')\gexec

SELECT 'CREATE DATABASE facilities_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'facilities_db')\gexec

SELECT 'CREATE DATABASE ecommerce_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce_db')\gexec

SELECT 'CREATE DATABASE pricing_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pricing_db')\gexec

SELECT 'CREATE DATABASE users_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'users_db')\gexec

SELECT 'CREATE DATABASE payment_db'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_db')\gexec
