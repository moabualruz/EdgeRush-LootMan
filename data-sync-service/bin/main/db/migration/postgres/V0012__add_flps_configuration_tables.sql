-- FLPS Configuration Tables for Multi-Guild Support
-- V0012__add_flps_configuration_tables.sql

-- Default FLPS modifiers (system-wide defaults)
CREATE TABLE flps_default_modifiers (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    modifier_key VARCHAR(100) NOT NULL,
    modifier_value DECIMAL(10, 6) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(category, modifier_key)
);

-- Guild-specific FLPS modifier overrides
CREATE TABLE flps_guild_modifiers (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,  -- Guild identifier (could be WoWAudit guild URI or guild name)
    category VARCHAR(50) NOT NULL,
    modifier_key VARCHAR(100) NOT NULL,
    modifier_value DECIMAL(10, 6) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(guild_id, category, modifier_key)
);

-- Indexes for performance
CREATE INDEX idx_flps_default_category ON flps_default_modifiers(category);
CREATE INDEX idx_flps_guild_modifiers_guild ON flps_guild_modifiers(guild_id);
CREATE INDEX idx_flps_guild_modifiers_category ON flps_guild_modifiers(guild_id, category);

-- Insert default system modifiers
INSERT INTO flps_default_modifiers (category, modifier_key, modifier_value, description) VALUES
-- RMS (Raider Merit Score) Weights
('rms', 'attendance_weight', 0.45, 'Weight for Attendance Consistency Score in RMS calculation'),
('rms', 'mechanical_weight', 0.35, 'Weight for Mechanical Adherence Score in RMS calculation'),
('rms', 'preparation_weight', 0.20, 'Weight for External Preparation Score in RMS calculation'),

-- IPI (Item Priority Index) Weights  
('ipi', 'upgrade_value_weight', 0.45, 'Weight for upgrade value in IPI calculation'),
('ipi', 'tier_bonus_weight', 0.35, 'Weight for tier bonus in IPI calculation'),
('ipi', 'role_multiplier_weight', 0.20, 'Weight for role multiplier in IPI calculation'),

-- Role Multipliers
('role', 'tank_multiplier', 1.2, 'Tank role multiplier (rare/critical role gets 20% bonus)'),
('role', 'healer_multiplier', 1.1, 'Healer role multiplier (important role gets 10% bonus)'),
('role', 'dps_multiplier', 1.0, 'DPS role multiplier (baseline)'),

-- Eligibility Thresholds
('threshold', 'eligibility_attendance', 0.8, 'Minimum attendance percentage for loot eligibility'),
('threshold', 'eligibility_activity', 0.0, 'Minimum activity score for loot eligibility'),
('threshold', 'recency_decay_days', 30, 'Days for recency decay factor calculation'),

-- Score Limits
('limit', 'max_attendance_bonus', 1.0, 'Maximum attendance score cap'),
('limit', 'min_mechanical_score', 0.0, 'Minimum mechanical adherence score'),
('limit', 'max_preparation_score', 1.0, 'Maximum preparation score cap');

COMMENT ON TABLE flps_default_modifiers IS 'System-wide default FLPS algorithm modifiers and weights';
COMMENT ON TABLE flps_guild_modifiers IS 'Guild-specific FLPS modifier overrides. Falls back to defaults if not specified.';
COMMENT ON COLUMN flps_guild_modifiers.guild_id IS 'Guild identifier - typically WoWAudit guild URI or standardized guild name';