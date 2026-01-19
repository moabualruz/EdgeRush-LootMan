package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.LootAwardEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * Spring Data JDBC repository for LootAwardEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface LootAwardEntitySpringRepository :
    CrudRepository<LootAwardEntity, Long>,
    PagingAndSortingRepository<LootAwardEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<LootAwardEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRclootcouncilId(rclootcouncilId: String): LootAwardEntity?

    // Note: Guild-based queries require joins through raiders table
    // Use JdbcLootAwardEntityRepository for guild-filtered queries

    @Query(
        """
        SELECT la.* FROM loot_awards la
        INNER JOIN raiders r ON la.raider_id = r.id
        WHERE r.guild_id = :guildId AND la.awarded_at BETWEEN :startDate AND :endDate
        ORDER BY la.awarded_at DESC
        """
    )
    fun findByGuildIdAndDateRange(
        guildId: String,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
    ): List<LootAwardEntity>

    @Query(
        """
        SELECT COUNT(*) FROM loot_awards la
        INNER JOIN raiders r ON la.raider_id = r.id
        WHERE r.guild_id = :guildId AND la.awarded_at BETWEEN :startDate AND :endDate
        """
    )
    fun countByGuildIdAndDateRange(
        guildId: String,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
    ): Long

    @Query(
        """
        SELECT * FROM loot_awards
        WHERE raider_id = :raiderId AND awarded_at BETWEEN :startDate AND :endDate
        ORDER BY awarded_at DESC
        """
    )
    fun findByRaiderIdAndDateRange(
        raiderId: Long,
        startDate: OffsetDateTime,
        endDate: OffsetDateTime,
    ): List<LootAwardEntity>

    fun findByItemId(itemId: Long, pageable: Pageable): Page<LootAwardEntity>

    fun countByItemId(itemId: Long): Long

    fun findByTier(tier: String, pageable: Pageable): Page<LootAwardEntity>

    fun countByTier(tier: String): Long
}
