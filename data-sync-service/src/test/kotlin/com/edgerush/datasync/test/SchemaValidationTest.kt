package com.edgerush.datasync.test

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

/**
 * Schema validation tests to catch entity-database mismatches early.
 *
 * These tests verify that expected tables and columns exist in the database,
 * preventing runtime errors from schema/entity misalignment.
 *
 * Run these tests after any migration changes to catch issues before deployment.
 */
class SchemaValidationTest : IntegrationTest() {

    // ============================================================================
    // TABLE EXISTENCE TESTS
    // ============================================================================

    @Test
    fun `all core tables should exist`() {
        val expectedTables = listOf(
            "characters",
            "raiders",
            "guild_configurations",
            "loot_awards",
            "attendance_stats",
            "users",
            "wow_classes",
            "wow_specializations",
            "flps_default_modifiers",
            "flps_guild_modifiers",
            "simulation_profiles",
            "simulation_requests",
            "simulation_results",
        )

        val actualTables = getTableNames()

        assertThat(actualTables).containsAll(expectedTables)
    }

    // ============================================================================
    // COLUMN VALIDATION TESTS
    // ============================================================================

    @Test
    fun `raiders table should have all required columns`() {
        val columns = getColumnNames("raiders")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "character_name",
                "realm",
                "region",
                "character_class",
                "spec",
                "role",
                "rank",
                "status",
                "guild_id",
                "wowaudit_id",
                "blizzard_id",
                "character_id",
                "last_sync",
            )
        )
    }

    @Test
    fun `guild_configurations table should have all required columns`() {
        val columns = getColumnNames("guild_configurations")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "guild_id",
                "guild_name",
                "guild_description",
                "wowaudit_api_key_encrypted",
                "wowaudit_guild_uri",
                "wowaudit_base_url",
                "sync_enabled",
                "sync_cron_expression",
                "sync_run_on_startup",
                "last_sync_at",
                "last_sync_status",
                "last_sync_error",
                "timezone",
                "is_active",
                "created_at",
                "updated_at",
                "benchmark_mode",
                "bnet_realm_slug",
                "bnet_guild_name_slug",
                "bnet_region",
                "bnet_sync_enabled",
            )
        )
    }

    @Test
    fun `users table should have all required columns`() {
        val columns = getColumnNames("users")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "username",
                "email",
                "password_hash",
                "avatar_url",
                "discord_id",
                "battlenet_id",
                "role",
                "guild_id",
                "is_active",
                "created_at",
                "last_login",
            )
        )
    }

    @Test
    fun `wow_classes table should have all required columns`() {
        val columns = getColumnNames("wow_classes")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "name",
                "slug",
                "color",
                "media_url",
                "power_type",
                "synced_at",
            )
        )
    }

    @Test
    fun `wow_specializations table should have all required columns`() {
        val columns = getColumnNames("wow_specializations")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "class_id",
                "name",
                "slug",
                "role",
                "media_url",
                "synced_at",
            )
        )
    }

    @Test
    fun `simulation_profiles table should have all required columns`() {
        val columns = getColumnNames("simulation_profiles")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "guild_id",
                "character_name",
                "character_realm",
                "profile_content",
                "created_at",
            )
        )
    }

    @Test
    fun `simulation_requests table should have all required columns`() {
        val columns = getColumnNames("simulation_requests")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "profile_id",
                "status",
                "iterations",
                "fight_length_seconds",
                "source",
                "external_id",
                "submitted_at",
                "completed_at",
                "error_message",
            )
        )
    }

    @Test
    fun `simulation_results table should have all required columns`() {
        val columns = getColumnNames("simulation_results")

        assertThat(columns).containsAll(
            listOf(
                "id",
                "profile_id",
                "item_id",
                "item_name",
                "slot",
                "dps_gain",
                "percent_gain",
                "simulated_at",
            )
        )
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private fun getTableNames(): List<String> {
        return jdbcTemplate.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
            String::class.java
        )
    }

    private fun getColumnNames(tableName: String): List<String> {
        return jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns WHERE table_name = ?",
            String::class.java,
            tableName
        )
    }
}
