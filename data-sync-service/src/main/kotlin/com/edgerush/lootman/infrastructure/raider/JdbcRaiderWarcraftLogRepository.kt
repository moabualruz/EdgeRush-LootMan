package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderWarcraftLogEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderWarcraftLogRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderWarcraftLogRepository(
    private val springRepository: RaiderWarcraftLogEntitySpringRepository,
) : RaiderWarcraftLogRepository {
    override fun findById(id: Long): RaiderWarcraftLogEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderWarcraftLogEntity> {
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
    ): List<RaiderWarcraftLogEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("difficulty"),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderWarcraftLogEntity): RaiderWarcraftLogEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
