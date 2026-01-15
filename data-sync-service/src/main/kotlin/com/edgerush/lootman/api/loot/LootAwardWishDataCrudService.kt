package com.edgerush.lootman.api.loot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface LootAwardWishDataCrudService : CrudService<Long, CreateLootAwardWishDataRequest, UpdateLootAwardWishDataRequest, LootAwardWishDataResponse> {
    fun findByLootAwardId(
        lootAwardId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardWishDataResponse>

    fun countByLootAwardId(lootAwardId: Long): Long
}
