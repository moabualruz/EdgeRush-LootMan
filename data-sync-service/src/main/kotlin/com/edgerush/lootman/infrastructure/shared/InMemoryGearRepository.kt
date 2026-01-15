package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.repository.GearRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of GearRepository.
 *
 * Uses ConcurrentHashMap for thread-safe operations.
 * Storage key is a composite of RaiderId and GearSetType.
 */
class InMemoryGearRepository : GearRepository {
    private val storage = ConcurrentHashMap<GearKey, GearSet>()

    override fun findCurrentGear(raiderId: RaiderId): GearSet? = findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

    override fun findByRaiderIdAndType(
        raiderId: RaiderId,
        gearSetType: GearSetType,
    ): GearSet? = storage[GearKey(raiderId, gearSetType)]

    override fun save(
        raiderId: RaiderId,
        gearSet: GearSet,
    ): GearSet {
        storage[GearKey(raiderId, gearSet.gearSetType)] = gearSet
        return gearSet
    }

    private data class GearKey(val raiderId: RaiderId, val gearSetType: GearSetType)
}
