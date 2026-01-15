-- Create applications table for guild recruitment
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(100) NOT NULL,

    -- Applicant info
    discord_id VARCHAR(100),
    discord_username VARCHAR(100),
    battle_net_id VARCHAR(100),
    battle_net_tag VARCHAR(100),

    -- Character info
    character_name VARCHAR(100) NOT NULL,
    character_realm VARCHAR(100) NOT NULL,
    character_region VARCHAR(10) DEFAULT 'eu',
    character_class VARCHAR(50),
    character_spec VARCHAR(50),
    character_role VARCHAR(50),
    character_item_level INTEGER,

    -- Performance data (fetched from WCL/RIO)
    wcl_best_perf_avg DECIMAL(5,2),
    wcl_median_perf_avg DECIMAL(5,2),
    wcl_kills INTEGER DEFAULT 0,
    rio_score INTEGER,
    rio_mythic_plus_score INTEGER,
    current_progression VARCHAR(200),

    -- Application content
    about_me TEXT,
    raiding_experience TEXT,
    availability TEXT,
    why_join TEXT,
    additional_info TEXT,
    logs_url VARCHAR(500),

    -- Status
    status VARCHAR(50) DEFAULT 'PENDING',
    decision_reason TEXT,
    decided_by VARCHAR(100),
    decided_at TIMESTAMP WITH TIME ZONE,

    -- Metadata
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_application_guild FOREIGN KEY (guild_id)
        REFERENCES guilds(guild_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_applications_guild ON applications(guild_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_discord ON applications(discord_id);
CREATE INDEX idx_applications_submitted ON applications(submitted_at DESC);
CREATE INDEX idx_applications_character ON applications(character_name, character_realm);
