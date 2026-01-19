package com.edgerush.lootman.infrastructure.guest

import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.lootman.domain.guest.repository.GuestRepository
import com.edgerush.lootman.infrastructure.springdata.GuestEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of GuestRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcGuestRepository(
    private val springRepository: GuestEntitySpringRepository,
) : GuestRepository {

    override fun findById(guestId: Long): GuestEntity? =
        springRepository.findByGuestId(guestId)

    override fun existsById(guestId: Long): Boolean =
        springRepository.existsByGuestId(guestId)

    override fun findAll(offset: Long, limit: Int): List<GuestEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun save(entity: GuestEntity): GuestEntity =
        springRepository.save(entity)

    override fun delete(guestId: Long) {
        springRepository.deleteByGuestId(guestId)
    }
}
