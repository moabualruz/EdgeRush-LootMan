package com.edgerush.lootman.application.simulation

import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.stereotype.Service

/**
 * Service to calculate Upgrade Value (UV) from simulation data.
 *
 * Uses SimulationCraft simulation results when available,
 * falling back to wishlist percentages otherwise.
 */
@Service
class UpgradeValueCalculator(
    private val simulationRepository: SimulationRepository,
) {
    companion object {
        private const val DEFAULT_MAX_PERCENT_GAIN = 10.0
    }

    /**
     * Calculates the Upgrade Value for an item.
     *
     * Priority:
     * 1. Use simulation results if available
     * 2. Fall back to wishlist percentage if provided
     * 3. Return zero if no data available
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param characterRealm The realm name
     * @param itemId The WoW item ID
     * @param wishlistFallback Optional wishlist to use as fallback
     * @param maxPercentGain Maximum percent gain for normalization (default: 10%)
     * @return The calculated UpgradeValue (0.0-1.0)
     */
    fun calculateUpgradeValue(
        guildId: String,
        characterName: String,
        characterRealm: String,
        itemId: ItemId,
        wishlistFallback: Wishlist?,
        maxPercentGain: Double = DEFAULT_MAX_PERCENT_GAIN,
    ): UpgradeValue {
        // Try to get simulation-based UV first
        val simulationValue =
            getSimulationBasedValue(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                itemId = itemId,
                maxPercentGain = maxPercentGain,
            )

        if (simulationValue != null) {
            return simulationValue
        }

        // Fall back to wishlist if provided
        if (wishlistFallback != null) {
            return calculateFromWishlist(wishlistFallback, itemId)
        }

        // No data available
        return UpgradeValue.zero()
    }

    /**
     * Checks if simulation data exists for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param characterRealm The realm name
     * @return true if simulation results are available
     */
    fun hasSimulationData(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): Boolean {
        val profile =
            simulationRepository.findProfileByCharacter(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
            ) ?: return false

        // We need to find the profile ID - this is a limitation of the current interface
        // In production, we'd have the ID stored. For now, check if any results exist
        // by querying results for this profile
        val profileId = getProfileId(guildId, characterName, characterRealm) ?: return false
        val results = simulationRepository.findResultsByProfile(profileId)
        return results.isNotEmpty()
    }

    private fun getSimulationBasedValue(
        guildId: String,
        characterName: String,
        characterRealm: String,
        itemId: ItemId,
        maxPercentGain: Double,
    ): UpgradeValue? {
        val profile =
            simulationRepository.findProfileByCharacter(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
            ) ?: return null

        val profileId = getProfileId(guildId, characterName, characterRealm) ?: return null

        val result =
            simulationRepository.findLatestResultForItem(profileId, itemId.value)
                ?: return null

        val normalizedValue = result.normalizedUpgradeValue(maxPercentGain)
        return UpgradeValue.of(normalizedValue)
    }

    private fun calculateFromWishlist(
        wishlist: Wishlist,
        itemId: ItemId,
    ): UpgradeValue {
        val upgradePercentage = wishlist.getUpgradePercentage(itemId) ?: 0.0
        // Wishlist percentages are 0-100, normalize to 0-1
        val normalizedValue = (upgradePercentage / 100.0).coerceIn(0.0, 1.0)
        return UpgradeValue.of(normalizedValue)
    }

    private fun getProfileId(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): Long? {
        return simulationRepository.findProfileIdByCharacter(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
        )
    }
}
