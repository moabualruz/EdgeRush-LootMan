-- ============================================================================
-- V0037: Add Battle.net Guild Roster Configuration
-- ============================================================================
-- Adds Battle.net guild roster sync configuration to guild_configurations.
-- This allows each guild to configure and trigger their own roster sync.
-- ============================================================================

-- Add Battle.net guild roster configuration fields
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_realm_slug VARCHAR(255);
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_guild_name_slug VARCHAR(255);
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_region VARCHAR(10) DEFAULT 'eu';

-- Track Battle.net roster sync separately from WoWAudit sync
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_last_sync_at TIMESTAMPTZ;
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_last_sync_status VARCHAR(50);
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_last_sync_error TEXT;
ALTER TABLE guild_configurations ADD COLUMN IF NOT EXISTS bnet_sync_enabled BOOLEAN DEFAULT true;

-- Add comment for documentation
COMMENT ON COLUMN guild_configurations.bnet_realm_slug IS 'Battle.net realm slug (e.g., "twisting-nether")';
COMMENT ON COLUMN guild_configurations.bnet_guild_name_slug IS 'Battle.net guild name slug (lowercase, hyphens for spaces, e.g., "dod")';
COMMENT ON COLUMN guild_configurations.bnet_region IS 'Battle.net region (e.g., "eu", "us", "kr", "tw", "cn")';
COMMENT ON COLUMN guild_configurations.bnet_last_sync_at IS 'Last Battle.net roster sync timestamp';
COMMENT ON COLUMN guild_configurations.bnet_last_sync_status IS 'Last Battle.net sync status: SUCCESS, FAILED, IN_PROGRESS';
COMMENT ON COLUMN guild_configurations.bnet_sync_enabled IS 'Enable/disable Battle.net roster sync for this guild';
