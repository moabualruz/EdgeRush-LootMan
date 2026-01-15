package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.loot.repository.LootBanEntityRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Implementation of LootBanCrudService.
 *
 * Provides CRUD operations for loot bans.
 */
@Service
class LootBanCrudServiceImpl(
    private val repository: LootBanEntityRepository,
) : LootBanCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<LootBanResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { LootBanResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): LootBanResponse {
        val entity =
            repository.findById(id)
                ?: throw NoSuchElementException("Loot ban not found with id: $id")
        return LootBanResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateLootBanEntityRequest): LootBanResponse {
        val entity =
            LootBanEntity(
                guildId = request.guildId,
                characterName = request.characterName,
                reason = request.reason,
                bannedBy = request.bannedBy,
                bannedAt = LocalDateTime.now(),
                expiresAt = request.expiresAt,
                isActive = true,
            )
        val saved = repository.save(entity)
        return LootBanResponse.from(saved)
    }

    override fun update(
        id: Long,
        request: UpdateLootBanEntityRequest,
    ): LootBanResponse {
        val existing =
            repository.findById(id)
                ?: throw NoSuchElementException("Loot ban not found with id: $id")

        val updated =
            existing.copy(
                reason = request.reason ?: existing.reason,
                expiresAt = request.expiresAt ?: existing.expiresAt,
                isActive = request.isActive ?: existing.isActive,
            )

        repository.save(updated)
        return LootBanResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Loot ban not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<LootBanResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByGuildId(guildId, offset, pageRequest.size)
        val total = repository.countByGuildId(guildId)

        return PagedResponse(
            content = entities.map { LootBanResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findActiveByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<LootBanResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findActiveByGuildId(guildId, offset, pageRequest.size)
        val total = repository.countActiveByGuildId(guildId)

        return PagedResponse(
            content = entities.map { LootBanResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun isCharacterBanned(
        guildId: String,
        characterName: String,
    ): Boolean {
        return repository.isCharacterBanned(guildId, characterName)
    }

    override fun countByGuild(guildId: String): Long {
        return repository.countByGuildId(guildId)
    }
}
