package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateApplicationQuestionFileRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,
    val questionPosition: Int? = null,
    val question: String? = null,
    val originalFilename: String? = null,
    val url: String? = null,
)

data class UpdateApplicationQuestionFileRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,
    val questionPosition: Int? = null,
    val question: String? = null,
    val originalFilename: String? = null,
    val url: String? = null,
)
