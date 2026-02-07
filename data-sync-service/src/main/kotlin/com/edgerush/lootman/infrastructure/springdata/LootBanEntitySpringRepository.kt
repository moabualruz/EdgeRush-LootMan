package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.LootBanEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Spring Data JDBC repository for LootBanEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface LootBanEntitySpringRepository :
    CrudRepository<LootBanEntity, Long>,
    PagingAndSortingRepository<LootBanEntity, Long> {
    fun findByGuildId(
        guildId: String,
        pageable: Pageable,
    ): Page<LootBanEntity>

    fun countByGuildId(guildId: String): Long

    @Query(
        """
        SELECT * FROM loot_bans
        WHERE guild_id = :guildId AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        ORDER BY banned_at DESC
        """,
    )
    fun findActiveByGuildId(
        guildId: String,
        now: LocalDateTime,
    ): List<LootBanEntity>

    @Query(
        """
        SELECT COUNT(*) FROM loot_bans
        WHERE guild_id = :guildId AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        """,
    )
    fun countActiveByGuildId(
        guildId: String,
        now: LocalDateTime,
    ): Long

    @Query(
        """
        SELECT COUNT(*) FROM loot_bans
        WHERE guild_id = :guildId AND character_name = :characterName AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        """,
    )
    fun countActiveBansForCharacter(
        guildId: String,
        characterName: String,
        now: LocalDateTime,
    ): Long
}
