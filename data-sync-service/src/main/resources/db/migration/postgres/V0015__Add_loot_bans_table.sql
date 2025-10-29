-- Add loot bans table for guild leader managed loot exclusions
CREATE TABLE loot_bans (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    banned_by VARCHAR(255) NOT NULL,
    banned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    CONSTRAINT loot_bans_guild_character_idx 
    UNIQUE (guild_id, character_name, banned_at)
);

-- Index for performance on active ban queries
CREATE INDEX idx_loot_bans_active 
ON loot_bans (guild_id, character_name, is_active, expires_at);

-- Index for guild management queries
CREATE INDEX idx_loot_bans_guild 
ON loot_bans (guild_id, is_active, expires_at);