package com.edgerush.lootman.api.graphql.dataloader

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.springframework.stereotype.Component

/**
 * Batch loader for Raider entities.
 *
 * This loader batches multiple raider ID lookups into a single database query,
 * preventing N+1 query issues in GraphQL resolvers.
 *
 * Results are returned in the same order as the input IDs, with null values
 * for IDs that don't exist in the database.
 */
@Component
class RaiderBatchLoader(
    private val raiderRepository: RaiderRepository,
) {
    /**
     * Load multiple raiders by their IDs in a single batch query.
     *
     * @param ids The list of raider IDs to load
     * @return List of raiders in the same order as input IDs, with null for missing raiders
     */
    suspend fun load(ids: List<RaiderId>): List<Raider?> {
        if (ids.isEmpty()) return emptyList()

        val raiders = raiderRepository.findByIds(ids)
        val raiderMap = raiders.associateBy { it.id }

        // Return results in the same order as input IDs
        return ids.map { raiderMap[it] }
    }
}
