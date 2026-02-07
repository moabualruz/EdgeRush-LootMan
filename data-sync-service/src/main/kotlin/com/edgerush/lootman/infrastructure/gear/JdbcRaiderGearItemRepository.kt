package com.edgerush.lootman.infrastructure.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderGearItemEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderGearItemRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderGearItemRepository(
    private val springRepository: RaiderGearItemEntitySpringRepository,
) : RaiderGearItemRepository {
    override fun findById(id: Long): RaiderGearItemEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderGearItemEntity> {
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
    ): List<RaiderGearItemEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("slot"),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun findByRaiderIdAndGearSet(
        raiderId: Long,
        gearSet: String,
        offset: Long,
        limit: Int,
    ): List<RaiderGearItemEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("slot"),
            )
        return springRepository.findByRaiderIdAndGearSet(raiderId, gearSet, pageRequest).content
    }

    override fun countByRaiderIdAndGearSet(
        raiderId: Long,
        gearSet: String,
    ): Long = springRepository.countByRaiderIdAndGearSet(raiderId, gearSet)

    override fun save(entity: RaiderGearItemEntity): RaiderGearItemEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
