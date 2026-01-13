package com.edgerush.lootman.infrastructure.guild

import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.repository.GuildRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of GuildRepository.
 *
 * Useful for testing and development without database dependency.
 */
@Repository
class InMemoryGuildRepository : GuildRepository {

    private val guilds = ConcurrentHashMap<String, Guild>()

    override fun save(guild: Guild): Guild {
        guilds[guild.id.value] = guild
        return guild
    }

    override fun findById(id: GuildId): Guild? {
        return guilds[id.value]
    }

    override fun findAllActive(): List<Guild> {
        return guilds.values.filter { it.isActive }.toList()
    }

    override fun findAll(): List<Guild> {
        return guilds.values.toList()
    }

    override fun deleteById(id: GuildId): Boolean {
        return guilds.remove(id.value) != null
    }

    override fun existsById(id: GuildId): Boolean {
        return guilds.containsKey(id.value)
    }

    /**
     * Clear all guilds (for testing purposes).
     */
    fun clear() {
        guilds.clear()
    }
}
