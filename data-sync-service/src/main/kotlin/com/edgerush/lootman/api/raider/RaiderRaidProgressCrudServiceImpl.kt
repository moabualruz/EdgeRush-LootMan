package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderRaidProgressRepository
import org.springframework.stereotype.Service

@Service
class RaiderRaidProgressCrudServiceImpl(private val repository: RaiderRaidProgressRepository) : RaiderRaidProgressCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderRaidProgressResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { RaiderRaidProgressResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): RaiderRaidProgressResponse =
        repository.findById(id)?.let { RaiderRaidProgressResponse.from(it) }
            ?: throw NoSuchElementException("RaiderRaidProgress not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderRaidProgressRequest): RaiderRaidProgressResponse {
        val entity = RaiderRaidProgressEntity(null, request.raiderId, request.raid, request.difficulty, request.bossesDefeated)
        return RaiderRaidProgressResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderRaidProgressRequest,
    ): RaiderRaidProgressResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderRaidProgress not found with id: $id")
        val updated = existing.copy(bossesDefeated = request.bossesDefeated ?: existing.bossesDefeated)
        return RaiderRaidProgressResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderRaidProgress not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderRaidProgressResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderRaidProgressResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRaiderId(raiderId),
        )
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
