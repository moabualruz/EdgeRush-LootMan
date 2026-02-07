package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.lootman.domain.raider.repository.RaiderPvpBracketRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderPvpBracketEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderPvpBracketRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderPvpBracketRepository(
    private val springRepository: RaiderPvpBracketEntitySpringRepository,
) : RaiderPvpBracketRepository {
    override fun findById(id: Long): RaiderPvpBracketEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderPvpBracketEntity> {
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
    ): List<RaiderPvpBracketEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("bracket"),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun save(entity: RaiderPvpBracketEntity): RaiderPvpBracketEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
