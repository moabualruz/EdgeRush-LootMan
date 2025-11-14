-- Raidbots Integration Tables
-- Stores Raidbots simulation requests, results, and configuration

-- Configuration table for guild-specific Raidbots settings
CREATE TABLE raidbots_config (
    guild_id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    encrypted_api_key TEXT,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Simulations table stores simulation requests and their status
CREATE TABLE raidbots_simulations (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    sim_id VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    profile TEXT NOT NULL,
    sim_options TEXT NOT NULL,
    FOREIGN KEY (guild_id) REFERENCES raidbots_config(guild_id)
);

-- Results table stores simulation results per item
CREATE TABLE raidbots_results (
    id BIGSERIAL PRIMARY KEY,
    simulation_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    dps_gain DOUBLE PRECISION NOT NULL,
    percent_gain DOUBLE PRECISION NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (simulation_id) REFERENCES raidbots_simulations(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_rb_sims_char ON raidbots_simulations(character_name, character_realm, status);
CREATE INDEX idx_rb_sims_status ON raidbots_simulations(status, submitted_at);
CREATE INDEX idx_rb_results_sim ON raidbots_results(simulation_id, item_id);
CREATE INDEX idx_rb_results_item ON raidbots_results(item_id, calculated_at DESC);
