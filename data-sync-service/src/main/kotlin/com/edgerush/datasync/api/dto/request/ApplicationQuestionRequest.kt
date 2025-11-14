package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateApplicationQuestionRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,
    val position: Int? = null,
    val question: String? = null,
    val answer: String? = null,
    val filesJson: String? = null,
)

data class UpdateApplicationQuestionRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,
    val position: Int? = null,
    val question: String? = null,
    val answer: String? = null,
    val filesJson: String? = null,
)
