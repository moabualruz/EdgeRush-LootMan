package com.edgerush.lootman.infrastructure.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderStatisticsEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderStatisticsRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderStatisticsRepository(
    private val springRepository: RaiderStatisticsEntitySpringRepository,
) : RaiderStatisticsRepository {
    override fun findById(id: Long): RaiderStatisticsEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderStatisticsEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByRaiderId(raiderId: Long): RaiderStatisticsEntity? = springRepository.findByRaiderId(raiderId)

    override fun existsByRaiderId(raiderId: Long): Boolean = springRepository.existsByRaiderId(raiderId)

    override fun save(entity: RaiderStatisticsEntity): RaiderStatisticsEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
