package com.edgerush.lootman.api.loot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for LootAward entity operations.
 *
 * Extends the generic CrudService with loot-award-specific query methods.
 */
interface LootAwardCrudService : CrudService<Long, CreateLootAwardEntityRequest, UpdateLootAwardEntityRequest, LootAwardEntityResponse> {
    /**
     * Find loot awards by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of loot awards for the raider
     */
    fun findByRaider(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardEntityResponse>

    /**
     * Find loot awards by item with pagination.
     *
     * @param itemId The item identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of loot awards for the item
     */
    fun findByItem(
        itemId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardEntityResponse>

    /**
     * Find loot awards by tier with pagination.
     *
     * @param tier The loot tier
     * @param pageRequest Pagination parameters
     * @return Paginated list of loot awards for the tier
     */
    fun findByTier(
        tier: String,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardEntityResponse>

    /**
     * Count loot awards for a raider.
     *
     * @param raiderId The raider identifier
     * @return The count of loot awards for the raider
     */
    fun countByRaider(raiderId: Long): Long
}
