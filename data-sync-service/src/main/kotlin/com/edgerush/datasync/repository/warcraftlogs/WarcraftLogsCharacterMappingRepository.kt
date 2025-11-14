package com.edgerush.datasync.repository.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsCharacterMappingEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface WarcraftLogsCharacterMappingRepository : CrudRepository<WarcraftLogsCharacterMappingEntity, Long> {
    @Query(
        """
        SELECT * FROM warcraft_logs_character_mappings 
        WHERE guild_id = :guildId 
        AND wowaudit_name = :wowauditName 
        AND wowaudit_realm = :wowauditRealm
    """,
    )
    fun findByGuildAndWoWAuditCharacter(
        @Param("guildId") guildId: String,
        @Param("wowauditName") wowauditName: String,
        @Param("wowauditRealm") wowauditRealm: String,
    ): WarcraftLogsCharacterMappingEntity?

    @Query("SELECT * FROM warcraft_logs_character_mappings WHERE guild_id = :guildId")
    fun findByGuildId(
        @Param("guildId") guildId: String,
    ): List<WarcraftLogsCharacterMappingEntity>
}
