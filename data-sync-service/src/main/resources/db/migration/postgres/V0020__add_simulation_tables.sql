-- Local SimulationCraft Integration Tables
-- Stores simulation profiles and results for local Docker-based simulations

-- Simulation profiles table stores SimC profile content per character
CREATE TABLE simulation_profiles (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    profile_content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(guild_id, character_name, character_realm)
);

-- Simulation requests table tracks simulation job lifecycle
CREATE TABLE simulation_requests (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id) ON DELETE CASCADE,
    iterations INTEGER NOT NULL DEFAULT 10000,
    fight_length_seconds INTEGER NOT NULL DEFAULT 300,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT
);

-- Simulation results table stores per-item DPS gains
CREATE TABLE simulation_results (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    dps_gain DOUBLE PRECISION NOT NULL,
    percent_gain DOUBLE PRECISION NOT NULL,
    simulated_at TIMESTAMP NOT NULL
);

-- Indexes for efficient queries
CREATE INDEX idx_sim_profiles_char ON simulation_profiles(guild_id, character_name, character_realm);
CREATE INDEX idx_sim_requests_status ON simulation_requests(status, submitted_at);
CREATE INDEX idx_sim_requests_profile ON simulation_requests(profile_id, status);
CREATE INDEX idx_sim_results_profile ON simulation_results(profile_id, item_id);
CREATE INDEX idx_sim_results_item ON simulation_results(item_id, simulated_at DESC);
