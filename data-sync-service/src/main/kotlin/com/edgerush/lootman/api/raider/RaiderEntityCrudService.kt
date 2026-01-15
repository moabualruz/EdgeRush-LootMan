package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface RaiderEntityCrudService : CrudService<Long, CreateRaiderEntityRequest, UpdateRaiderEntityRequest, RaiderEntityResponse> {
    fun findByRealm(
        realm: String,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderEntityResponse>

    fun findByRegion(
        region: String,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderEntityResponse>

    fun countByRealm(realm: String): Long
}
