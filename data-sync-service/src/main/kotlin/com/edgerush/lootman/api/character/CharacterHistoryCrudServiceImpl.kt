package com.edgerush.lootman.api.character

import com.edgerush.datasync.entity.CharacterHistoryEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.character.repository.CharacterHistoryRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of CharacterHistoryCrudService.
 *
 * Provides CRUD operations for character history.
 */
@Service
class CharacterHistoryCrudServiceImpl(
    private val repository: CharacterHistoryRepository,
) : CharacterHistoryCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<CharacterHistoryResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { CharacterHistoryResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): CharacterHistoryResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Character history not found with id: $id")
        return CharacterHistoryResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateCharacterHistoryRequest): CharacterHistoryResponse {
        val entity = CharacterHistoryEntity(
            characterId = request.characterId,
            characterName = request.characterName,
            characterRealm = request.characterRealm,
            characterRegion = request.characterRegion,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            historyJson = request.historyJson,
            bestGearJson = request.bestGearJson,
            syncedAt = OffsetDateTime.now(),
        )
        val saved = repository.save(entity)
        return CharacterHistoryResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateCharacterHistoryRequest): CharacterHistoryResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Character history not found with id: $id")

        val updated = existing.copy(
            historyJson = request.historyJson ?: existing.historyJson,
            bestGearJson = request.bestGearJson ?: existing.bestGearJson,
        )

        repository.save(updated)
        return CharacterHistoryResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Character history not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByCharacterId(characterId: Long, pageRequest: PageRequest): PagedResponse<CharacterHistoryResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByCharacterId(characterId, offset, pageRequest.size)
        val total = repository.countByCharacterId(characterId)

        return PagedResponse(
            content = entities.map { CharacterHistoryResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<CharacterHistoryResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByTeamId(teamId, offset, pageRequest.size)
        val total = repository.countByTeamId(teamId)

        return PagedResponse(
            content = entities.map { CharacterHistoryResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByCharacterId(characterId: Long): Long {
        return repository.countByCharacterId(characterId)
    }
}
