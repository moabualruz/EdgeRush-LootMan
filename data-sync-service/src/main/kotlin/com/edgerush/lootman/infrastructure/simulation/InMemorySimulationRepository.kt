package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory implementation of SimulationRepository.
 *
 * Stores simulation profiles, requests, and results in memory using
 * ConcurrentHashMap for thread-safe operations.
 *
 * Suitable for testing and development without database dependency.
 */
@Repository("inMemorySimulationRepository")
class InMemorySimulationRepository : SimulationRepository {

    private val profileIdGenerator = AtomicLong(1)
    private val requestIdGenerator = AtomicLong(1)

    // Storage for profiles keyed by profile ID
    private val profiles = ConcurrentHashMap<Long, SimulationProfile>()

    // Index for finding profiles by character
    private val profilesByCharacter = ConcurrentHashMap<String, Long>()

    // Storage for requests keyed by request ID
    private val requests = ConcurrentHashMap<Long, SimulationRequest>()

    // Storage for results keyed by profile ID
    private val results = ConcurrentHashMap<Long, MutableList<SimulationResult>>()

    override fun saveProfile(profile: SimulationProfile): Pair<Long, SimulationProfile> {
        val characterKey = buildCharacterKey(profile.guildId, profile.characterName, profile.characterRealm)

        // Check if profile for this character already exists
        val existingId = profilesByCharacter[characterKey]

        val profileId = existingId ?: profileIdGenerator.getAndIncrement()

        profiles[profileId] = profile
        profilesByCharacter[characterKey] = profileId

        return Pair(profileId, profile)
    }

    override fun findProfileById(id: Long): SimulationProfile? = profiles[id]

    override fun findProfileByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): SimulationProfile? {
        val characterKey = buildCharacterKey(guildId, characterName, characterRealm)
        val profileId = profilesByCharacter[characterKey] ?: return null
        return profiles[profileId]
    }

    override fun findProfileIdByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): Long? {
        val characterKey = buildCharacterKey(guildId, characterName, characterRealm)
        return profilesByCharacter[characterKey]
    }

    override fun saveRequest(request: SimulationRequest): SimulationRequest {
        val savedRequest = if (request.id == null) {
            val newId = requestIdGenerator.getAndIncrement()
            request.withId(newId)
        } else {
            request
        }

        requests[savedRequest.id!!] = savedRequest
        return savedRequest
    }

    override fun findRequestById(id: Long): SimulationRequest? = requests[id]

    override fun findPendingRequests(): List<SimulationRequest> {
        return requests.values.filter { it.status == SimulationStatus.PENDING }
    }

    override fun saveResult(profileId: Long, result: SimulationResult) {
        results.computeIfAbsent(profileId) { mutableListOf() }.add(result)
    }

    override fun findLatestResultForItem(profileId: Long, itemId: Long): SimulationResult? {
        return results[profileId]
            ?.filter { it.itemId == itemId }
            ?.maxByOrNull { it.simulatedAt }
    }

    override fun findResultsByProfile(profileId: Long): List<SimulationResult> {
        return results[profileId]?.toList() ?: emptyList()
    }

    /**
     * Clears all stored data (for testing purposes).
     */
    fun clear() {
        profiles.clear()
        profilesByCharacter.clear()
        requests.clear()
        results.clear()
        profileIdGenerator.set(1)
        requestIdGenerator.set(1)
    }

    private fun buildCharacterKey(guildId: String, characterName: String, characterRealm: String): String {
        return "${guildId.lowercase()}-${characterName.lowercase()}-${characterRealm.lowercase()}"
    }
}
