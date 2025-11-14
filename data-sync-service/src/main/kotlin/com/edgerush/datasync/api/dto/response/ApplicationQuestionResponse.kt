package com.edgerush.datasync.api.dto.response


data class ApplicationQuestionResponse(
    val id: Long?,
    val applicationId: Long,
    val position: Int?,
    val question: String?,
    val answer: String?,
    val filesJson: String?
)
