package com.edgerush.lootman.infrastructure.discord

import com.edgerush.datasync.entity.DiscordUserLinkEntity
import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.repository.DiscordUserLinkRepository
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.infrastructure.springdata.DiscordUserLinkEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of DiscordUserLinkRepository.
 *
 * Persists Discord user links to the discord_user_links table using Spring Data JDBC.
 */
@Repository
class JdbcDiscordUserLinkRepository(
    private val springRepository: DiscordUserLinkEntitySpringRepository,
) : DiscordUserLinkRepository {

    override fun findById(id: DiscordUserLinkId): DiscordUserLink? =
        springRepository.findById(id.value).orElse(null)?.toDomain()

    override fun findByDiscordUserId(discordUserId: DiscordUserId): List<DiscordUserLink> =
        springRepository.findByDiscordUserIdOrderByIsPrimaryDescLinkedAtAsc(discordUserId.value)
            .map { it.toDomain() }

    override fun findPrimaryByDiscordUserId(discordUserId: DiscordUserId): DiscordUserLink? =
        springRepository.findByDiscordUserIdAndIsPrimaryTrue(discordUserId.value)?.toDomain()

    override fun findByRaiderId(raiderId: RaiderId): List<DiscordUserLink> =
        springRepository.findByRaiderIdOrderByLinkedAtAsc(raiderId.value)
            .map { it.toDomain() }

    override fun existsByDiscordUserIdAndRaiderId(
        discordUserId: DiscordUserId,
        raiderId: RaiderId,
    ): Boolean =
        springRepository.existsByDiscordUserIdAndRaiderId(discordUserId.value, raiderId.value)

    override fun save(link: DiscordUserLink): DiscordUserLink {
        val entity = link.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: DiscordUserLinkId) {
        springRepository.deleteById(id.value)
    }

    override fun deleteByDiscordUserId(discordUserId: DiscordUserId): Int =
        springRepository.deleteByDiscordUserId(discordUserId.value)

    override fun clearPrimaryForDiscordUser(discordUserId: DiscordUserId) {
        springRepository.clearPrimaryForDiscordUserId(discordUserId.value)
    }

    override fun countByDiscordUserId(discordUserId: DiscordUserId): Long =
        springRepository.countByDiscordUserId(discordUserId.value)

    override fun findAll(offset: Long, limit: Int): List<DiscordUserLink> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content.map { it.toDomain() }
    }

    override fun count(): Long =
        springRepository.count()

    private fun DiscordUserLinkEntity.toDomain(): DiscordUserLink =
        DiscordUserLink(
            id = id?.let { DiscordUserLinkId(it) },
            discordUserId = DiscordUserId(discordUserId),
            raiderId = RaiderId(raiderId),
            isPrimary = isPrimary,
            linkedAt = linkedAt,
            linkedBy = linkedBy,
        )

    private fun DiscordUserLink.toEntity(): DiscordUserLinkEntity =
        DiscordUserLinkEntity(
            id = id?.value,
            discordUserId = discordUserId.value,
            raiderId = raiderId.value,
            isPrimary = isPrimary,
            linkedAt = linkedAt,
            linkedBy = linkedBy,
        )
}
