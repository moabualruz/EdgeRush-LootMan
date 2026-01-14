package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.EnabledIfDockerAvailable
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import java.time.Instant

/**
 * Docker-dependent tests for SimulationHealthIndicator.
 *
 * These tests are skipped when Docker is not available.
 * They cover the health status branches that require Docker to be running.
 */
@EnabledIfDockerAvailable
class SimulationHealthIndicatorDockerTest : UnitTest() {

    private lateinit var simulationRepository: SimulationRepository
    private lateinit var dockerSimulationExecutor: DockerSimulationExecutor
    private lateinit var healthIndicator: SimulationHealthIndicator

    @BeforeEach
    fun setUp() {
        simulationRepository = mockk()
        dockerSimulationExecutor = mockk()
        healthIndicator = SimulationHealthIndicator(simulationRepository, dockerSimulationExecutor)
    }

    private fun createPendingRequest(): SimulationRequest {
        val profile = SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = """warrior="Testchar"""",
            createdAt = Instant.now()
        )
        return SimulationRequest.create(profile = profile)
    }

    @Nested
    inner class WhenDockerAvailable {
        @Test
        fun `should report UP when Docker is available and no issues`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.UP
            health.details["dockerAvailable"] shouldBe true
            health.details["pendingSimulations"] shouldBe 0
        }

        @Test
        fun `should report UP with pending simulations below threshold`() {
            // Arrange
            val pendingRequests = (1..10).map { createPendingRequest() }
            every { simulationRepository.findPendingRequests() } returns pendingRequests

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.UP
            health.details["pendingSimulations"] shouldBe 10
        }

        @Test
        fun `should report OUT_OF_SERVICE when too many pending simulations`() {
            // Arrange - Create more than 50 pending requests
            val pendingRequests = (1..51).map { createPendingRequest() }
            every { simulationRepository.findPendingRequests() } returns pendingRequests

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.OUT_OF_SERVICE
            health.details["reason"] shouldBe "Too many pending simulations"
            health.details["pendingSimulations"] shouldBe 51
        }

        @Test
        fun `should report OUT_OF_SERVICE when success rate below threshold with enough executions`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            // Record 1 success and 5 failures (16.7% success rate, below 80% threshold)
            // Need at least 5 executions for rate to be checked
            healthIndicator.recordSuccess(100)
            repeat(5) { healthIndicator.recordFailure() }

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.OUT_OF_SERVICE
            health.details["reason"] shouldBe "Success rate below threshold"
            health.details["successCount"] shouldBe 1L
            health.details["failureCount"] shouldBe 5L
        }

        @Test
        fun `should report UP when success rate above threshold`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            // Record 4 successes and 1 failure (80% success rate, at threshold)
            repeat(4) { healthIndicator.recordSuccess(100) }
            healthIndicator.recordFailure()

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.UP
            health.details["successCount"] shouldBe 4L
            health.details["failureCount"] shouldBe 1L
        }

        @Test
        fun `should not check success rate when less than 5 executions`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            // Record low success rate but only 4 total executions
            healthIndicator.recordSuccess(100)
            repeat(3) { healthIndicator.recordFailure() }

            // Act
            val health = healthIndicator.health()

            // Assert
            // Should be UP because we don't check success rate with less than 5 executions
            health.status shouldBe Status.UP
            health.details["successCount"] shouldBe 1L
            health.details["failureCount"] shouldBe 3L
        }

        @Test
        fun `should track execution time correctly`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            healthIndicator.recordSuccess(500)
            healthIndicator.recordSuccess(300)

            // Act
            val health = healthIndicator.health()

            // Assert
            health.details["lastExecutionTimeMs"] shouldBe 300L
        }

        @Test
        fun `should report exactly at pending threshold as UP`() {
            // Arrange - Create exactly 50 pending requests (at threshold, not over)
            val pendingRequests = (1..50).map { createPendingRequest() }
            every { simulationRepository.findPendingRequests() } returns pendingRequests

            // Act
            val health = healthIndicator.health()

            // Assert
            health.status shouldBe Status.UP
            health.details["pendingSimulations"] shouldBe 50
        }

        @Test
        fun `should include success rate percentage in details`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            repeat(3) { healthIndicator.recordSuccess(100) }
            healthIndicator.recordFailure()

            // Act
            val health = healthIndicator.health()

            // Assert
            health.details["successRate"] shouldNotBe null
            health.details["successRate"].toString() shouldBe "75.00%"
        }
    }
}
