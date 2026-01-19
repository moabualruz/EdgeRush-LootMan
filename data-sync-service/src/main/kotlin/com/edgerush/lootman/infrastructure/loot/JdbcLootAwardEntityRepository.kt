package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardEntityRepository
import com.edgerush.lootman.infrastructure.springdata.LootAwardEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of LootAwardEntityRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcLootAwardEntityRepository(
    private val springRepository: LootAwardEntitySpringRepository,
) : LootAwardEntityRepository {

    override fun findById(id: Long): LootAwardEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<LootAwardEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "awardedAt").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<LootAwardEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "awardedAt").and(Sort.by("id")),
        )
        return springRepository.findByRaiderId(raiderId, pageRequest).content
    }

    override fun countByRaiderId(raiderId: Long): Long =
        springRepository.countByRaiderId(raiderId)

    override fun findByItemId(itemId: Long, offset: Long, limit: Int): List<LootAwardEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "awardedAt").and(Sort.by("id")),
        )
        return springRepository.findByItemId(itemId, pageRequest).content
    }

    override fun countByItemId(itemId: Long): Long =
        springRepository.countByItemId(itemId)

    override fun findByTier(tier: String, offset: Long, limit: Int): List<LootAwardEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "awardedAt").and(Sort.by("id")),
        )
        return springRepository.findByTier(tier, pageRequest).content
    }

    override fun countByTier(tier: String): Long =
        springRepository.countByTier(tier)

    override fun save(entity: LootAwardEntity): LootAwardEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
