package com.edgerush.lootman.api.wishlist

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface WishlistSnapshotCrudService : CrudService<Long, CreateWishlistSnapshotRequest, UpdateWishlistSnapshotRequest, WishlistSnapshotResponse> {
    fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<WishlistSnapshotResponse>

    fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<WishlistSnapshotResponse>

    fun countByRaiderId(raiderId: Long): Long

    fun countByTeamId(teamId: Long): Long
}
