package com.edgerush.datasync.test.util

import org.springframework.jdbc.core.JdbcTemplate

/**
 * Utility functions for database operations in tests.
 *
 * Provides helper methods for:
 * - Database cleanup
 * - Data verification
 * - Test data setup
 */
object DatabaseTestUtils {
    /**
     * Cleans all tables in the database except flyway_schema_history.
     *
     * This is useful for ensuring a clean state between tests.
     *
     * @param jdbcTemplate The JdbcTemplate to use for database operations
     */
    fun cleanAllTables(jdbcTemplate: JdbcTemplate) {
        val tables =
            jdbcTemplate.queryForList(
                """
            SELECT tablename FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename != 'flyway_schema_history'
            """,
                String::class.java,
            )

        tables.forEach { table ->
            jdbcTemplate.execute("TRUNCATE TABLE $table CASCADE")
        }
    }

    /**
     * Resets the sequence for a table's ID column.
     *
     * This ensures that auto-generated IDs start from 1 after cleanup.
     *
     * @param jdbcTemplate The JdbcTemplate to use
     * @param tableName The name of the table
     * @param sequenceName The name of the sequence (defaults to {tableName}_id_seq)
     */
    fun resetSequence(
        jdbcTemplate: JdbcTemplate,
        tableName: String,
        sequenceName: String = "${tableName}_id_seq",
    ) {
        jdbcTemplate.execute("ALTER SEQUENCE $sequenceName RESTART WITH 1")
    }

    /**
     * Counts the number of rows in a table.
     *
     * @param jdbcTemplate The JdbcTemplate to use
     * @param tableName The name of the table
     * @return The number of rows in the table
     */
    fun countRows(
        jdbcTemplate: JdbcTemplate,
        tableName: String,
    ): Int {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM $tableName",
            Int::class.java,
        ) ?: 0
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param jdbcTemplate The JdbcTemplate to use
     * @param tableName The name of the table
     * @return true if the table exists, false otherwise
     */
    fun tableExists(
        jdbcTemplate: JdbcTemplate,
        tableName: String,
    ): Boolean {
        val count =
            jdbcTemplate.queryForObject(
                """
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = ?
            """,
                Int::class.java,
                tableName,
            ) ?: 0
        return count > 0
    }

    /**
     * Executes a SQL script from a file.
     *
     * @param jdbcTemplate The JdbcTemplate to use
     * @param scriptPath The path to the SQL script file
     */
    fun executeSqlScript(
        jdbcTemplate: JdbcTemplate,
        scriptPath: String,
    ) {
        val script =
            DatabaseTestUtils::class.java.getResourceAsStream(scriptPath)
                ?.bufferedReader()
                ?.readText()
                ?: throw IllegalArgumentException("Script not found: $scriptPath")

        jdbcTemplate.execute(script)
    }
}
