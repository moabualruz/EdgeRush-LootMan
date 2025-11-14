package com.edgerush.datasync.config

import com.edgerush.datasync.security.AdminModeConfig
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    private val adminModeConfig: AdminModeConfig,
) {
    @Bean
    fun customOpenAPI(): OpenAPI {
        val securityScheme =
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .`in`(SecurityScheme.In.HEADER)
                .name("Authorization")

        val securityRequirement = SecurityRequirement().addList("bearerAuth")

        val info =
            Info()
                .title("EdgeRush LootMan API")
                .version("1.0.0")
                .description(buildDescription())
                .contact(Contact().name("EdgeRush Team").email("support@edgerush.com"))

        return OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(Components().addSecuritySchemes("bearerAuth", securityScheme))
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local Development"),
                    Server().url("https://api.edgerush.com").description("Production"),
                ),
            )
    }

    private fun buildDescription(): String {
        val baseDesc =
            """
            # EdgeRush LootMan API
            
            Comprehensive REST API for EdgeRush LootMan guild management system implementing the FLPS (Final Loot Priority Score) algorithm.
            
            ## Features
            - Full CRUD operations for all entities
            - Role-based access control (SYSTEM_ADMIN, GUILD_ADMIN, PUBLIC_USER)
            - Pagination and filtering support
            - OpenAPI 3.0 documentation
            - Real-time FLPS calculations
            
            ## Authentication
            Most endpoints require JWT bearer token authentication. Include the token in the Authorization header:
            ```
            Authorization: Bearer <your-jwt-token>
            ```
            
            ## Roles
            - **SYSTEM_ADMIN**: Full system access
            - **GUILD_ADMIN**: Guild-specific administrative access
            - **PUBLIC_USER**: Read-only access to public data
            """.trimIndent()

        return if (adminModeConfig.isEnabled()) {
            """
            ⚠️ **ADMIN MODE ACTIVE** - Authentication is bypassed!
            
            This instance is running in development mode with authentication disabled.
            All requests are treated as SYSTEM_ADMIN.
            
            ---
            
            $baseDesc
            """.trimIndent()
        } else {
            baseDesc
        }
    }
}
