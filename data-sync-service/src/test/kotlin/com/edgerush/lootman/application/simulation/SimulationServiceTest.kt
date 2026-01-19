package com.edgerush.lootman.application.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import com.edgerush.lootman.infrastructure.external.raidbots.RaidbotsConfig
import com.edgerush.lootman.infrastructure.external.raidbots.RaidbotsService
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
    private lateinit var raidbotsService: RaidbotsService
    private lateinit var raidbotsConfig: RaidbotsConfig
    private lateinit var simulationExecutor: SimulationExecutor
    private lateinit var service: SimulationService

    @BeforeEach
    fun setUp() {
        simulationRepository = mockk(relaxed = true)
        profileGenerator = mockk()
        raidbotsService = mockk()
        raidbotsConfig = RaidbotsConfig(enabled = false) // Disable Raidbots for local executor tests
        simulationExecutor = mockk()
        service = SimulationService(simulationRepository, profileGenerator, raidbotsService, raidbotsConfig, simulationExecutor)
    }

    private fun createProfile(): SimulationProfile {
        return SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = """warrior="Testchar"""",
            createdAt = Instant.now(),
        )
    }

    @Nested
    inner class SubmitSimulation {
        @Test
        fun `should generate profile and create simulation request`() =
            runBlocking {
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
                        gear = gear,
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
                val request =
                    service.submitSimulation(
                        guildId = guildId,
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterClass = characterClass,
                        characterSpec = characterSpec,
                        characterLevel = 80,
                        characterRace = "human",
                        gear = gear,
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

        @Test
        fun `should use default parameters when not specified`() =
            runBlocking {
                // Arrange
                val guildId = "guild-123"
                val characterName = "Defaultchar"
                val characterRealm = "DefaultRealm"
                val characterClass = "mage"
                val characterSpec = "fire"
                val profileContent = """mage="Defaultchar""""

                every {
                    profileGenerator.generateProfile(
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterClass = characterClass,
                        characterSpec = characterSpec,
                        characterLevel = 80,
                        characterRace = "human",
                        gear = null,
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

                // Act - only required parameters, use defaults
                val request =
                    service.submitSimulation(
                        guildId = guildId,
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterClass = characterClass,
                        characterSpec = characterSpec,
                    )

                // Assert
                request shouldNotBe null
                request.iterations shouldBe SimulationRequest.DEFAULT_ITERATIONS
                request.fightLengthSeconds shouldBe SimulationRequest.DEFAULT_FIGHT_LENGTH_SECONDS
                verify {
                    profileGenerator.generateProfile(
                        characterName,
                        characterRealm,
                        characterClass,
                        characterSpec,
                        80,
                        "human",
                        null,
                    )
                }
            }

        @Test
        fun `should accept custom iterations and fight length`() =
            runBlocking {
                // Arrange
                val guildId = "guild-123"
                val characterName = "Customchar"
                val characterRealm = "CustomRealm"
                val characterClass = "rogue"
                val characterSpec = "assassination"
                val profileContent = """rogue="Customchar""""
                val customIterations = 5000
                val customFightLength = 180

                every {
                    profileGenerator.generateProfile(any(), any(), any(), any(), any(), any(), any())
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
                val request =
                    service.submitSimulation(
                        guildId = guildId,
                        characterName = characterName,
                        characterRealm = characterRealm,
                        characterClass = characterClass,
                        characterSpec = characterSpec,
                        iterations = customIterations,
                        fightLengthSeconds = customFightLength,
                    )

                // Assert
                request.iterations shouldBe customIterations
                request.fightLengthSeconds shouldBe customFightLength
            }
    }

    @Nested
    inner class ExecutePendingSimulations {
        @Test
        fun `should execute all pending simulations`() =
            runBlocking {
                // Arrange
                val profile = createProfile()
                val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)
                val results =
                    listOf(
                        SimulationResult.create(
                            itemId = 12345L,
                            itemName = "Test Item",
                            slot = "head",
                            dpsGain = 1000.0,
                            percentGain = 1.0,
                            simulatedAt = Instant.now(),
                        ),
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
        fun `should mark request as failed when executor fails`() =
            runBlocking {
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

        @Test
        fun `should return zero when no pending simulations`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            val executed = service.executePendingSimulations()

            // Assert
            executed shouldBe 0
            coVerify(exactly = 0) { simulationExecutor.execute(any()) }
        }

        @Test
        fun `should continue executing remaining simulations when one throws exception`() =
            runBlocking {
                // Arrange
                val profile1 = createProfile()
                val profile2 =
                    SimulationProfile.create(
                        guildId = "guild-123",
                        characterName = "Secondchar",
                        characterRealm = "TestRealm",
                        profileContent = """warrior="Secondchar"""",
                        createdAt = Instant.now(),
                    )
                val pendingRequest1 = SimulationRequest.create(profile = profile1).withId(1L)
                val pendingRequest2 = SimulationRequest.create(profile = profile2).withId(2L)
                val results =
                    listOf(
                        SimulationResult.create(
                            itemId = 99999L,
                            itemName = "Success Item",
                            slot = "chest",
                            dpsGain = 500.0,
                            percentGain = 0.5,
                            simulatedAt = Instant.now(),
                        ),
                    )

                every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest1, pendingRequest2)

                // First request throws an exception in executeSimulation
                coEvery { simulationExecutor.execute(match { it.id == 1L }) } throws RuntimeException("Unexpected error")
                // Second request succeeds
                coEvery { simulationExecutor.execute(match { it.id == 2L }) } returns Result.success(results)

                // Act
                val executed = service.executePendingSimulations()

                // Assert - first one threw exception (not counted), second one succeeded
                executed shouldBe 1
            }

        @Test
        fun `should save results when profileId exists after successful execution`() =
            runBlocking {
                // Arrange
                val profile = createProfile()
                val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)
                val results =
                    listOf(
                        SimulationResult.create(
                            itemId = 12345L,
                            itemName = "Test Item",
                            slot = "head",
                            dpsGain = 1000.0,
                            percentGain = 1.0,
                            simulatedAt = Instant.now(),
                        ),
                    )

                every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest)
                coEvery { simulationExecutor.execute(any()) } returns Result.success(results)
                every {
                    simulationRepository.findProfileIdByCharacter(
                        profile.guildId,
                        profile.characterName,
                        profile.characterRealm,
                    )
                } returns 42L

                // Act
                service.executePendingSimulations()

                // Assert
                verify { simulationRepository.saveResult(42L, any()) }
            }

        @Test
        fun `should not save results when profileId is null after successful execution`() =
            runBlocking {
                // Arrange
                val profile = createProfile()
                val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)
                val results =
                    listOf(
                        SimulationResult.create(
                            itemId = 12345L,
                            itemName = "Test Item",
                            slot = "head",
                            dpsGain = 1000.0,
                            percentGain = 1.0,
                            simulatedAt = Instant.now(),
                        ),
                    )

                every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest)
                coEvery { simulationExecutor.execute(any()) } returns Result.success(results)
                every {
                    simulationRepository.findProfileIdByCharacter(
                        profile.guildId,
                        profile.characterName,
                        profile.characterRealm,
                    )
                } returns null

                // Act
                service.executePendingSimulations()

                // Assert - saveResult should NOT be called since profileId is null
                verify(exactly = 0) { simulationRepository.saveResult(any(), any()) }
            }

        @Test
        fun `should handle failure with null error message`() =
            runBlocking {
                // Arrange
                val profile = createProfile()
                val pendingRequest = SimulationRequest.create(profile = profile).withId(1L)

                every { simulationRepository.findPendingRequests() } returns listOf(pendingRequest)
                // Create an exception with null message
                coEvery { simulationExecutor.execute(any()) } returns Result.failure(RuntimeException())

                // Act
                val executed = service.executePendingSimulations()

                // Assert
                executed shouldBe 1
                val savedRequests = mutableListOf<SimulationRequest>()
                verify { simulationRepository.saveRequest(capture(savedRequests)) }

                val failedRequest = savedRequests.find { it.status == SimulationStatus.FAILED }
                failedRequest shouldNotBe null
                failedRequest?.errorMessage shouldBe "Unknown error"
            }
    }

    @Nested
    inner class GetSimulationResults {
        @Test
        fun `should return results for a character`() {
            // Arrange
            val profile = createProfile()
            val results =
                listOf(
                    SimulationResult.create(
                        itemId = 12345L,
                        itemName = "Test Item",
                        slot = "head",
                        dpsGain = 1000.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    ),
                )

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 1L
            every { simulationRepository.findResultsByProfile(1L) } returns results

            // Act
            val foundResults =
                service.getSimulationResults(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
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
            val results =
                service.getSimulationResults(
                    guildId = "guild-123",
                    characterName = "Unknown",
                    characterRealm = "TestRealm",
                )

            // Assert
            results shouldHaveSize 0
        }

        @Test
        fun `should return empty list when profile exists but profileId is null`() {
            // Arrange
            val profile = createProfile()
            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns null

            // Act
            val results =
                service.getSimulationResults(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                )

            // Assert
            results shouldHaveSize 0
            verify(exactly = 0) { simulationRepository.findResultsByProfile(any()) }
        }

        @Test
        fun `should return empty list when results not found for profile`() {
            // Arrange
            val profile = createProfile()
            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 99L
            every { simulationRepository.findResultsByProfile(99L) } returns emptyList()

            // Act
            val results =
                service.getSimulationResults(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                )

            // Assert
            results shouldHaveSize 0
        }

        @Test
        fun `should return multiple results when multiple simulations exist`() {
            // Arrange
            val profile = createProfile()
            val results =
                listOf(
                    SimulationResult.create(
                        itemId = 12345L,
                        itemName = "First Item",
                        slot = "head",
                        dpsGain = 1000.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    ),
                    SimulationResult.create(
                        itemId = 67890L,
                        itemName = "Second Item",
                        slot = "chest",
                        dpsGain = 500.0,
                        percentGain = 0.5,
                        simulatedAt = Instant.now(),
                    ),
                )

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 1L
            every { simulationRepository.findResultsByProfile(1L) } returns results

            // Act
            val foundResults =
                service.getSimulationResults(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                )

            // Assert
            foundResults shouldHaveSize 2
            foundResults[0].itemId shouldBe 12345L
            foundResults[1].itemId shouldBe 67890L
        }
    }
}
