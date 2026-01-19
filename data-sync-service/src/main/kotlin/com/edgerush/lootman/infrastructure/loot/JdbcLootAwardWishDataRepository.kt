package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.lootman.domain.loot.repository.LootAwardWishDataRepository
import com.edgerush.lootman.infrastructure.springdata.LootAwardWishDataEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of LootAwardWishDataRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcLootAwardWishDataRepository(
    private val springRepository: LootAwardWishDataEntitySpringRepository,
) : LootAwardWishDataRepository {

    override fun findById(id: Long): LootAwardWishDataEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<LootAwardWishDataEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByLootAwardId(lootAwardId: Long, offset: Long, limit: Int): List<LootAwardWishDataEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findByLootAwardId(lootAwardId, pageRequest).content
    }

    override fun countByLootAwardId(lootAwardId: Long): Long =
        springRepository.countByLootAwardId(lootAwardId)

    override fun save(entity: LootAwardWishDataEntity): LootAwardWishDataEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
