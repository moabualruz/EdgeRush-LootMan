-- WoW Classes table (synced from Blizzard API)
CREATE TABLE IF NOT EXISTS wow_classes (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    media_url VARCHAR(500),
    power_type VARCHAR(50),
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- WoW Specializations table (synced from Blizzard API)
CREATE TABLE IF NOT EXISTS wow_specializations (
    id INTEGER PRIMARY KEY,
    class_id INTEGER NOT NULL REFERENCES wow_classes(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL, -- TANK, HEALER, DPS
    media_url VARCHAR(500),
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wow_specs_class_id ON wow_specializations(class_id);
CREATE INDEX IF NOT EXISTS idx_wow_specs_role ON wow_specializations(role);

-- Rename character_class to class_name for clarity
ALTER TABLE user_characters RENAME COLUMN character_class TO class_name;

-- Update user_characters to use dynamic class reference
ALTER TABLE user_characters
    ADD COLUMN IF NOT EXISTS class_id INTEGER REFERENCES wow_classes(id),
    ADD COLUMN IF NOT EXISTS spec_id INTEGER REFERENCES wow_specializations(id);

-- Make class_name nullable for backwards compatibility
ALTER TABLE user_characters ALTER COLUMN class_name DROP NOT NULL;
