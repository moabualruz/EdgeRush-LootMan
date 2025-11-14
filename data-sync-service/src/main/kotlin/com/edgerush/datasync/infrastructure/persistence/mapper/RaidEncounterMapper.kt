package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.RaidEncounter
import com.edgerush.datasync.entity.RaidEncounterEntity
import org.springframework.stereotype.Component

/**
 * Mapper for converting between RaidEncounter domain model and RaidEncounterEntity.
 */
@Component
class RaidEncounterMapper {
    
    fun toDomain(entity: RaidEncounterEntity): RaidEncounter {
        return if (entity.id != null) {
            RaidEncounter.reconstitute(
                id = entity.id,
                encounterId = entity.encounterId,
                name = entity.name ?: "",
                enabled = entity.enabled ?: true,
                extra = entity.extra ?: false,
                notes = entity.notes
            )
        } else {
            RaidEncounter.create(
                encounterId = entity.encounterId,
                name = entity.name ?: "",
                enabled = entity.enabled ?: true,
                extra = entity.extra ?: false,
                notes = entity.notes
            )
        }
    }
    
    fun toEntity(encounter: RaidEncounter, raidId: Long): RaidEncounterEntity {
        return RaidEncounterEntity(
            id = encounter.id,
            raidId = raidId,
            encounterId = encounter.encounterId,
            name = encounter.name,
            enabled = encounter.enabled,
            extra = encounter.extra,
            notes = encounter.notes
        )
    }
    
    fun toEntities(encounters: List<RaidEncounter>, raidId: Long): List<RaidEncounterEntity> {
        return encounters.map { toEntity(it, raidId) }
    }
    
    fun toDomainList(entities: List<RaidEncounterEntity>): List<RaidEncounter> {
        return entities.map { toDomain(it) }
    }
}
