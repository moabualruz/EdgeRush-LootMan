package com.edgerush.lootman.domain.simulation.repository

import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult

/**
 * Repository port for simulation persistence.
 *
 * Defines the contract for storing and retrieving simulation profiles,
 * requests, and results. Implementations should handle the actual
 * database operations.
 */
interface SimulationRepository {
    /**
     * Saves or updates a simulation profile.
     *
     * If a profile for the same guild/character/realm exists, it will be updated.
     *
     * @param profile The profile to save
     * @return Pair of profile ID and the saved profile
     */
    fun saveProfile(profile: SimulationProfile): Pair<Long, SimulationProfile>

    /**
     * Finds a profile by its ID.
     *
     * @param id The profile ID
     * @return The profile, or null if not found
     */
    fun findProfileById(id: Long): SimulationProfile?

    /**
     * Finds a profile by guild and character identifiers.
     *
     * @param guildId The guild ID
     * @param characterName The character name
     * @param characterRealm The realm name
     * @return The profile, or null if not found
     */
    fun findProfileByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): SimulationProfile?

    /**
     * Finds a profile ID by guild and character identifiers.
     *
     * @param guildId The guild ID
     * @param characterName The character name
     * @param characterRealm The realm name
     * @return The profile ID, or null if not found
     */
    fun findProfileIdByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): Long?

    /**
     * Saves a simulation request.
     *
     * @param request The request to save
     * @return The saved request with ID populated
     */
    fun saveRequest(request: SimulationRequest): SimulationRequest

    /**
     * Finds a request by its ID.
     *
     * @param id The request ID
     * @return The request, or null if not found
     */
    fun findRequestById(id: Long): SimulationRequest?

    /**
     * Finds all pending simulation requests.
     *
     * @return List of pending requests
     */
    fun findPendingRequests(): List<SimulationRequest>

    /**
     * Saves a simulation result for a profile.
     *
     * @param profileId The profile ID this result belongs to
     * @param result The simulation result to save
     */
    fun saveResult(
        profileId: Long,
        result: SimulationResult,
    )

    /**
     * Finds the latest simulation result for a specific item.
     *
     * @param profileId The profile ID
     * @param itemId The WoW item ID
     * @return The latest result, or null if none exists
     */
    fun findLatestResultForItem(
        profileId: Long,
        itemId: Long,
    ): SimulationResult?

    /**
     * Finds all simulation results for a profile.
     *
     * @param profileId The profile ID
     * @return List of all results for the profile
     */
    fun findResultsByProfile(profileId: Long): List<SimulationResult>
}
