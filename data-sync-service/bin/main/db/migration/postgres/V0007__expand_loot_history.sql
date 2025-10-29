ALTER TABLE loot_awards
    ADD COLUMN IF NOT EXISTS rclootcouncil_id TEXT,
    ADD COLUMN IF NOT EXISTS icon TEXT,
    ADD COLUMN IF NOT EXISTS slot TEXT,
    ADD COLUMN IF NOT EXISTS quality TEXT,
    ADD COLUMN IF NOT EXISTS response_type_id INTEGER,
    ADD COLUMN IF NOT EXISTS response_type_name TEXT,
    ADD COLUMN IF NOT EXISTS response_type_rgba TEXT,
    ADD COLUMN IF NOT EXISTS response_type_excluded BOOLEAN,
    ADD COLUMN IF NOT EXISTS propagated_response_type_id INTEGER,
    ADD COLUMN IF NOT EXISTS propagated_response_type_name TEXT,
    ADD COLUMN IF NOT EXISTS propagated_response_type_rgba TEXT,
    ADD COLUMN IF NOT EXISTS propagated_response_type_excluded BOOLEAN,
    ADD COLUMN IF NOT EXISTS same_response_amount INTEGER,
    ADD COLUMN IF NOT EXISTS note TEXT,
    ADD COLUMN IF NOT EXISTS wish_value INTEGER,
    ADD COLUMN IF NOT EXISTS difficulty TEXT,
    ADD COLUMN IF NOT EXISTS discarded BOOLEAN,
    ADD COLUMN IF NOT EXISTS character_id BIGINT,
    ADD COLUMN IF NOT EXISTS awarded_by_character_id BIGINT,
    ADD COLUMN IF NOT EXISTS awarded_by_name TEXT;

CREATE TABLE IF NOT EXISTS loot_award_bonus_ids (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    bonus_id TEXT
);

CREATE INDEX IF NOT EXISTS idx_loot_award_bonus_loot_award_id ON loot_award_bonus_ids(loot_award_id);

CREATE TABLE IF NOT EXISTS loot_award_old_items (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    item_id BIGINT,
    bonus_id TEXT
);

CREATE INDEX IF NOT EXISTS idx_loot_award_old_items_loot_award_id ON loot_award_old_items(loot_award_id);

CREATE TABLE IF NOT EXISTS loot_award_wish_data (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    spec_name TEXT,
    spec_icon TEXT,
    value INTEGER
);

CREATE INDEX IF NOT EXISTS idx_loot_award_wish_data_loot_award_id ON loot_award_wish_data(loot_award_id);
