package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import jakarta.validation.constraints.NotNull

data class CreateApplicationQuestionRequest(
    @field:NotNull val applicationId: Long,
    val position: Int? = null,
    val question: String? = null,
    val answer: String? = null,
    val filesJson: String? = null,
)

data class UpdateApplicationQuestionRequest(val answer: String? = null, val filesJson: String? = null)

data class ApplicationQuestionResponse(val id: Long, val applicationId: Long, val position: Int?, val question: String?, val answer: String?, val filesJson: String?) {
    companion object {
        fun from(e: ApplicationQuestionEntity) =
            ApplicationQuestionResponse(e.id!!, e.applicationId, e.position, e.question, e.answer, e.filesJson)
    }
}

data class ApplicationQuestionExistsResponse(val exists: Boolean)
