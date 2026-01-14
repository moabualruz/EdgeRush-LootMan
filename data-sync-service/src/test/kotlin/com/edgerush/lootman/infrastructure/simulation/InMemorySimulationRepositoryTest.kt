package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for InMemorySimulationRepository.
 *
 * Tests the in-memory storage and retrieval of simulation profiles,
 * requests, and results.
 */
class InMemorySimulationRepositoryTest : UnitTest() {

    private lateinit var repository: InMemorySimulationRepository

    private val guildId = "test-guild"
    private val now = Instant.now()

    @BeforeEach
    fun setUp() {
        repository = InMemorySimulationRepository()
    }

    @Nested
    inner class ProfileTests {

        @Test
        fun `should save and retrieve profile by id`() {
            // Given
            val profile = createProfile()

            // When
            val (id, savedProfile) = repository.saveProfile(profile)
            val result = repository.findProfileById(id)

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "TestChar"
            result?.characterRealm shouldBe "Area52"
        }

        @Test
        fun `should return null when profile not found by id`() {
            // When
            val result = repository.findProfileById(999L)

            // Then
            result shouldBe null
        }

        @Test
        fun `should find profile by character`() {
            // Given
            val profile = createProfile("UniqueChar", "UniqueRealm")
            repository.saveProfile(profile)

            // When
            val result = repository.findProfileByCharacter(guildId, "UniqueChar", "UniqueRealm")

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "UniqueChar"
        }

        @Test
        fun `should return null when profile not found by character`() {
            // When
            val result = repository.findProfileByCharacter(guildId, "NonExistent", "NoRealm")

            // Then
            result shouldBe null
        }

        @Test
        fun `should find profile id by character`() {
            // Given
            val profile = createProfile("IdTestChar", "IdTestRealm")
            val (savedId, _) = repository.saveProfile(profile)

            // When
            val result = repository.findProfileIdByCharacter(guildId, "IdTestChar", "IdTestRealm")

            // Then
            result shouldBe savedId
        }

        @Test
        fun `should update existing profile for same character`() {
            // Given
            val profile1 = createProfile(profileContent = "profile_v1")
            val (id1, _) = repository.saveProfile(profile1)

            val profile2 = createProfile(profileContent = "profile_v2")

            // When
            val (id2, _) = repository.saveProfile(profile2)

            // Then - should use same ID for same character
            id2 shouldBe id1

            val result = repository.findProfileById(id1)
            result?.profileContent shouldBe "profile_v2"
        }

        @Test
        fun `should generate unique ids for different characters`() {
            // Given
            val profile1 = createProfile("Char1", "Realm1")
            val profile2 = createProfile("Char2", "Realm2")

            // When
            val (id1, _) = repository.saveProfile(profile1)
            val (id2, _) = repository.saveProfile(profile2)

            // Then
            id1 shouldNotBe id2
        }
    }

    @Nested
    inner class RequestTests {

        @Test
        fun `should save and retrieve request by id`() {
            // Given
            val profile = createProfile()
            val (_, savedProfile) = repository.saveProfile(profile)
            val request = SimulationRequest.create(savedProfile)

            // When
            val savedRequest = repository.saveRequest(request)
            val result = repository.findRequestById(savedRequest.id!!)

            // Then
            result shouldNotBe null
            result?.profile?.characterName shouldBe "TestChar"
        }

        @Test
        fun `should return null when request not found`() {
            // When
            val result = repository.findRequestById(999L)

            // Then
            result shouldBe null
        }

        @Test
        fun `should find pending requests`() {
            // Given
            val profile = createProfile()
            val (_, savedProfile) = repository.saveProfile(profile)

            val pendingRequest = SimulationRequest.create(savedProfile)
            repository.saveRequest(pendingRequest)

            // When
            val result = repository.findPendingRequests()

            // Then
            result.size shouldBe 1
            result[0].status shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should not return non-pending requests`() {
            // Given
            val profile = createProfile()
            val (_, savedProfile) = repository.saveProfile(profile)

            val request = SimulationRequest.create(savedProfile)
            val savedRequest = repository.saveRequest(request)

            // Mark as running (transition from PENDING)
            val runningRequest = savedRequest.markRunning()
            repository.saveRequest(runningRequest)

            // When
            val result = repository.findPendingRequests()

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should assign unique id to saved request`() {
            // Given
            val profile = createProfile()
            val (_, savedProfile) = repository.saveProfile(profile)
            val request = SimulationRequest.create(savedProfile)

            // When
            val savedRequest = repository.saveRequest(request)

            // Then
            savedRequest.id shouldNotBe null
        }
    }

    @Nested
    inner class ResultTests {

        @Test
        fun `should save and retrieve results for profile`() {
            // Given
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)
            val result = createResult()

            // When
            repository.saveResult(profileId, result)
            val results = repository.findResultsByProfile(profileId)

            // Then
            results.size shouldBe 1
            results[0].itemId shouldBe 12345L
        }

        @Test
        fun `should find latest result for item`() {
            // Given
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)

            val oldResult = createResult(dpsGain = 1000.0, simulatedAt = now.minusSeconds(3600))
            val newResult = createResult(dpsGain = 1500.0, simulatedAt = now)

            repository.saveResult(profileId, oldResult)
            repository.saveResult(profileId, newResult)

            // When
            val result = repository.findLatestResultForItem(profileId, 12345L)

            // Then
            result shouldNotBe null
            result?.dpsGain shouldBe 1500.0
        }

