package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.loot.repository.LootAwardWishDataRepository
import org.springframework.stereotype.Service

@Service
class LootAwardWishDataCrudServiceImpl(private val repository: LootAwardWishDataRepository) : LootAwardWishDataCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<LootAwardWishDataResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { LootAwardWishDataResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): LootAwardWishDataResponse =
        repository.findById(id)?.let { LootAwardWishDataResponse.from(it) }
            ?: throw NoSuchElementException("LootAwardWishData not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateLootAwardWishDataRequest): LootAwardWishDataResponse {
        val entity = LootAwardWishDataEntity(null, request.lootAwardId, request.specName, request.specIcon, request.value)
        return LootAwardWishDataResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateLootAwardWishDataRequest,
    ): LootAwardWishDataResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("LootAwardWishData not found with id: $id")
        val updated =
            existing.copy(
                specName = request.specName ?: existing.specName,
                specIcon = request.specIcon ?: existing.specIcon,
                value = request.value ?: existing.value,
            )
        return LootAwardWishDataResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("LootAwardWishData not found with id: $id")
        repository.delete(id)
    }

    override fun findByLootAwardId(
        lootAwardId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<LootAwardWishDataResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByLootAwardId(lootAwardId, offset, pageRequest.size).map { LootAwardWishDataResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByLootAwardId(lootAwardId),
        )
    }

    override fun countByLootAwardId(lootAwardId: Long): Long = repository.countByLootAwardId(lootAwardId)
}
