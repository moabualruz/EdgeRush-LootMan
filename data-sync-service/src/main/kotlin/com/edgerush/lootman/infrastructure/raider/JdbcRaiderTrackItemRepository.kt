package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import com.edgerush.lootman.domain.raider.repository.RaiderTrackItemRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderTrackItemEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderTrackItemRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderTrackItemRepository(
    private val springRepository: RaiderTrackItemEntitySpringRepository,
) : RaiderTrackItemRepository {
    override fun findById(id: Long): RaiderTrackItemEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("tier"),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderTrackItemEntity): RaiderTrackItemEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
