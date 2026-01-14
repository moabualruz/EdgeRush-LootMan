package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.loot.repository.LootAwardOldItemRepository
import org.springframework.stereotype.Service

@Service
class LootAwardOldItemCrudServiceImpl(private val repository: LootAwardOldItemRepository) : LootAwardOldItemCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<LootAwardOldItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { LootAwardOldItemResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): LootAwardOldItemResponse = repository.findById(id)?.let { LootAwardOldItemResponse.from(it) }
        ?: throw NoSuchElementException("LootAwardOldItem not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateLootAwardOldItemRequest): LootAwardOldItemResponse {
        val entity = LootAwardOldItemEntity(null, request.lootAwardId, request.itemId, request.bonusId)
        return LootAwardOldItemResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateLootAwardOldItemRequest): LootAwardOldItemResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("LootAwardOldItem not found with id: $id")
        val updated = existing.copy(
            itemId = request.itemId ?: existing.itemId,
            bonusId = request.bonusId ?: existing.bonusId
        )
        return LootAwardOldItemResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("LootAwardOldItem not found with id: $id")
        repository.delete(id)
    }

    override fun findByLootAwardId(lootAwardId: Long, pageRequest: PageRequest): PagedResponse<LootAwardOldItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByLootAwardId(lootAwardId, offset, pageRequest.size).map { LootAwardOldItemResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByLootAwardId(lootAwardId))
    }

    override fun countByLootAwardId(lootAwardId: Long): Long = repository.countByLootAwardId(lootAwardId)
}
