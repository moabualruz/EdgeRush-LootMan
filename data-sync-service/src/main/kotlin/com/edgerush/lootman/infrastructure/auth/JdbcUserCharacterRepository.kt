package com.edgerush.lootman.infrastructure.auth

import com.edgerush.datasync.entity.UserCharacterEntity
import com.edgerush.lootman.domain.auth.model.UserCharacter
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import com.edgerush.lootman.infrastructure.springdata.UserCharacterEntitySpringRepository
import org.springframework.stereotype.Repository

@Repository
class JdbcUserCharacterRepository(
    private val springRepository: UserCharacterEntitySpringRepository,
) : UserCharacterRepository {

    override fun save(character: UserCharacter): UserCharacter {
        val entity = character.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun saveAll(characters: List<UserCharacter>): List<UserCharacter> {
        if (characters.isEmpty()) return emptyList()

        val userId = characters.first().userId

        // Get existing characters by (name, realm) for upsert matching
        val existing = findAllByUserId(userId)
            .associateBy { "${it.name.lowercase()}-${it.realm.lowercase()}" }

        return characters.map { char ->
            val key = "${char.name.lowercase()}-${char.realm.lowercase()}"
            val existingChar = existing[key]
            if (existingChar != null) {
                // Update existing character, preserving its ID
                save(char.copy(id = existingChar.id))
            } else {
                // Insert new character
                save(char)
            }
        }
    }

    override fun findAllByUserId(userId: UserId): List<UserCharacter> =
        springRepository.findByUserIdOrderByLevelDescCharacterNameAsc(userId.value)
            .map { it.toDomain() }

    override fun deleteAllByUserId(userId: UserId) {
        springRepository.deleteAllByUserId(userId.value)
    }

    private fun UserCharacterEntity.toDomain(): UserCharacter =
        UserCharacter(
            id = id,
            userId = UserId(userId),
            name = characterName,
            realm = realm,
            className = className ?: "Unknown",
            classId = classId,
            specId = specId,
            level = level,
            race = playableRace,
            faction = faction,
            blizzardId = blizzardId,
            guildName = guildName,
            guildRealm = guildRealm,
            guildId = guildId,
            lastSyncedAt = lastSyncedAt,
        )

    private fun UserCharacter.toEntity(): UserCharacterEntity =
        UserCharacterEntity(
            id = id,
            userId = userId.value,
            characterName = name,
            realm = realm,
            className = className,
            classId = classId,
            specId = specId,
            level = level,
            playableRace = race,
            faction = faction,
            blizzardId = blizzardId,
            guildName = guildName,
            guildRealm = guildRealm,
            guildId = guildId,
            lastSyncedAt = lastSyncedAt,
        )
}
