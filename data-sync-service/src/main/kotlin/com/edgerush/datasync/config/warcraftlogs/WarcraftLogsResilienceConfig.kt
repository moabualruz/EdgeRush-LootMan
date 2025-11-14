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
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofMinutes(5))
            .slidingWindowSize(10)
            .build()
        
        return CircuitBreaker.of("warcraftLogs", config)
    }
    
    @Bean
    fun warcraftLogsRetry(): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(2))
            .build()
        
        return Retry.of("warcraftLogs", config)
    }
}
