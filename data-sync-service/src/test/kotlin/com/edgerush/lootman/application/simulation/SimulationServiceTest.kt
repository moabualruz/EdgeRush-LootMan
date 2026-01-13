package com.edgerush.lootman.application.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SimulationServiceTest : UnitTest() {

    private lateinit var simulationRepository: SimulationRepository
    private lateinit var profileGenerator: ProfileGeneratorService
    private lateinit var simulationExecutor: SimulationExecutor
    private lateinit var service: SimulationService

    @BeforeEach
    fun setUp() {
        simulationRepository = mockk(relaxed = true)
        profileGenerator = mockk()
        simulationExecutor = mockk()
        service = SimulationService(simulationRepository, profileGenerator, simulationExecutor)
    }

    private fun createProfile(): SimulationProfile {
        return SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = """warrior="Testchar"""",
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class SubmitSimulation {
        @Test
        fun `should generate profile and create simulation request`() = runBlocking {
            // Arrange
            val guildId = "guild-123"
            val characterName = "Testchar"
            val characterRealm = "TestRealm"
            val characterClass = "warrior"
            val characterSpec = "fury"
            val profileContent = """warrior="Testchar""""
            val gear = GearSet(emptyMap(), GearSetType.EQUIPPED)

            every {
                profileGenerator.generateProfile(
                    characterName = characterName,
                    characterRealm = characterRealm,
                    characterClass = characterClass,
                    characterSpec = characterSpec,
                    characterLevel = 80,
                    characterRace = "human",
                    gear = gear
                )
            } returns profileContent

            val profileSlot = slot<SimulationProfile>()
            every { simulationRepository.saveProfile(capture(profileSlot)) } answers {
                1L to profileSlot.captured
            }

            val requestSlot = slot<SimulationRequest>()
            every { simulationRepository.saveRequest(capture(requestSlot)) } answers {
                requestSlot.captured.withId(1L)
            }

            // Act
            val request = service.submitSimulation(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                characterClass = characterClass,
                characterSpec = characterSpec,
                characterLevel = 80,
                characterRace = "human",
                gear = gear
            )

            // Assert
            request shouldNotBe null
            request.profile.guildId shouldBe guildId
            request.profile.characterName shouldBe characterName
            request.status shouldBe SimulationStatus.PENDING
            verify { profileGenerator.generateProfile(any(), any(), any(), any(), any(), any(), any()) }
            verify { simulationRepository.saveProfile(any()) }
            verify { simulationRepository.saveRequest(any()) }
        }
    }

    @Nested
    inner class ExecutePendingSimulations {
        @Test
        fun `should execute all pending simulations`() = runBlocking {
            // Arrange
            val profile = createProfile()
            val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)
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

            every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest)
            coEvery { simulationExecutor.execute(any()) } returns Result.success(results)

            // Act
            val executed = service.executePendingSimulations()

            // Assert
            executed shouldBe 1
            coVerify { simulationExecutor.execute(any()) }
            verify(atLeast = 2) { simulationRepository.saveRequest(any()) } // RUNNING and COMPLETED
        }

        @Test
        fun `should mark request as failed when executor fails`() = runBlocking {
            // Arrange
            val profile = createProfile()
            val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)

            every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest)
            coEvery { simulationExecutor.execute(any()) } returns Result.failure(RuntimeException("Docker error"))

            // Act
            val executed = service.executePendingSimulations()

            // Assert
            executed shouldBe 1
            val savedRequests = mutableListOf<SimulationRequest>()
            verify { simulationRepository.saveRequest(capture(savedRequests)) }

            // Find the failed request
            val failedRequest = savedRequests.find { it.status == SimulationStatus.FAILED }
            failedRequest shouldNotBe null
            failedRequest?.errorMessage shouldBe "Docker error"
        }
    }

    @Nested
    inner class GetSimulationResults {
        @Test
        fun `should return results for a character`() {
            // Arrange
            val profile = createProfile()
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

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findResultsByProfile(any()) } returns results

            // Act
            val foundResults = service.getSimulationResults(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm"
            )

            // Assert
            foundResults shouldHaveSize 1
            foundResults[0].itemId shouldBe 12345L
        }

        @Test
        fun `should return empty list when no profile exists`() {
            // Arrange
            every { simulationRepository.findProfileByCharacter(any(), any(), any()) } returns null

            // Act
            val results = service.getSimulationResults(
                guildId = "guild-123",
                characterName = "Unknown",
                characterRealm = "TestRealm"
            )

            // Assert
            results shouldHaveSize 0
        }
    }
}
