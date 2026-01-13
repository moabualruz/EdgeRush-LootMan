package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of RaiderRepository.
 *
 * Uses ConcurrentHashMap for thread-safe operations.
 */
@Repository
class InMemoryRaiderRepository : RaiderRepository {
    private val storage = ConcurrentHashMap<RaiderId, Raider>()

    override fun findById(id: RaiderId): Raider? = storage[id]

    override fun findByGuildId(guildId: GuildId): List<Raider> =
        storage.values.filter { it.guildId == guildId }

    override fun findByCharacterNameAndRealm(characterName: String, realm: String): Raider? =
        storage.values.find { it.characterName == characterName && it.realm == realm }

    override fun save(raider: Raider): Raider {
        storage[raider.id] = raider
        return raider
    }

    override fun delete(id: RaiderId) {
        storage.remove(id)
    }
}
