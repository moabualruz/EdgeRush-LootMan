package com.edgerush.lootman.api.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.simulation.SimulationService
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

class SimulationControllerTest : UnitTest() {
    private lateinit var simulationService: SimulationService
    private lateinit var simulationRepository: SimulationRepository
    private lateinit var controller: SimulationController

    @BeforeEach
    fun setUp() {
        simulationService = mockk()
        simulationRepository = mockk()
        controller = SimulationController(simulationService, simulationRepository)
    }

    private fun createTestProfile(
        guildId: String = "guild-123",
        characterName: String = "Testchar",
        characterRealm: String = "TestRealm",
    ): SimulationProfile {
        return SimulationProfile.create(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
            profileContent = """warrior="$characterName"""",
            createdAt = Instant.now(),
        )
    }

    private fun createTestRequest(
        profile: SimulationProfile = createTestProfile(),
        status: SimulationStatus = SimulationStatus.PENDING,
    ): SimulationRequest {
        return SimulationRequest.create(profile = profile)
    }

    @Nested
    inner class SubmitSimulation {
        @Test
        fun `should submit simulation and return accepted status`() {
            // Arrange
            val profile = createTestProfile()
            val request = createTestRequest(profile)
            every {
                simulationService.submitSimulation(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    iterations = 10000,
                    fightLengthSeconds = 300,
                )
            } returns request

            val submitRequest =
                SubmitSimulationRequest(
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                )

            // Act
            val response = controller.submitSimulation("guild-123", "Testchar", submitRequest)

            // Assert
            response.statusCode shouldBe HttpStatus.ACCEPTED
            response.body shouldNotBe null
            response.body?.characterName shouldBe "Testchar"
            response.body?.status shouldBe SimulationStatus.PENDING

            verify {
                simulationService.submitSimulation(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    iterations = 10000,
                    fightLengthSeconds = 300,
                )
            }
        }
    }

