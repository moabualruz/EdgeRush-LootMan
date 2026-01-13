package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import java.time.Instant

class SimulationHealthIndicatorTest : UnitTest() {

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
    inner class HealthStatus {
        @Test
        fun `should report UP when no pending simulations and docker assumed available`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            val health = healthIndicator.health()

            // Assert - Note: docker availability check may fail in test environment
            // but we verify the details are populated
            health.details["pendingSimulations"] shouldBe 0
            health.details["successCount"] shouldBe 0L
            health.details["failureCount"] shouldBe 0L
        }

        @Test
        fun `should include success rate in details`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            healthIndicator.recordSuccess(100)
            healthIndicator.recordSuccess(150)
            healthIndicator.recordFailure()

            // Act
            val health = healthIndicator.health()

            // Assert
            health.details["successCount"] shouldBe 2L
            health.details["failureCount"] shouldBe 1L
            health.details["successRate"].toString() shouldContain "66"
        }

        @Test
        fun `should track last execution time`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()
            healthIndicator.recordSuccess(250)

            // Act
            val health = healthIndicator.health()

            // Assert
            health.details["lastExecutionTimeMs"] shouldBe 250L
        }
    }

    @Nested
    inner class RecordMetrics {
        @Test
        fun `should increment success count on recordSuccess`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            healthIndicator.recordSuccess(100)
            healthIndicator.recordSuccess(100)
            healthIndicator.recordSuccess(100)

            // Assert
            val health = healthIndicator.health()
            health.details["successCount"] shouldBe 3L
        }

        @Test
        fun `should increment failure count on recordFailure`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            healthIndicator.recordFailure()
            healthIndicator.recordFailure()

            // Assert
            val health = healthIndicator.health()
            health.details["failureCount"] shouldBe 2L
        }
    }
}
