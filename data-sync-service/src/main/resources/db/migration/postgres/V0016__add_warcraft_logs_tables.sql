-- Warcraft Logs Integration Tables
-- Stores Warcraft Logs reports, fights, performance data, and configuration

-- Configuration table for guild-specific Warcraft Logs settings
CREATE TABLE warcraft_logs_config (
    guild_id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    guild_name VARCHAR(255) NOT NULL,
    realm VARCHAR(255) NOT NULL,
    region VARCHAR(10) NOT NULL,
    encrypted_client_id TEXT,
    encrypted_client_secret TEXT,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255)
);

-- Reports table stores metadata about synced Warcraft Logs reports
CREATE TABLE warcraft_logs_reports (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    report_code VARCHAR(50) NOT NULL,
    title VARCHAR(500),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    owner VARCHAR(255),
    zone INT,
    synced_at TIMESTAMP NOT NULL,
    raw_metadata TEXT,
    UNIQUE(report_code),
    FOREIGN KEY (guild_id) REFERENCES warcraft_logs_config(guild_id)
);

-- Fights table stores individual encounter attempts from reports
CREATE TABLE warcraft_logs_fights (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    fight_id INT NOT NULL,
    encounter_id INT NOT NULL,
    encounter_name VARCHAR(255) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    kill BOOLEAN NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    boss_percentage DOUBLE PRECISION,
    UNIQUE(report_id, fight_id),
    FOREIGN KEY (report_id) REFERENCES warcraft_logs_reports(id) ON DELETE CASCADE
);

-- Performance table stores character performance metrics per fight
CREATE TABLE warcraft_logs_performance (
    id BIGSERIAL PRIMARY KEY,
    fight_id BIGINT NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(50) NOT NULL,
    character_spec VARCHAR(50) NOT NULL,
    deaths INT NOT NULL,
    damage_taken BIGINT NOT NULL,
    avoidable_damage_taken BIGINT NOT NULL,
    avoidable_damage_percentage DOUBLE PRECISION NOT NULL,
    performance_percentile DOUBLE PRECISION,
    item_level INT NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (fight_id) REFERENCES warcraft_logs_fights(id) ON DELETE CASCADE
);

-- Character mappings for handling name differences between systems
CREATE TABLE warcraft_logs_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    wowaudit_name VARCHAR(255) NOT NULL,
    wowaudit_realm VARCHAR(255) NOT NULL,
    warcraft_logs_name VARCHAR(255) NOT NULL,
    warcraft_logs_realm VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    UNIQUE(guild_id, wowaudit_name, wowaudit_realm),
    FOREIGN KEY (guild_id) REFERENCES warcraft_logs_config(guild_id)
);

-- Indexes for performance optimization
CREATE INDEX idx_wcl_reports_guild ON warcraft_logs_reports(guild_id, start_time DESC);
CREATE INDEX idx_wcl_reports_code ON warcraft_logs_reports(report_code);
CREATE INDEX idx_wcl_fights_report ON warcraft_logs_fights(report_id, encounter_id);
CREATE INDEX idx_wcl_fights_encounter ON warcraft_logs_fights(encounter_id, difficulty, kill);
CREATE INDEX idx_wcl_perf_char ON warcraft_logs_performance(character_name, character_realm, fight_id);
CREATE INDEX idx_wcl_perf_spec ON warcraft_logs_performance(character_spec, calculated_at);
CREATE INDEX idx_wcl_perf_fight ON warcraft_logs_performance(fight_id);
