package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardOldItemRepository
import com.edgerush.lootman.infrastructure.springdata.LootAwardOldItemEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of LootAwardOldItemRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcLootAwardOldItemRepository(
    private val springRepository: LootAwardOldItemEntitySpringRepository,
) : LootAwardOldItemRepository {

    override fun findById(id: Long): LootAwardOldItemEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<LootAwardOldItemEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardOldItemEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findByLootAwardId(lootAwardId, pageRequest).content
    }

    override fun countByLootAwardId(lootAwardId: Long): Long =
        springRepository.countByLootAwardId(lootAwardId)

    override fun save(entity: LootAwardOldItemEntity): LootAwardOldItemEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
