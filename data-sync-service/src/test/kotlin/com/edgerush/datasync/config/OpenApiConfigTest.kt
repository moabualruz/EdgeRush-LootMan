package com.edgerush.datasync.config

import com.edgerush.datasync.security.AdminModeConfig
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.swagger.v3.oas.models.security.SecurityScheme
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for OpenApiConfig.
 *
 * Tests OpenAPI specification generation and configuration including
 * security schemes, API info, and admin mode behavior.
 */
class OpenApiConfigTest : UnitTest() {
    @MockK
    private lateinit var adminModeConfig: AdminModeConfig

    private lateinit var openApiConfig: OpenApiConfig

    @BeforeEach
    fun setUp() {
        openApiConfig = OpenApiConfig(adminModeConfig)
    }

    @Nested
    inner class CustomOpenAPI {
        @Test
        fun `should create OpenAPI with correct title`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.title shouldBe "EdgeRush LootMan API"
        }

        @Test
        fun `should create OpenAPI with correct version`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.version shouldBe "1.0.0"
        }

        @Test
        fun `should create OpenAPI with contact information`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.contact.shouldNotBeNull()
            openAPI.info.contact.name shouldBe "EdgeRush Team"
            openAPI.info.contact.email shouldBe "support@edgerush.com"
        }

        @Test
        fun `should configure bearer authentication security scheme`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            val securityScheme = openAPI.components.securitySchemes["bearerAuth"]
            securityScheme.shouldNotBeNull()
            securityScheme.type shouldBe SecurityScheme.Type.HTTP
            securityScheme.scheme shouldBe "bearer"
            securityScheme.bearerFormat shouldBe "JWT"
            securityScheme.`in` shouldBe SecurityScheme.In.HEADER
            securityScheme.name shouldBe "Authorization"
        }

        @Test
        fun `should add security requirement to OpenAPI`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.security.shouldNotBeNull()
            openAPI.security shouldHaveSize 1
            openAPI.security[0].containsKey("bearerAuth") shouldBe true
        }

        @Test
        fun `should configure two server URLs`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.servers shouldHaveSize 2
        }

        @Test
        fun `should configure local development server`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            val localServer = openAPI.servers.find { it.description == "Local Development" }
            localServer.shouldNotBeNull()
            localServer.url shouldBe "http://localhost:8080"
        }

        @Test
        fun `should configure production server`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            val productionServer = openAPI.servers.find { it.description == "Production" }
            productionServer.shouldNotBeNull()
            productionServer.url shouldBe "https://api.edgerush.com"
        }
    }

    @Nested
    inner class DescriptionContent {
        @Test
        fun `should include FLPS algorithm information in description`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "FLPS"
        }

        @Test
        fun `should include role-based access control information`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "SYSTEM_ADMIN"
            openAPI.info.description shouldContain "GUILD_ADMIN"
            openAPI.info.description shouldContain "PUBLIC_USER"
        }

        @Test
        fun `should include authentication information`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "JWT bearer token"
            openAPI.info.description shouldContain "Authorization"
        }

        @Test
        fun `should include features list`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "CRUD operations"
            openAPI.info.description shouldContain "Pagination"
            openAPI.info.description shouldContain "OpenAPI 3.0"
        }
    }

    @Nested
    inner class AdminModeDescription {
        @Test
        fun `should include admin mode warning when enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "ADMIN MODE ACTIVE"
        }

        @Test
        fun `should indicate authentication bypass when admin mode enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "Authentication is bypassed"
        }

        @Test
        fun `should indicate development mode when admin mode enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "development mode"
        }

        @Test
        fun `should indicate SYSTEM_ADMIN treatment when admin mode enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "SYSTEM_ADMIN"
        }

        @Test
        fun `should not include admin mode warning when disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldNotContain "ADMIN MODE ACTIVE"
        }

        @Test
        fun `should still include base description when admin mode enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val openAPI = openApiConfig.customOpenAPI()

            // Assert
            openAPI.info.description shouldContain "EdgeRush LootMan API"
            openAPI.info.description shouldContain "FLPS"
        }
    }
}
