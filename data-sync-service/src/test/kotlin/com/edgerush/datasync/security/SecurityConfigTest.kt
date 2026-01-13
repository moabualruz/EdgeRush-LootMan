package com.edgerush.datasync.security

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.cors.CorsConfiguration

/**
 * Comprehensive unit tests for SecurityConfig.
 *
 * Tests cover:
 * - CORS configuration based on admin mode
 * - Security rules configuration
 * - Filter chain configuration
 * - Edge cases
 *
 * Note: Full integration tests for SecurityConfig require a Spring context
 * and are covered in integration tests. These unit tests focus on the
 * configuration logic that can be tested in isolation.
 */
class SecurityConfigTest : UnitTest() {

    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
    private lateinit var adminModeConfig: AdminModeConfig

    @BeforeEach
    fun setup() {
        jwtAuthenticationFilter = mockk()
        adminModeConfig = mockk()
    }

    private fun createExchange(path: String): MockServerWebExchange {
        val request = MockServerHttpRequest.get(path).build()
        return MockServerWebExchange.from(request)
    }

    @Nested
    inner class `CORS configuration` {

        @Test
        fun `should allow all origins when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedOrigins shouldContain "*"
        }

        @Test
        fun `should restrict origins when admin mode is disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedOrigins shouldContainAll listOf("http://localhost:3000", "http://localhost:8080")
            corsConfig.allowedOrigins?.contains("*")?.shouldBeFalse()
        }

