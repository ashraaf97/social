ALTER TABLE users ADD COLUMN overlay_token VARCHAR(64);

-- H2 doesn't support partial indexes with WHERE clause, so we create a standard unique index
-- This means NULL values will be allowed but duplicates won't be
CREATE UNIQUE INDEX idx_users_overlay_token ON users(overlay_token);
