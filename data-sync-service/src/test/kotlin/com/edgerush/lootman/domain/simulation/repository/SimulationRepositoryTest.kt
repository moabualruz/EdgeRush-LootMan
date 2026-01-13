package com.edgerush.lootman.domain.simulation.repository

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Tests for SimulationRepository interface contract.
 *
 * These tests use an in-memory implementation to verify the interface behavior.
 * Integration tests should verify the actual database implementation.
 */
class SimulationRepositoryTest : UnitTest() {

    private lateinit var repository: InMemorySimulationRepository

    @BeforeEach
    fun setUp() {
        repository = InMemorySimulationRepository()
    }

    private fun createProfile(
        guildId: String = "guild-123",
        characterName: String = "Testchar",
        characterRealm: String = "TestRealm"
    ): SimulationProfile {
        return SimulationProfile.create(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
            profileContent = """warrior="$characterName"""",
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class SaveProfile {
        @Test
        fun `should save and retrieve profile by id`() {
            // Arrange
            val profile = createProfile()

            // Act
            val savedProfile = repository.saveProfile(profile)
            val found = repository.findProfileById(savedProfile.first)

            // Assert
            savedProfile.first shouldNotBe null
            found shouldBe profile
        }

        @Test
        fun `should update existing profile for same character`() {
            // Arrange
            val profile1 = createProfile(characterName = "Testchar")
            val profile2 = SimulationProfile.create(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                profileContent = """warrior="Testchar" # Updated""",
                createdAt = Instant.now()
            )

            // Act
            repository.saveProfile(profile1)
            val savedProfile2 = repository.saveProfile(profile2)
            val found = repository.findProfileByCharacter("guild-123", "Testchar", "TestRealm")

            // Assert
            found shouldNotBe null
            found?.profileContent shouldBe profile2.profileContent
        }
    }

    @Nested
    inner class FindProfileByCharacter {
        @Test
        fun `should find profile by guild and character`() {
            // Arrange
            val profile = createProfile()
            repository.saveProfile(profile)

            // Act
            val found = repository.findProfileByCharacter("guild-123", "Testchar", "TestRealm")

            // Assert
            found shouldNotBe null
            found shouldBe profile
        }

        @Test
        fun `should return null when profile not found`() {
            // Act
            val found = repository.findProfileByCharacter("guild-123", "Unknown", "TestRealm")

            // Assert
            found shouldBe null
        }
    }

    @Nested
    inner class SaveRequest {
        @Test
        fun `should save and retrieve request by id`() {
            // Arrange
            val profile = createProfile()
            repository.saveProfile(profile)
            val request = SimulationRequest.create(profile = profile)

            // Act
            val saved = repository.saveRequest(request)
            val found = repository.findRequestById(saved.id!!)

            // Assert
            saved.id shouldNotBe null
            found shouldNotBe null
            found?.profile shouldBe profile
            found?.status shouldBe SimulationStatus.PENDING
        }
    }

    @Nested
    inner class FindPendingRequests {
        @Test
        fun `should find all pending requests`() {
            // Arrange
            val profile1 = createProfile(characterName = "Char1")
            val profile2 = createProfile(characterName = "Char2")
            repository.saveProfile(profile1)
            repository.saveProfile(profile2)

            val request1 = SimulationRequest.create(profile = profile1)
            val request2 = SimulationRequest.create(profile = profile2)
            repository.saveRequest(request1)
            repository.saveRequest(request2)

            // Act
            val pending = repository.findPendingRequests()

            // Assert
            pending shouldHaveSize 2
        }

        @Test
        fun `should not include completed requests`() {
            // Arrange
            val profile = createProfile()
            repository.saveProfile(profile)
            val request = SimulationRequest.create(profile = profile)
            val saved = repository.saveRequest(request)
            val completed = saved.markRunning().markCompleted(emptyList())
            repository.saveRequest(completed)

            // Act
            val pending = repository.findPendingRequests()

            // Assert
            pending.shouldBeEmpty()
        }
    }

    @Nested
    inner class SaveResult {
        @Test
        fun `should save and retrieve results by item id`() {
            // Arrange
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)
            val result = SimulationResult.create(
                itemId = 12345L,
                itemName = "Test Item",
                slot = "head",
                dpsGain = 1000.0,
                percentGain = 1.0,
                simulatedAt = Instant.now()
            )

            // Act
            repository.saveResult(profileId, result)
            val found = repository.findLatestResultForItem(profileId, 12345L)

            // Assert
            found shouldNotBe null
            found?.itemId shouldBe 12345L
            found?.dpsGain shouldBe 1000.0
        }

        @Test
        fun `should return latest result when multiple exist`() {
            // Arrange
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)
            val oldResult = SimulationResult.create(
                itemId = 12345L,
                itemName = "Test Item",
                slot = "head",
                dpsGain = 500.0,
                percentGain = 0.5,
                simulatedAt = Instant.now().minusSeconds(3600)
            )
            val newResult = SimulationResult.create(
                itemId = 12345L,
                itemName = "Test Item",
                slot = "head",
                dpsGain = 1000.0,
                percentGain = 1.0,
                simulatedAt = Instant.now()
            )

            // Act
            repository.saveResult(profileId, oldResult)
            repository.saveResult(profileId, newResult)
            val found = repository.findLatestResultForItem(profileId, 12345L)

            // Assert
            found shouldNotBe null
            found?.dpsGain shouldBe 1000.0
        }
    }

