-- Migration to make tables JPA-compatible
-- Renames snake_case columns to camelCase for Spring Data JDBC conventions
-- Uses a helper function to safely rename columns only if source exists and target doesn't

-- Helper function to safely rename column if it exists
CREATE OR REPLACE FUNCTION safe_rename_column(
    p_table TEXT,
    p_old_column TEXT,
    p_new_column TEXT
) RETURNS VOID AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = p_table AND column_name = p_old_column
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = p_table AND column_name = p_new_column
    ) THEN
        EXECUTE format('ALTER TABLE %I RENAME COLUMN %I TO %I', p_table, p_old_column, p_new_column);
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Raiders table
-- V0001 uses: character_name, class, realm, region, spec, role, last_sync
-- V0005 adds: wowaudit_id, join_date
SELECT safe_rename_column('raiders', 'character_name', 'characterName');
SELECT safe_rename_column('raiders', 'class', 'characterClass');
SELECT safe_rename_column('raiders', 'last_sync', 'lastSync');
SELECT safe_rename_column('raiders', 'wowaudit_id', 'wowauditId');
SELECT safe_rename_column('raiders', 'join_date', 'joinDate');
SELECT safe_rename_column('raiders', 'blizzard_id', 'blizzardId');
SELECT safe_rename_column('raiders', 'tracking_since', 'trackingSince');
SELECT safe_rename_column('raiders', 'blizzard_last_modified', 'blizzardLastModified');

-- Attendance stats (V0004)
SELECT safe_rename_column('attendance_stats', 'character_id', 'characterId');
SELECT safe_rename_column('attendance_stats', 'character_name', 'characterName');
SELECT safe_rename_column('attendance_stats', 'character_realm', 'characterRealm');
SELECT safe_rename_column('attendance_stats', 'character_class', 'characterClass');
SELECT safe_rename_column('attendance_stats', 'character_role', 'characterRole');
SELECT safe_rename_column('attendance_stats', 'start_date', 'startDate');
SELECT safe_rename_column('attendance_stats', 'end_date', 'endDate');
SELECT safe_rename_column('attendance_stats', 'attended_amount_of_raids', 'attendedAmountOfRaids');
SELECT safe_rename_column('attendance_stats', 'total_amount_of_raids', 'totalAmountOfRaids');
SELECT safe_rename_column('attendance_stats', 'attended_percentage', 'attendedPercentage');
SELECT safe_rename_column('attendance_stats', 'selected_amount_of_encounters', 'selectedAmountOfEncounters');
SELECT safe_rename_column('attendance_stats', 'total_amount_of_encounters', 'totalAmountOfEncounters');
SELECT safe_rename_column('attendance_stats', 'selected_percentage', 'selectedPercentage');
SELECT safe_rename_column('attendance_stats', 'synced_at', 'syncedAt');
SELECT safe_rename_column('attendance_stats', 'team_id', 'teamId');
SELECT safe_rename_column('attendance_stats', 'season_id', 'seasonId');
SELECT safe_rename_column('attendance_stats', 'period_id', 'periodId');

-- Loot awards (V0001, V0007)
SELECT safe_rename_column('loot_awards', 'raider_id', 'raiderId');
SELECT safe_rename_column('loot_awards', 'item_id', 'itemId');
SELECT safe_rename_column('loot_awards', 'item_name', 'itemName');
SELECT safe_rename_column('loot_awards', 'item_level', 'itemLevel');
SELECT safe_rename_column('loot_awards', 'awarded_at', 'awardedAt');
SELECT safe_rename_column('loot_awards', 'character_name', 'characterName');
SELECT safe_rename_column('loot_awards', 'character_realm', 'characterRealm');
SELECT safe_rename_column('loot_awards', 'boss_name', 'bossName');
SELECT safe_rename_column('loot_awards', 'difficulty_id', 'difficultyId');
SELECT safe_rename_column('loot_awards', 'instance_id', 'instanceId');
SELECT safe_rename_column('loot_awards', 'synced_at', 'syncedAt');

