package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardBonusIdRepository
import com.edgerush.lootman.infrastructure.springdata.LootAwardBonusIdEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of LootAwardBonusIdRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcLootAwardBonusIdRepository(
    private val springRepository: LootAwardBonusIdEntitySpringRepository,
) : LootAwardBonusIdRepository {
    override fun findById(id: Long): LootAwardBonusIdEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootAwardBonusIdEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByLootAwardId(
        lootAwardId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardBonusIdEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findByLootAwardId(lootAwardId, pageRequest).content
    }

    override fun countByLootAwardId(lootAwardId: Long): Long = springRepository.countByLootAwardId(lootAwardId)

    override fun save(entity: LootAwardBonusIdEntity): LootAwardBonusIdEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
