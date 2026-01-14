package com.edgerush.lootman.domain.simulation.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SimulationRequestTest : UnitTest() {

    private fun createValidProfile(): SimulationProfile {
        return SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = "warrior=\"Testchar\"",
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class Creation {
        @Test
        fun `should create SimulationRequest with PENDING status`() {
            // Arrange
            val profile = createValidProfile()
            val iterations = 10000
            val fightLength = 300

            // Act
            val request = SimulationRequest.create(
                profile = profile,
                iterations = iterations,
                fightLengthSeconds = fightLength
            )

            // Assert
            request.profile shouldBe profile
            request.iterations shouldBe iterations
            request.fightLengthSeconds shouldBe fightLength
            request.status shouldBe SimulationStatus.PENDING
            request.submittedAt shouldNotBe null
            request.completedAt shouldBe null
            request.errorMessage shouldBe null
        }

        @Test
        fun `should throw exception when iterations is less than 1`() {
            // Arrange
            val profile = createValidProfile()

            // Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                SimulationRequest.create(
                    profile = profile,
                    iterations = 0,
                    fightLengthSeconds = 300
                )
            }
            exception.message shouldContain "iterations"
        }

        @Test
        fun `should throw exception when fightLengthSeconds is less than 1`() {
            // Arrange
            val profile = createValidProfile()

            // Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                SimulationRequest.create(
                    profile = profile,
                    iterations = 10000,
                    fightLengthSeconds = 0
                )
            }
            exception.message shouldContain "fightLengthSeconds"
        }
    }

    @Nested
    inner class StatusTransitions {
        @Test
        fun `should transition from PENDING to RUNNING`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            )

            // Act
            val runningRequest = request.markRunning()

            // Assert
            runningRequest.status shouldBe SimulationStatus.RUNNING
            runningRequest.completedAt shouldBe null
        }

        @Test
        fun `should transition from RUNNING to COMPLETED`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning()
            val results = listOf(
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now()
                )
            )

            // Act
            val completedRequest = request.markCompleted(results)

            // Assert
            completedRequest.status shouldBe SimulationStatus.COMPLETED
            completedRequest.completedAt shouldNotBe null
            completedRequest.results shouldBe results
            completedRequest.errorMessage shouldBe null
        }

        @Test
        fun `should transition from RUNNING to FAILED with error message`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning()
            val errorMessage = "Docker container exited with code 1"

            // Act
            val failedRequest = request.markFailed(errorMessage)

            // Assert
            failedRequest.status shouldBe SimulationStatus.FAILED
            failedRequest.completedAt shouldNotBe null
            failedRequest.errorMessage shouldBe errorMessage
            failedRequest.results shouldBe emptyList()
        }

        @Test
        fun `should throw exception when transitioning from PENDING to COMPLETED`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            )

            // Act & Assert
            shouldThrow<IllegalStateException> {
                request.markCompleted(emptyList())
            }
        }

        @Test
        fun `should throw exception when transitioning from COMPLETED to RUNNING`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning().markCompleted(emptyList())

            // Act & Assert
            shouldThrow<IllegalStateException> {
                request.markRunning()
            }
        }

        @Test
        fun `should throw exception when transitioning from PENDING to FAILED`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            )

            // Act & Assert
            val exception = shouldThrow<IllegalStateException> {
                request.markFailed("Error message")
            }
            exception.message shouldContain "Cannot transition to FAILED from PENDING"
        }
    }

    @Nested
    inner class QueryMethods {
        @Test
        fun `isPending should return true for PENDING status`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            )

            // Assert
            request.isPending shouldBe true
            request.isRunning shouldBe false
            request.isCompleted shouldBe false
            request.isFailed shouldBe false
        }

        @Test
        fun `isRunning should return true for RUNNING status`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning()

            // Assert
            request.isPending shouldBe false
            request.isRunning shouldBe true
            request.isCompleted shouldBe false
            request.isFailed shouldBe false
        }

        @Test
        fun `isCompleted should return true for COMPLETED status`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning().markCompleted(emptyList())

            // Assert
            request.isPending shouldBe false
            request.isRunning shouldBe false
            request.isCompleted shouldBe true
            request.isFailed shouldBe false
        }

        @Test
        fun `isFailed should return true for FAILED status`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 10000,
                fightLengthSeconds = 300
            ).markRunning().markFailed("Error")

            // Assert
            request.isPending shouldBe false
            request.isRunning shouldBe false
            request.isCompleted shouldBe false
            request.isFailed shouldBe true
        }
    }

    @Nested
    inner class SimulationOptions {
        @Test
        fun `should use default values for optional parameters`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile()
            )

            // Assert
            request.iterations shouldBe SimulationRequest.DEFAULT_ITERATIONS
            request.fightLengthSeconds shouldBe SimulationRequest.DEFAULT_FIGHT_LENGTH_SECONDS
        }

        @Test
        fun `should allow custom iterations and fight length`() {
            // Arrange
            val request = SimulationRequest.create(
                profile = createValidProfile(),
                iterations = 50000,
                fightLengthSeconds = 600
            )

            // Assert
            request.iterations shouldBe 50000
            request.fightLengthSeconds shouldBe 600
        }
    }
}
