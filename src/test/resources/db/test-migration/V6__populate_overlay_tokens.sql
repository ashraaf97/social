-- Generate overlay tokens for existing users who don't have one
-- H2 doesn't support pgcrypto, so we use a simple UUID-based approach for tests
UPDATE users 
SET overlay_token = REPLACE(CAST(RANDOM_UUID() AS VARCHAR), '-', '')
WHERE role = 'STREAMER' 
  AND overlay_token IS NULL;
