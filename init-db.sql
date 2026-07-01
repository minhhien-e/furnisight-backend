-- Create all service databases.
-- Table schemas and seed data are managed by Flyway migrations
-- running inside each service container.
SELECT 'CREATE DATABASE furnisight_user_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_user_db')\gexec

SELECT 'CREATE DATABASE furnisight_catalog_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_catalog_db')\gexec

SELECT 'CREATE DATABASE furnisight_review_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_review_db')\gexec

SELECT 'CREATE DATABASE furnisight_media_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_media_db')\gexec

SELECT 'CREATE DATABASE furnisight_order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_order_db')\gexec

SELECT 'CREATE DATABASE furnisight_promotion_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_promotion_db')\gexec

SELECT 'CREATE DATABASE furnisight_message_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_message_db')\gexec

SELECT 'CREATE DATABASE furnisight_admin_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'furnisight_admin_db')\gexec
