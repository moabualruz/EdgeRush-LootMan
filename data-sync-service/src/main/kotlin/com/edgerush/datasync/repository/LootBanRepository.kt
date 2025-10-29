package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.LootBanEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LootBanRepository : CrudRepository<LootBanEntity, Long> {
    
    @Query("""
        SELECT * FROM loot_bans 
        WHERE guild_id = :guildId 
        AND character_name = :characterName 
        AND is_active = true 
        AND (expires_at IS NULL OR expires_at > :currentTime)
        ORDER BY banned_at DESC
        LIMIT 1
    """)
    fun findActiveBanForCharacter(
        guildId: String, 
        characterName: String, 
        currentTime: LocalDateTime
    ): LootBanEntity?
    
    @Query("""
        SELECT * FROM loot_bans 
        WHERE guild_id = :guildId 
        AND is_active = true 
        AND (expires_at IS NULL OR expires_at > :currentTime)
        ORDER BY character_name, banned_at DESC
    """)
    fun findActiveBansForGuild(
        guildId: String, 
        currentTime: LocalDateTime
    ): List<LootBanEntity>
}