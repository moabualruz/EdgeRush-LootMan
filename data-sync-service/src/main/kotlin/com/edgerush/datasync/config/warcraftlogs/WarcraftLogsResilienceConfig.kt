package com.edgerush.datasync.config.warcraftlogs

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class WarcraftLogsResilienceConfig {
    @Bean
    fun warcraftLogsCircuitBreaker(): CircuitBreaker {
        val config =
            CircuitBreakerConfig.custom()
                .failureRateThreshold(FAILURE_RATE_THRESHOLD)
                .waitDurationInOpenState(Duration.ofMinutes(CIRCUIT_BREAKER_WAIT_MINUTES))
                .slidingWindowSize(SLIDING_WINDOW_SIZE)
                .build()

        return CircuitBreaker.of("warcraftLogs", config)
    }

    @Bean
    fun warcraftLogsRetry(): Retry {
        val config =
            RetryConfig.custom<Any>()
                .maxAttempts(MAX_RETRY_ATTEMPTS)
                .waitDuration(Duration.ofSeconds(RETRY_WAIT_SECONDS))
                .build()

        return Retry.of("warcraftLogs", config)
    }

    companion object {
        private const val FAILURE_RATE_THRESHOLD = 50.0f
        private const val CIRCUIT_BREAKER_WAIT_MINUTES = 5L
        private const val SLIDING_WINDOW_SIZE = 10
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_WAIT_SECONDS = 2L
    }
}