    @Nested
    inner class FindResultsByProfile {
        @Test
        fun `should find all results for a profile`() {
            // Arrange
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)
            val result1 = SimulationResult.create(
                itemId = 12345L,
                itemName = "Item 1",
                slot = "head",
                dpsGain = 1000.0,
                percentGain = 1.0,
                simulatedAt = Instant.now()
            )
            val result2 = SimulationResult.create(
                itemId = 12346L,
                itemName = "Item 2",
                slot = "neck",
                dpsGain = 500.0,
                percentGain = 0.5,
                simulatedAt = Instant.now()
            )
            repository.saveResult(profileId, result1)
            repository.saveResult(profileId, result2)

            // Act
            val results = repository.findResultsByProfile(profileId)

            // Assert
            results shouldHaveSize 2
            results.map { it.itemId } shouldContain 12345L
            results.map { it.itemId } shouldContain 12346L
        }
    }
}

/**
 * In-memory implementation for testing the repository interface contract.
 */
class InMemorySimulationRepository : SimulationRepository {
    private val profiles = mutableMapOf<Long, SimulationProfile>()
    private val requests = mutableMapOf<Long, SimulationRequest>()
    private val results = mutableMapOf<Long, MutableList<SimulationResult>>()
    private var profileIdCounter = 1L
    private var requestIdCounter = 1L

    override fun saveProfile(profile: SimulationProfile): Pair<Long, SimulationProfile> {
        val existingId = profiles.entries
            .find {
                it.value.guildId == profile.guildId &&
                    it.value.characterName == profile.characterName &&
                    it.value.characterRealm == profile.characterRealm
            }?.key

        val id = existingId ?: profileIdCounter++
        profiles[id] = profile
        return id to profile
    }

    override fun findProfileById(id: Long): SimulationProfile? = profiles[id]

    override fun findProfileByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): SimulationProfile? {
        return profiles.values.find {
            it.guildId == guildId &&
                it.characterName == characterName &&
                it.characterRealm == characterRealm
        }
    }

    override fun findProfileIdByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): Long? {
        return profiles.entries.find {
            it.value.guildId == guildId &&
                it.value.characterName == characterName &&
                it.value.characterRealm == characterRealm
        }?.key
    }

    override fun saveRequest(request: SimulationRequest): SimulationRequest {
        val id = request.id ?: requestIdCounter++
        val savedRequest = if (request.id == null) {
            request.withId(id)
        } else {
            request
        }
        requests[id] = savedRequest
        return savedRequest
    }

    override fun findRequestById(id: Long): SimulationRequest? = requests[id]

    override fun findPendingRequests(): List<SimulationRequest> {
        return requests.values.filter { it.status == SimulationStatus.PENDING }
    }

    override fun saveResult(profileId: Long, result: SimulationResult) {
        results.getOrPut(profileId) { mutableListOf() }.add(result)
    }

    override fun findLatestResultForItem(profileId: Long, itemId: Long): SimulationResult? {
        return results[profileId]
            ?.filter { it.itemId == itemId }
            ?.maxByOrNull { it.simulatedAt }
    }

    override fun findResultsByProfile(profileId: Long): List<SimulationResult> {
        return results[profileId] ?: emptyList()
    }
}
