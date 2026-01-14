package com.edgerush.lootman.api.contract

import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Contract tests for OpenAPI specification.
 *
 * These tests verify that the generated OpenAPI specification:
 * - Is accessible via the expected endpoint
 * - Contains required API metadata
 * - Documents all expected controllers and endpoints
 * - Includes security scheme definitions
 * - Contains proper server URLs
 */
class OpenApiContractTest : IntegrationTest() {

    @Nested
    inner class OpenApiSpecAvailability {

        @Test
        fun `should return OpenAPI spec from v3 api-docs endpoint`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                String::class.java,
            )

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body!! shouldContain "openapi"
        }

        @Test
        fun `should return OpenAPI spec in JSON format`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            val body = response.body!!
            body["openapi"] shouldNotBe null
            body["info"] shouldNotBe null
            body["paths"] shouldNotBe null
        }
    }

    @Nested
    inner class ApiMetadata {

        @Test
        fun `should have correct API title`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val info = response.body!!["info"] as Map<*, *>
            info["title"] shouldBe "EdgeRush LootMan API"
        }

        @Test
        fun `should have API version`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val info = response.body!!["info"] as Map<*, *>
            info["version"] shouldNotBe null
        }

        @Test
        fun `should have API description`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val info = response.body!!["info"] as Map<*, *>
            info["description"] shouldNotBe null
            (info["description"] as String) shouldContain "FLPS"
        }
    }

    @Nested
    inner class SecuritySchemes {

        @Test
        fun `should define bearer authentication scheme`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val components = response.body!!["components"] as Map<*, *>
            val securitySchemes = components["securitySchemes"] as Map<*, *>
            securitySchemes["bearerAuth"] shouldNotBe null

            val bearerAuth = securitySchemes["bearerAuth"] as Map<*, *>
            bearerAuth["type"] shouldBe "http"
            bearerAuth["scheme"] shouldBe "bearer"
            bearerAuth["bearerFormat"] shouldBe "JWT"
        }
    }

    @Nested
    inner class ServerConfiguration {

        @Test
        fun `should include server URLs`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val servers = response.body!!["servers"] as List<*>
            servers.shouldNotBeEmpty()

            val serverUrls = servers.map { (it as Map<*, *>)["url"] }
            serverUrls shouldContain "http://localhost:8080"
        }
    }

    @Nested
    inner class PathDocumentation {

        @Test
        fun `should document raider endpoints`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val paths = response.body!!["paths"] as Map<*, *>
            val raiderPaths = paths.keys.filter { (it as String).contains("raider") }
            raiderPaths.shouldNotBeEmpty()
        }

        @Test
        fun `should document loot endpoints`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val paths = response.body!!["paths"] as Map<*, *>
            val lootPaths = paths.keys.filter { (it as String).contains("loot") }
            lootPaths.shouldNotBeEmpty()
        }

        @Test
        fun `should document FLPS endpoints`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val paths = response.body!!["paths"] as Map<*, *>
            val flpsPaths = paths.keys.filter { (it as String).contains("flps") }
            flpsPaths.shouldNotBeEmpty()
        }

        @Test
        fun `should document attendance endpoints`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert
            val paths = response.body!!["paths"] as Map<*, *>
            val attendancePaths = paths.keys.filter { (it as String).contains("attendance") }
            attendancePaths.shouldNotBeEmpty()
        }

        @Test
        fun `should have significant number of documented paths`() {
            // Act
            val response = restTemplate.getForEntity(
                "/v3/api-docs",
                Map::class.java,
            )

            // Assert - we have 44 controllers, should have many paths
            val paths = response.body!!["paths"] as Map<*, *>
            paths.size shouldNotBe 0
            // Expect at least 50 paths given 44 controllers with CRUD operations
            (paths.size >= 50) shouldBe true
        }
    }

    @Nested
    inner class SwaggerUiAvailability {

        @Test
        fun `should have Swagger UI available`() {
            // Act
            val response = restTemplate.getForEntity(
                "/swagger-ui.html",
                String::class.java,
            )

            // Assert - Should redirect or return HTML
            (response.statusCode == HttpStatus.OK ||
                response.statusCode == HttpStatus.FOUND ||
                response.statusCode == HttpStatus.MOVED_PERMANENTLY) shouldBe true
        }
    }
}
