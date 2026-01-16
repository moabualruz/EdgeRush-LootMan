-- ============================================================================
-- V0031: Multi-Guild User Support
-- ============================================================================
-- Adds support for:
-- 1. Guild association for raiders (characters)
-- 2. Configurable guild permissions based on rank
-- 3. User preferences for active character persistence
-- ============================================================================

-- 1. Add guild_id to raiders table (links characters to guilds)
ALTER TABLE raiders ADD COLUMN IF NOT EXISTS guild_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_raiders_guild_id ON raiders(guild_id);

-- 2. Guild permissions table (configurable rank-based access control)
-- Maps guild ranks (from WoWAudit) to permission types
CREATE TABLE IF NOT EXISTS guild_permissions (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    rank_name VARCHAR(100) NOT NULL,
    permission_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_guild_rank_permission UNIQUE(guild_id, rank_name, permission_type)
);

CREATE INDEX IF NOT EXISTS idx_guild_permissions_guild ON guild_permissions(guild_id);
CREATE INDEX IF NOT EXISTS idx_guild_permissions_rank ON guild_permissions(guild_id, rank_name);

-- 3. User preferences table (persist active character selection)
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    active_character_mapping_id BIGINT REFERENCES user_character_mappings(id) ON DELETE SET NULL,
    last_guild_id VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_preferences_user UNIQUE(user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_preferences_user ON user_preferences(user_id);

-- 4. Insert default permissions for existing guilds
-- Default: Guild Master and Officer ranks get SETTINGS_ACCESS permission
-- Note: guildId column uses camelCase, so we quote it
INSERT INTO guild_permissions (guild_id, rank_name, permission_type)
SELECT DISTINCT gc."guildId", 'Guild Master', 'SETTINGS_ACCESS'
FROM guild_configurations gc
ON CONFLICT ON CONSTRAINT uq_guild_rank_permission DO NOTHING;

INSERT INTO guild_permissions (guild_id, rank_name, permission_type)
SELECT DISTINCT gc."guildId", 'Officer', 'SETTINGS_ACCESS'
FROM guild_configurations gc
ON CONFLICT ON CONSTRAINT uq_guild_rank_permission DO NOTHING;

-- Also add LOOT_MANAGEMENT for Guild Master
INSERT INTO guild_permissions (guild_id, rank_name, permission_type)
SELECT DISTINCT gc."guildId", 'Guild Master', 'LOOT_MANAGEMENT'
FROM guild_configurations gc
ON CONFLICT ON CONSTRAINT uq_guild_rank_permission DO NOTHING;

INSERT INTO guild_permissions (guild_id, rank_name, permission_type)
SELECT DISTINCT gc."guildId", 'Officer', 'LOOT_MANAGEMENT'
FROM guild_configurations gc
ON CONFLICT ON CONSTRAINT uq_guild_rank_permission DO NOTHING;

-- Add comment for documentation
COMMENT ON TABLE guild_permissions IS 'Maps guild ranks (from WoWAudit) to application permissions. Configurable per guild.';
COMMENT ON COLUMN guild_permissions.rank_name IS 'Guild rank name from WoWAudit (e.g., Guild Master, Officer, Raider)';
COMMENT ON COLUMN guild_permissions.permission_type IS 'Permission type: SETTINGS_ACCESS, LOOT_MANAGEMENT, MEMBER_MANAGEMENT, VIEW_ALL_SCORES';

COMMENT ON TABLE user_preferences IS 'Stores user preferences including active character selection';
COMMENT ON COLUMN user_preferences.active_character_mapping_id IS 'Currently selected character for the user';
COMMENT ON COLUMN user_preferences.last_guild_id IS 'Last accessed guild ID for quick context restoration';
