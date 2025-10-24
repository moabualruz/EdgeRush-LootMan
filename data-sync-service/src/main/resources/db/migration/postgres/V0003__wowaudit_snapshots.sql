
CREATE TABLE IF NOT EXISTS wowaudit_snapshots (
    id SERIAL PRIMARY KEY,
    endpoint TEXT NOT NULL,
    raw_payload TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wowaudit_snapshots_endpoint ON wowaudit_snapshots (endpoint);
