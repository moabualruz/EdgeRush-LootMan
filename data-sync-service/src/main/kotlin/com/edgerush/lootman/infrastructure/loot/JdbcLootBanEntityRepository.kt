package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.lootman.domain.loot.repository.LootBanEntityRepository
import com.edgerush.lootman.infrastructure.springdata.LootBanEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Implementation of LootBanEntityRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcLootBanEntityRepository(
    private val springRepository: LootBanEntitySpringRepository,
) : LootBanEntityRepository {
    override fun findById(id: Long): LootBanEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootBanEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "bannedAt").and(Sort.by("id")),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<LootBanEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "bannedAt").and(Sort.by("id")),
            )
        return springRepository.findByGuildId(guildId, pageRequest).content
    }

    override fun countByGuildId(guildId: String): Long = springRepository.countByGuildId(guildId)

    override fun findActiveByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<LootBanEntity> {
        // Spring Data JDBC doesn't support Page with @Query, so we fetch all and paginate manually
        return springRepository.findActiveByGuildId(guildId, LocalDateTime.now())
            .drop(offset.toInt())
            .take(limit)
    }

    override fun countActiveByGuildId(guildId: String): Long = springRepository.countActiveByGuildId(guildId, LocalDateTime.now())

    override fun isCharacterBanned(
        guildId: String,
        characterName: String,
    ): Boolean = springRepository.countActiveBansForCharacter(guildId, characterName, LocalDateTime.now()) > 0

    override fun save(lootBan: LootBanEntity): LootBanEntity = springRepository.save(lootBan)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
