package com.edgerush.datasync.api.dto

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.model.ApplicationQuestion
import com.edgerush.datasync.domain.applications.model.CharacterInfo
import java.time.OffsetDateTime

/**
 * DTO for Application responses
 */
data class ApplicationDto(
    val id: Long,
    val appliedAt: OffsetDateTime,
    val status: String,
    val applicantInfo: ApplicantInfoDto,
    val mainCharacter: CharacterInfoDto,
    val altCharacters: List<CharacterInfoDto>,
    val questions: List<ApplicationQuestionDto>
) {
    companion object {
        fun from(application: Application): ApplicationDto {
            return ApplicationDto(
                id = application.id.value,
                appliedAt = application.appliedAt,
                status = application.status.name,
                applicantInfo = ApplicantInfoDto.from(application.applicantInfo),
                mainCharacter = CharacterInfoDto.from(application.mainCharacter),
                altCharacters = application.altCharacters.map { CharacterInfoDto.from(it) },
                questions = application.questions.map { ApplicationQuestionDto.from(it) }
            )
        }
    }
}

data class ApplicantInfoDto(
    val age: Int?,
    val country: String?,
    val battletag: String?,
    val discordId: String?,
    val role: String?
) {
    companion object {
        fun from(info: com.edgerush.datasync.domain.applications.model.ApplicantInfo): ApplicantInfoDto {
            return ApplicantInfoDto(
                age = info.age,
                country = info.country,
                battletag = info.battletag,
                discordId = info.discordId,
                role = info.role
            )
        }
    }
}

data class CharacterInfoDto(
    val name: String,
    val realm: String,
    val region: String,
    val characterClass: String,
    val role: String,
    val race: String?,
    val faction: String?,
    val level: Int?
) {
    companion object {
        fun from(info: CharacterInfo): CharacterInfoDto {
            return CharacterInfoDto(
                name = info.name,
                realm = info.realm,
                region = info.region,
                characterClass = info.characterClass,
                role = info.role,
                race = info.race,
                faction = info.faction,
                level = info.level
            )
        }
    }
}

data class ApplicationQuestionDto(
    val position: Int,
    val question: String,
    val answer: String,
    val files: List<ApplicationFileDto>
) {
    companion object {
        fun from(question: ApplicationQuestion): ApplicationQuestionDto {
            return ApplicationQuestionDto(
                position = question.position,
                question = question.question,
                answer = question.answer,
                files = question.files.map { ApplicationFileDto.from(it) }
            )
        }
    }
}

data class ApplicationFileDto(
    val originalFilename: String,
    val url: String
) {
    companion object {
        fun from(file: com.edgerush.datasync.domain.applications.model.ApplicationFile): ApplicationFileDto {
            return ApplicationFileDto(
                originalFilename = file.originalFilename,
                url = file.url
            )
        }
    }
}

/**
 * Request DTO for reviewing applications
 */
data class ReviewApplicationRequest(
    val action: String
)
