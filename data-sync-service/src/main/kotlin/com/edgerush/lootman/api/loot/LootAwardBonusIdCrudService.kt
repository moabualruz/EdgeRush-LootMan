package com.edgerush.lootman.api.loot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface LootAwardBonusIdCrudService : CrudService<Long, CreateLootAwardBonusIdRequest, UpdateLootAwardBonusIdRequest, LootAwardBonusIdResponse> {
    fun findByLootAwardId(lootAwardId: Long, pageRequest: PageRequest): PagedResponse<LootAwardBonusIdResponse>
    fun countByLootAwardId(lootAwardId: Long): Long
}
