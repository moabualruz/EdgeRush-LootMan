package com.edgerush.lootman.infrastructure.wishlist

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import com.edgerush.lootman.domain.wishlist.repository.WishlistSnapshotRepository
import com.edgerush.lootman.infrastructure.springdata.WishlistSnapshotEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of WishlistSnapshotRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcWishlistSnapshotRepository(
    private val springRepository: WishlistSnapshotEntitySpringRepository,
) : WishlistSnapshotRepository {

    override fun findById(id: Long): WishlistSnapshotEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<WishlistSnapshotEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long =
        springRepository.countByRaiderId(raiderId)

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<WishlistSnapshotEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun save(entity: WishlistSnapshotEntity): WishlistSnapshotEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
