package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.lootman.domain.raids.repository.RaidEncounterRepository
import com.edgerush.lootman.infrastructure.springdata.RaidEncounterEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaidEncounterRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaidEncounterRepository(
    private val springRepository: RaidEncounterEntitySpringRepository,
) : RaidEncounterRepository {

    override fun findById(id: Long): RaidEncounterEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<RaidEncounterEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("raidId").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidEncounterEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findByRaidId(raidId, pageRequest).content
    }

    override fun countByRaidId(raidId: Long): Long =
        springRepository.countByRaidId(raidId)

    override fun findEnabledByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidEncounterEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findByRaidIdAndEnabledTrue(raidId, pageRequest).content
    }

    override fun countEnabledByRaidId(raidId: Long): Long =
        springRepository.countByRaidIdAndEnabledTrue(raidId)

    override fun save(encounter: RaidEncounterEntity): RaidEncounterEntity =
        springRepository.save(encounter)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
