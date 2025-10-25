package com.edgerush.datasync.service

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.repository.RaiderRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class RaiderService(
    private val raiderRepository: RaiderRepository
) {

    fun findRaider(name: String, realm: String): RaiderEntity? =
        raiderRepository.findByCharacterNameAndRealm(name, realm).orElse(null)

    fun upsertCharacter(record: RaiderRecord): RaiderEntity {
        val existing = raiderRepository
            .findByCharacterNameAndRealm(record.name, record.realm)
            .orElse(null)

        val now = OffsetDateTime.now()
        val entity = if (existing != null) {
            existing.copy(
                clazz = record.clazz.ifBlank { existing.clazz },
                spec = record.spec.ifBlank { existing.spec },
                role = record.role.ifBlank { existing.role },
                region = record.region.ifBlank { existing.region },
                lastSync = now
            )
        } else {
            RaiderEntity(
                characterName = record.name,
                realm = record.realm,
                region = record.region,
                clazz = record.clazz,
                spec = record.spec,
                role = record.role,
                lastSync = now
            )
        }

        return raiderRepository.save(entity)
    }
}

