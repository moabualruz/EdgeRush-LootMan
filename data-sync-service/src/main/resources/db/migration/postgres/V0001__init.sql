-- V0001: Consolidated initial schema
-- All column names use snake_case for consistency

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- Characters table (authoritative source for WoW character identity)
CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    realm VARCHAR(100) NOT NULL,
    region VARCHAR(10) NOT NULL DEFAULT 'eu',
    character_class VARCHAR(50) NOT NULL,
    blizzard_id BIGINT UNIQUE,
    account_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_character_identity UNIQUE (name, realm, region)
);

CREATE INDEX idx_characters_name_realm ON characters(name, realm);
CREATE INDEX idx_characters_blizzard_id ON characters(blizzard_id) WHERE blizzard_id IS NOT NULL;

-- Raiders table (guild members with character reference)
CREATE TABLE raiders (
    id SERIAL PRIMARY KEY,
    character_name TEXT NOT NULL,
    realm TEXT NOT NULL,
    region TEXT NOT NULL DEFAULT 'eu',
    character_class TEXT NOT NULL,
    spec TEXT NOT NULL DEFAULT '',
    role TEXT NOT NULL,
    last_sync TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    wowaudit_id BIGINT UNIQUE,
    rank TEXT,
    status TEXT DEFAULT 'ACTIVE',
    note TEXT,
    blizzard_id BIGINT,
    tracking_since TIMESTAMPTZ,
    join_date TIMESTAMPTZ,
    blizzard_last_modified TIMESTAMPTZ,
    guild_id VARCHAR(255),
    character_id BIGINT REFERENCES characters(id),
    CONSTRAINT uq_raiders_character_realm UNIQUE (character_name, realm)
);

CREATE INDEX idx_raiders_guild_id ON raiders(guild_id);
CREATE INDEX idx_raiders_character_id ON raiders(character_id);

-- ============================================================================
-- RAIDER SUB-TABLES
-- ============================================================================

CREATE TABLE raider_statistics (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    mythic_plus_score DOUBLE PRECISION,
    weekly_highest_mplus INTEGER,
    season_highest_mplus INTEGER,
    world_quests_total INTEGER,
    world_quests_this_week INTEGER,
    collectibles_mounts INTEGER,
    collectibles_toys INTEGER,
    collectibles_unique_pets INTEGER,
    collectibles_level_25_pets INTEGER,
    honor_level INTEGER,
    CONSTRAINT uq_raider_statistics UNIQUE (raider_id)
);

CREATE TABLE raider_gear_items (
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
    CONSTRAINT uq_raider_gear UNIQUE (raider_id, gear_set, slot)
);

CREATE TABLE raider_track_items (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    tier TEXT NOT NULL,
    item_count INTEGER,
    CONSTRAINT uq_raider_track_item UNIQUE (raider_id, tier)
);

CREATE TABLE raider_crest_counts (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    crest_type TEXT NOT NULL,
    crest_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_raider_crest UNIQUE (raider_id, crest_type)
);

CREATE TABLE raider_pvp_bracket_stats (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    bracket TEXT NOT NULL,
    rating INTEGER,
    season_played INTEGER,
    week_played INTEGER,
    max_rating INTEGER,
    CONSTRAINT uq_raider_pvp_bracket UNIQUE (raider_id, bracket)
);

CREATE TABLE raider_raid_progress (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    raid TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    bosses_defeated INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_raider_raid_progress UNIQUE (raider_id, raid, difficulty)
);

CREATE TABLE raider_renown (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    faction TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_raider_renown UNIQUE (raider_id, faction)
);

CREATE TABLE raider_warcraft_logs (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    difficulty TEXT NOT NULL,
    score INTEGER,
    CONSTRAINT uq_raider_wcl UNIQUE (raider_id, difficulty)
);

CREATE TABLE raider_vault_slots (
    id BIGSERIAL PRIMARY KEY,
    raider_id BIGINT NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    slot VARCHAR(100) NOT NULL,
    unlocked BOOLEAN DEFAULT FALSE,
    CONSTRAINT uq_raider_vault UNIQUE (raider_id, slot)
);

