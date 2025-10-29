ALTER TABLE attendance_stats
    ADD COLUMN IF NOT EXISTS character_region TEXT,
    ADD COLUMN IF NOT EXISTS team_id BIGINT,
    ADD COLUMN IF NOT EXISTS season_id BIGINT,
    ADD COLUMN IF NOT EXISTS period_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_attendance_stats_team ON attendance_stats(team_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'historical_activity'
          AND column_name = 'period'
    ) THEN
        EXECUTE 'ALTER TABLE historical_activity RENAME COLUMN period TO period_id';
    END IF;
END $$;

ALTER TABLE historical_activity
    ADD COLUMN IF NOT EXISTS team_id BIGINT,
    ADD COLUMN IF NOT EXISTS season_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_historical_activity_team ON historical_activity(team_id);

ALTER TABLE raids
    ADD COLUMN IF NOT EXISTS team_id BIGINT,
    ADD COLUMN IF NOT EXISTS season_id BIGINT,
    ADD COLUMN IF NOT EXISTS period_id BIGINT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_raids_team ON raids(team_id);

ALTER TABLE raid_signups
    ADD COLUMN IF NOT EXISTS character_region TEXT;

ALTER TABLE wishlist_snapshots
    ADD COLUMN IF NOT EXISTS character_region TEXT,
    ADD COLUMN IF NOT EXISTS team_id BIGINT,
    ADD COLUMN IF NOT EXISTS season_id BIGINT,
    ADD COLUMN IF NOT EXISTS period_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_wishlist_snapshots_team ON wishlist_snapshots(team_id);
