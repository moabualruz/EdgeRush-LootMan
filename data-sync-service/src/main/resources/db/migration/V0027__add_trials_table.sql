-- Create trials table for tracking trial raiders
CREATE TABLE trials (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    raider_id BIGINT,
    guild_id VARCHAR(100) NOT NULL,

    -- Trial info
    status VARCHAR(50) DEFAULT 'ACTIVE',
    start_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    end_date TIMESTAMP WITH TIME ZONE,
    expected_end_date TIMESTAMP WITH TIME ZONE,

    -- Trial metrics
    raids_attended INTEGER DEFAULT 0,
    raids_required INTEGER DEFAULT 0,
    attendance_rate DECIMAL(5,4),
    average_performance DECIMAL(5,2),
    deaths_per_raid DECIMAL(5,2),

    -- Outcome
    outcome VARCHAR(50),
    outcome_reason TEXT,
    promoted_by VARCHAR(100),
    promoted_at TIMESTAMP WITH TIME ZONE,

    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_trial_application FOREIGN KEY (application_id)
        REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_trial_raider FOREIGN KEY (raider_id)
        REFERENCES raiders(id) ON DELETE SET NULL,
    CONSTRAINT fk_trial_guild FOREIGN KEY (guild_id)
        REFERENCES guilds(guild_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_trials_application ON trials(application_id);
CREATE INDEX idx_trials_raider ON trials(raider_id);
CREATE INDEX idx_trials_guild ON trials(guild_id);
CREATE INDEX idx_trials_status ON trials(status);
CREATE INDEX idx_trials_start_date ON trials(start_date DESC);
