package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.DiscordNotificationConfigEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DiscordNotificationConfigEntitySpringRepository :
    CrudRepository<DiscordNotificationConfigEntity, Long>,
    PagingAndSortingRepository<DiscordNotificationConfigEntity, Long> {

    fun findByGuildIdOrderByNotificationTypeAsc(guildId: String): List<DiscordNotificationConfigEntity>
    fun findByGuildIdAndNotificationType(guildId: String, notificationType: String): DiscordNotificationConfigEntity?
    fun findByGuildIdAndEnabledTrueOrderByNotificationTypeAsc(guildId: String): List<DiscordNotificationConfigEntity>
    fun findByGuildIdAndNotificationTypeAndEnabledTrue(guildId: String, notificationType: String): DiscordNotificationConfigEntity?
    fun deleteByGuildId(guildId: String): Int
    fun existsByGuildIdAndNotificationType(guildId: String, notificationType: String): Boolean
}
