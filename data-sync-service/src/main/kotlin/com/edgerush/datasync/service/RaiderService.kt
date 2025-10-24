package com.edgerush.datasync.service

import com.edgerush.datasync.api.wowaudit.CharacterDto
import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.repository.RaiderRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class RaiderService(
    private val raiderRepository: RaiderRepository
) {

    fun upsertCharacter(character: CharacterDto): RaiderEntity {
        val existing = raiderRepository
            .findByCharacterNameAndRealm(character.name, character.realm)
            .orElse(null)

        val now = OffsetDateTime.now()
        val entity = if (existing != null) {
            existing.copy(
                clazz = character.clazz,
                spec = character.spec,
                role = character.role,
                region = character.region,
                lastSync = now
            )
        } else {
            RaiderEntity(
                characterName = character.name,
                realm = character.realm,
                region = character.region,
                clazz = character.clazz,
                spec = character.spec,
                role = character.role,
                lastSync = now
            )
        }

        return raiderRepository.save(entity)
    }
}
