package com.edgerush.lootman.infrastructure.discord

import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.repository.DiscordUserLinkRepository
import com.edgerush.lootman.domain.shared.RaiderId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory implementation of DiscordUserLinkRepository.
 *
 * Used for unit testing without database dependencies.
 * Uses ConcurrentHashMap for thread-safe operations.
 */
class InMemoryDiscordUserLinkRepository : DiscordUserLinkRepository {
    private val storage = ConcurrentHashMap<DiscordUserLinkId, DiscordUserLink>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: DiscordUserLinkId): DiscordUserLink? = storage[id]

    override fun findByDiscordUserId(discordUserId: DiscordUserId): List<DiscordUserLink> =
        storage.values
            .filter { it.discordUserId == discordUserId }
            .sortedWith(compareByDescending<DiscordUserLink> { it.isPrimary }.thenBy { it.linkedAt })

    override fun findPrimaryByDiscordUserId(discordUserId: DiscordUserId): DiscordUserLink? =
        storage.values.find { it.discordUserId == discordUserId && it.isPrimary }

    override fun findByRaiderId(raiderId: RaiderId): List<DiscordUserLink> =
        storage.values
            .filter { it.raiderId == raiderId }
            .sortedBy { it.linkedAt }

    override fun existsByDiscordUserIdAndRaiderId(
        discordUserId: DiscordUserId,
        raiderId: RaiderId,
    ): Boolean = storage.values.any { it.discordUserId == discordUserId && it.raiderId == raiderId }

    override fun save(link: DiscordUserLink): DiscordUserLink {
        val savedLink =
            if (link.id == null) {
                val newId = DiscordUserLinkId(idGenerator.getAndIncrement())
                link.withId(newId)
            } else {
                link
            }
        storage[savedLink.id!!] = savedLink
        return savedLink
    }

    override fun deleteById(id: DiscordUserLinkId) {
        storage.remove(id)
    }

    override fun deleteByDiscordUserId(discordUserId: DiscordUserId): Int {
        val toRemove = storage.values.filter { it.discordUserId == discordUserId }
        toRemove.forEach { storage.remove(it.id) }
        return toRemove.size
    }

    override fun clearPrimaryForDiscordUser(discordUserId: DiscordUserId) {
        storage.values
            .filter { it.discordUserId == discordUserId && it.isPrimary }
            .forEach { link ->
                storage[link.id!!] = link.markAsNonPrimary()
            }
    }

    override fun countByDiscordUserId(discordUserId: DiscordUserId): Long =
        storage.values.count { it.discordUserId == discordUserId }.toLong()

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<DiscordUserLink> =
        storage.values
            .sortedBy { it.id?.value }
            .drop(offset.toInt())
            .take(limit)

    override fun count(): Long = storage.size.toLong()

    /**
     * Clears all data. Useful for test setup/teardown.
     */
    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }
}
