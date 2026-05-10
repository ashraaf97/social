ALTER TABLE users ADD COLUMN donation_token VARCHAR(64);

CREATE UNIQUE INDEX idx_users_donation_token ON users(donation_token) WHERE donation_token IS NOT NULL;

UPDATE users
SET donation_token = rtrim(translate(encode(gen_random_bytes(32), 'base64'), '+/', '-_'), '=')
WHERE role = 'STREAMER'
  AND donation_token IS NULL;

UPDATE users
SET overlay_token = rtrim(translate(overlay_token, '+/', '-_'), '=')
WHERE overlay_token LIKE '%/%' OR overlay_token LIKE '%+%' OR overlay_token LIKE '%=%';
