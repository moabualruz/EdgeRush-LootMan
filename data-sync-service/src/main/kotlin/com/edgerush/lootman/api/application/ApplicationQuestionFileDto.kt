package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import jakarta.validation.constraints.NotNull

data class CreateApplicationQuestionFileRequest(
    @field:NotNull(message = "Application ID is required")
    val applicationId: Long,
    val questionPosition: Int? = null,
    val question: String? = null,
    val originalFilename: String? = null,
    val url: String? = null,
)

data class UpdateApplicationQuestionFileRequest(
    val questionPosition: Int? = null,
    val question: String? = null,
    val originalFilename: String? = null,
    val url: String? = null,
)

data class ApplicationQuestionFileResponse(
    val id: Long,
    val applicationId: Long,
    val questionPosition: Int?,
    val question: String?,
    val originalFilename: String?,
    val url: String?,
) {
    companion object {
        fun from(e: ApplicationQuestionFileEntity) = ApplicationQuestionFileResponse(
            e.id!!, e.applicationId, e.questionPosition, e.question, e.originalFilename, e.url
        )
    }
}

data class ApplicationQuestionFileExistsResponse(val exists: Boolean)
data class ApplicationQuestionFileCountResponse(val count: Long)
