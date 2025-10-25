CREATE TABLE IF NOT EXISTS wowaudit_roster_members_raw (
    character_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_applications_raw (
    application_id BIGINT PRIMARY KEY,
    summary_json JSONB,
    detail_json JSONB,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_raids_raw (
    raid_id BIGINT PRIMARY KEY,
    summary_json JSONB,
    detail_json JSONB,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_wishlists_raw (
    character_id BIGINT PRIMARY KEY,
    summary_json JSONB,
    detail_json JSONB,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_loot_history_raw (
    season_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_attendance_raw (
    id INTEGER PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_historical_data_raw (
    period_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_guests_raw (
    guest_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_team_raw (
    team_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wowaudit_period_raw (
    period_id BIGINT PRIMARY KEY,
    payload JSONB NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