-- Raider gear items (V0005)
SELECT safe_rename_column('raider_gear_items', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_gear_items', 'gear_set', 'gearSet');
SELECT safe_rename_column('raider_gear_items', 'item_id', 'itemId');
SELECT safe_rename_column('raider_gear_items', 'item_level', 'itemLevel');
SELECT safe_rename_column('raider_gear_items', 'upgrade_level', 'upgradeLevel');
SELECT safe_rename_column('raider_gear_items', 'enchant_quality', 'enchantQuality');

-- Loot bans (V0015)
SELECT safe_rename_column('loot_bans', 'raider_id', 'raiderId');
SELECT safe_rename_column('loot_bans', 'banned_at', 'bannedAt');
SELECT safe_rename_column('loot_bans', 'expires_at', 'expiresAt');
SELECT safe_rename_column('loot_bans', 'banned_by', 'bannedBy');

-- Behavioral actions (V0014)
SELECT safe_rename_column('behavioral_actions', 'raider_id', 'raiderId');
SELECT safe_rename_column('behavioral_actions', 'action_type', 'actionType');
SELECT safe_rename_column('behavioral_actions', 'modifier_value', 'modifierValue');
SELECT safe_rename_column('behavioral_actions', 'start_date', 'startDate');
SELECT safe_rename_column('behavioral_actions', 'end_date', 'endDate');
SELECT safe_rename_column('behavioral_actions', 'created_by', 'createdBy');
SELECT safe_rename_column('behavioral_actions', 'created_at', 'createdAt');

-- Sync runs (V0001)
SELECT safe_rename_column('sync_runs', 'started_at', 'startedAt');
SELECT safe_rename_column('sync_runs', 'completed_at', 'completedAt');

-- Historical activity (V0004)
SELECT safe_rename_column('historical_activity', 'character_id', 'characterId');
SELECT safe_rename_column('historical_activity', 'character_name', 'characterName');
SELECT safe_rename_column('historical_activity', 'character_realm', 'characterRealm');
SELECT safe_rename_column('historical_activity', 'data_json', 'dataJson');
SELECT safe_rename_column('historical_activity', 'synced_at', 'syncedAt');

-- Guests (V0004)
SELECT safe_rename_column('guests', 'guest_id', 'guestId');
SELECT safe_rename_column('guests', 'blizzard_id', 'blizzardId');
SELECT safe_rename_column('guests', 'tracking_since', 'trackingSince');
SELECT safe_rename_column('guests', 'synced_at', 'syncedAt');

-- Raids (V0004)
SELECT safe_rename_column('raids', 'raid_id', 'raidId');
SELECT safe_rename_column('raids', 'start_time', 'startTime');
SELECT safe_rename_column('raids', 'end_time', 'endTime');
SELECT safe_rename_column('raids', 'present_size', 'presentSize');
SELECT safe_rename_column('raids', 'total_size', 'totalSize');
SELECT safe_rename_column('raids', 'selections_image', 'selectionsImage');
SELECT safe_rename_column('raids', 'synced_at', 'syncedAt');

-- Raid signups (V0004)
SELECT safe_rename_column('raid_signups', 'raid_id', 'raidId');
SELECT safe_rename_column('raid_signups', 'character_id', 'characterId');
SELECT safe_rename_column('raid_signups', 'character_name', 'characterName');
SELECT safe_rename_column('raid_signups', 'character_realm', 'characterRealm');
SELECT safe_rename_column('raid_signups', 'character_class', 'characterClass');
SELECT safe_rename_column('raid_signups', 'character_role', 'characterRole');

-- Raid encounters (V0004)
SELECT safe_rename_column('raid_encounters', 'raid_id', 'raidId');
SELECT safe_rename_column('raid_encounters', 'encounter_id', 'encounterId');

-- Applications (V0004)
SELECT safe_rename_column('applications', 'application_id', 'applicationId');
SELECT safe_rename_column('applications', 'applied_at', 'appliedAt');
SELECT safe_rename_column('applications', 'discord_id', 'discordId');
SELECT safe_rename_column('applications', 'main_character_name', 'mainCharacterName');
SELECT safe_rename_column('applications', 'main_character_realm', 'mainCharacterRealm');
SELECT safe_rename_column('applications', 'main_character_class', 'mainCharacterClass');
SELECT safe_rename_column('applications', 'main_character_role', 'mainCharacterRole');
SELECT safe_rename_column('applications', 'synced_at', 'syncedAt');

-- Application alts (V0004)
SELECT safe_rename_column('application_alts', 'application_id', 'applicationId');

-- Application questions (V0004)
SELECT safe_rename_column('application_questions', 'application_id', 'applicationId');
SELECT safe_rename_column('application_questions', 'files_json', 'filesJson');

-- Raider statistics (V0005)
SELECT safe_rename_column('raider_statistics', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_statistics', 'mythic_plus_score', 'mythicPlusScore');
SELECT safe_rename_column('raider_statistics', 'weekly_highest_mplus', 'weeklyHighestMplus');
SELECT safe_rename_column('raider_statistics', 'season_highest_mplus', 'seasonHighestMplus');
SELECT safe_rename_column('raider_statistics', 'world_quests_total', 'worldQuestsTotal');
SELECT safe_rename_column('raider_statistics', 'world_quests_this_week', 'worldQuestsThisWeek');
SELECT safe_rename_column('raider_statistics', 'collectibles_mounts', 'collectiblesMounts');
SELECT safe_rename_column('raider_statistics', 'collectibles_toys', 'collectiblesToys');
SELECT safe_rename_column('raider_statistics', 'collectibles_unique_pets', 'collectiblesUniquePets');
SELECT safe_rename_column('raider_statistics', 'collectibles_level_25_pets', 'collectiblesLevel25Pets');
SELECT safe_rename_column('raider_statistics', 'honor_level', 'honorLevel');

-- Raider warcraft logs (V0005)
SELECT safe_rename_column('raider_warcraft_logs', 'raider_id', 'raiderId');

-- Raider track items (V0005)
SELECT safe_rename_column('raider_track_items', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_track_items', 'item_count', 'itemCount');

-- Raider crest counts (V0005)
SELECT safe_rename_column('raider_crest_counts', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_crest_counts', 'crest_type', 'crestType');
SELECT safe_rename_column('raider_crest_counts', 'crest_count', 'crestCount');

-- Raider vault slots (V0005)
SELECT safe_rename_column('raider_vault_slots', 'raider_id', 'raiderId');

-- Raider renown (V0005)
SELECT safe_rename_column('raider_renown', 'raider_id', 'raiderId');

-- Raider raid progress (V0005)
SELECT safe_rename_column('raider_raid_progress', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_raid_progress', 'bosses_defeated', 'bossesDefeated');

-- Raider pvp bracket stats (V0005)
SELECT safe_rename_column('raider_pvp_bracket_stats', 'raider_id', 'raiderId');
SELECT safe_rename_column('raider_pvp_bracket_stats', 'season_played', 'seasonPlayed');
SELECT safe_rename_column('raider_pvp_bracket_stats', 'week_played', 'weekPlayed');
SELECT safe_rename_column('raider_pvp_bracket_stats', 'max_rating', 'maxRating');

-- Character history (V0011)
SELECT safe_rename_column('character_history', 'raider_id', 'raiderId');
SELECT safe_rename_column('character_history', 'changed_at', 'changedAt');
SELECT safe_rename_column('character_history', 'previous_name', 'previousName');
SELECT safe_rename_column('character_history', 'new_name', 'newName');
SELECT safe_rename_column('character_history', 'previous_realm', 'previousRealm');
SELECT safe_rename_column('character_history', 'new_realm', 'newRealm');
SELECT safe_rename_column('character_history', 'previous_class', 'previousClass');
SELECT safe_rename_column('character_history', 'new_class', 'newClass');

-- FLPS configuration tables (V0012)
SELECT safe_rename_column('flps_weights', 'created_at', 'createdAt');
SELECT safe_rename_column('flps_weights', 'updated_at', 'updatedAt');
SELECT safe_rename_column('flps_weights', 'rms_weight', 'rmsWeight');
SELECT safe_rename_column('flps_weights', 'ipi_weight', 'ipiWeight');
SELECT safe_rename_column('flps_weights', 'rdf_weight', 'rdfWeight');

SELECT safe_rename_column('role_multipliers', 'created_at', 'createdAt');
SELECT safe_rename_column('role_multipliers', 'updated_at', 'updatedAt');
SELECT safe_rename_column('role_multipliers', 'tank_multiplier', 'tankMultiplier');
SELECT safe_rename_column('role_multipliers', 'healer_multiplier', 'healerMultiplier');
SELECT safe_rename_column('role_multipliers', 'dps_multiplier', 'dpsMultiplier');

SELECT safe_rename_column('tier_set_bonuses', 'created_at', 'createdAt');
SELECT safe_rename_column('tier_set_bonuses', 'updated_at', 'updatedAt');
SELECT safe_rename_column('tier_set_bonuses', 'four_piece_bonus', 'fourPieceBonus');
SELECT safe_rename_column('tier_set_bonuses', 'two_piece_bonus', 'twoPieceBonus');

SELECT safe_rename_column('eligibility_thresholds', 'created_at', 'createdAt');
SELECT safe_rename_column('eligibility_thresholds', 'updated_at', 'updatedAt');
SELECT safe_rename_column('eligibility_thresholds', 'min_attendance_percentage', 'minAttendancePercentage');
SELECT safe_rename_column('eligibility_thresholds', 'min_raids_attended', 'minRaidsAttended');
SELECT safe_rename_column('eligibility_thresholds', 'max_loot_ban_duration_days', 'maxLootBanDurationDays');

-- Guild configuration (V0013)
SELECT safe_rename_column('guild_configurations', 'guild_id', 'guildId');
SELECT safe_rename_column('guild_configurations', 'config_key', 'configKey');
SELECT safe_rename_column('guild_configurations', 'config_value', 'configValue');
SELECT safe_rename_column('guild_configurations', 'value_type', 'valueType');
SELECT safe_rename_column('guild_configurations', 'created_at', 'createdAt');
SELECT safe_rename_column('guild_configurations', 'updated_at', 'updatedAt');

-- Warcraft Logs tables (V0016)
SELECT safe_rename_column('warcraft_logs_config', 'guild_id', 'guildId');
SELECT safe_rename_column('warcraft_logs_config', 'wcl_guild_id', 'wclGuildId');
SELECT safe_rename_column('warcraft_logs_config', 'wcl_server_slug', 'wclServerSlug');
SELECT safe_rename_column('warcraft_logs_config', 'wcl_server_region', 'wclServerRegion');
SELECT safe_rename_column('warcraft_logs_config', 'sync_enabled', 'syncEnabled');
SELECT safe_rename_column('warcraft_logs_config', 'last_sync_at', 'lastSyncAt');
SELECT safe_rename_column('warcraft_logs_config', 'created_at', 'createdAt');
SELECT safe_rename_column('warcraft_logs_config', 'updated_at', 'updatedAt');

SELECT safe_rename_column('warcraft_logs_oauth', 'guild_id', 'guildId');
SELECT safe_rename_column('warcraft_logs_oauth', 'access_token', 'accessToken');
SELECT safe_rename_column('warcraft_logs_oauth', 'refresh_token', 'refreshToken');
SELECT safe_rename_column('warcraft_logs_oauth', 'token_type', 'tokenType');
SELECT safe_rename_column('warcraft_logs_oauth', 'expires_at', 'expiresAt');
SELECT safe_rename_column('warcraft_logs_oauth', 'created_at', 'createdAt');
SELECT safe_rename_column('warcraft_logs_oauth', 'updated_at', 'updatedAt');

SELECT safe_rename_column('warcraft_logs_reports', 'report_id', 'reportId');
SELECT safe_rename_column('warcraft_logs_reports', 'guild_id', 'guildId');
SELECT safe_rename_column('warcraft_logs_reports', 'start_time', 'startTime');
SELECT safe_rename_column('warcraft_logs_reports', 'end_time', 'endTime');
SELECT safe_rename_column('warcraft_logs_reports', 'synced_at', 'syncedAt');

SELECT safe_rename_column('warcraft_logs_fights', 'report_id', 'reportId');
SELECT safe_rename_column('warcraft_logs_fights', 'fight_id', 'fightId');
SELECT safe_rename_column('warcraft_logs_fights', 'boss_id', 'bossId');
SELECT safe_rename_column('warcraft_logs_fights', 'start_time', 'startTime');
SELECT safe_rename_column('warcraft_logs_fights', 'end_time', 'endTime');

SELECT safe_rename_column('warcraft_logs_performances', 'report_id', 'reportId');
SELECT safe_rename_column('warcraft_logs_performances', 'fight_id', 'fightId');
SELECT safe_rename_column('warcraft_logs_performances', 'character_name', 'characterName');
SELECT safe_rename_column('warcraft_logs_performances', 'character_realm', 'characterRealm');
SELECT safe_rename_column('warcraft_logs_performances', 'spec_name', 'specName');

-- Audit logs (V0018)
SELECT safe_rename_column('audit_logs', 'user_id', 'userId');
SELECT safe_rename_column('audit_logs', 'target_type', 'targetType');
SELECT safe_rename_column('audit_logs', 'target_id', 'targetId');
SELECT safe_rename_column('audit_logs', 'old_values', 'oldValues');
SELECT safe_rename_column('audit_logs', 'new_values', 'newValues');
SELECT safe_rename_column('audit_logs', 'created_at', 'createdAt');
SELECT safe_rename_column('audit_logs', 'ip_address', 'ipAddress');

-- Drop the helper function
DROP FUNCTION IF EXISTS safe_rename_column(TEXT, TEXT, TEXT);
