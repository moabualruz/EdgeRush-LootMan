package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.loot.repository.LootAwardBonusIdRepository
import org.springframework.stereotype.Service

@Service
class LootAwardBonusIdCrudServiceImpl(private val repository: LootAwardBonusIdRepository) : LootAwardBonusIdCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<LootAwardBonusIdResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { LootAwardBonusIdResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): LootAwardBonusIdResponse = repository.findById(id)?.let { LootAwardBonusIdResponse.from(it) }
        ?: throw NoSuchElementException("LootAwardBonusId not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateLootAwardBonusIdRequest): LootAwardBonusIdResponse {
        val entity = LootAwardBonusIdEntity(null, request.lootAwardId, request.bonusId)
        return LootAwardBonusIdResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateLootAwardBonusIdRequest): LootAwardBonusIdResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("LootAwardBonusId not found with id: $id")
        val updated = existing.copy(bonusId = request.bonusId ?: existing.bonusId)
        return LootAwardBonusIdResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("LootAwardBonusId not found with id: $id")
        repository.delete(id)
    }

    override fun findByLootAwardId(lootAwardId: Long, pageRequest: PageRequest): PagedResponse<LootAwardBonusIdResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByLootAwardId(lootAwardId, offset, pageRequest.size).map { LootAwardBonusIdResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByLootAwardId(lootAwardId))
    }

    override fun countByLootAwardId(lootAwardId: Long): Long = repository.countByLootAwardId(lootAwardId)
}
