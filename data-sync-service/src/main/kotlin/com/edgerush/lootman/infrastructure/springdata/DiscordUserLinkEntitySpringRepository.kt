package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.DiscordUserLinkEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface DiscordUserLinkEntitySpringRepository :
    CrudRepository<DiscordUserLinkEntity, Long>,
    PagingAndSortingRepository<DiscordUserLinkEntity, Long> {

    fun findByDiscordUserId(discordUserId: String): List<DiscordUserLinkEntity>
    fun findByDiscordUserIdOrderByIsPrimaryDescLinkedAtAsc(discordUserId: String): List<DiscordUserLinkEntity>
    fun findByDiscordUserIdAndIsPrimaryTrue(discordUserId: String): DiscordUserLinkEntity?
    fun findByRaiderId(raiderId: Long): List<DiscordUserLinkEntity>
    fun findByRaiderIdOrderByLinkedAtAsc(raiderId: Long): List<DiscordUserLinkEntity>
    fun existsByDiscordUserIdAndRaiderId(discordUserId: String, raiderId: Long): Boolean
    fun deleteByDiscordUserId(discordUserId: String): Int
    fun countByDiscordUserId(discordUserId: String): Long

    @Modifying
    @Query("UPDATE discord_user_links SET is_primary = false WHERE discord_user_id = :discordUserId AND is_primary = true")
    fun clearPrimaryForDiscordUserId(discordUserId: String): Int
}
