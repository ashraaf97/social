ALTER TABLE users ADD COLUMN donation_token VARCHAR(64);

-- H2 doesn't support partial indexes with WHERE clause
CREATE UNIQUE INDEX idx_users_donation_token ON users(donation_token);

-- H2-compatible token generation using UUID
UPDATE users
SET donation_token = REPLACE(CAST(RANDOM_UUID() AS VARCHAR), '-', '')
WHERE role = 'STREAMER'
  AND donation_token IS NULL;

-- H2 doesn't have translate function, so we skip this cleanup for tests
-- UPDATE users
-- SET overlay_token = rtrim(translate(overlay_token, '+/', '-_'), '=')
-- WHERE overlay_token LIKE '%/%' OR overlay_token LIKE '%+%' OR overlay_token LIKE '%=%';
