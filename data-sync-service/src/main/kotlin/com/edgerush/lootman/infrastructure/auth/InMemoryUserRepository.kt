package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.shared.GuildId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory implementation of UserRepository.
 *
 * Used for unit testing without database dependencies.
 */
class InMemoryUserRepository : UserRepository {
    private val storage = ConcurrentHashMap<UserId, User>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: UserId): User? = storage[id]

    override fun findByDiscordId(discordId: String): User? = storage.values.find { it.discordId == discordId }

    override fun findByBattlenetId(battlenetId: String): User? = storage.values.find { it.battlenetId == battlenetId }

    override fun findByGuildId(guildId: GuildId): List<User> =
        storage.values
            .filter { it.guildId == guildId }
            .sortedBy { it.username }

    override fun save(user: User): User {
        val savedUser =
            if (user.id == null) {
                val newId = UserId(idGenerator.getAndIncrement())
                user.withId(newId)
            } else {
                user
            }
        storage[savedUser.id!!] = savedUser
        return savedUser
    }

    override fun deleteById(id: UserId) {
        storage.remove(id)
    }

    override fun existsByDiscordId(discordId: String): Boolean = storage.values.any { it.discordId == discordId }

    override fun existsByBattlenetId(battlenetId: String): Boolean = storage.values.any { it.battlenetId == battlenetId }

    override fun findByUsername(username: String): User? =
        storage.values.find { it.username.equals(username, ignoreCase = true) }

    override fun findByEmail(email: String): User? =
        storage.values.find { it.email?.equals(email, ignoreCase = true) == true }

    override fun existsByUsername(username: String): Boolean =
        storage.values.any { it.username.equals(username, ignoreCase = true) }

    override fun existsByEmail(email: String): Boolean =
        storage.values.any { it.email?.equals(email, ignoreCase = true) == true }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<User> =
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
