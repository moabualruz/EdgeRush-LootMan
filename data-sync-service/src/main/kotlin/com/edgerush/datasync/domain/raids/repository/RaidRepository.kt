package com.edgerush.datasync.domain.raids.repository

import com.edgerush.datasync.domain.raids.model.GuildId
import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidId
import java.time.LocalDate

/**
 * Repository interface for Raid aggregate.
 */
interface RaidRepository {
    fun findById(id: RaidId): Raid?
    
    fun findByGuildId(guildId: GuildId): List<Raid>
    
    fun findByGuildIdAndDate(guildId: GuildId, date: LocalDate): List<Raid>
    
    fun save(raid: Raid): Raid
    
    fun delete(id: RaidId)
}
