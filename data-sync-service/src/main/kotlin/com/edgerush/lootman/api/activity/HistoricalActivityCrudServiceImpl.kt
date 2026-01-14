package com.edgerush.lootman.api.activity

import com.edgerush.datasync.entity.HistoricalActivityEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.activity.repository.HistoricalActivityRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class HistoricalActivityCrudServiceImpl(private val repository: HistoricalActivityRepository) : HistoricalActivityCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<HistoricalActivityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { HistoricalActivityResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): HistoricalActivityResponse = repository.findById(id)?.let { HistoricalActivityResponse.from(it) }
        ?: throw NoSuchElementException("HistoricalActivity not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateHistoricalActivityRequest): HistoricalActivityResponse {
        val entity = HistoricalActivityEntity(
            null, request.characterId, request.characterName, request.characterRealm,
            request.periodId, request.teamId, request.seasonId, request.dataJson, OffsetDateTime.now()
        )
        return HistoricalActivityResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateHistoricalActivityRequest): HistoricalActivityResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("HistoricalActivity not found with id: $id")
        val updated = existing.copy(dataJson = request.dataJson ?: existing.dataJson)
        return HistoricalActivityResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("HistoricalActivity not found with id: $id")
        repository.delete(id)
    }

    override fun findByCharacterId(characterId: Long, pageRequest: PageRequest): PagedResponse<HistoricalActivityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByCharacterId(characterId, offset, pageRequest.size).map { HistoricalActivityResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByCharacterId(characterId))
    }

    override fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<HistoricalActivityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByTeamId(teamId, offset, pageRequest.size).map { HistoricalActivityResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByTeamId(teamId))
    }

    override fun countByCharacterId(characterId: Long): Long = repository.countByCharacterId(characterId)

    override fun countByTeamId(teamId: Long): Long = repository.countByTeamId(teamId)
}
