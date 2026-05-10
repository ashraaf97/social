-- Enable pgcrypto extension for secure random token generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Generate overlay tokens for existing users who don't have one
-- This is a one-time migration to backfill tokens for existing streamers
UPDATE users 
SET overlay_token = encode(gen_random_bytes(32), 'base64')
WHERE role = 'STREAMER' 
  AND overlay_token IS NULL;
