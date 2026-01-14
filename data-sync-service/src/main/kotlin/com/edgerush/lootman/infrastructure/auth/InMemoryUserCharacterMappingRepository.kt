package com.edgerush.lootman.infrastructure.auth

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.shared.RaiderId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory implementation of UserCharacterMappingRepository.
 *
 * Used for unit testing without database dependencies.
 */
class InMemoryUserCharacterMappingRepository : UserCharacterMappingRepository {
    private val storage = ConcurrentHashMap<UserCharacterMappingId, UserCharacterMapping>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: UserCharacterMappingId): UserCharacterMapping? = storage[id]

    override fun findByUserId(userId: UserId): List<UserCharacterMapping> =
        storage.values
            .filter { it.userId == userId }
            .sortedWith(compareByDescending<UserCharacterMapping> { it.isPrimary }.thenBy { it.linkedAt })

    override fun findPrimaryByUserId(userId: UserId): UserCharacterMapping? =
        storage.values.find { it.userId == userId && it.isPrimary }

    override fun findByRaiderId(raiderId: RaiderId): List<UserCharacterMapping> =
        storage.values
            .filter { it.raiderId == raiderId }
            .sortedBy { it.linkedAt }

    override fun existsByUserIdAndRaiderId(userId: UserId, raiderId: RaiderId): Boolean =
        storage.values.any { it.userId == userId && it.raiderId == raiderId }

    override fun save(mapping: UserCharacterMapping): UserCharacterMapping {
        val savedMapping = if (mapping.id == null) {
            val newId = UserCharacterMappingId(idGenerator.getAndIncrement())
            mapping.withId(newId)
        } else {
            mapping
        }
        storage[savedMapping.id!!] = savedMapping
        return savedMapping
    }

    override fun deleteById(id: UserCharacterMappingId) {
        storage.remove(id)
    }

    override fun deleteByUserId(userId: UserId): Int {
        val toRemove = storage.values.filter { it.userId == userId }
        toRemove.forEach { storage.remove(it.id) }
        return toRemove.size
    }

    override fun clearPrimaryForUser(userId: UserId) {
        storage.values
            .filter { it.userId == userId && it.isPrimary }
            .forEach { mapping ->
                storage[mapping.id!!] = mapping.markAsNonPrimary()
            }
    }

    override fun countByUserId(userId: UserId): Long =
        storage.values.count { it.userId == userId }.toLong()

    /**
     * Clears all data. Useful for test setup/teardown.
     */
    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }
}
