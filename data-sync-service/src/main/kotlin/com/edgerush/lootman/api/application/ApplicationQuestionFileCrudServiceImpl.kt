package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionFileRepository
import org.springframework.stereotype.Service

@Service
class ApplicationQuestionFileCrudServiceImpl(private val repository: ApplicationQuestionFileRepository) : ApplicationQuestionFileCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<ApplicationQuestionFileResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { ApplicationQuestionFileResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): ApplicationQuestionFileResponse = repository.findById(id)?.let { ApplicationQuestionFileResponse.from(it) }
        ?: throw NoSuchElementException("ApplicationQuestionFile not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateApplicationQuestionFileRequest): ApplicationQuestionFileResponse {
        val entity = ApplicationQuestionFileEntity(
            null, request.applicationId, request.questionPosition, request.question, request.originalFilename, request.url
        )
        return ApplicationQuestionFileResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateApplicationQuestionFileRequest): ApplicationQuestionFileResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("ApplicationQuestionFile not found with id: $id")
        val updated = existing.copy(
            questionPosition = request.questionPosition ?: existing.questionPosition,
            question = request.question ?: existing.question,
            originalFilename = request.originalFilename ?: existing.originalFilename,
            url = request.url ?: existing.url
        )
        return ApplicationQuestionFileResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("ApplicationQuestionFile not found with id: $id")
        repository.delete(id)
    }

    override fun findByApplicationId(applicationId: Long, pageRequest: PageRequest): PagedResponse<ApplicationQuestionFileResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByApplicationId(applicationId, offset, pageRequest.size).map { ApplicationQuestionFileResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByApplicationId(applicationId))
    }

    override fun countByApplicationId(applicationId: Long): Long = repository.countByApplicationId(applicationId)
}
