package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderPvpBracketRepository
import org.springframework.stereotype.Service

@Service
class RaiderPvpBracketCrudServiceImpl(private val repository: RaiderPvpBracketRepository) : RaiderPvpBracketCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderPvpBracketResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { RaiderPvpBracketResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): RaiderPvpBracketResponse = repository.findById(id)?.let { RaiderPvpBracketResponse.from(it) }
        ?: throw NoSuchElementException("RaiderPvpBracket not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderPvpBracketRequest): RaiderPvpBracketResponse {
        val entity = RaiderPvpBracketEntity(null, request.raiderId, request.bracket, request.rating, request.seasonPlayed, request.weekPlayed, request.maxRating)
        return RaiderPvpBracketResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateRaiderPvpBracketRequest): RaiderPvpBracketResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderPvpBracket not found with id: $id")
        val updated = existing.copy(
            rating = request.rating ?: existing.rating,
            seasonPlayed = request.seasonPlayed ?: existing.seasonPlayed,
            weekPlayed = request.weekPlayed ?: existing.weekPlayed,
            maxRating = request.maxRating ?: existing.maxRating
        )
        return RaiderPvpBracketResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderPvpBracket not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderPvpBracketResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderPvpBracketResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByRaiderId(raiderId))
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)
}
