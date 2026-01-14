package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionRepository
import org.springframework.stereotype.Service

@Service
class ApplicationQuestionCrudServiceImpl(private val repository: ApplicationQuestionRepository) : ApplicationQuestionCrudService {
    override fun findAll(pageRequest: PageRequest) = PagedResponse(repository.findAll(pageRequest.page.toLong() * pageRequest.size, pageRequest.size).map { ApplicationQuestionResponse.from(it) }, pageRequest.page, pageRequest.size, repository.count())
    override fun findById(id: Long) = repository.findById(id)?.let { ApplicationQuestionResponse.from(it) } ?: throw NoSuchElementException("ApplicationQuestion not found with id: $id")
    override fun existsById(id: Long) = repository.existsById(id)
    override fun create(request: CreateApplicationQuestionRequest) = ApplicationQuestionResponse.from(repository.save(ApplicationQuestionEntity(null, request.applicationId, request.position, request.question, request.answer, request.filesJson)))
    override fun update(id: Long, request: UpdateApplicationQuestionRequest): ApplicationQuestionResponse { val existing = repository.findById(id) ?: throw NoSuchElementException("ApplicationQuestion not found with id: $id"); return ApplicationQuestionResponse.from(repository.save(existing.copy(answer = request.answer ?: existing.answer, filesJson = request.filesJson ?: existing.filesJson))) }
    override fun delete(id: Long) { if (!repository.existsById(id)) throw NoSuchElementException("ApplicationQuestion not found with id: $id"); repository.delete(id) }
    override fun findByApplicationId(applicationId: Long, pageRequest: PageRequest) = PagedResponse(repository.findByApplicationId(applicationId, pageRequest.page.toLong() * pageRequest.size, pageRequest.size).map { ApplicationQuestionResponse.from(it) }, pageRequest.page, pageRequest.size, repository.countByApplicationId(applicationId))
    override fun countByApplicationId(applicationId: Long) = repository.countByApplicationId(applicationId)
}
