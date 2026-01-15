package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderTrackItemRepository
import org.springframework.stereotype.Service

@Service
class RaiderTrackItemCrudServiceImpl(private val repository: RaiderTrackItemRepository) : RaiderTrackItemCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderTrackItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { RaiderTrackItemResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): RaiderTrackItemResponse =
        repository.findById(id)?.let { RaiderTrackItemResponse.from(it) }
            ?: throw NoSuchElementException("RaiderTrackItem not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderTrackItemRequest): RaiderTrackItemResponse {
        val entity = RaiderTrackItemEntity(null, request.raiderId, request.tier, request.itemCount)
        return RaiderTrackItemResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderTrackItemRequest,
    ): RaiderTrackItemResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderTrackItem not found with id: $id")
        val updated = existing.copy(itemCount = request.itemCount ?: existing.itemCount)
        return RaiderTrackItemResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderTrackItem not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderTrackItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderTrackItemResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRaiderId(raiderId),
        )
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
