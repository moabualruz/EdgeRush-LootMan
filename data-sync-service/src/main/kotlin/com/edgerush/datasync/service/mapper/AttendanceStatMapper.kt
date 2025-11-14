package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateAttendanceStatRequest
import com.edgerush.datasync.api.dto.request.UpdateAttendanceStatRequest
import com.edgerush.datasync.api.dto.response.AttendanceStatResponse
import com.edgerush.datasync.entity.AttendanceStatEntity
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.OffsetDateTime

@Component
class AttendanceStatMapper {

    fun toEntity(request: CreateAttendanceStatRequest): AttendanceStatEntity {
        return AttendanceStatEntity(
            id = null,
            instance = request.instance,
            encounter = request.encounter,
            startDate = request.startDate,
            endDate = request.endDate,
            characterId = request.characterId,
            characterName = request.characterName ?: "",
            characterRealm = request.characterRealm,
            characterClass = request.characterClass,
            characterRole = request.characterRole,
            characterRegion = request.characterRegion,
            attendedAmountOfRaids = request.attendedAmountOfRaids,
            totalAmountOfRaids = request.totalAmountOfRaids,
            attendedPercentage = request.attendedPercentage,
            selectedAmountOfEncounters = request.selectedAmountOfEncounters,
            totalAmountOfEncounters = request.totalAmountOfEncounters,
            selectedPercentage = request.selectedPercentage,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(entity: AttendanceStatEntity, request: UpdateAttendanceStatRequest): AttendanceStatEntity {
        return entity.copy(
            instance = request.instance ?: entity.instance,
            encounter = request.encounter ?: entity.encounter,
            startDate = request.startDate ?: entity.startDate,
            endDate = request.endDate ?: entity.endDate,
            characterId = request.characterId ?: entity.characterId,
            characterName = request.characterName ?: entity.characterName,
            characterRealm = request.characterRealm ?: entity.characterRealm,
            characterClass = request.characterClass ?: entity.characterClass,
            characterRole = request.characterRole ?: entity.characterRole,
            characterRegion = request.characterRegion ?: entity.characterRegion,
            attendedAmountOfRaids = request.attendedAmountOfRaids ?: entity.attendedAmountOfRaids,
            totalAmountOfRaids = request.totalAmountOfRaids ?: entity.totalAmountOfRaids,
            attendedPercentage = request.attendedPercentage ?: entity.attendedPercentage,
            selectedAmountOfEncounters = request.selectedAmountOfEncounters ?: entity.selectedAmountOfEncounters,
            totalAmountOfEncounters = request.totalAmountOfEncounters ?: entity.totalAmountOfEncounters,
            selectedPercentage = request.selectedPercentage ?: entity.selectedPercentage,
            teamId = request.teamId ?: entity.teamId,
            seasonId = request.seasonId ?: entity.seasonId,
            periodId = request.periodId ?: entity.periodId,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: AttendanceStatEntity): AttendanceStatResponse {
        return AttendanceStatResponse(
            id = entity.id!!,
            instance = entity.instance!!,
            encounter = entity.encounter!!,
            startDate = entity.startDate!!,
            endDate = entity.endDate!!,
            characterId = entity.characterId!!,
            characterName = entity.characterName,
            characterRealm = entity.characterRealm!!,
            characterClass = entity.characterClass!!,
            characterRole = entity.characterRole!!,
            characterRegion = entity.characterRegion!!,
            attendedAmountOfRaids = entity.attendedAmountOfRaids!!,
            totalAmountOfRaids = entity.totalAmountOfRaids!!,
            attendedPercentage = entity.attendedPercentage!!,
            selectedAmountOfEncounters = entity.selectedAmountOfEncounters!!,
            totalAmountOfEncounters = entity.totalAmountOfEncounters!!,
            selectedPercentage = entity.selectedPercentage!!,
            teamId = entity.teamId!!,
            seasonId = entity.seasonId!!,
            periodId = entity.periodId!!,
            syncedAt = entity.syncedAt,
        )
    }
}
