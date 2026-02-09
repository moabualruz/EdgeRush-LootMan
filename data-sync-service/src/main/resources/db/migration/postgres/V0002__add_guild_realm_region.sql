-- V0002: Add realm and region columns to guild_configurations for GuildController
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS realm TEXT;
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS region TEXT DEFAULT 'EU';
