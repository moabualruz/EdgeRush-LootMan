package com.edgerush.datasync.config.warcraftlogs

import com.edgerush.datasync.test.base.UnitTest
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Unit tests for WarcraftLogsResilienceConfig.
 *
 * Tests the resilience4j CircuitBreaker and Retry bean configurations
 * including failure thresholds, wait durations, and retry attempts.
 */
class WarcraftLogsResilienceConfigTest : UnitTest() {

    private lateinit var config: WarcraftLogsResilienceConfig

    @BeforeEach
    fun setUp() {
        config = WarcraftLogsResilienceConfig()
    }

    @Nested
    inner class CircuitBreakerBean {

        @Test
        fun `should create a CircuitBreaker bean`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.shouldNotBeNull()
            circuitBreaker.shouldBeInstanceOf<CircuitBreaker>()
        }

        @Test
        fun `should have correct name`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.name shouldBe "warcraftLogs"
        }

        @Test
        fun `should configure failure rate threshold to 50 percent`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.circuitBreakerConfig.failureRateThreshold shouldBe 50.0f
        }

        @Test
        fun `should configure wait duration in open state to 5 minutes`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert - check via the wait interval function
            val waitIntervalFunction = circuitBreaker.circuitBreakerConfig.waitIntervalFunctionInOpenState
            waitIntervalFunction.apply(1) shouldBe Duration.ofMinutes(5).toMillis()
        }

        @Test
        fun `should configure sliding window size to 10`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.circuitBreakerConfig.slidingWindowSize shouldBe 10
        }

        @Test
        fun `should start in closed state`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.state shouldBe CircuitBreaker.State.CLOSED
        }

        @Test
        fun `should allow calls when in closed state`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            circuitBreaker.tryAcquirePermission() shouldBe true
        }
    }

    @Nested
    inner class RetryBean {

        @Test
        fun `should create a Retry bean`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            retry.shouldNotBeNull()
            retry.shouldBeInstanceOf<Retry>()
        }

        @Test
        fun `should have correct name`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            retry.name shouldBe "warcraftLogs"
        }

        @Test
        fun `should configure max attempts to 3`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            retry.retryConfig.maxAttempts shouldBe 3
        }

        @Test
        fun `should configure retry with expected settings`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert - verify the retry config exists and has expected max attempts
            // The wait duration is configured internally; we verify the retry bean works
            retry.retryConfig.shouldNotBeNull()
            retry.retryConfig.maxAttempts shouldBe 3
        }
    }

    @Nested
    inner class CircuitBreakerBehavior {

        @Test
        fun `should track successful calls`() {
            // Arrange
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Act
            circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS)

            // Assert
            circuitBreaker.metrics.numberOfSuccessfulCalls shouldBe 1
        }

        @Test
        fun `should track failed calls`() {
            // Arrange
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Act
            circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, RuntimeException("test"))

            // Assert
            circuitBreaker.metrics.numberOfFailedCalls shouldBe 1
        }

        @Test
        fun `should remain closed after single failure`() {
            // Arrange
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Act
            circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, RuntimeException("test"))

            // Assert
            circuitBreaker.state shouldBe CircuitBreaker.State.CLOSED
        }

        @Test
        fun `should open after exceeding failure threshold`() {
            // Arrange
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Act - Fill the sliding window with failures (need minimum calls before evaluation)
            // Sliding window is 10, failure rate threshold is 50%
            // Need to fill window first before circuit can open
            repeat(10) {
                circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, RuntimeException("test $it"))
            }

            // Assert - After 10 failures (100% failure rate > 50% threshold), circuit should open
            circuitBreaker.state shouldBe CircuitBreaker.State.OPEN
        }

        @Test
        fun `should stay closed when failure rate is below threshold`() {
            // Arrange
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Act - 4 failures and 6 successes = 40% failure rate (below 50% threshold)
            repeat(4) {
                circuitBreaker.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, RuntimeException("test"))
            }
            repeat(6) {
                circuitBreaker.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS)
            }

            // Assert
            circuitBreaker.state shouldBe CircuitBreaker.State.CLOSED
        }
    }

    @Nested
    inner class RetryBehavior {

        @Test
        fun `should have metrics available`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            retry.metrics.shouldNotBeNull()
        }

        @Test
        fun `should start with zero retry counts`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            retry.metrics.numberOfSuccessfulCallsWithoutRetryAttempt shouldBe 0
            retry.metrics.numberOfSuccessfulCallsWithRetryAttempt shouldBe 0
            retry.metrics.numberOfFailedCallsWithoutRetryAttempt shouldBe 0
            retry.metrics.numberOfFailedCallsWithRetryAttempt shouldBe 0
        }
    }

    @Nested
    inner class IndependentInstances {

        @Test
        fun `should create independent circuit breaker instances`() {
            // Act
            val cb1 = config.warcraftLogsCircuitBreaker()
            val cb2 = config.warcraftLogsCircuitBreaker()

            // Assert
            (cb1 !== cb2) shouldBe true
        }

        @Test
        fun `should create independent retry instances`() {
            // Act
            val retry1 = config.warcraftLogsRetry()
            val retry2 = config.warcraftLogsRetry()

            // Assert
            (retry1 !== retry2) shouldBe true
        }

        @Test
        fun `circuit breaker instances should have same configuration`() {
            // Act
            val cb1 = config.warcraftLogsCircuitBreaker()
            val cb2 = config.warcraftLogsCircuitBreaker()

            // Assert
            cb1.circuitBreakerConfig.failureRateThreshold shouldBe cb2.circuitBreakerConfig.failureRateThreshold
            cb1.circuitBreakerConfig.slidingWindowSize shouldBe cb2.circuitBreakerConfig.slidingWindowSize
            cb1.circuitBreakerConfig.waitIntervalFunctionInOpenState.apply(1) shouldBe cb2.circuitBreakerConfig.waitIntervalFunctionInOpenState.apply(1)
        }

        @Test
        fun `retry instances should have same configuration`() {
            // Act
            val retry1 = config.warcraftLogsRetry()
            val retry2 = config.warcraftLogsRetry()

            // Assert
            retry1.retryConfig.maxAttempts shouldBe retry2.retryConfig.maxAttempts
        }

        @Test
        fun `state changes in one circuit breaker should not affect another`() {
            // Arrange
            val cb1 = config.warcraftLogsCircuitBreaker()
            val cb2 = config.warcraftLogsCircuitBreaker()

            // Act - Open cb1 by recording failures
            repeat(10) {
                cb1.onError(100, java.util.concurrent.TimeUnit.MILLISECONDS, RuntimeException("test"))
            }

            // Assert
            cb1.state shouldBe CircuitBreaker.State.OPEN
            cb2.state shouldBe CircuitBreaker.State.CLOSED
        }
    }

    @Nested
    inner class ConfigurationValues {

        @Test
        fun `circuit breaker should have reasonable failure threshold`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert - threshold should be between 0 and 100
            val threshold = circuitBreaker.circuitBreakerConfig.failureRateThreshold
            (threshold > 0f && threshold <= 100f) shouldBe true
        }

        @Test
        fun `circuit breaker should have positive wait duration`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert - check via the wait interval function
            val waitDurationMs = circuitBreaker.circuitBreakerConfig.waitIntervalFunctionInOpenState.apply(1)
            (waitDurationMs > 0) shouldBe true
        }

        @Test
        fun `circuit breaker should have positive sliding window size`() {
            // Act
            val circuitBreaker = config.warcraftLogsCircuitBreaker()

            // Assert
            (circuitBreaker.circuitBreakerConfig.slidingWindowSize > 0) shouldBe true
        }

        @Test
        fun `retry should have at least one attempt`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert
            (retry.retryConfig.maxAttempts >= 1) shouldBe true
        }

        @Test
        fun `retry should be properly configured`() {
            // Act
            val retry = config.warcraftLogsRetry()

            // Assert - verify retry is usable and has expected configuration
            retry.retryConfig.shouldNotBeNull()
            // Max attempts being greater than 1 implies retry functionality is enabled
            (retry.retryConfig.maxAttempts > 1) shouldBe true
        }
    }
}
