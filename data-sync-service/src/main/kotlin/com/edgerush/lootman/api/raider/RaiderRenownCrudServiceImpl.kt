package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderRenownRepository
import org.springframework.stereotype.Service

@Service
class RaiderRenownCrudServiceImpl(private val repository: RaiderRenownRepository) : RaiderRenownCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderRenownResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { RaiderRenownResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): RaiderRenownResponse =
        repository.findById(id)?.let { RaiderRenownResponse.from(it) }
            ?: throw NoSuchElementException("RaiderRenown not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderRenownRequest): RaiderRenownResponse {
        val entity = RaiderRenownEntity(null, request.raiderId, request.faction, request.level)
        return RaiderRenownResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderRenownRequest,
    ): RaiderRenownResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderRenown not found with id: $id")
        val updated = existing.copy(level = request.level ?: existing.level)
        return RaiderRenownResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderRenown not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderRenownResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderRenownResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRaiderId(raiderId),
        )
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
