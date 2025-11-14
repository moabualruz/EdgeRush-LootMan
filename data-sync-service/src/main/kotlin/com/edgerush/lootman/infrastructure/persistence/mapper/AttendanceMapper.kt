package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Mapper for converting between AttendanceRecord domain models and AttendanceStatEntity database entities.
 *
 * Note: The attendance_stats table stores aggregated statistics, so we map individual records
 * by treating each stat entity as representing attendance for the end date of the period.
 */
class AttendanceMapper {
    /**
     * Maps an AttendanceStatEntity to an AttendanceRecord domain model.
     *
     * @param entity The database entity
     * @param guildId The guild identifier (not stored in legacy table)
     * @return AttendanceRecord domain model
     */
    fun toDomain(
        entity: AttendanceStatEntity,
        guildId: GuildId,
    ): AttendanceRecord {
        val characterId = entity.characterId ?: throw IllegalArgumentException("Entity must have a character ID")
        val raiderId = RaiderId(characterId)
        val startDate = entity.startDate ?: throw IllegalArgumentException("Entity must have a start date")
        val endDate = entity.endDate ?: startDate
        val instance = entity.instance ?: "Unknown"
        val attendedRaids = entity.attendedAmountOfRaids ?: 0
        val totalRaids = entity.totalAmountOfRaids ?: 1
        
        return AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance,
            encounter = entity.encounter,
            startDate = startDate,
            endDate = endDate,
            attendedRaids = attendedRaids,
            totalRaids = totalRaids,
        )
    }

    /**
     * Maps an AttendanceRecord domain model to an AttendanceStatEntity.
     *
     * @param domain The domain model
     * @return AttendanceStatEntity ready to be persisted
     */
    fun toEntity(domain: AttendanceRecord): AttendanceStatEntity {
        val id = domain.id.value.toLongOrNull()
        
        return AttendanceStatEntity(
            id = id,
            instance = domain.instance,
            encounter = domain.encounter,
            startDate = domain.startDate,
            endDate = domain.endDate,
            characterId = domain.raiderId.value,
            characterName = "", // Not tracked in domain model
            characterRealm = null,
            characterClass = null,
            characterRole = null,
            characterRegion = null,
            attendedAmountOfRaids = domain.attendedRaids,
            totalAmountOfRaids = domain.totalRaids,
            attendedPercentage = domain.attendancePercentage,
            selectedAmountOfEncounters = null,
            totalAmountOfEncounters = null,
            selectedPercentage = null,
            teamId = null,
            seasonId = null,
            periodId = null,
            syncedAt = OffsetDateTime.now(ZoneOffset.UTC),
        )
    }
}
