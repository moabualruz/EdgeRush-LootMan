-- Trials table for tracking trial raiders
-- Created for the new trial management domain model
-- Trials are created when an application is approved

CREATE TABLE IF NOT EXISTS trials (
    id VARCHAR(255) PRIMARY KEY,
    application_id VARCHAR(255) NOT NULL,
    raider_id BIGINT,
    guild_id VARCHAR(255) NOT NULL,

    -- Trial status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_date TIMESTAMPTZ,
    expected_end_date TIMESTAMPTZ NOT NULL,

    -- Trial metrics
    raids_attended INTEGER NOT NULL DEFAULT 0,
    raids_required INTEGER NOT NULL DEFAULT 8,
    attendance_rate DOUBLE PRECISION,
    average_performance DOUBLE PRECISION,
    deaths_per_raid DOUBLE PRECISION,

    -- Outcome
    outcome VARCHAR(50),
    outcome_reason TEXT,
    promoted_by VARCHAR(255),
    promoted_at TIMESTAMPTZ,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_trials_guild_id ON trials(guild_id);
CREATE INDEX IF NOT EXISTS idx_trials_application_id ON trials(application_id);
CREATE INDEX IF NOT EXISTS idx_trials_raider_id ON trials(raider_id);
CREATE INDEX IF NOT EXISTS idx_trials_status ON trials(status);
CREATE INDEX IF NOT EXISTS idx_trials_guild_status ON trials(guild_id, status);
CREATE INDEX IF NOT EXISTS idx_trials_created_at ON trials(created_at DESC);
