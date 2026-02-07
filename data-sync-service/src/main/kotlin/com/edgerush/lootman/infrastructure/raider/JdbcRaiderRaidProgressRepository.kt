package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.lootman.domain.raider.repository.RaiderRaidProgressRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderRaidProgressEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderRaidProgressRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderRaidProgressRepository(
    private val springRepository: RaiderRaidProgressEntitySpringRepository,
) : RaiderRaidProgressRepository {
    override fun findById(id: Long): RaiderRaidProgressEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderRaidProgressEntity> {
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
    ): List<RaiderRaidProgressEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("raid").and(Sort.by("difficulty")),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderRaidProgressEntity): RaiderRaidProgressEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
