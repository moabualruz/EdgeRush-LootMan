package com.edgerush.lootman.api.loot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface LootAwardOldItemCrudService : CrudService<Long, CreateLootAwardOldItemRequest, UpdateLootAwardOldItemRequest, LootAwardOldItemResponse> {
    fun findByLootAwardId(
        lootAwardId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardOldItemResponse>

    fun countByLootAwardId(lootAwardId: Long): Long
}
