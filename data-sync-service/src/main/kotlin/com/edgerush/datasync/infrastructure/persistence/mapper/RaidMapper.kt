package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * Mapper for converting between Raid domain model and RaidEntity.
 */
@Component
class RaidMapper(
    val encounterMapper: RaidEncounterMapper = RaidEncounterMapper(),
    val signupMapper: RaidSignupMapper = RaidSignupMapper()
) {
    
    fun toDomain(
        entity: RaidEntity,
        guildId: String,
        encounterEntities: List<RaidEncounterEntity>,
        signupEntities: List<RaidSignupEntity>
    ): Raid {
        val encounters = encounterMapper.toDomainList(encounterEntities)
        val signups = signupMapper.toDomainList(signupEntities)
        
        return Raid.reconstitute(
            id = RaidId(entity.raidId),
            guildId = GuildId(guildId),
            scheduledDate = entity.date ?: throw IllegalStateException("Raid date cannot be null"),
            startTime = entity.startTime,
            endTime = entity.endTime,
            instance = entity.instance,
            difficulty = RaidDifficulty.fromString(entity.difficulty),
            optional = entity.optional ?: false,
            status = RaidStatus.fromString(entity.status) ?: RaidStatus.SCHEDULED,
            notes = entity.notes,
            encounters = encounters,
            signups = signups
        )
    }
    
    fun toEntity(raid: Raid): RaidEntity {
        return RaidEntity(
            raidId = raid.id.value,
            date = raid.scheduledDate,
            startTime = raid.startTime,
            endTime = raid.endTime,
            instance = raid.instance,
            difficulty = raid.difficulty?.name,
            optional = raid.optional,
            status = raid.status.name,
            presentSize = null,
            totalSize = null,
            notes = raid.notes,
            selectionsImage = null,
            teamId = null,
            seasonId = null,
            periodId = null,
            createdAt = null,
            updatedAt = null,
            syncedAt = OffsetDateTime.now()
        )
    }
}
