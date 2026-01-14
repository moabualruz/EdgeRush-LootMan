-- Create discord_user_links table for linking Discord users to WoW characters
-- This enables the Discord bot to map Discord users to their in-game characters
-- and allows the web frontend to authenticate users via Discord OAuth2

CREATE TABLE IF NOT EXISTS discord_user_links (
    id BIGSERIAL PRIMARY KEY,
    discord_user_id VARCHAR(255) NOT NULL,
    raider_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    linked_by VARCHAR(255),
    UNIQUE(discord_user_id, raider_id)
);

-- Index for looking up links by Discord user ID (most common query pattern)
CREATE INDEX IF NOT EXISTS idx_discord_user_links_discord_user_id ON discord_user_links(discord_user_id);

-- Index for looking up links by raider ID
CREATE INDEX IF NOT EXISTS idx_discord_user_links_raider_id ON discord_user_links(raider_id);

-- Ensure only one primary link per Discord user
CREATE UNIQUE INDEX IF NOT EXISTS idx_discord_user_links_primary
ON discord_user_links(discord_user_id) WHERE is_primary = true;

COMMENT ON TABLE discord_user_links IS 'Links Discord user accounts to WoW raider characters';
COMMENT ON COLUMN discord_user_links.discord_user_id IS 'Discord user ID (snowflake)';
COMMENT ON COLUMN discord_user_links.raider_id IS 'Reference to raiders table';
COMMENT ON COLUMN discord_user_links.is_primary IS 'Whether this is the users primary character';
COMMENT ON COLUMN discord_user_links.linked_at IS 'When the link was created';
COMMENT ON COLUMN discord_user_links.linked_by IS 'Who created the link (user or admin)';
