package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderCrestCountEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderCrestCountRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderCrestCountRepository(
    private val springRepository: RaiderCrestCountEntitySpringRepository,
) : RaiderCrestCountRepository {

    override fun findById(id: Long): RaiderCrestCountEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<RaiderCrestCountEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderCrestCountEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("crestType"),
        )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long =
        springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderCrestCountEntity): RaiderCrestCountEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
