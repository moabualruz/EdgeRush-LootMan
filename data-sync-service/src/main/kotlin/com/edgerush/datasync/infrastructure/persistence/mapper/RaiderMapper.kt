package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.shared.model.Raider
import com.edgerush.datasync.domain.shared.model.RaiderId
import com.edgerush.datasync.domain.shared.model.RaiderRole
import com.edgerush.datasync.domain.shared.model.RaiderStatus
import com.edgerush.datasync.domain.shared.model.WowClass
import com.edgerush.datasync.entity.RaiderEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Raider domain model and RaiderEntity.
 */
@Component
class RaiderMapper {
    /**
     * Convert domain model to entity
     */
    fun toEntity(raider: Raider): RaiderEntity {
        return RaiderEntity(
            id = raider.id.value,
            characterName = raider.characterName,
            realm = raider.realm,
            region = raider.region,
            wowauditId = raider.wowauditId,
            clazz = raider.clazz.name,
            spec = raider.spec,
            role = raider.role.name,
            rank = raider.rank,
            status = raider.status.name,
            note = raider.note,
            blizzardId = raider.blizzardId,
            trackingSince = raider.trackingSince,
            joinDate = raider.joinDate,
            blizzardLastModified = raider.blizzardLastModified,
            lastSync = raider.lastSync
        )
    }
    
    /**
     * Convert entity to domain model
     */
    fun toDomain(entity: RaiderEntity): Raider {
        return Raider(
            id = RaiderId(entity.id ?: throw IllegalStateException("Raider entity must have an ID")),
            characterName = entity.characterName,
            realm = entity.realm,
            region = entity.region,
            wowauditId = entity.wowauditId,
            clazz = WowClass.fromString(entity.clazz),
            spec = entity.spec,
            role = RaiderRole.fromString(entity.role),
            rank = entity.rank,
            status = RaiderStatus.fromString(entity.status),
            note = entity.note,
            blizzardId = entity.blizzardId,
            trackingSince = entity.trackingSince,
            joinDate = entity.joinDate,
            blizzardLastModified = entity.blizzardLastModified,
            lastSync = entity.lastSync
        )
    }
}
