package com.edgerush.lootman.domain.wishlist.repository

import com.edgerush.datasync.entity.WishlistSnapshotEntity

interface WishlistSnapshotRepository {
    fun findById(id: Long): WishlistSnapshotEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<WishlistSnapshotEntity>
    fun count(): Long
    fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity>
    fun countByRaiderId(raiderId: Long): Long
    fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity>
    fun countByTeamId(teamId: Long): Long
    fun save(entity: WishlistSnapshotEntity): WishlistSnapshotEntity
    fun delete(id: Long)
}
