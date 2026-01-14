-- V0023__add_users_and_auth_tables.sql
-- Creates users and authentication-related tables for OAuth2 login

-- Users table for storing authenticated user accounts
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    discord_id VARCHAR(255) UNIQUE,
    battlenet_id VARCHAR(255) UNIQUE,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    avatar_url VARCHAR(512),
    role VARCHAR(50) NOT NULL DEFAULT 'RAIDER',
    guild_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login TIMESTAMPTZ,
    CONSTRAINT chk_user_has_auth CHECK (discord_id IS NOT NULL OR battlenet_id IS NOT NULL)
);

-- Refresh tokens table for JWT refresh token management
CREATE TABLE IF NOT EXISTS user_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);

-- Indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_users_discord_id ON users(discord_id);
CREATE INDEX IF NOT EXISTS idx_users_battlenet_id ON users(battlenet_id);
CREATE INDEX IF NOT EXISTS idx_users_guild_id ON users(guild_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON user_refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON user_refresh_tokens(token_hash);

-- Comments for documentation
COMMENT ON TABLE users IS 'Authenticated user accounts linked via Discord or Battle.net OAuth2';
COMMENT ON TABLE user_refresh_tokens IS 'JWT refresh tokens for maintaining user sessions';
COMMENT ON COLUMN users.role IS 'User role: RAIDER, GUILD_ADMIN, or SYSTEM_ADMIN';
COMMENT ON COLUMN users.guild_id IS 'Associated guild ID for guild-specific access control';
COMMENT ON COLUMN user_refresh_tokens.token_hash IS 'SHA-256 hash of the refresh token';
COMMENT ON COLUMN user_refresh_tokens.revoked_at IS 'When the token was revoked, NULL if still valid';
