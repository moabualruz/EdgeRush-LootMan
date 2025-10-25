package com.edgerush.datasync.service

import com.edgerush.datasync.entity.AttendanceStatEntity
import java.time.OffsetDateTime

data class AttendanceRecord(
    val instance: String?,
    val encounter: String?,
    val startDate: String?,
    val endDate: String?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val attendedAmountOfRaids: Int?,
    val totalAmountOfRaids: Int?,
    val attendedPercentage: Double?,
    val selectedAmountOfEncounters: Int?,
    val totalAmountOfEncounters: Int?,
    val selectedPercentage: Double?
) {
    fun toEntity(): AttendanceStatEntity = AttendanceStatEntity(
        instance = instance,
        encounter = encounter,
        startDate = parseLocalDate(startDate),
        endDate = parseLocalDate(endDate),
        characterId = characterId,
        characterName = characterName,
        characterRealm = characterRealm,
        characterClass = characterClass,
        characterRole = characterRole,
        attendedAmountOfRaids = attendedAmountOfRaids,
        totalAmountOfRaids = totalAmountOfRaids,
        attendedPercentage = attendedPercentage,
        selectedAmountOfEncounters = selectedAmountOfEncounters,
        totalAmountOfEncounters = totalAmountOfEncounters,
        selectedPercentage = selectedPercentage,
        syncedAt = OffsetDateTime.now()
    )
}

