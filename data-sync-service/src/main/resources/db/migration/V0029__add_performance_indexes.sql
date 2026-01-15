-- V0029: Performance optimization indexes
-- Adds indexes identified through query analysis for improved performance

-- ===========================================
-- HIGH PRIORITY: Foreign Key Join Optimization
-- ===========================================

-- Raid signups - heavily used in raid roster queries
CREATE INDEX IF NOT EXISTS idx_raid_signups_raid_id ON raid_signups(raid_id);

-- Raid encounters - used when querying loot drops by raid
CREATE INDEX IF NOT EXISTS idx_raid_encounters_raid_id ON raid_encounters(raid_id);

-- Application question files - used in application detail views
CREATE INDEX IF NOT EXISTS idx_application_question_files_app_id ON application_question_files(application_id);

-- ===========================================
-- HIGH PRIORITY: WHERE Clause Filtering
-- ===========================================

-- Raid signup status filtering (accepted, declined, tentative)
CREATE INDEX IF NOT EXISTS idx_raid_signups_status ON raid_signups(status);
CREATE INDEX IF NOT EXISTS idx_raid_signups_selected ON raid_signups(selected);
CREATE INDEX IF NOT EXISTS idx_raid_signups_character ON raid_signups(character_name, character_realm);

-- Period/Season filtering for snapshots
CREATE INDEX IF NOT EXISTS idx_period_snapshots_season ON period_snapshots(season_id);
CREATE INDEX IF NOT EXISTS idx_period_snapshots_period ON period_snapshots(period_id);

-- Raider detail table indexes (all have raider_id FK)
CREATE INDEX IF NOT EXISTS idx_raider_warcraft_logs_raider ON raider_warcraft_logs(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_track_items_raider ON raider_track_items(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_crest_counts_raider ON raider_crest_counts(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_vault_slots_raider ON raider_vault_slots(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_renown_raider ON raider_renown(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_raid_progress_raider ON raider_raid_progress(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_pvp_bracket_stats_raider ON raider_pvp_bracket_stats(raider_id);
CREATE INDEX IF NOT EXISTS idx_raider_gear_items_raider ON raider_gear_items(raider_id);

-- ===========================================
-- MEDIUM PRIORITY: Composite Indexes for Common Query Patterns
-- ===========================================

-- Loot award pagination by guild (most common query pattern)
CREATE INDEX IF NOT EXISTS idx_loot_awards_guild_date ON loot_awards(guild_id, awarded_at DESC);

-- Loot award pagination by raider (raider loot history)
CREATE INDEX IF NOT EXISTS idx_loot_awards_raider_date ON loot_awards(raider_id, awarded_at DESC);

-- Raid signup filtering by raid + status (roster management)
CREATE INDEX IF NOT EXISTS idx_raid_signups_raid_status ON raid_signups(raid_id, status);
CREATE INDEX IF NOT EXISTS idx_raid_signups_raid_selected ON raid_signups(raid_id, selected);

-- Warcraft Logs performance filtering
CREATE INDEX IF NOT EXISTS idx_wcl_perf_fight_class ON warcraft_logs_performance(fight_id, character_class);

-- ===========================================
-- MEDIUM PRIORITY: ORDER BY Optimization
-- ===========================================

-- Raid list ordering (dashboard, raid list page)
CREATE INDEX IF NOT EXISTS idx_raids_date ON raids(date DESC);

-- Signup ordering
CREATE INDEX IF NOT EXISTS idx_raid_signups_created_at ON raid_signups(created_at DESC);

-- ===========================================
-- LOW PRIORITY: Less Frequent Query Patterns
-- ===========================================

-- Multi-guild queries
CREATE INDEX IF NOT EXISTS idx_team_metadata_guild ON team_metadata(guild_id);

-- Raid filtering by instance type
CREATE INDEX IF NOT EXISTS idx_raids_instance_difficulty ON raids(instance, difficulty);

-- Wishlist history lookups
CREATE INDEX IF NOT EXISTS idx_wishlist_snapshots_raider ON wishlist_snapshots(raider_id);

-- Character history tracking
CREATE INDEX IF NOT EXISTS idx_character_history_raider ON character_history(raider_id);

-- Active encryption key lookup
CREATE INDEX IF NOT EXISTS idx_encryption_keys_active ON encryption_keys(is_active);

-- ===========================================
-- ANALYZE tables to update statistics
-- ===========================================
ANALYZE raid_signups;
ANALYZE raid_encounters;
ANALYZE loot_awards;
ANALYZE raiders;
ANALYZE raids;
