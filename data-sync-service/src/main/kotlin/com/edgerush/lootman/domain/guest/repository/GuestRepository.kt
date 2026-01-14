package com.edgerush.lootman.domain.guest.repository

import com.edgerush.datasync.entity.GuestEntity

interface GuestRepository {
    fun findById(guestId: Long): GuestEntity?
    fun existsById(guestId: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<GuestEntity>
    fun count(): Long
    fun save(entity: GuestEntity): GuestEntity
    fun delete(guestId: Long)
}
