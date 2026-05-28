-- Create all service databases
-- NOTE: Table schemas and seed data are managed by Flyway migrations
--       running inside each service container (not here).
CREATE DATABASE furnisight_user_db;

CREATE DATABASE furnisight_catalog_db;

CREATE DATABASE furnisight_media_db;

CREATE DATABASE furnisight_order_db;

CREATE DATABASE furnisight_message_db;