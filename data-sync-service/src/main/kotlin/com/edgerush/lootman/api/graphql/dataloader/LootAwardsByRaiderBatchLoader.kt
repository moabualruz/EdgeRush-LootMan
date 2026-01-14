package com.edgerush.lootman.api.graphql.dataloader

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Component

/**
 * Batch loader for LootAward entities grouped by raider.
 *
 * This loader batches multiple raider ID lookups into a single database query,
 * preventing N+1 query issues when loading loot awards for multiple raiders.
 *
 * Results are returned in the same order as the input raider IDs, with empty lists
 * for raiders that have no loot awards.
 */
@Component
class LootAwardsByRaiderBatchLoader(
    private val lootAwardRepository: LootAwardRepository,
) {
    /**
     * Load loot awards for multiple raiders in a single batch query.
     *
     * @param raiderIds The list of raider IDs to load awards for
     * @return List of loot award lists, one per raider ID in the same order as input
     */
    suspend fun load(raiderIds: List<RaiderId>): List<List<LootAward>> {
        if (raiderIds.isEmpty()) return emptyList()

        val awards = lootAwardRepository.findByRaiderIds(raiderIds)
        val awardsByRaider = awards.groupBy { it.raiderId }

        // Return results in the same order as input raider IDs
        return raiderIds.map { raiderId ->
            awardsByRaider[raiderId] ?: emptyList()
        }
    }
}