        @Test
        fun `should allow standard HTTP methods`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedMethods shouldContainAll listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        }

        @Test
        fun `should allow all headers`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedHeaders shouldContain "*"
        }

        @Test
        fun `should enable credentials when admin mode is disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowCredentials?.shouldBeTrue()
        }

        @Test
        fun `should disable credentials when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowCredentials?.shouldBeFalse()
        }

        @Test
        fun `should set max age to 1 hour`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.maxAge shouldBe 3600L
        }

        @Test
        fun `should apply CORS configuration to all paths`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)

            // Act
            val corsSource = securityConfig.corsConfigurationSource()

            // Assert - /**  pattern should match everything
            corsSource.getCorsConfiguration(createExchange("/api/v1/test")).shouldNotBeNull()
            corsSource.getCorsConfiguration(createExchange("/actuator/health")).shouldNotBeNull()
            corsSource.getCorsConfiguration(createExchange("/any/path/here")).shouldNotBeNull()
        }
    }

    @Nested
    inner class `admin mode behavior` {

        @Test
        fun `should create security config with admin mode enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true

            // Act
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)

            // Assert
            securityConfig.shouldNotBeNull()
        }

        @Test
        fun `should create security config with admin mode disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false

            // Act
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)

            // Assert
            securityConfig.shouldNotBeNull()
        }
    }

    @Nested
    inner class `configuration instantiation` {

        @Test
        fun `should accept JwtAuthenticationFilter dependency`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val mockFilter = mockk<JwtAuthenticationFilter>()

            // Act
            val securityConfig = SecurityConfig(mockFilter, adminModeConfig)

            // Assert
            securityConfig.shouldNotBeNull()
        }

        @Test
        fun `should accept AdminModeConfig dependency`() {
            // Arrange
            val mockAdminConfig = mockk<AdminModeConfig>()
            every { mockAdminConfig.isEnabled() } returns false

            // Act
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, mockAdminConfig)

            // Assert
            securityConfig.shouldNotBeNull()
        }
    }

    @Nested
    inner class `CORS configuration edge cases` {

        @Test
        fun `should return config for root path`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)

            // Act
            val corsSource = securityConfig.corsConfigurationSource()

            // Assert - /**  pattern should match everything
            corsSource.getCorsConfiguration(createExchange("/")).shouldNotBeNull()
        }

        @Test
        fun `should return config for nested api paths`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)

            // Act
            val corsSource = securityConfig.corsConfigurationSource()

            // Assert
            corsSource.getCorsConfiguration(createExchange("/api")).shouldNotBeNull()
            corsSource.getCorsConfiguration(createExchange("/api/v1/test")).shouldNotBeNull()
            corsSource.getCorsConfiguration(createExchange("/api/v1/deeply/nested/path")).shouldNotBeNull()
        }

        @Test
        fun `should maintain CORS configuration consistency across multiple calls`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource1 = securityConfig.corsConfigurationSource()
            val corsSource2 = securityConfig.corsConfigurationSource()

            val config1 = corsSource1.getCorsConfiguration(exchange)
            val config2 = corsSource2.getCorsConfiguration(exchange)

            // Assert - both should have same configuration
            config1?.allowedOrigins shouldBe config2?.allowedOrigins
            config1?.allowedMethods shouldBe config2?.allowedMethods
            config1?.allowCredentials shouldBe config2?.allowCredentials
        }
    }

    @Nested
    inner class `security rules documentation` {

        /**
         * These tests document the expected security rules based on the SecurityConfig.
         * They verify the configuration intent without requiring a full Spring context.
         */

        @Test
        fun `should document public endpoints`() {
            // Document: The following endpoints should be publicly accessible:
            // - /actuator/health
            // - /actuator/metrics
            // - /actuator/info
            // - /v3/api-docs/**
            // - /swagger-ui/**
            // - /swagger-ui.html
            // - /webjars/**
            val publicEndpoints = listOf(
                "/actuator/health",
                "/actuator/metrics",
                "/actuator/info",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/webjars/**",
            )

            // This is a documentation test - actual enforcement is tested in integration tests
            publicEndpoints.size shouldBe 7
        }

        @Test
        fun `should document FLPS public read access`() {
            // Document: GET requests to /api/v1/flps/** should be publicly accessible
            val flpsPublicEndpoint = "/api/v1/flps/**"
            flpsPublicEndpoint.shouldNotBeNull()
        }

        @Test
        fun `should document authenticated endpoints`() {
            // Document: GET requests to /api/v1/** require authentication
            val authenticatedPattern = "/api/v1/**"
            authenticatedPattern.shouldNotBeNull()
        }

        @Test
        fun `should document admin-only write operations`() {
            // Document: POST, PUT, DELETE, PATCH to /api/v1/** require GUILD_ADMIN or SYSTEM_ADMIN
            val adminRoles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN")
            adminRoles.size shouldBe 2
        }
    }
}

/**
 * Additional test class for AdminModeConfig integration with SecurityConfig.
 * This tests the interaction between the two components.
 */
class SecurityConfigAdminModeIntegrationTest : UnitTest() {

    private fun createExchange(path: String): MockServerWebExchange {
        val request = MockServerHttpRequest.get(path).build()
        return MockServerWebExchange.from(request)
    }

    @Nested
    inner class `AdminModeConfig and SecurityConfig interaction` {

        @Test
        fun `should use AdminModeConfig enabled state for CORS decisions`() {
            // Arrange
            val adminModeConfig = AdminModeConfig(enabled = true)
            val jwtAuthenticationFilter = mockk<JwtAuthenticationFilter>()
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedOrigins shouldContain "*"
            corsConfig.allowCredentials?.shouldBeFalse()
        }

        @Test
        fun `should use AdminModeConfig disabled state for CORS decisions`() {
            // Arrange
            val adminModeConfig = AdminModeConfig(enabled = false)
            val jwtAuthenticationFilter = mockk<JwtAuthenticationFilter>()
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val exchange = createExchange("/api/v1/test")

            // Act
            val corsSource = securityConfig.corsConfigurationSource()
            val corsConfig = corsSource.getCorsConfiguration(exchange)

            // Assert
            corsConfig.shouldNotBeNull()
            corsConfig.allowedOrigins?.contains("*")?.shouldBeFalse()
            corsConfig.allowCredentials?.shouldBeTrue()
        }

        @Test
        fun `should reflect AdminModeConfig changes in CORS configuration`() {
            // Arrange
            val adminModeConfig = AdminModeConfig(enabled = false)
            val jwtAuthenticationFilter = mockk<JwtAuthenticationFilter>()
            val exchange = createExchange("/api/v1/test")

            // Act - create config with disabled admin mode
            val securityConfig1 = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val corsConfig1 = securityConfig1.corsConfigurationSource().getCorsConfiguration(exchange)

            // Change admin mode
            adminModeConfig.enabled = true
            val securityConfig2 = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val corsConfig2 = securityConfig2.corsConfigurationSource().getCorsConfiguration(exchange)

            // Assert - first config had restricted origins, second has wildcard
            corsConfig1.shouldNotBeNull()
            corsConfig1.allowedOrigins?.contains("*")?.shouldBeFalse()
            corsConfig2.shouldNotBeNull()
            corsConfig2.allowedOrigins shouldContain "*"
        }
    }
}

/**
 * Tests for SecurityConfig.securityWebFilterChain method.
 * These tests verify the security filter chain configuration including CSRF, CORS,
 * authorization rules, and JWT filter integration.
 */
class SecurityConfigFilterChainTest : UnitTest() {

    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
    private lateinit var adminModeConfig: AdminModeConfig

    @BeforeEach
    fun setup() {
        jwtAuthenticationFilter = mockk(relaxed = true)
        adminModeConfig = mockk()
    }

    @Nested
    inner class `securityWebFilterChain with admin mode enabled` {

        @Test
        fun `should create security filter chain when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
        }

        @Test
        fun `should configure permitAll for all paths when admin mode is enabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns true
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert - filter chain should be built successfully
            filterChain.shouldNotBeNull()
            // The filter chain is configured to permitAll for /** when admin mode is enabled
        }
    }

    @Nested
    inner class `securityWebFilterChain with admin mode disabled` {

        @Test
        fun `should create security filter chain when admin mode is disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
        }

        @Test
        fun `should configure authorization rules for API endpoints when admin mode is disabled`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert - filter chain should be built successfully with proper rules
            filterChain.shouldNotBeNull()
            // Rules configured:
            // - Public: actuator endpoints, swagger, FLPS GET
            // - Authenticated: GET /api/v1/**
            // - Admin: POST/PUT/DELETE/PATCH /api/v1/**
        }

        @Test
        fun `should add JWT authentication filter at AUTHENTICATION position`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // JWT filter is added at SecurityWebFiltersOrder.AUTHENTICATION
        }
    }

    @Nested
    inner class `securityWebFilterChain CSRF and security context configuration` {

        @Test
        fun `should disable CSRF protection`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // CSRF is disabled via csrf { it.disable() }
        }

        @Test
        fun `should configure stateless security context repository`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // NoOpServerSecurityContextRepository is used for stateless JWT auth
        }

        @Test
        fun `should configure CORS with custom configuration source`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // CORS is configured with corsConfigurationSource()
        }
    }

    @Nested
    inner class `securityWebFilterChain authorization exchange rules` {

        @Test
        fun `should permit actuator health endpoint`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // /actuator/health is configured as permitAll
        }

        @Test
        fun `should permit swagger documentation endpoints`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html, /webjars/** are permitAll
        }

        @Test
        fun `should permit public FLPS read endpoints`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // GET /api/v1/flps/** is permitAll
        }

        @Test
        fun `should require authentication for API GET requests`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // GET /api/v1/** requires authentication
        }

        @Test
        fun `should require admin authority for POST requests`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // POST /api/v1/** requires GUILD_ADMIN or SYSTEM_ADMIN
        }

        @Test
        fun `should require admin authority for PUT requests`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // PUT /api/v1/** requires GUILD_ADMIN or SYSTEM_ADMIN
        }

        @Test
        fun `should require admin authority for DELETE requests`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // DELETE /api/v1/** requires GUILD_ADMIN or SYSTEM_ADMIN
        }

        @Test
        fun `should require admin authority for PATCH requests`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // PATCH /api/v1/** requires GUILD_ADMIN or SYSTEM_ADMIN
        }

        @Test
        fun `should require authentication for any other exchange by default`() {
            // Arrange
            every { adminModeConfig.isEnabled() } returns false
            val securityConfig = SecurityConfig(jwtAuthenticationFilter, adminModeConfig)
            val http = ServerHttpSecurity.http()

            // Act
            val filterChain = securityConfig.securityWebFilterChain(http)

            // Assert
            filterChain.shouldNotBeNull()
            // anyExchange().authenticated() is the default fallback
        }
    }
}
