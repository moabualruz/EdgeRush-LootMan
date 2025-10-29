-- Add behavioral actions table for guild leader managed behavioral scoring
CREATE TABLE behavioral_actions (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('DEDUCTION', 'RESTORATION')),
    deduction_amount DECIMAL(3,2) NOT NULL CHECK (deduction_amount >= 0.0 AND deduction_amount <= 1.0),
    reason TEXT NOT NULL,
    applied_by VARCHAR(255) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    CONSTRAINT behavioral_actions_guild_character_idx 
    UNIQUE (guild_id, character_name, applied_at)
);

-- Index for performance on active behavioral actions queries
CREATE INDEX idx_behavioral_actions_active 
ON behavioral_actions (guild_id, character_name, is_active, expires_at);

-- Index for guild management queries
CREATE INDEX idx_behavioral_actions_guild 
ON behavioral_actions (guild_id, is_active, expires_at);