-- ============================================================================
-- LOOT TABLES
-- ============================================================================

CREATE TABLE loot_awards (
    id SERIAL PRIMARY KEY,
    raider_id INTEGER NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL,
    item_name TEXT NOT NULL,
    tier TEXT NOT NULL,
    flps NUMERIC(10,4) NOT NULL,
    rdf NUMERIC(10,4) NOT NULL,
    awarded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    discarded BOOLEAN DEFAULT FALSE,
    character_id BIGINT,
    awarded_by_character_id BIGINT,
    awarded_by_name TEXT,
    rclootcouncil_id TEXT,
    icon TEXT,
    slot TEXT,
    quality TEXT,
    response_type_id INTEGER,
    response_type_name TEXT,
    response_type_rgba TEXT,
    response_type_excluded BOOLEAN,
    propagated_response_type_id INTEGER,
    propagated_response_type_name TEXT,
    propagated_response_type_rgba TEXT,
    propagated_response_type_excluded BOOLEAN,
    same_response_amount INTEGER,
    note TEXT,
    wish_value INTEGER,
    difficulty TEXT
);

CREATE INDEX idx_loot_awards_raider_id ON loot_awards(raider_id);
CREATE INDEX idx_loot_awards_item_id ON loot_awards(item_id);
CREATE INDEX idx_loot_awards_awarded_at ON loot_awards(awarded_at);

CREATE TABLE loot_award_bonus_ids (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    bonus_id TEXT
);

CREATE TABLE loot_award_old_items (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    item_id BIGINT,
    bonus_id TEXT
);

CREATE TABLE loot_award_wish_data (
    id SERIAL PRIMARY KEY,
    loot_award_id INTEGER NOT NULL REFERENCES loot_awards(id) ON DELETE CASCADE,
    spec_name TEXT,
    spec_icon TEXT,
    value INTEGER
);

