package com.edgerush.datasync.test.examples

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.datasync.test.builders.TestDataFactory
import com.edgerush.datasync.test.util.DatabaseTestUtils
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Example integration test demonstrating database and API testing.
 *
 * This example shows:
 * - How to extend IntegrationTest base class
 * - How to use TestDataFactory for creating test data
 * - How to use DatabaseTestUtils for database operations
 * - How to test REST endpoints with TestRestTemplate
 * - How to verify database state
 */
class IntegrationTestExample : IntegrationTest() {
    @Test
    fun `should demonstrate database operations`() {
        // Arrange - Verify database is clean
        val initialCount = DatabaseTestUtils.countRows(jdbcTemplate, "raiders")
        initialCount shouldBe 0

        // Act - Insert test data using JdbcTemplate
        val raider =
            TestDataFactory.createTestRaider(
                characterName = "TestRaider",
            )

        jdbcTemplate.update(
            """
            INSERT INTO raiders (character_name, realm, region, class, spec, role, rank, status, last_sync)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            raider.characterName,
            raider.realm,
            raider.region,
            raider.clazz,
            raider.spec,
            raider.role,
            raider.rank,
            raider.status,
            raider.lastSync,
        )

        // Assert - Verify data was inserted
        val finalCount = DatabaseTestUtils.countRows(jdbcTemplate, "raiders")
        finalCount shouldBe 1

        // Verify we can query the data
        val name =
            jdbcTemplate.queryForObject(
                "SELECT character_name FROM raiders WHERE character_name = ?",
                String::class.java,
                "TestRaider",
            )
        name shouldBe "TestRaider"
    }

    @Test
    fun `should demonstrate API testing with health endpoint`() {
        // Arrange - No setup needed for health check

        // Act - Call the health endpoint
        val response =
            restTemplate.getForEntity(
                "/actuator/health",
                Map::class.java,
            )

        // Assert - Verify response
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!["status"] shouldBe "UP"
    }

    @Test
    fun `should demonstrate test data factory usage`() {
        // Arrange - Create multiple test entities
        val raider1 = TestDataFactory.createTestRaider(characterName = "Raider1")
        val raider2 = TestDataFactory.createTestRaider(characterName = "Raider2")
        val raid = TestDataFactory.createTestRaid()
        val behavioralAction = TestDataFactory.createTestBehavioralAction()

        // Act - Verify entities have different IDs and names
        raider1.characterName shouldNotBe raider2.characterName

        // Assert - Verify default values are set correctly
        raider1.realm shouldBe "TestRealm"
        raider2.realm shouldBe "TestRealm"
        raid.instance shouldBe "Nerub-ar Palace"
        behavioralAction.guildId shouldBe "test-guild"
    }
}
