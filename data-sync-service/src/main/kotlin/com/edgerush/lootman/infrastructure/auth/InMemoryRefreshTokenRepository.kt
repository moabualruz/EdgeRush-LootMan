package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.RefreshTokenId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken
import com.edgerush.lootman.domain.auth.repository.RefreshTokenRepository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory implementation of RefreshTokenRepository.
 *
 * Used for unit testing without database dependencies.
 */
class InMemoryRefreshTokenRepository : RefreshTokenRepository {
    private val storage = ConcurrentHashMap<RefreshTokenId, UserRefreshToken>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: RefreshTokenId): UserRefreshToken? = storage[id]

    override fun findByTokenHash(tokenHash: String): UserRefreshToken? = storage.values.find { it.tokenHash == tokenHash }

    override fun findByUserId(userId: UserId): List<UserRefreshToken> =
        storage.values
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }

    override fun findValidByUserId(userId: UserId): List<UserRefreshToken> {
        val now = Instant.now()
        return storage.values
            .filter { it.userId == userId && it.isValid(now) }
            .sortedByDescending { it.createdAt }
    }

    override fun save(token: UserRefreshToken): UserRefreshToken {
        val savedToken =
            if (token.id == null) {
                val newId = RefreshTokenId(idGenerator.getAndIncrement())
                token.withId(newId)
            } else {
                token
            }
        storage[savedToken.id!!] = savedToken
        return savedToken
    }

    override fun deleteById(id: RefreshTokenId) {
        storage.remove(id)
    }

    override fun deleteByUserId(userId: UserId): Int {
        val toRemove = storage.values.filter { it.userId == userId }
        toRemove.forEach { storage.remove(it.id) }
        return toRemove.size
    }

    override fun revokeAllByUserId(userId: UserId): Int {
        var count = 0
        storage.values
            .filter { it.userId == userId && !it.isRevoked() }
            .forEach { token ->
                storage[token.id!!] = token.revoke()
                count++
            }
        return count
    }

    override fun deleteExpired(): Int {
        val now = Instant.now()
        val toRemove = storage.values.filter { it.isExpired(now) }
        toRemove.forEach { storage.remove(it.id) }
        return toRemove.size
    }

    /**
     * Clears all data. Useful for test setup/teardown.
     */
    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }
}
