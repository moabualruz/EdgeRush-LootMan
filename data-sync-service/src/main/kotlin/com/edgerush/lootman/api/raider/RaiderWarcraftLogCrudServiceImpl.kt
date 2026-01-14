package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import org.springframework.stereotype.Service

@Service
class RaiderWarcraftLogCrudServiceImpl(private val repository: RaiderWarcraftLogRepository) : RaiderWarcraftLogCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderWarcraftLogResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { RaiderWarcraftLogResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): RaiderWarcraftLogResponse = repository.findById(id)?.let { RaiderWarcraftLogResponse.from(it) }
        ?: throw NoSuchElementException("RaiderWarcraftLog not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderWarcraftLogRequest): RaiderWarcraftLogResponse {
        val entity = RaiderWarcraftLogEntity(null, request.raiderId, request.difficulty, request.score)
        return RaiderWarcraftLogResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateRaiderWarcraftLogRequest): RaiderWarcraftLogResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderWarcraftLog not found with id: $id")
        val updated = existing.copy(score = request.score ?: existing.score)
        return RaiderWarcraftLogResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderWarcraftLog not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderWarcraftLogResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderWarcraftLogResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByRaiderId(raiderId))
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
