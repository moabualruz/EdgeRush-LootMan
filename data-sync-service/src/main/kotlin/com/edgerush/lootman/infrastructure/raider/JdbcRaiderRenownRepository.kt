package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.lootman.domain.raider.repository.RaiderRenownRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderRenownEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderRenownRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderRenownRepository(
    private val springRepository: RaiderRenownEntitySpringRepository,
) : RaiderRenownRepository {

    override fun findById(id: Long): RaiderRenownEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<RaiderRenownEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderRenownEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("faction"),
        )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long =
        springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderRenownEntity): RaiderRenownEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
