package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderVaultSlotEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderVaultSlotRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderVaultSlotRepository(
    private val springRepository: RaiderVaultSlotEntitySpringRepository,
) : RaiderVaultSlotRepository {
    override fun findById(id: Long): RaiderVaultSlotEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderVaultSlotEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("raiderId").and(Sort.by("slot")).and(Sort.by("id")),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderVaultSlotEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("slot").and(Sort.by("id")),
            )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long = springRepository.countByRaiderId(raiderId)

    override fun findUnlockedByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderVaultSlotEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("slot").and(Sort.by("id")),
            )
        return springRepository.findByRaiderIdAndUnlockedTrue(raiderId, pageRequest).content
    }

    override fun countUnlockedByRaiderId(raiderId: Long): Long = springRepository.countByRaiderIdAndUnlockedTrue(raiderId)

    override fun save(entity: RaiderVaultSlotEntity): RaiderVaultSlotEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
