package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateApplicationQuestionFileRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationQuestionFileRequest
import com.edgerush.datasync.api.dto.response.ApplicationQuestionFileResponse
import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import org.springframework.stereotype.Component

@Component
class ApplicationQuestionFileMapper {

    fun toEntity(request: CreateApplicationQuestionFileRequest): ApplicationQuestionFileEntity {
        return ApplicationQuestionFileEntity(
            id = null,
            applicationId = request.applicationId ?: 0L,
            questionPosition = request.questionPosition,
            question = request.question,
            originalFilename = request.originalFilename,
            url = request.url,
        )
    }

    fun updateEntity(entity: ApplicationQuestionFileEntity, request: UpdateApplicationQuestionFileRequest): ApplicationQuestionFileEntity {
        return entity.copy(
            applicationId = request.applicationId ?: entity.applicationId,
            questionPosition = request.questionPosition ?: entity.questionPosition,
            question = request.question ?: entity.question,
            originalFilename = request.originalFilename ?: entity.originalFilename,
            url = request.url ?: entity.url,
        )
    }

    fun toResponse(entity: ApplicationQuestionFileEntity): ApplicationQuestionFileResponse {
        return ApplicationQuestionFileResponse(
            id = entity.id!!,
            applicationId = entity.applicationId,
            questionPosition = entity.questionPosition!!,
            question = entity.question!!,
            originalFilename = entity.originalFilename!!,
            url = entity.url!!,
        )
    }
}
