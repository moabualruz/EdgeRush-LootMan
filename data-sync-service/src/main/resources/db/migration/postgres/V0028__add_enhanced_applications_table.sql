-- Enhanced applications table for the new recruitment system domain model
-- This is separate from the existing applications table to support the new domain model
-- with enhanced recruitment features (OAuth, auto-fetch, review workflow)

CREATE TABLE IF NOT EXISTS enhanced_applications (
    enhanced_application_id VARCHAR(255) PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    -- OAuth identity
    battle_net_id VARCHAR(255) NOT NULL,
    discord_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    -- Character data (auto-fetched)
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    item_level DOUBLE PRECISION NOT NULL,
    raider_io_score DOUBLE PRECISION,
    best_parse_average DOUBLE PRECISION,
    -- User input
    age INTEGER NOT NULL,
    location TEXT NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    raid_days_available TEXT NOT NULL, -- Stored as JSON array
    previous_guilds TEXT NOT NULL,
    reason_for_leaving TEXT NOT NULL,
    why_this_guild TEXT NOT NULL,
    -- Status and review
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMPTZ,
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_enhanced_apps_guild_id ON enhanced_applications(guild_id);
CREATE INDEX IF NOT EXISTS idx_enhanced_apps_status ON enhanced_applications(status);
CREATE INDEX IF NOT EXISTS idx_enhanced_apps_discord_id ON enhanced_applications(guild_id, discord_id);
CREATE INDEX IF NOT EXISTS idx_enhanced_apps_battle_net ON enhanced_applications(guild_id, battle_net_id);
CREATE INDEX IF NOT EXISTS idx_enhanced_apps_created_at ON enhanced_applications(created_at DESC);
