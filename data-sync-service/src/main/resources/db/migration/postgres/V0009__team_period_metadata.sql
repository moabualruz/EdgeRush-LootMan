CREATE TABLE IF NOT EXISTS team_metadata (
    team_id BIGINT PRIMARY KEY,
    guild_id BIGINT,
    name TEXT,
    region TEXT,
    realm TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS period_snapshots (
    id SERIAL PRIMARY KEY,
    team_id BIGINT REFERENCES team_metadata(team_id) ON DELETE CASCADE,
    season_id BIGINT,
    period_id BIGINT,
    current_period BIGINT,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_period_snapshots_team_period ON period_snapshots(team_id, period_id);
CREATE INDEX IF NOT EXISTS idx_period_snapshots_team ON period_snapshots(team_id);
