ALTER TABLE team_metadata
    ADD COLUMN IF NOT EXISTS guild_name TEXT,
    ADD COLUMN IF NOT EXISTS url TEXT,
    ADD COLUMN IF NOT EXISTS last_refreshed_blizzard TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_refreshed_percentiles TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_refreshed_mythic_plus TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS wishlist_updated_at TIMESTAMPTZ;

CREATE TABLE IF NOT EXISTS team_raid_days (
    id SERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES team_metadata(team_id) ON DELETE CASCADE,
    week_day TEXT,
    start_time TIME,
    end_time TIME,
    current_instance TEXT,
    difficulty TEXT,
    active_from DATE,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_team_raid_days_team ON team_raid_days(team_id);

ALTER TABLE raid_signups
    ADD COLUMN IF NOT EXISTS character_guest BOOLEAN;
