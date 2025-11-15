package com.edgerush.datasync.test.base

import com.edgerush.datasync.config.TestSecurityConfig
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests using Testcontainers.
 *
 * This class provides:
 * - PostgreSQL database via Testcontainers
 * - Spring Boot test context with random port
 * - TestRestTemplate for HTTP requests
 * - JdbcTemplate for database operations
 * - Automatic database cleanup between tests
 * - Test profile activation
 *
 * Usage:
 * ```kotlin
 * class MyControllerIntegrationTest : IntegrationTest() {
 *     @Test
 *     fun `should return 200 OK when getting resource`() {
 *         // Arrange
 *         val request = MyRequest(data = "test")
 *
 *         // Act
 *         val response = restTemplate.postForEntity(
 *             "/api/v1/resource",
 *             request,
 *             MyResponse::class.java
 *         )
 *
 *         // Assert
 *         response.statusCode shouldBe HttpStatus.OK
 *         response.body shouldNotBeNull()
 *     }
 * }
 * ```
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [com.edgerush.datasync.DataSyncApplication::class],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
    ],
)
@Import(TestSecurityConfig::class)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTest {
    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> =
            PostgreSQLContainer<Nothing>("postgres:18").apply {
                withDatabaseName("lootman_test")
                withUsername("test")
                withPassword("test")
                withReuse(true)
            }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.flyway.enabled") { "true" }
            registry.add("spring.flyway.clean-disabled") { "false" }
        }
    }

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        // Clean all tables except flyway_schema_history
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
}
