-- V0025__add_discord_notification_configs_table.sql
-- Creates Discord notification configuration for guilds

CREATE TABLE IF NOT EXISTS discord_notification_configs (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    discord_server_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    mention_role_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    UNIQUE(guild_id, notification_type)
);

-- Indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_discord_notif_guild_id ON discord_notification_configs(guild_id);
CREATE INDEX IF NOT EXISTS idx_discord_notif_server_id ON discord_notification_configs(discord_server_id);
CREATE INDEX IF NOT EXISTS idx_discord_notif_type ON discord_notification_configs(notification_type);

-- Comments for documentation
COMMENT ON TABLE discord_notification_configs IS 'Discord notification channel configuration per guild';
COMMENT ON COLUMN discord_notification_configs.guild_id IS 'The WoW guild ID this config belongs to';
COMMENT ON COLUMN discord_notification_configs.discord_server_id IS 'Discord server (guild) snowflake ID';
COMMENT ON COLUMN discord_notification_configs.notification_type IS 'Type: LOOT_AWARD, RDF_EXPIRY, PENALTY, LOOT_BAN, SYNC_COMPLETE';
COMMENT ON COLUMN discord_notification_configs.channel_id IS 'Discord channel snowflake ID to send notifications to';
COMMENT ON COLUMN discord_notification_configs.mention_role_id IS 'Optional Discord role to mention with notifications';
