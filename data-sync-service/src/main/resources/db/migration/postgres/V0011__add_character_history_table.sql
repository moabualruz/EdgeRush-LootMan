-- Add character history table for individual character gear and activity tracking
CREATE TABLE character_history (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255),
    character_region VARCHAR(255),
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    history_json TEXT,
    best_gear_json TEXT,
    synced_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for efficient querying
CREATE INDEX idx_character_history_character_id ON character_history(character_id);
CREATE INDEX idx_character_history_character_name_realm ON character_history(character_name, character_realm);
CREATE INDEX idx_character_history_team_id ON character_history(team_id);
CREATE INDEX idx_character_history_synced_at ON character_history(synced_at);

-- Add comment to document the table purpose
COMMENT ON TABLE character_history IS 'Individual character activity history and gear progression from WoWAudit API for FLPS IPI calculations';