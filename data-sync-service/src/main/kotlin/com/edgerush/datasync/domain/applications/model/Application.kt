package com.edgerush.datasync.domain.applications.model

import java.time.OffsetDateTime

/**
 * Aggregate root representing a guild application
 */
data class Application(
    val id: ApplicationId,
    val appliedAt: OffsetDateTime,
    val status: ApplicationStatus,
    val applicantInfo: ApplicantInfo,
    val mainCharacter: CharacterInfo,
    val altCharacters: List<CharacterInfo> = emptyList(),
    val questions: List<ApplicationQuestion> = emptyList()
) {
    fun approve(): Application {
        require(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Can only approve pending or under review applications"
        }
        return copy(status = ApplicationStatus.APPROVED)
    }

    fun reject(): Application {
        require(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Can only reject pending or under review applications"
        }
        return copy(status = ApplicationStatus.REJECTED)
    }

    fun withdraw(): Application {
        require(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Can only withdraw pending or under review applications"
        }
        return copy(status = ApplicationStatus.WITHDRAWN)
    }

    fun startReview(): Application {
        require(status == ApplicationStatus.PENDING) {
            "Can only start review on pending applications"
        }
        return copy(status = ApplicationStatus.UNDER_REVIEW)
    }

    fun isActive(): Boolean = status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW

    fun isClosed(): Boolean = status == ApplicationStatus.APPROVED || 
                              status == ApplicationStatus.REJECTED || 
                              status == ApplicationStatus.WITHDRAWN

    companion object {
        fun create(
            id: ApplicationId,
            appliedAt: OffsetDateTime,
            applicantInfo: ApplicantInfo,
            mainCharacter: CharacterInfo,
            altCharacters: List<CharacterInfo> = emptyList(),
            questions: List<ApplicationQuestion> = emptyList()
        ): Application = Application(
            id = id,
            appliedAt = appliedAt,
            status = ApplicationStatus.PENDING,
            applicantInfo = applicantInfo,
            mainCharacter = mainCharacter,
            altCharacters = altCharacters,
            questions = questions
        )
    }
}

/**
 * Value object representing applicant personal information
 */
data class ApplicantInfo(
    val age: Int?,
    val country: String?,
    val battletag: String?,
    val discordId: String?,
    val role: String?
) {
    init {
        age?.let { require(it in 13..120) { "Age must be between 13 and 120" } }
    }
}
