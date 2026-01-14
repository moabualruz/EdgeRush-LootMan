package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.application.repository.ApplicationAltRepository
import org.springframework.stereotype.Service

@Service
class ApplicationAltCrudServiceImpl(private val repository: ApplicationAltRepository) : ApplicationAltCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<ApplicationAltResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { ApplicationAltResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): ApplicationAltResponse = repository.findById(id)?.let { ApplicationAltResponse.from(it) }
        ?: throw NoSuchElementException("ApplicationAlt not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateApplicationAltRequest): ApplicationAltResponse {
        val entity = ApplicationAltEntity(
            null, request.applicationId, request.name, request.realm, request.region,
            request.clazz, request.role, request.level, request.faction, request.race
        )
        return ApplicationAltResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateApplicationAltRequest): ApplicationAltResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("ApplicationAlt not found with id: $id")
        val updated = existing.copy(
            name = request.name ?: existing.name,
            realm = request.realm ?: existing.realm,
            region = request.region ?: existing.region,
            clazz = request.clazz ?: existing.clazz,
            role = request.role ?: existing.role,
            level = request.level ?: existing.level
        )
        return ApplicationAltResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("ApplicationAlt not found with id: $id")
        repository.delete(id)
    }

    override fun findByApplicationId(applicationId: Long, pageRequest: PageRequest): PagedResponse<ApplicationAltResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByApplicationId(applicationId, offset, pageRequest.size).map { ApplicationAltResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByApplicationId(applicationId))
    }

    override fun countByApplicationId(applicationId: Long): Long = repository.countByApplicationId(applicationId)
}
