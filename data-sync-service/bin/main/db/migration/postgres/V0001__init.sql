CREATE TABLE IF NOT EXISTS raiders (
    id SERIAL PRIMARY KEY,
    character_name TEXT NOT NULL UNIQUE,
    realm TEXT NOT NULL,
    region TEXT NOT NULL,
    class TEXT NOT NULL,
    spec TEXT NOT NULL,
    role TEXT NOT NULL,
    last_sync TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS loot_awards (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL,
    item_name TEXT NOT NULL,
    tier TEXT NOT NULL,
    flps NUMERIC(10,4) NOT NULL,
    rdf NUMERIC(10,4) NOT NULL,
    awarded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sync_runs (
    id SERIAL PRIMARY KEY,
    source TEXT NOT NULL,
    status TEXT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    message TEXT
);

CREATE INDEX IF NOT EXISTS idx_loot_awards_raider_id ON loot_awards (raider_id);
CREATE INDEX IF NOT EXISTS idx_loot_awards_item_id ON loot_awards (item_id);
CREATE INDEX IF NOT EXISTS idx_sync_runs_source ON sync_runs (source);
