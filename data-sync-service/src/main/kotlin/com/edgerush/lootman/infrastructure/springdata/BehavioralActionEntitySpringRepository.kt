package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.BehavioralActionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Spring Data JDBC repository for BehavioralActionEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface BehavioralActionEntitySpringRepository :
    CrudRepository<BehavioralActionEntity, Long>,
    PagingAndSortingRepository<BehavioralActionEntity, Long> {
    fun findByGuildId(
        guildId: String,
        pageable: Pageable,
    ): Page<BehavioralActionEntity>

    fun countByGuildId(guildId: String): Long

    fun findByCharacterName(
        characterName: String,
        pageable: Pageable,
    ): Page<BehavioralActionEntity>

    fun countByCharacterName(characterName: String): Long

    @Query(
        """
        SELECT * FROM behavioral_actions
        WHERE guild_id = :guildId AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        ORDER BY applied_at DESC
        """,
    )
    fun findActiveByGuildId(
        guildId: String,
        now: LocalDateTime,
    ): List<BehavioralActionEntity>

    @Query(
        """
        SELECT * FROM behavioral_actions
        WHERE guild_id = :guildId AND character_name = :characterName AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        ORDER BY applied_at DESC
        """,
    )
    fun findActiveByGuildIdAndCharacterName(
        guildId: String,
        characterName: String,
        now: LocalDateTime,
    ): List<BehavioralActionEntity>

    fun findByActionType(
        actionType: String,
        pageable: Pageable,
    ): Page<BehavioralActionEntity>

    fun countByActionType(actionType: String): Long

    fun findByGuildIdAndCharacterName(
        guildId: String,
        characterName: String,
        pageable: Pageable,
    ): Page<BehavioralActionEntity>

    fun countByGuildIdAndCharacterName(
        guildId: String,
        characterName: String,
    ): Long

    @Query(
        """
        SELECT COUNT(*) FROM behavioral_actions
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
        SELECT COALESCE(SUM(
            CASE WHEN action_type = 'DEDUCTION' THEN deduction_amount
                 WHEN action_type = 'RESTORATION' THEN -deduction_amount
                 ELSE 0
            END
        ), 0.0) as total
        FROM behavioral_actions
        WHERE guild_id = :guildId AND character_name = :characterName AND is_active = true
        AND (expires_at IS NULL OR expires_at > :now)
        """,
    )
    fun getTotalActiveDeduction(
        guildId: String,
        characterName: String,
        now: LocalDateTime,
    ): Double
}
