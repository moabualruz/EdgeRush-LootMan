package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import org.springframework.stereotype.Service

/**
 * Implementation of RaiderVaultSlotCrudService.
 *
 * Provides CRUD operations for raider vault slots.
 */
@Service
class RaiderVaultSlotCrudServiceImpl(
    private val repository: RaiderVaultSlotRepository,
) : RaiderVaultSlotCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderVaultSlotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { RaiderVaultSlotResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): RaiderVaultSlotResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Vault slot not found with id: $id")
        return RaiderVaultSlotResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateRaiderVaultSlotRequest): RaiderVaultSlotResponse {
        val entity = RaiderVaultSlotEntity(
            raiderId = request.raiderId,
            slot = request.slot,
            unlocked = request.unlocked,
        )
        val saved = repository.save(entity)
        return RaiderVaultSlotResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateRaiderVaultSlotRequest): RaiderVaultSlotResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Vault slot not found with id: $id")

        val updated = existing.copy(
            slot = request.slot ?: existing.slot,
            unlocked = request.unlocked ?: existing.unlocked,
        )

        repository.save(updated)
        return RaiderVaultSlotResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Vault slot not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByRaider(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderVaultSlotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByRaiderId(raiderId, offset, pageRequest.size)
        val total = repository.countByRaiderId(raiderId)

        return PagedResponse(
            content = entities.map { RaiderVaultSlotResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findUnlockedByRaider(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderVaultSlotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findUnlockedByRaiderId(raiderId, offset, pageRequest.size)
        val total = repository.countUnlockedByRaiderId(raiderId)

        return PagedResponse(
            content = entities.map { RaiderVaultSlotResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByRaider(raiderId: Long): Long {
        return repository.countByRaiderId(raiderId)
    }
}
