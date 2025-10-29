CREATE TABLE IF NOT EXISTS wishlist_snapshots (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER REFERENCES raiders(id) ON DELETE SET NULL,
    character_name TEXT NOT NULL,
    character_realm TEXT NOT NULL,
    raw_payload TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wishlist_snapshots_character ON wishlist_snapshots (character_name, character_realm);
