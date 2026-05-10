ALTER TABLE users ADD COLUMN overlay_token VARCHAR(64);

CREATE UNIQUE INDEX idx_users_overlay_token ON users(overlay_token) WHERE overlay_token IS NOT NULL;
