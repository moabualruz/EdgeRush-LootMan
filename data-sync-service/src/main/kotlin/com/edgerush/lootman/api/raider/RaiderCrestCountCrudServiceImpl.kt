package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import org.springframework.stereotype.Service

@Service
class RaiderCrestCountCrudServiceImpl(private val repository: RaiderCrestCountRepository) : RaiderCrestCountCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderCrestCountResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { RaiderCrestCountResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): RaiderCrestCountResponse =
        repository.findById(id)?.let { RaiderCrestCountResponse.from(it) }
            ?: throw NoSuchElementException("RaiderCrestCount not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderCrestCountRequest): RaiderCrestCountResponse {
        val entity = RaiderCrestCountEntity(null, request.raiderId, request.crestType, request.crestCount)
        return RaiderCrestCountResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderCrestCountRequest,
    ): RaiderCrestCountResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderCrestCount not found with id: $id")
        val updated = existing.copy(crestCount = request.crestCount ?: existing.crestCount)
        return RaiderCrestCountResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderCrestCount not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderCrestCountResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderCrestCountResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRaiderId(raiderId),
        )
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
