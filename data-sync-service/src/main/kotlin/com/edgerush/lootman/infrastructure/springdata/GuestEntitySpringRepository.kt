package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.GuestEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for GuestEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface GuestEntitySpringRepository :
    CrudRepository<GuestEntity, Long>,
    PagingAndSortingRepository<GuestEntity, Long> {

    fun findByGuestId(guestId: Long): GuestEntity?

    fun findByBlizzardId(blizzardId: Long): GuestEntity?

    fun existsByGuestId(guestId: Long): Boolean

    fun deleteByGuestId(guestId: Long)
}
