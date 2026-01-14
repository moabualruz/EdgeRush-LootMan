package com.edgerush.lootman.api.behavioral

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.behavioral.repository.BehavioralActionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Implementation of BehavioralActionCrudService.
 *
 * Provides CRUD operations for behavioral actions.
 */
@Service
class BehavioralActionCrudServiceImpl(
    private val repository: BehavioralActionRepository,
) : BehavioralActionCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<BehavioralActionResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { BehavioralActionResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): BehavioralActionResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Behavioral action not found with id: $id")
        return BehavioralActionResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateBehavioralActionRequest): BehavioralActionResponse {
        val entity = BehavioralActionEntity(
            guildId = request.guildId,
            characterName = request.characterName,
            actionType = request.actionType,
            deductionAmount = request.deductionAmount,
            reason = request.reason,
            appliedBy = request.appliedBy,
            appliedAt = LocalDateTime.now(),
            expiresAt = request.expiresAt,
            isActive = true,
        )
        val saved = repository.save(entity)
        return BehavioralActionResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateBehavioralActionRequest): BehavioralActionResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Behavioral action not found with id: $id")

        val updated = existing.copy(
            reason = request.reason ?: existing.reason,
            deductionAmount = request.deductionAmount ?: existing.deductionAmount,
            expiresAt = request.expiresAt ?: existing.expiresAt,
            isActive = request.isActive ?: existing.isActive,
        )

        repository.save(updated)
        return BehavioralActionResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Behavioral action not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByGuild(guildId: String, pageRequest: PageRequest): PagedResponse<BehavioralActionResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByGuildId(guildId, offset, pageRequest.size)
        val total = repository.countByGuildId(guildId)

        return PagedResponse(
            content = entities.map { BehavioralActionResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findActiveByGuild(guildId: String, pageRequest: PageRequest): PagedResponse<BehavioralActionResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findActiveByGuildId(guildId, offset, pageRequest.size)
        val total = repository.countActiveByGuildId(guildId)

        return PagedResponse(
            content = entities.map { BehavioralActionResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByCharacter(guildId: String, characterName: String, pageRequest: PageRequest): PagedResponse<BehavioralActionResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByCharacter(guildId, characterName, offset, pageRequest.size)
        val total = repository.countByCharacter(guildId, characterName)

        return PagedResponse(
            content = entities.map { BehavioralActionResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun getTotalDeduction(guildId: String, characterName: String): Double {
        return repository.getTotalActiveDeduction(guildId, characterName)
    }

    override fun countByGuild(guildId: String): Long {
        return repository.countByGuildId(guildId)
    }
}
