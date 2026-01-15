-- V0030__add_local_auth_support.sql
-- Adds support for local username/password authentication

-- Add password_hash column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Remove the OAuth requirement constraint
-- Users can now authenticate with just username/password
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_user_has_auth;

-- Add unique constraint on username for local login
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username_unique ON users(LOWER(username));

-- Add unique constraint on email for password reset
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique ON users(LOWER(email)) WHERE email IS NOT NULL;

-- Comments for documentation
COMMENT ON COLUMN users.password_hash IS 'BCrypt hash of the user password, NULL for OAuth-only accounts';
