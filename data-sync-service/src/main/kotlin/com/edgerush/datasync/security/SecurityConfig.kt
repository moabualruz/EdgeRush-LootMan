package com.edgerush.datasync.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val adminModeConfig: AdminModeConfig,
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange { exchanges ->
                exchanges
                    // Public endpoints
                    .pathMatchers("/actuator/health", "/actuator/metrics", "/actuator/info").permitAll()
                    .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                    // Admin mode bypasses all auth
                    .apply {
                        if (adminModeConfig.isEnabled()) {
                            pathMatchers("/**").permitAll()
                        } else {
                            // FLPS endpoints - public read access
                            pathMatchers(HttpMethod.GET, "/api/v1/flps/**").permitAll()

                            // All GET requests - authenticated users
                            pathMatchers(HttpMethod.GET, "/api/v1/**").authenticated()

                            // Write operations - require admin roles
                            pathMatchers(HttpMethod.POST, "/api/v1/**").hasAnyAuthority("GUILD_ADMIN", "SYSTEM_ADMIN")
                            pathMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyAuthority("GUILD_ADMIN", "SYSTEM_ADMIN")
                            pathMatchers(HttpMethod.DELETE, "/api/v1/**").hasAnyAuthority("GUILD_ADMIN", "SYSTEM_ADMIN")
                            pathMatchers(HttpMethod.PATCH, "/api/v1/**").hasAnyAuthority("GUILD_ADMIN", "SYSTEM_ADMIN")

                            // Default - require authentication
                            anyExchange().authenticated()
                        }
                    }
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        if (adminModeConfig.isEnabled()) {
            configuration.allowedOrigins = listOf("*")
        } else {
            configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
        }

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = !adminModeConfig.isEnabled()
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
