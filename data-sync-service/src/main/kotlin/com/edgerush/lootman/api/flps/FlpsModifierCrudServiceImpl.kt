package com.edgerush.lootman.api.flps

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.flps.repository.FlpsDefaultModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsGuildModifierRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of FlpsDefaultModifierCrudService.
 */
@Service
class FlpsDefaultModifierCrudServiceImpl(
    private val repository: FlpsDefaultModifierRepository,
) : FlpsDefaultModifierCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<FlpsDefaultModifierResponse> {
        val modifiers = repository.findAll(pageRequest.offset, pageRequest.size)
        val total = repository.count()
        return PagedResponse.of(
            content = modifiers.map { FlpsDefaultModifierResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findById(id: Long): FlpsDefaultModifierResponse {
        val modifier = repository.findById(id)
            ?: throw NoSuchElementException("Default modifier not found with id: $id")
        return FlpsDefaultModifierResponse.from(modifier)
    }

    override fun create(request: CreateFlpsDefaultModifierRequest): FlpsDefaultModifierResponse {
        val now = OffsetDateTime.now()
        val entity = FlpsDefaultModifierEntity(
            id = null,
            category = request.category,
            modifierKey = request.modifierKey,
            modifierValue = request.modifierValue,
            description = request.description,
            createdAt = now,
            updatedAt = now,
        )
        val saved = repository.save(entity)
        return FlpsDefaultModifierResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateFlpsDefaultModifierRequest): FlpsDefaultModifierResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Default modifier not found with id: $id")

        val updated = existing.copy(
            category = request.category ?: existing.category,
            modifierKey = request.modifierKey ?: existing.modifierKey,
            modifierValue = request.modifierValue ?: existing.modifierValue,
            description = request.description ?: existing.description,
            updatedAt = OffsetDateTime.now(),
        )

        val saved = repository.save(updated)
        return FlpsDefaultModifierResponse.from(saved)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Default modifier not found with id: $id")
        }
        repository.delete(id)
    }

    override fun existsById(id: Long): Boolean =
        repository.existsById(id)

    override fun findByCategory(category: String, pageRequest: PageRequest): PagedResponse<FlpsDefaultModifierResponse> {
        val modifiers = repository.findByCategory(category, pageRequest.offset, pageRequest.size)
        val total = repository.countByCategory(category)
        return PagedResponse.of(
            content = modifiers.map { FlpsDefaultModifierResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }
}

/**
 * Implementation of FlpsGuildModifierCrudService.
 */
@Service
class FlpsGuildModifierCrudServiceImpl(
    private val repository: FlpsGuildModifierRepository,
) : FlpsGuildModifierCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<FlpsGuildModifierResponse> {
        val modifiers = repository.findAll(pageRequest.offset, pageRequest.size)
        val total = repository.count()
        return PagedResponse.of(
            content = modifiers.map { FlpsGuildModifierResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findById(id: Long): FlpsGuildModifierResponse {
        val modifier = repository.findById(id)
            ?: throw NoSuchElementException("Guild modifier not found with id: $id")
        return FlpsGuildModifierResponse.from(modifier)
    }

    override fun create(request: CreateFlpsGuildModifierRequest): FlpsGuildModifierResponse {
        val now = OffsetDateTime.now()
        val entity = FlpsGuildModifierEntity(
            id = null,
            guildId = request.guildId,
            category = request.category,
            modifierKey = request.modifierKey,
            modifierValue = request.modifierValue,
            description = request.description,
            createdAt = now,
            updatedAt = now,
        )
        val saved = repository.save(entity)
        return FlpsGuildModifierResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateFlpsGuildModifierRequest): FlpsGuildModifierResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Guild modifier not found with id: $id")

        val updated = existing.copy(
            category = request.category ?: existing.category,
            modifierKey = request.modifierKey ?: existing.modifierKey,
            modifierValue = request.modifierValue ?: existing.modifierValue,
            description = request.description ?: existing.description,
            updatedAt = OffsetDateTime.now(),
        )

        val saved = repository.save(updated)
        return FlpsGuildModifierResponse.from(saved)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Guild modifier not found with id: $id")
        }
        repository.delete(id)
    }

    override fun existsById(id: Long): Boolean =
        repository.existsById(id)

    override fun findByGuild(guildId: String, pageRequest: PageRequest): PagedResponse<FlpsGuildModifierResponse> {
        val modifiers = repository.findByGuildId(guildId, pageRequest.offset, pageRequest.size)
        val total = repository.countByGuildId(guildId)
        return PagedResponse.of(
            content = modifiers.map { FlpsGuildModifierResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findByGuildAndCategory(guildId: String, category: String, pageRequest: PageRequest): PagedResponse<FlpsGuildModifierResponse> {
        val modifiers = repository.findByGuildIdAndCategory(guildId, category, pageRequest.offset, pageRequest.size)
        val total = repository.countByGuildIdAndCategory(guildId, category)
        return PagedResponse.of(
            content = modifiers.map { FlpsGuildModifierResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun countByGuild(guildId: String): Long =
        repository.countByGuildId(guildId)
}
