package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.GuildConfigurationEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GuildConfigurationRepository : CrudRepository<GuildConfigurationEntity, Long> {
    fun findByGuildId(guildId: String): GuildConfigurationEntity?
    fun findByIsActive(isActive: Boolean): List<GuildConfigurationEntity>
    fun findBySyncEnabled(syncEnabled: Boolean): List<GuildConfigurationEntity>
    fun findByGuildIdAndIsActive(guildId: String, isActive: Boolean): GuildConfigurationEntity?
}