    @Nested
    inner class GetSimulationStatus {
        @Test
        fun `should return simulation request when found`() {
            // Arrange
            val request = createTestRequest().withId(42L)
            every { simulationRepository.findRequestById(42L) } returns request

            // Act
            val response = controller.getSimulationStatus(42L)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.id shouldBe 42L
            response.body?.status shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should return not found when request does not exist`() {
            // Arrange
            every { simulationRepository.findRequestById(999L) } returns null

            // Act
            val response = controller.getSimulationStatus(999L)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetSimulationResults {
        @Test
        fun `should return results for character`() {
            // Arrange
            val results =
                listOf(
                    SimulationResult.create(
                        itemId = 12345L,
                        itemName = "Test Sword",
                        slot = "main_hand",
                        dpsGain = 1000.0,
                        percentGain = 1.5,
                        simulatedAt = Instant.now(),
                    ),
                )
            every {
                simulationService.getSimulationResults("guild-123", "Testchar", "TestRealm")
            } returns results

            // Act
            val response = controller.getSimulationResults("guild-123", "Testchar", "TestRealm")

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.results?.size shouldBe 1
            response.body?.results?.first()?.itemId shouldBe 12345L
            response.body?.results?.first()?.dpsGain shouldBe 1000.0
        }

        @Test
        fun `should return empty results when no simulations exist`() {
            // Arrange
            every {
                simulationService.getSimulationResults("guild-123", "Unknown", "TestRealm")
            } returns emptyList()

            // Act
            val response = controller.getSimulationResults("guild-123", "Unknown", "TestRealm")

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.results shouldBe emptyList()
        }
    }

    @Nested
    inner class GetPendingSimulations {
        @Test
        fun `should return pending simulations for guild`() {
            // Arrange
            val profile = createTestProfile(guildId = "guild-123")
            val request = createTestRequest(profile).withId(1L)
            every { simulationRepository.findPendingRequests() } returns listOf(request)

            // Act
            val response = controller.getPendingSimulations("guild-123")

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.size shouldBe 1
            response.body?.first()?.guildId shouldBe "guild-123"
        }

        @Test
        fun `should filter out other guilds pending simulations`() {
            // Arrange
            val otherGuildProfile = createTestProfile(guildId = "other-guild")
            val request = createTestRequest(otherGuildProfile).withId(1L)
            every { simulationRepository.findPendingRequests() } returns listOf(request)

            // Act
            val response = controller.getPendingSimulations("guild-123")

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldBe emptyList()
        }
    }

    @Nested
    inner class ExecutePendingSimulations {
        @Test
        fun `should execute pending and return count`() {
            // Arrange
            every { simulationService.executePendingSimulations() } returns 5

            // Act
            val response = controller.executePendingSimulations()

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.executedCount shouldBe 5
        }
    }

    @Nested
    inner class GetStatus {
        @Test
        fun `should return service status`() {
            // Arrange
            every { simulationRepository.findPendingRequests() } returns emptyList()

            // Act
            val response = controller.getStatus()

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.status shouldBe "operational"
            response.body?.pendingSimulations shouldBe 0
            response.body?.endpoints shouldNotBe null
        }

        @Test
        fun `should return pending count in status`() {
            // Arrange
            val profile = createTestProfile()
            val requests =
                listOf(
                    createTestRequest(profile).withId(1L),
                    createTestRequest(profile).withId(2L),
                )
            every { simulationRepository.findPendingRequests() } returns requests

            // Act
            val response = controller.getStatus()

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.pendingSimulations shouldBe 2
        }
    }

    @Nested
    inner class SimulationRequestDtoMapping {
        @Test
        fun `should map all fields from SimulationRequest`() {
            // Arrange
            val profile =
                createTestProfile(
                    guildId = "guild-456",
                    characterName = "MapTestChar",
                    characterRealm = "MapRealm",
                )
            val request = createTestRequest(profile).withId(99L)

            // Act
            val dto = SimulationRequestDto.from(request)

            // Assert
            dto.id shouldBe 99L
            dto.characterName shouldBe "MapTestChar"
            dto.characterRealm shouldBe "MapRealm"
            dto.guildId shouldBe "guild-456"
            dto.status shouldBe SimulationStatus.PENDING
            dto.submittedAt shouldNotBe null
            dto.completedAt shouldBe null
            dto.errorMessage shouldBe null
            dto.resultCount shouldBe 0
        }

        @Test
        fun `should map completed request with results`() {
            // Arrange
            val profile = createTestProfile()
            val results =
                listOf(
                    SimulationResult.create(
                        itemId = 1L,
                        itemName = "Item1",
                        slot = "head",
                        dpsGain = 100.0,
                        percentGain = 1.0,
                        simulatedAt = Instant.now(),
                    ),
                    SimulationResult.create(
                        itemId = 2L,
                        itemName = "Item2",
                        slot = "neck",
                        dpsGain = 200.0,
                        percentGain = 2.0,
                        simulatedAt = Instant.now(),
                    ),
                )
            val request =
                createTestRequest(profile)
                    .withId(100L)
                    .markRunning()
                    .markCompleted(results)

            // Act
            val dto = SimulationRequestDto.from(request)

            // Assert
            dto.status shouldBe SimulationStatus.COMPLETED
            dto.completedAt shouldNotBe null
            dto.resultCount shouldBe 2
        }

        @Test
        fun `should map failed request with error message`() {
            // Arrange
            val profile = createTestProfile()
            val request =
                createTestRequest(profile)
                    .withId(101L)
                    .markRunning()
                    .markFailed("Simulation timeout")

            // Act
            val dto = SimulationRequestDto.from(request)

            // Assert
            dto.status shouldBe SimulationStatus.FAILED
            dto.errorMessage shouldBe "Simulation timeout"
        }
    }

    @Nested
    inner class SimulationResultDtoMapping {
        @Test
        fun `should map all fields from SimulationResult`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Awesome Sword",
                    slot = "main_hand",
                    dpsGain = 5000.0,
                    percentGain = 3.5,
                    simulatedAt = Instant.now(),
                )

            // Act
            val dto = SimulationResultDto.from(result)

            // Assert
            dto.itemId shouldBe 12345L
            dto.itemName shouldBe "Awesome Sword"
            dto.slot shouldBe "main_hand"
            dto.dpsGain shouldBe 5000.0
            dto.percentGain shouldBe 3.5
            dto.isUpgrade shouldBe true
            dto.normalizedValue shouldBe result.normalizedUpgradeValue()
            dto.simulatedAt shouldNotBe null
        }

        @Test
        fun `should identify non-upgrade when dps gain is negative`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 99999L,
                    itemName = "Downgrade Helm",
                    slot = "head",
                    dpsGain = -500.0,
                    percentGain = -0.5,
                    simulatedAt = Instant.now(),
                )

            // Act
            val dto = SimulationResultDto.from(result)

            // Assert
            dto.isUpgrade shouldBe false
        }
    }

    @Nested
    inner class SubmitSimulationRequestDefaults {
        @Test
        fun `should use custom values when provided`() {
            // Arrange
            val profile = createTestProfile()
            val request = createTestRequest(profile)
            every {
                simulationService.submitSimulation(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "mage",
                    characterSpec = "fire",
                    characterLevel = 70,
                    characterRace = "dwarf",
                    iterations = 5000,
                    fightLengthSeconds = 180,
                )
            } returns request

            val submitRequest =
                SubmitSimulationRequest(
                    characterRealm = "TestRealm",
                    characterClass = "mage",
                    characterSpec = "fire",
                    characterLevel = 70,
                    characterRace = "dwarf",
                    iterations = 5000,
                    fightLengthSeconds = 180,
                )

            // Act
            val response = controller.submitSimulation("guild-123", "Testchar", submitRequest)

            // Assert
            response.statusCode shouldBe HttpStatus.ACCEPTED
            verify {
                simulationService.submitSimulation(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "mage",
                    characterSpec = "fire",
                    characterLevel = 70,
                    characterRace = "dwarf",
                    iterations = 5000,
                    fightLengthSeconds = 180,
                )
            }
        }
    }
}