CREATE TABLE loot_bans (
    id VARCHAR(255) PRIMARY KEY,
    raider_id VARCHAR(255) NOT NULL,
    guild_id VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    banned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_loot_bans_guild ON loot_bans(guild_id, is_active, expires_at);
CREATE INDEX idx_loot_bans_raider ON loot_bans(raider_id, guild_id, is_active);

-- ============================================================================
-- WISHLIST TABLES
-- ============================================================================

CREATE TABLE wishlist_items (
    id SERIAL PRIMARY KEY,
    raider_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name TEXT NOT NULL,
    priority INTEGER NOT NULL,
    upgrade_percentage DOUBLE PRECISION NOT NULL,
    spec_name TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wishlist_item UNIQUE (raider_id, item_id)
);

CREATE INDEX idx_wishlist_items_raider ON wishlist_items(raider_id);
CREATE INDEX idx_wishlist_items_item ON wishlist_items(item_id);

CREATE TABLE wishlist_snapshots (
    id SERIAL PRIMARY KEY,
    raider_id BIGINT,
    character_name TEXT NOT NULL,
    character_realm TEXT NOT NULL,
    character_region TEXT,
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    raw_payload TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- ATTENDANCE TABLES
-- ============================================================================

CREATE TABLE attendance_stats (
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
    character_region TEXT,
    attended_amount_of_raids INTEGER,
    total_amount_of_raids INTEGER,
    attended_percentage DOUBLE PRECISION,
    selected_amount_of_encounters INTEGER,
    total_amount_of_encounters INTEGER,
    selected_percentage DOUBLE PRECISION,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    characters_fk_id BIGINT,
    account_id BIGINT
);

-- ============================================================================
-- RAID TABLES
-- ============================================================================

CREATE TABLE raids (
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
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE raid_signups (
    id SERIAL PRIMARY KEY,
    raid_id BIGINT NOT NULL,
    character_id BIGINT,
    character_name TEXT,
    character_realm TEXT,
    character_region TEXT,
    character_class TEXT,
    character_role TEXT,
    character_guest BOOLEAN,
    status TEXT,
    comment TEXT,
    selected BOOLEAN DEFAULT FALSE
);

CREATE TABLE raid_encounters (
    id SERIAL PRIMARY KEY,
    raid_id BIGINT NOT NULL,
    encounter_id BIGINT,
    name TEXT,
    enabled BOOLEAN,
    extra BOOLEAN,
    notes TEXT
);

-- ============================================================================
-- GUILD CONFIGURATION TABLES
-- ============================================================================

CREATE TABLE guild_configurations (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL UNIQUE,
    guild_name TEXT,
    guild_description TEXT,
    wowaudit_api_key_encrypted TEXT,
    wowaudit_guild_uri TEXT,
    wowaudit_base_url TEXT DEFAULT 'https://wowaudit.com',
    sync_enabled BOOLEAN DEFAULT TRUE,
    sync_cron_expression TEXT DEFAULT '0 */2 * * *',
    sync_run_on_startup BOOLEAN DEFAULT FALSE,
    last_sync_at TIMESTAMPTZ,
    last_sync_status TEXT,
    last_sync_error TEXT,
    timezone TEXT DEFAULT 'UTC',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    benchmark_mode TEXT DEFAULT 'PERCENTILE_BASED',
    custom_benchmark_rms DOUBLE PRECISION,
    custom_benchmark_ipi DOUBLE PRECISION,
    benchmark_updated_at TIMESTAMPTZ,
    attendance_aggregation_mode VARCHAR(20) NOT NULL DEFAULT 'CHARACTER',
    attendance_aggregation_scope VARCHAR(20) NOT NULL DEFAULT 'GUILD',
    bnet_client_id_encrypted TEXT,
    bnet_client_secret_encrypted TEXT,
    bnet_realm_slug TEXT,
    bnet_guild_name_slug TEXT,
    bnet_region TEXT DEFAULT 'eu',
    bnet_last_sync_at TIMESTAMPTZ,
    bnet_last_sync_status TEXT,
    bnet_last_sync_error TEXT,
    bnet_sync_enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE guild_permissions (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    rank_name VARCHAR(255) NOT NULL,
    permission_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_guild_rank_permission UNIQUE (guild_id, rank_name, permission_type)
);

-- ============================================================================
-- TEAM METADATA TABLES
-- ============================================================================

CREATE TABLE team_metadata (
    team_id BIGINT PRIMARY KEY,
    guild_id BIGINT,
    guild_name TEXT,
    name TEXT,
    region TEXT,
    realm TEXT,
    url TEXT,
    last_refreshed_blizzard TIMESTAMPTZ,
    last_refreshed_percentiles TIMESTAMPTZ,
    last_refreshed_mythic_plus TIMESTAMPTZ,
    wishlist_updated_at TIMESTAMPTZ,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE team_raid_days (
    id SERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    week_day TEXT,
    start_time TIME,
    end_time TIME,
    current_instance TEXT,
    difficulty TEXT,
    active_from DATE,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE period_snapshots (
    id SERIAL PRIMARY KEY,
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    current_period BIGINT,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_period_snapshot UNIQUE (team_id, season_id, period_id)
);

-- ============================================================================
-- FLPS CONFIGURATION TABLES
-- ============================================================================

CREATE TABLE flps_default_modifiers (
    id SERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    modifier_key VARCHAR(100) NOT NULL,
    modifier_value DECIMAL NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_flps_default UNIQUE (category, modifier_key)
);

CREATE TABLE flps_guild_modifiers (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    modifier_key VARCHAR(100) NOT NULL,
    modifier_value DECIMAL NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_flps_guild UNIQUE (guild_id, category, modifier_key)
);

-- ============================================================================
-- WARCRAFT LOGS TABLES
-- ============================================================================

CREATE TABLE warcraft_logs_config (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL UNIQUE,
    encrypted_client_id TEXT,
    encrypted_client_secret TEXT,
    config_json TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(255)
);

CREATE TABLE warcraft_logs_reports (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    report_code TEXT NOT NULL UNIQUE,
    title TEXT,
    owner TEXT,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    zone_id INTEGER,
    zone_name TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE warcraft_logs_fights (
    id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL REFERENCES warcraft_logs_reports(id) ON DELETE CASCADE,
    fight_id INTEGER NOT NULL,
    encounter_id INTEGER,
    encounter_name TEXT,
    difficulty INTEGER,
    kill BOOLEAN DEFAULT FALSE,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    boss_percentage DOUBLE PRECISION,
    CONSTRAINT uq_wcl_fight UNIQUE (report_id, fight_id)
);

CREATE TABLE warcraft_logs_performance (
    id SERIAL PRIMARY KEY,
    fight_id INTEGER NOT NULL REFERENCES warcraft_logs_fights(id) ON DELETE CASCADE,
    character_name TEXT NOT NULL,
    character_realm TEXT,
    character_class TEXT,
    spec TEXT,
    deaths INTEGER DEFAULT 0,
    avoidable_damage_percentage DOUBLE PRECISION,
    parse_percent DOUBLE PRECISION,
    ilvl_parse_percent DOUBLE PRECISION,
    dps DOUBLE PRECISION,
    hps DOUBLE PRECISION
);

CREATE INDEX idx_wcl_perf_fight ON warcraft_logs_performance(fight_id);
CREATE INDEX idx_wcl_perf_char ON warcraft_logs_performance(character_name, character_realm);

CREATE TABLE warcraft_logs_character_mappings (
    id SERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    wowaudit_name VARCHAR(255) NOT NULL,
    wowaudit_realm VARCHAR(255) NOT NULL,
    warcraft_logs_name VARCHAR(255) NOT NULL,
    warcraft_logs_realm VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    CONSTRAINT uq_wcl_mapping UNIQUE (guild_id, wowaudit_name, wowaudit_realm)
);

-- ============================================================================
-- APPLICATION TABLES
-- ============================================================================

CREATE TABLE applications (
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
    main_character_race TEXT,
    main_character_faction TEXT,
    main_character_level INTEGER,
    main_character_region TEXT,
    main_character_spec TEXT,
    item_level DOUBLE PRECISION,
    raider_io_score DOUBLE PRECISION,
    best_parse_average DOUBLE PRECISION,
    email VARCHAR(255),
    location TEXT,
    timezone VARCHAR(100),
    raid_days_available TEXT,
    previous_guilds TEXT,
    reason_for_leaving TEXT,
    why_this_guild TEXT,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMPTZ,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    guild_id VARCHAR(255)
);

CREATE TABLE application_alts (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    name TEXT,
    realm TEXT,
    class TEXT,
    role TEXT,
    level INTEGER,
    region TEXT,
    faction TEXT,
    race TEXT
);

CREATE TABLE application_questions (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    question TEXT,
    answer TEXT,
    files_json TEXT,
    position INTEGER
);

CREATE TABLE application_question_files (
    id SERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    question_position INTEGER,
    question TEXT,
    original_filename TEXT,
    url TEXT
);

CREATE TABLE enhanced_applications (
    enhanced_application_id VARCHAR(255) PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    battle_net_id VARCHAR(255) NOT NULL,
    discord_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    item_level DOUBLE PRECISION NOT NULL,
    raider_io_score DOUBLE PRECISION,
    best_parse_average DOUBLE PRECISION,
    age INTEGER NOT NULL,
    location VARCHAR(255) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    raid_days_available TEXT NOT NULL,
    previous_guilds TEXT NOT NULL,
    reason_for_leaving TEXT NOT NULL,
    why_this_guild TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE recruitment_comments (
    id BIGSERIAL PRIMARY KEY,
    application_id VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE trials (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    character_region VARCHAR(50) DEFAULT 'eu',
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    assigned_mentor_id BIGINT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

-- ============================================================================
-- USER/AUTH TABLES
-- ============================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(255),
    avatar_url TEXT,
    blizzard_id BIGINT UNIQUE,
    battlenet_id VARCHAR(255) UNIQUE,
    discord_id VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    guild_id VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMPTZ,
    last_login TIMESTAMPTZ
);

CREATE TABLE user_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    active_character_mapping_id BIGINT,
    last_guild_id VARCHAR(255),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_preferences_user UNIQUE (user_id)
);

CREATE TABLE user_characters (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    character_name VARCHAR(255) NOT NULL,
    realm VARCHAR(255) NOT NULL,
    class_name VARCHAR(100),
    class_id INTEGER,
    spec_id INTEGER,
    level INTEGER NOT NULL DEFAULT 1,
    playable_race VARCHAR(100) NOT NULL DEFAULT 'Unknown',
    faction VARCHAR(50) NOT NULL DEFAULT 'Unknown',
    blizzard_id BIGINT,
    guild_name VARCHAR(255),
    guild_realm VARCHAR(255),
    guild_id VARCHAR(255),
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_character UNIQUE (user_id, character_name, realm)
);

CREATE INDEX idx_user_characters_user_id ON user_characters(user_id);
CREATE INDEX idx_user_characters_blizzard_id ON user_characters(blizzard_id) WHERE blizzard_id IS NOT NULL;

CREATE TABLE user_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    raider_id BIGINT REFERENCES raiders(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    CONSTRAINT uq_user_raider_mapping UNIQUE (user_id, raider_id)
);

CREATE INDEX idx_user_character_mappings_user_id ON user_character_mappings(user_id);
CREATE INDEX idx_user_character_mappings_raider_id ON user_character_mappings(raider_id);

CREATE TABLE discord_user_links (
    id BIGSERIAL PRIMARY KEY,
    discord_user_id VARCHAR(255) NOT NULL,
    raider_id BIGINT NOT NULL REFERENCES raiders(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    linked_by VARCHAR(255),
    CONSTRAINT uq_discord_user_link UNIQUE (discord_user_id, raider_id)
);

CREATE TABLE discord_notification_configs (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    discord_server_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    mention_role_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_discord_notif UNIQUE (guild_id, discord_server_id, notification_type)
);

-- ============================================================================
-- SIMULATION TABLES
-- ============================================================================

CREATE TABLE simulation_profiles (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    profile_content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_simulation_profile UNIQUE (guild_id, character_name, character_realm)
);

CREATE TABLE simulation_requests (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    simulation_type VARCHAR(50),
    iterations INTEGER DEFAULT 10000,
    fight_length_seconds INTEGER DEFAULT 300,
    options_json JSONB,
    source VARCHAR(50) DEFAULT 'LOCAL',
    external_id VARCHAR(255),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_message TEXT
);

CREATE TABLE simulation_results (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id) ON DELETE CASCADE,
    request_id BIGINT REFERENCES simulation_requests(id) ON DELETE SET NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    dps_gain DOUBLE PRECISION,
    percent_gain DOUBLE PRECISION,
    dps DOUBLE PRECISION,
    dps_error DOUBLE PRECISION,
    result_json JSONB,
    html_report TEXT,
    simulated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- RAID PLAN TABLES
-- ============================================================================

CREATE TABLE raid_plans (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    encounter_id INTEGER,
    encounter_name VARCHAR(255),
    difficulty VARCHAR(50),
    is_template BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE raid_plan_steps (
    id BIGSERIAL PRIMARY KEY,
    raid_plan_id BIGINT NOT NULL REFERENCES raid_plans(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL,
    title VARCHAR(255),
    description TEXT,
    timestamp_seconds INTEGER,
    boss_health_percent DOUBLE PRECISION,
    assignments_json JSONB
);

CREATE TABLE raid_plan_markers (
    id BIGSERIAL PRIMARY KEY,
    raid_plan_id BIGINT NOT NULL REFERENCES raid_plans(id) ON DELETE CASCADE,
    marker_type VARCHAR(50) NOT NULL,
    x_position DOUBLE PRECISION NOT NULL,
    y_position DOUBLE PRECISION NOT NULL,
    label VARCHAR(255),
    color VARCHAR(50),
    step_id BIGINT REFERENCES raid_plan_steps(id) ON DELETE SET NULL
);

CREATE TABLE raid_plan_shapes (
    id BIGSERIAL PRIMARY KEY,
    raid_plan_id BIGINT NOT NULL REFERENCES raid_plans(id) ON DELETE CASCADE,
    shape_type VARCHAR(50) NOT NULL,
    points_json JSONB NOT NULL,
    color VARCHAR(50),
    fill_color VARCHAR(50),
    label VARCHAR(255),
    step_id BIGINT REFERENCES raid_plan_steps(id) ON DELETE SET NULL
);

-- ============================================================================
-- MISCELLANEOUS TABLES
-- ============================================================================

CREATE TABLE behavioral_actions (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('DEDUCTION', 'RESTORATION')),
    deduction_amount NUMERIC(3,2) NOT NULL CHECK (deduction_amount >= 0.0 AND deduction_amount <= 1.0),
    reason TEXT NOT NULL,
    applied_by VARCHAR(255) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    operation VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    is_admin_mode BOOLEAN NOT NULL DEFAULT FALSE,
    request_id VARCHAR(255),
    user_id VARCHAR(255)
);

CREATE TABLE character_history (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255),
    character_region VARCHAR(255),
    team_id BIGINT,
    season_id BIGINT,
    period_id BIGINT,
    history_json TEXT,
    best_gear_json TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE guests (
    guest_id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    realm TEXT,
    class TEXT,
    role TEXT,
    blizzard_id BIGINT,
    tracking_since TIMESTAMPTZ,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE historical_activity (
    id SERIAL PRIMARY KEY,
    character_id BIGINT,
    character_name TEXT NOT NULL,
    character_realm TEXT,
    period_id BIGINT,
    team_id BIGINT,
    season_id BIGINT,
    data_json TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sync_runs (
    id SERIAL PRIMARY KEY,
    source TEXT NOT NULL,
    status TEXT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    message TEXT
);

CREATE INDEX idx_sync_runs_source ON sync_runs(source);

CREATE TABLE wowaudit_snapshots (
    id SERIAL PRIMARY KEY,
    endpoint TEXT NOT NULL,
    raw_payload TEXT NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE encryption_keys (
    id SERIAL PRIMARY KEY,
    key_name VARCHAR(255) NOT NULL UNIQUE,
    key_value_encrypted TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE wow_classes (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50),
    color VARCHAR(7),
    media_url TEXT,
    power_type VARCHAR(50),
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE wow_specializations (
    id INTEGER PRIMARY KEY,
    class_id INTEGER NOT NULL REFERENCES wow_classes(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    media_url TEXT,
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_wow_spec UNIQUE (class_id, name)
);

-- ============================================================================
-- INSERT DEFAULT DATA
-- ============================================================================

-- Insert default FLPS modifiers
INSERT INTO flps_default_modifiers (category, modifier_key, modifier_value, description) VALUES
    ('RMS', 'attendance_weight', 0.40, 'Weight for attendance component in RMS'),
    ('RMS', 'performance_weight', 0.35, 'Weight for performance component in RMS'),
    ('RMS', 'tenure_weight', 0.15, 'Weight for tenure component in RMS'),
    ('RMS', 'behavioral_weight', 0.10, 'Weight for behavioral component in RMS'),
    ('RMS', 'perfect_attendance_threshold', 0.95, 'Threshold for perfect attendance bonus'),
    ('RMS', 'attendance_decay_weeks', 8, 'Number of weeks for attendance decay'),
    ('IPI', 'wishlist_weight', 0.40, 'Weight for wishlist in IPI'),
    ('IPI', 'upgrade_weight', 0.35, 'Weight for upgrade value in IPI'),
    ('IPI', 'role_priority_weight', 0.15, 'Weight for role priority in IPI'),
    ('IPI', 'tier_completion_weight', 0.10, 'Weight for tier set completion in IPI'),
    ('RDF', 'decay_rate', 0.10, 'Weekly decay rate for RDF'),
    ('RDF', 'max_decay_weeks', 12, 'Maximum weeks before full decay'),
    ('FLPS', 'rms_weight', 0.40, 'Weight for RMS in final FLPS'),
    ('FLPS', 'ipi_weight', 0.40, 'Weight for IPI in final FLPS'),
    ('FLPS', 'rdf_weight', 0.20, 'Weight for RDF in final FLPS');

-- Insert WoW classes (with official Blizzard class IDs)
INSERT INTO wow_classes (id, name, slug, color, power_type) VALUES
    (1, 'Warrior', 'warrior', '#C69B6D', 'rage'),
    (2, 'Paladin', 'paladin', '#F48CBA', 'mana'),
    (3, 'Hunter', 'hunter', '#AAD372', 'focus'),
    (4, 'Rogue', 'rogue', '#FFF468', 'energy'),
    (5, 'Priest', 'priest', '#FFFFFF', 'mana'),
    (6, 'Death Knight', 'death-knight', '#C41E3A', 'runic-power'),
    (7, 'Shaman', 'shaman', '#0070DD', 'mana'),
    (8, 'Mage', 'mage', '#3FC7EB', 'mana'),
    (9, 'Warlock', 'warlock', '#8788EE', 'mana'),
    (10, 'Monk', 'monk', '#00FF98', 'energy'),
    (11, 'Druid', 'druid', '#FF7C0A', 'mana'),
    (12, 'Demon Hunter', 'demon-hunter', '#A330C9', 'fury'),
    (13, 'Evoker', 'evoker', '#33937F', 'mana');

-- Insert WoW specializations (with official Blizzard spec IDs)
INSERT INTO wow_specializations (id, class_id, name, slug, role) VALUES
    -- Warrior (class 1)
    (71, 1, 'Arms', 'arms', 'DPS'),
    (72, 1, 'Fury', 'fury', 'DPS'),
    (73, 1, 'Protection', 'protection', 'TANK'),
    -- Paladin (class 2)
    (65, 2, 'Holy', 'holy', 'HEALER'),
    (66, 2, 'Protection', 'protection', 'TANK'),
    (70, 2, 'Retribution', 'retribution', 'DPS'),
    -- Hunter (class 3)
    (253, 3, 'Beast Mastery', 'beast-mastery', 'DPS'),
    (254, 3, 'Marksmanship', 'marksmanship', 'DPS'),
    (255, 3, 'Survival', 'survival', 'DPS'),
    -- Rogue (class 4)
    (259, 4, 'Assassination', 'assassination', 'DPS'),
    (260, 4, 'Outlaw', 'outlaw', 'DPS'),
    (261, 4, 'Subtlety', 'subtlety', 'DPS'),
    -- Priest (class 5)
    (256, 5, 'Discipline', 'discipline', 'HEALER'),
    (257, 5, 'Holy', 'holy', 'HEALER'),
    (258, 5, 'Shadow', 'shadow', 'DPS'),
    -- Death Knight (class 6)
    (250, 6, 'Blood', 'blood', 'TANK'),
    (251, 6, 'Frost', 'frost', 'DPS'),
    (252, 6, 'Unholy', 'unholy', 'DPS'),
    -- Shaman (class 7)
    (262, 7, 'Elemental', 'elemental', 'DPS'),
    (263, 7, 'Enhancement', 'enhancement', 'DPS'),
    (264, 7, 'Restoration', 'restoration', 'HEALER'),
    -- Mage (class 8)
    (62, 8, 'Arcane', 'arcane', 'DPS'),
    (63, 8, 'Fire', 'fire', 'DPS'),
    (64, 8, 'Frost', 'frost', 'DPS'),
    -- Warlock (class 9)
    (265, 9, 'Affliction', 'affliction', 'DPS'),
    (266, 9, 'Demonology', 'demonology', 'DPS'),
    (267, 9, 'Destruction', 'destruction', 'DPS'),
    -- Monk (class 10)
    (268, 10, 'Brewmaster', 'brewmaster', 'TANK'),
    (270, 10, 'Mistweaver', 'mistweaver', 'HEALER'),
    (269, 10, 'Windwalker', 'windwalker', 'DPS'),
    -- Druid (class 11)
    (102, 11, 'Balance', 'balance', 'DPS'),
    (103, 11, 'Feral', 'feral', 'DPS'),
    (104, 11, 'Guardian', 'guardian', 'TANK'),
    (105, 11, 'Restoration', 'restoration', 'HEALER'),
    -- Demon Hunter (class 12)
    (577, 12, 'Havoc', 'havoc', 'DPS'),
    (581, 12, 'Vengeance', 'vengeance', 'TANK'),
    -- Evoker (class 13)
    (1467, 13, 'Devastation', 'devastation', 'DPS'),
    (1468, 13, 'Preservation', 'preservation', 'HEALER'),
    (1473, 13, 'Augmentation', 'augmentation', 'DPS');
