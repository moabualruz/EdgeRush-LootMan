-- Guild Configuration System for Multi-Guild Support
-- V0013__add_guild_configuration_system.sql

-- Guild configurations with encrypted API keys and settings
CREATE TABLE guild_configurations (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL UNIQUE,  -- Unique guild identifier
    guild_name VARCHAR(255) NOT NULL,       -- Human-readable guild name
    guild_description TEXT,                 -- Optional description
    
    -- WoWAudit API Configuration (encrypted)
    wowaudit_api_key_encrypted TEXT,        -- Encrypted API key
    wowaudit_guild_uri VARCHAR(500),        -- Guild profile URI
    wowaudit_base_url VARCHAR(255) DEFAULT 'https://wowaudit.com',
    
    -- Sync Configuration
    sync_enabled BOOLEAN DEFAULT true,      -- Enable/disable sync for this guild
    sync_cron_expression VARCHAR(100) DEFAULT '0 0 4 * * *',  -- Cron for sync schedule
    sync_run_on_startup BOOLEAN DEFAULT false,
    last_sync_at TIMESTAMP WITH TIME ZONE, -- Last successful sync
    last_sync_status VARCHAR(50),           -- 'SUCCESS', 'FAILED', 'IN_PROGRESS'
    last_sync_error TEXT,                   -- Error message if failed
    
    -- Guild Settings
    timezone VARCHAR(50) DEFAULT 'UTC',     -- Guild timezone for scheduling
    is_active BOOLEAN DEFAULT true,         -- Active/inactive guild
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Perfect Score Benchmarking
    benchmark_mode VARCHAR(20) DEFAULT 'THEORETICAL',  -- 'THEORETICAL', 'TOP_PERFORMER', 'CUSTOM'
    custom_benchmark_rms DECIMAL(10, 6),    -- Custom RMS benchmark if using CUSTOM mode
    custom_benchmark_ipi DECIMAL(10, 6),    -- Custom IPI benchmark if using CUSTOM mode
    benchmark_updated_at TIMESTAMP WITH TIME ZONE
);

-- Encryption key storage (for API key encryption/decryption)
CREATE TABLE encryption_keys (
    id SERIAL PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    key_value_encrypted TEXT NOT NULL,      -- The encryption key itself, encrypted with master key
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN DEFAULT true
);

-- Guild data isolation - add guild_id to existing tables
-- This will be done in separate migration files to avoid conflicts

-- Indexes for performance
CREATE INDEX idx_guild_configurations_guild_id ON guild_configurations(guild_id);
CREATE INDEX idx_guild_configurations_active ON guild_configurations(is_active);
CREATE INDEX idx_guild_configurations_sync_enabled ON guild_configurations(sync_enabled);

-- Insert default encryption key (in production, this should be properly managed)
INSERT INTO encryption_keys (key_name, key_value_encrypted) VALUES 
('default_guild_encryption', 'PLACEHOLDER_ENCRYPTED_KEY_TO_BE_REPLACED_IN_PRODUCTION');

-- Sample guild configuration (can be removed in production)
INSERT INTO guild_configurations (
    guild_id, 
    guild_name, 
    guild_description,
    wowaudit_guild_uri,
    sync_enabled,
    benchmark_mode
) VALUES (
    'default',
    'Default Guild',
    'Default guild configuration for single-guild setups',
    '',  -- Will be populated from environment variables
    true,
    'THEORETICAL'
);

COMMENT ON TABLE guild_configurations IS 'Per-guild configuration including encrypted API keys and sync settings';
COMMENT ON TABLE encryption_keys IS 'Encryption keys for securing guild API credentials';
COMMENT ON COLUMN guild_configurations.wowaudit_api_key_encrypted IS 'WoWAudit API key encrypted using guild-specific encryption';
COMMENT ON COLUMN guild_configurations.benchmark_mode IS 'How to calculate perfect score: THEORETICAL (mathematical max), TOP_PERFORMER (best guild member), CUSTOM (manual values)';