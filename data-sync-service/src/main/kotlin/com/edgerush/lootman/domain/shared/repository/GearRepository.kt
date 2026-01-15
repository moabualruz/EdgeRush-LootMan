package com.edgerush.lootman.domain.shared.repository

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType

/**
 * Repository interface for GearSet aggregate.
 */
interface GearRepository {
    /**
     * Finds the current equipped gear for a raider.
     *
     * @param raiderId The raider's unique identifier
     * @return The equipped gear set if found, null otherwise
     */
    fun findCurrentGear(raiderId: RaiderId): GearSet?

    /**
     * Finds a specific gear set type for a raider.
     *
     * @param raiderId The raider's unique identifier
     * @param gearSetType The type of gear set (equipped or best)
     * @return The gear set if found, null otherwise
     */
    fun findByRaiderIdAndType(
        raiderId: RaiderId,
        gearSetType: GearSetType,
    ): GearSet?

    /**
     * Saves a gear set.
     *
     * @param raiderId The raider's unique identifier
     * @param gearSet The gear set to save
     * @return The saved gear set
     */
    fun save(
        raiderId: RaiderId,
        gearSet: GearSet,
    ): GearSet
}
