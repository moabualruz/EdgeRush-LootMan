package com.edgerush.datasync.config

import com.edgerush.datasync.security.AdminModeConfig
import com.google.common.util.concurrent.RateLimiter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "api.rate-limit")
data class RateLimitProperties(
    var enabled: Boolean = true,
    var readRequestsPerSecond: Double = 100.0,
    var writeRequestsPerSecond: Double = 20.0,
)

@Component
class RateLimitFilter(
    private val properties: RateLimitProperties,
    private val adminModeConfig: AdminModeConfig,
) : WebFilter {
    private val readLimiter: RateLimiter by lazy {
        RateLimiter.create(properties.readRequestsPerSecond)
    }

    private val writeLimiter: RateLimiter by lazy {
        RateLimiter.create(properties.writeRequestsPerSecond)
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        // Skip rate limiting if disabled or in admin mode
        if (!properties.enabled || adminModeConfig.isEnabled()) {
            return chain.filter(exchange)
        }

        val limiter =
            if (isWriteOperation(exchange.request.method)) {
                writeLimiter
            } else {
                readLimiter
            }

        if (!limiter.tryAcquire()) {
            exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            exchange.response.headers.add("Retry-After", "1")
            return exchange.response.setComplete()
        }

        return chain.filter(exchange)
    }

    private fun isWriteOperation(method: HttpMethod?): Boolean {
        return method != null && method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
    }
}
