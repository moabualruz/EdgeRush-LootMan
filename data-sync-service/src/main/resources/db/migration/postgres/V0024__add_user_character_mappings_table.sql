-- V0024__add_user_character_mappings_table.sql
-- Creates user-to-character mapping for authenticated users

CREATE TABLE IF NOT EXISTS user_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    raider_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    verified BOOLEAN DEFAULT false,
    verified_at TIMESTAMPTZ,
    UNIQUE(user_id, raider_id)
);

-- Indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_user_char_user_id ON user_character_mappings(user_id);
CREATE INDEX IF NOT EXISTS idx_user_char_raider_id ON user_character_mappings(raider_id);
CREATE INDEX IF NOT EXISTS idx_user_char_primary ON user_character_mappings(user_id, is_primary) WHERE is_primary = true;

-- Comments for documentation
COMMENT ON TABLE user_character_mappings IS 'Maps authenticated users to their WoW characters (raiders)';
COMMENT ON COLUMN user_character_mappings.is_primary IS 'Primary character for this user';
COMMENT ON COLUMN user_character_mappings.verified IS 'Whether ownership has been verified (e.g., via Battle.net)';
COMMENT ON COLUMN user_character_mappings.verified_at IS 'When the character was verified';
