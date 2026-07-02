ALTER TABLE revenue_snapshots ALTER COLUMN year_month TYPE VARCHAR(20);
ALTER TABLE revenue_snapshots DROP COLUMN IF EXISTS label;
