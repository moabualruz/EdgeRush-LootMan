package com.edgerush.datasync.api.dto.response

data class ApplicationQuestionFileResponse(
    val id: Long?,
    val applicationId: Long,
    val questionPosition: Int?,
    val question: String?,
    val originalFilename: String?,
    val url: String?,
)
