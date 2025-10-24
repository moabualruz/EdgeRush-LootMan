CREATE TABLE IF NOT EXISTS attendance_stats (
    id SERIAL PRIMARY KEY,
    instance TEXT,
    encounter TEXT,
    start_date DATE,
    end_date DATE,
    character_id BIGINT,
    character_name TEXT NOT NULL,
    character_realm TEXT,
    character_class TEXT,
    character_role TEXT,
    attended_amount_of_raids INTEGER,
    total_amount_of_raids INTEGER,
    attended_percentage DOUBLE PRECISION,
    selected_amount_of_encounters INTEGER,
    total_amount_of_encounters INTEGER,
    selected_percentage DOUBLE PRECISION,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS raids (
    raid_id BIGINT PRIMARY KEY,
    date DATE,
    start_time TIME,
    end_time TIME,
    instance TEXT,
    difficulty TEXT,
    optional BOOLEAN,
    status TEXT,
    present_size INTEGER,
    total_size INTEGER,
    notes TEXT,
    selections_image TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS raid_signups (
    id SERIAL PRIMARY KEY,
    raid_id BIGINT NOT NULL REFERENCES raids(raid_id) ON DELETE CASCADE,
    character_id BIGINT,
    character_name TEXT,
    character_realm TEXT,
    character_class TEXT,
    character_role TEXT,
    status TEXT,
    comment TEXT,
    selected BOOLEAN
);

CREATE TABLE IF NOT EXISTS raid_encounters (
    id SERIAL PRIMARY KEY,
    raid_id BIGINT NOT NULL REFERENCES raids(raid_id) ON DELETE CASCADE,
    encounter_id BIGINT,
    name TEXT,
    enabled BOOLEAN,
    extra BOOLEAN,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS historical_activity (
    id SERIAL PRIMARY KEY,
    character_id BIGINT,
    character_name TEXT NOT NULL,
    character_realm TEXT,
    period BIGINT,
    data_json TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS guests (
    guest_id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    realm TEXT,
    class TEXT,
    role TEXT,
    blizzard_id BIGINT,
    tracking_since TIMESTAMPTZ,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS applications (
    application_id BIGINT PRIMARY KEY,
    applied_at TIMESTAMPTZ,
    status TEXT,
    role TEXT,
    age INTEGER,
    country TEXT,
    battletag TEXT,
    discord_id TEXT,
    main_character_name TEXT,
    main_character_realm TEXT,
    main_character_class TEXT,
    main_character_role TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS application_alts (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(application_id) ON DELETE CASCADE,
    name TEXT,
    realm TEXT,
    class TEXT,
    role TEXT,
    level INTEGER
);

CREATE TABLE IF NOT EXISTS application_questions (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(application_id) ON DELETE CASCADE,
    question TEXT,
    answer TEXT,
    files_json TEXT
);

CREATE INDEX IF NOT EXISTS idx_attendance_stats_character ON attendance_stats(character_name, character_realm);
CREATE INDEX IF NOT EXISTS idx_historical_activity_character ON historical_activity(character_name, character_realm);
