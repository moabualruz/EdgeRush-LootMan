package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for RaiderVaultSlot operations.
 *
 * Extends the generic CrudService with vault-slot-specific query methods.
 */
interface RaiderVaultSlotCrudService : CrudService<Long, CreateRaiderVaultSlotRequest, UpdateRaiderVaultSlotRequest, RaiderVaultSlotResponse> {

    /**
     * Find vault slots by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of vault slots for the raider
     */
    fun findByRaider(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderVaultSlotResponse>

    /**
     * Find unlocked vault slots by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of unlocked vault slots for the raider
     */
    fun findUnlockedByRaider(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderVaultSlotResponse>

    /**
     * Count vault slots for a raider.
     *
     * @param raiderId The raider identifier
     * @return The count of vault slots for the raider
     */
    fun countByRaider(raiderId: Long): Long
}
