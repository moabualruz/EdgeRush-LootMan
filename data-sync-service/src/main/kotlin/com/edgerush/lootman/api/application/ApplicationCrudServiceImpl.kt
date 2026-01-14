package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.application.repository.ApplicationRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class ApplicationCrudServiceImpl(private val repository: ApplicationRepository) : ApplicationCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<ApplicationResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { ApplicationResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): ApplicationResponse = repository.findById(id)?.let { ApplicationResponse.from(it) }
        ?: throw NoSuchElementException("Application not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateApplicationRequest): ApplicationResponse {
        val entity = ApplicationEntity(
            request.applicationId, request.appliedAt, request.status, request.role, request.age,
            request.country, request.battletag, request.discordId, request.mainCharacterName,
            request.mainCharacterRealm, request.mainCharacterClass, request.mainCharacterRole,
            request.mainCharacterRace, request.mainCharacterFaction, request.mainCharacterLevel,
            request.mainCharacterRegion, OffsetDateTime.now()
        )
        return ApplicationResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateApplicationRequest): ApplicationResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("Application not found with id: $id")
        val updated = existing.copy(
            status = request.status ?: existing.status,
            role = request.role ?: existing.role,
            age = request.age ?: existing.age,
            country = request.country ?: existing.country,
            battletag = request.battletag ?: existing.battletag,
            discordId = request.discordId ?: existing.discordId
        )
        return ApplicationResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("Application not found with id: $id")
        repository.delete(id)
    }

    override fun findByStatus(status: String, pageRequest: PageRequest): PagedResponse<ApplicationResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByStatus(status, offset, pageRequest.size).map { ApplicationResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByStatus(status))
    }

    override fun countByStatus(status: String): Long = repository.countByStatus(status)
}
