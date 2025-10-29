ALTER TABLE raiders
    DROP CONSTRAINT IF EXISTS raiders_character_name_key,
    ADD COLUMN IF NOT EXISTS wowaudit_id BIGINT,
    ADD COLUMN IF NOT EXISTS rank TEXT,
    ADD COLUMN IF NOT EXISTS status TEXT,
    ADD COLUMN IF NOT EXISTS note TEXT,
    ADD COLUMN IF NOT EXISTS blizzard_id BIGINT,
    ADD COLUMN IF NOT EXISTS tracking_since TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS join_date TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS blizzard_last_modified TIMESTAMPTZ;

CREATE UNIQUE INDEX IF NOT EXISTS uq_raiders_wowaudit_id ON raiders(wowaudit_id) WHERE wowaudit_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_raiders_character_realm ON raiders(character_name, realm);

CREATE TABLE IF NOT EXISTS raider_statistics (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE UNIQUE,
    mythic_plus_score DOUBLE PRECISION,
    weekly_highest_mplus INTEGER,
    season_highest_mplus INTEGER,
    world_quests_total INTEGER,
    world_quests_this_week INTEGER,
    collectibles_mounts INTEGER,
    collectibles_toys INTEGER,
    collectibles_unique_pets INTEGER,
    collectibles_level_25_pets INTEGER,
    honor_level INTEGER
);

CREATE TABLE IF NOT EXISTS raider_warcraft_logs (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    difficulty TEXT NOT NULL,
    score INTEGER,
    UNIQUE (raider_id, difficulty)
);

CREATE TABLE IF NOT EXISTS raider_track_items (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    tier TEXT NOT NULL,
    item_count INTEGER,
    UNIQUE (raider_id, tier)
);

CREATE TABLE IF NOT EXISTS raider_crest_counts (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    crest_type TEXT NOT NULL,
    crest_count INTEGER,
    UNIQUE (raider_id, crest_type)
);

CREATE TABLE IF NOT EXISTS raider_vault_slots (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    slot TEXT NOT NULL,
    unlocked BOOLEAN,
    UNIQUE (raider_id, slot)
);

CREATE TABLE IF NOT EXISTS raider_renown (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    faction TEXT NOT NULL,
    level INTEGER,
    UNIQUE (raider_id, faction)
);

CREATE TABLE IF NOT EXISTS raider_raid_progress (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    raid TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    bosses_defeated INTEGER,
    UNIQUE (raider_id, raid, difficulty)
);

CREATE TABLE IF NOT EXISTS raider_pvp_bracket_stats (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    bracket TEXT NOT NULL,
    rating INTEGER,
    season_played INTEGER,
    week_played INTEGER,
    max_rating INTEGER,
    UNIQUE (raider_id, bracket)
);

CREATE TABLE IF NOT EXISTS raider_gear_items (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    gear_set TEXT NOT NULL,
    slot TEXT NOT NULL,
    item_id BIGINT,
    item_level INTEGER,
    quality INTEGER,
    enchant TEXT,
    enchant_quality INTEGER,
    upgrade_level INTEGER,
    sockets INTEGER,
    name TEXT,
    UNIQUE (raider_id, gear_set, slot)
);
