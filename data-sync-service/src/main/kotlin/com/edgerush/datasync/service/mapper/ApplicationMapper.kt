package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateApplicationRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationRequest
import com.edgerush.datasync.api.dto.response.ApplicationResponse
import com.edgerush.datasync.entity.ApplicationEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class ApplicationMapper {
    fun toEntity(
        request: CreateApplicationRequest,
        applicationId: Long,
    ): ApplicationEntity {
        return ApplicationEntity(
            applicationId = applicationId,
            appliedAt = request.appliedAt,
            status = request.status,
            role = request.role,
            age = request.age,
            country = request.country,
            battletag = request.battletag,
            discordId = request.discordId,
            mainCharacterName = request.mainCharacterName,
            mainCharacterRealm = request.mainCharacterRealm,
            mainCharacterClass = request.mainCharacterClass,
            mainCharacterRole = request.mainCharacterRole,
            mainCharacterRace = request.mainCharacterRace,
            mainCharacterFaction = request.mainCharacterFaction,
            mainCharacterLevel = request.mainCharacterLevel,
            mainCharacterRegion = request.mainCharacterRegion,
            syncedAt = OffsetDateTime.now(),
        )
    }

    fun toEntity(
        existing: ApplicationEntity,
        request: UpdateApplicationRequest,
    ): ApplicationEntity {
        return existing.copy(
            appliedAt = request.appliedAt ?: existing.appliedAt,
            status = request.status ?: existing.status,
            role = request.role ?: existing.role,
            age = request.age ?: existing.age,
            country = request.country ?: existing.country,
            battletag = request.battletag ?: existing.battletag,
            discordId = request.discordId ?: existing.discordId,
            mainCharacterName = request.mainCharacterName ?: existing.mainCharacterName,
            mainCharacterRealm = request.mainCharacterRealm ?: existing.mainCharacterRealm,
            mainCharacterClass = request.mainCharacterClass ?: existing.mainCharacterClass,
            mainCharacterRole = request.mainCharacterRole ?: existing.mainCharacterRole,
            mainCharacterRace = request.mainCharacterRace ?: existing.mainCharacterRace,
            mainCharacterFaction = request.mainCharacterFaction ?: existing.mainCharacterFaction,
            mainCharacterLevel = request.mainCharacterLevel ?: existing.mainCharacterLevel,
            mainCharacterRegion = request.mainCharacterRegion ?: existing.mainCharacterRegion,
            syncedAt = OffsetDateTime.now(),
        )
    }

    fun toResponse(entity: ApplicationEntity): ApplicationResponse {
        return ApplicationResponse(
            applicationId = entity.applicationId,
            appliedAt = entity.appliedAt,
            status = entity.status,
            role = entity.role,
            age = entity.age,
            country = entity.country,
            battletag = entity.battletag,
            discordId = entity.discordId,
            mainCharacterName = entity.mainCharacterName,
            mainCharacterRealm = entity.mainCharacterRealm,
            mainCharacterClass = entity.mainCharacterClass,
            mainCharacterRole = entity.mainCharacterRole,
            mainCharacterRace = entity.mainCharacterRace,
            mainCharacterFaction = entity.mainCharacterFaction,
            mainCharacterLevel = entity.mainCharacterLevel,
            mainCharacterRegion = entity.mainCharacterRegion,
            syncedAt = entity.syncedAt,
        )
    }
}
