package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.applications.model.*
import com.edgerush.datasync.entity.ApplicationEntity
import org.springframework.stereotype.Component

/**
 * Mapper between Application domain model and ApplicationEntity
 */
@Component
class ApplicationMapper {

    fun toDomain(
        entity: ApplicationEntity,
        alts: List<CharacterInfo>,
        questions: List<ApplicationQuestion>
    ): Application {
        val applicantInfo = ApplicantInfo(
            age = entity.age,
            country = entity.country,
            battletag = entity.battletag,
            discordId = entity.discordId,
            role = entity.role
        )

        val mainCharacter = CharacterInfo(
            name = entity.mainCharacterName ?: "",
            realm = entity.mainCharacterRealm ?: "",
            region = entity.mainCharacterRegion ?: "",
            characterClass = entity.mainCharacterClass ?: "",
            role = entity.mainCharacterRole ?: "",
            race = entity.mainCharacterRace,
            faction = entity.mainCharacterFaction,
            level = entity.mainCharacterLevel
        )

        return Application(
            id = ApplicationId(entity.applicationId),
            appliedAt = entity.appliedAt ?: entity.syncedAt,
            status = ApplicationStatus.fromString(entity.status),
            applicantInfo = applicantInfo,
            mainCharacter = mainCharacter,
            altCharacters = alts,
            questions = questions
        )
    }

    fun toEntity(application: Application): ApplicationEntity {
        return ApplicationEntity(
            applicationId = application.id.value,
            appliedAt = application.appliedAt,
            status = application.status.name,
            role = application.applicantInfo.role,
            age = application.applicantInfo.age,
            country = application.applicantInfo.country,
            battletag = application.applicantInfo.battletag,
            discordId = application.applicantInfo.discordId,
            mainCharacterName = application.mainCharacter.name,
            mainCharacterRealm = application.mainCharacter.realm,
            mainCharacterClass = application.mainCharacter.characterClass,
            mainCharacterRole = application.mainCharacter.role,
            mainCharacterRace = application.mainCharacter.race,
            mainCharacterFaction = application.mainCharacter.faction,
            mainCharacterLevel = application.mainCharacter.level,
            mainCharacterRegion = application.mainCharacter.region,
            syncedAt = application.appliedAt
        )
    }
}
