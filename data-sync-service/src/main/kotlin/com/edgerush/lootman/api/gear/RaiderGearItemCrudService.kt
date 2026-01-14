package com.edgerush.lootman.api.gear

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderGearItemCrudService : CrudService<Long, CreateRaiderGearItemRequest, UpdateRaiderGearItemRequest, RaiderGearItemResponse> {
    fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderGearItemResponse>
    fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String, pageRequest: PageRequest): PagedResponse<RaiderGearItemResponse>
    fun countByRaiderId(raiderId: Long): Long
    fun countByRaiderIdAndGearSet(raiderId: Long, gearSet: String): Long
}
