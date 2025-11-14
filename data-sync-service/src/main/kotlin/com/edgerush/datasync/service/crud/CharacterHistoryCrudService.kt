package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.request.UpdateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.response.CharacterHistoryResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.CharacterHistoryEntity
import com.edgerush.datasync.repository.CharacterHistoryRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.CharacterHistoryMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CharacterHistoryCrudService(
    private val repository: CharacterHistoryRepository,
    private val mapper: CharacterHistoryMapper,
    private val auditLogger: AuditLogger,
) : CrudService<CharacterHistoryEntity, Long, CreateCharacterHistoryRequest, UpdateCharacterHistoryRequest, CharacterHistoryResponse> {
    override fun findAll(pageable: Pageable): Page<CharacterHistoryResponse> {
        val allEntities = repository.findAll().toList()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allEntities.size)
        val end = (start + pageable.pageSize).coerceAtMost(allEntities.size)
        val pageContent = allEntities.subList(start, end)
        return org.springframework.data.domain.PageImpl(
            pageContent.map(mapper::toResponse),
            pageable,
            allEntities.size.toLong(),
        )
    }

    override fun findById(id: Long): CharacterHistoryResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("CharacterHistory not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateCharacterHistoryRequest,
        user: AuthenticatedUser,
    ): CharacterHistoryResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "CharacterHistory",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateCharacterHistoryRequest,
        user: AuthenticatedUser,
    ): CharacterHistoryResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("CharacterHistory not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "CharacterHistory",
            entityId = id,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(
        id: Long,
        user: AuthenticatedUser,
    ) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("CharacterHistory not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "CharacterHistory",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: CharacterHistoryEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