        @Test
        fun `should return null when no result for item`() {
            // Given
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)

            // When
            val result = repository.findLatestResultForItem(profileId, 99999L)

            // Then
            result shouldBe null
        }

        @Test
        fun `should return null when results exist for profile but not for requested item`() {
            // Given - exercises the filter branch when results exist but itemId doesn't match
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)
            val result = createResult(itemId = 12345L)
            repository.saveResult(profileId, result)

            // When - search for a different item ID
            val found = repository.findLatestResultForItem(profileId, 99999L)

            // Then
            found shouldBe null
        }

        @Test
        fun `should return null when profile has no results`() {
            // Given - exercises the null check on results[profileId]
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)

            // When - profile exists but no results saved
            val found = repository.findLatestResultForItem(profileId, 12345L)

            // Then
            found shouldBe null
        }

        @Test
        fun `should return empty list when no results for profile`() {
            // Given
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)

            // When
            val results = repository.findResultsByProfile(profileId)

            // Then
            results shouldBe emptyList()
        }

        @Test
        fun `should store multiple results for same profile`() {
            // Given
            val profile = createProfile()
            val (profileId, _) = repository.saveProfile(profile)

            val result1 = createResult(itemId = 111L, itemName = "Item 1")
            val result2 = createResult(itemId = 222L, itemName = "Item 2")
            val result3 = createResult(itemId = 333L, itemName = "Item 3")

            // When
            repository.saveResult(profileId, result1)
            repository.saveResult(profileId, result2)
            repository.saveResult(profileId, result3)

            val results = repository.findResultsByProfile(profileId)

            // Then
            results.size shouldBe 3
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `should clear all data`() {
            // Given
            val profile = createProfile()
            val (profileId, savedProfile) = repository.saveProfile(profile)
            repository.saveRequest(SimulationRequest.create(savedProfile))
            repository.saveResult(profileId, createResult())

            // When
            repository.clear()

            // Then
            repository.findProfileById(profileId) shouldBe null
            repository.findPendingRequests() shouldBe emptyList()
            repository.findResultsByProfile(profileId) shouldBe emptyList()
        }
    }

    // Helper methods

    private fun createProfile(
        characterName: String = "TestChar",
        characterRealm: String = "Area52",
        profileContent: String = "priest=\"TestChar\"\nlevel=80"
    ): SimulationProfile = SimulationProfile.create(
        guildId = guildId,
        characterName = characterName,
        characterRealm = characterRealm,
        profileContent = profileContent,
        createdAt = now
    )

    private fun createResult(
        itemId: Long = 12345L,
        itemName: String = "Test Item",
        slot: String = "head",
        dpsGain: Double = 1000.0,
        percentGain: Double = 2.5,
        simulatedAt: Instant = now
    ): SimulationResult = SimulationResult.create(
        itemId = itemId,
        itemName = itemName,
        slot = slot,
        dpsGain = dpsGain,
        percentGain = percentGain,
        simulatedAt = simulatedAt
    )
}
