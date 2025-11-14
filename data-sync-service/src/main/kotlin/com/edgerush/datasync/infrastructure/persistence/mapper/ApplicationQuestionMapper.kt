package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.applications.model.ApplicationFile
import com.edgerush.datasync.domain.applications.model.ApplicationQuestion
import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component

/**
 * Mapper between ApplicationQuestion domain model and ApplicationQuestionEntity
 */
@Component
class ApplicationQuestionMapper(
    private val objectMapper: ObjectMapper
) {

    fun toDomain(entity: ApplicationQuestionEntity, files: List<ApplicationQuestionFileEntity>): ApplicationQuestion {
        val applicationFiles = files.map { file ->
            ApplicationFile(
                originalFilename = file.originalFilename ?: "",
                url = file.url ?: ""
            )
        }

        return ApplicationQuestion(
            position = entity.position ?: 0,
            question = entity.question ?: "",
            answer = entity.answer ?: "",
            files = applicationFiles
        )
    }

    fun toEntity(question: ApplicationQuestion, applicationId: Long): ApplicationQuestionEntity {
        val filesJson = if (question.files.isNotEmpty()) {
            objectMapper.writeValueAsString(question.files)
        } else {
            null
        }

        return ApplicationQuestionEntity(
            id = null,
            applicationId = applicationId,
            position = question.position,
            question = question.question,
            answer = question.answer,
            filesJson = filesJson
        )
    }

    fun toFileEntity(file: ApplicationFile, applicationId: Long, questionPosition: Int, question: String): ApplicationQuestionFileEntity {
        return ApplicationQuestionFileEntity(
            id = null,
            applicationId = applicationId,
            questionPosition = questionPosition,
            question = question,
            originalFilename = file.originalFilename,
            url = file.url
        )
    }
}
