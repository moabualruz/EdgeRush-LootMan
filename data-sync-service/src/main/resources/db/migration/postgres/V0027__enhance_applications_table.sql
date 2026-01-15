-- Enhanced applications table for the recruitment system with OAuth and auto-fetch support
-- This extends the existing applications table with additional fields for:
-- - OAuth identity (battle.net, discord, email)
-- - Auto-fetched character data (item level, raider.io score, warcraft logs parses)
-- - Recruitment-specific data (raid days, guild history)
-- - Review workflow (reviewer, review timestamp)

-- Add new columns for enhanced recruitment functionality
ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS guild_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS main_character_spec TEXT,
    ADD COLUMN IF NOT EXISTS item_level DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS raider_io_score DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS best_parse_average DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS location TEXT,
    ADD COLUMN IF NOT EXISTS timezone VARCHAR(100),
    ADD COLUMN IF NOT EXISTS raid_days_available TEXT, -- Stored as JSON array
    ADD COLUMN IF NOT EXISTS previous_guilds TEXT,
    ADD COLUMN IF NOT EXISTS reason_for_leaving TEXT,
    ADD COLUMN IF NOT EXISTS why_this_guild TEXT,
    ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- Set default values for existing rows
-- Note: V0019 renamed applied_at -> appliedAt and synced_at -> syncedAt
UPDATE applications
SET created_at = COALESCE("appliedAt", "syncedAt"),
    updated_at = COALESCE("appliedAt", "syncedAt")
WHERE created_at IS NULL;

-- Create indexes for common queries
-- Note: V0019 renamed discord_id -> discordId
CREATE INDEX IF NOT EXISTS idx_applications_guild_id ON applications(guild_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_discord_id ON applications("discordId");
CREATE INDEX IF NOT EXISTS idx_applications_battletag ON applications(battletag);
CREATE INDEX IF NOT EXISTS idx_applications_created_at ON applications(created_at);
