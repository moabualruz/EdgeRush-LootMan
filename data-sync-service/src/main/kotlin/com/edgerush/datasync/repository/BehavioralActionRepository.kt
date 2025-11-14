package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.BehavioralActionEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BehavioralActionRepository : CrudRepository<BehavioralActionEntity, Long>, org.springframework.data.repository.PagingAndSortingRepository<BehavioralActionEntity, Long> {
    @Query(
        """
        SELECT * FROM behavioral_actions 
        WHERE guild_id = :guildId 
        AND character_name = :characterName 
        AND is_active = true 
        AND (expires_at IS NULL OR expires_at > :currentTime)
        ORDER BY applied_at DESC
    """,
    )
    fun findActiveActionsForCharacter(
        guildId: String,
        characterName: String,
        currentTime: LocalDateTime,
    ): List<BehavioralActionEntity>

    @Query(
        """
        SELECT * FROM behavioral_actions 
        WHERE guild_id = :guildId 
        AND is_active = true 
        AND (expires_at IS NULL OR expires_at > :currentTime)
        ORDER BY character_name, applied_at DESC
    """,
    )
    fun findActiveActionsForGuild(
        guildId: String,
        currentTime: LocalDateTime,
    ): List<BehavioralActionEntity>
}
