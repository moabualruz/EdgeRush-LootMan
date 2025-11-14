package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.response.ApplicationQuestionResponse
import com.edgerush.datasync.entity.ApplicationQuestionEntity
import org.springframework.stereotype.Component

@Component
class ApplicationQuestionMapper {

    fun toEntity(request: CreateApplicationQuestionRequest): ApplicationQuestionEntity {
        return ApplicationQuestionEntity(
            id = null,
            applicationId = request.applicationId ?: 0L,
            position = request.position,
            question = request.question,
            answer = request.answer,
            filesJson = request.filesJson,
        )
    }

    fun updateEntity(entity: ApplicationQuestionEntity, request: UpdateApplicationQuestionRequest): ApplicationQuestionEntity {
        return entity.copy(
            applicationId = request.applicationId ?: entity.applicationId,
            position = request.position ?: entity.position,
            question = request.question ?: entity.question,
            answer = request.answer ?: entity.answer,
            filesJson = request.filesJson ?: entity.filesJson,
        )
    }

    fun toResponse(entity: ApplicationQuestionEntity): ApplicationQuestionResponse {
        return ApplicationQuestionResponse(
            id = entity.id!!,
            applicationId = entity.applicationId,
            position = entity.position!!,
            question = entity.question!!,
            answer = entity.answer!!,
            filesJson = entity.filesJson!!,
        )
    }
}
