package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.loot.repository.LootAwardEntityRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of LootAwardCrudService.
 *
 * Provides CRUD operations for loot awards.
 */
@Service
class LootAwardCrudServiceImpl(
    private val repository: LootAwardEntityRepository,
) : LootAwardCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<LootAwardEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { LootAwardEntityResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): LootAwardEntityResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Loot award not found with id: $id")
        return LootAwardEntityResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateLootAwardEntityRequest): LootAwardEntityResponse {
        val entity = LootAwardEntity(
            raiderId = request.raiderId,
            itemId = request.itemId,
            itemName = request.itemName,
            tier = request.tier,
            flps = request.flps,
            rdf = request.rdf,
            awardedAt = OffsetDateTime.now(),
            rclootcouncilId = request.rclootcouncilId,
            icon = request.icon,
            slot = request.slot,
            quality = request.quality,
            responseTypeId = request.responseTypeId,
            responseTypeName = request.responseTypeName,
            responseTypeRgba = null,
            responseTypeExcluded = null,
            propagatedResponseTypeId = null,
            propagatedResponseTypeName = null,
            propagatedResponseTypeRgba = null,
            propagatedResponseTypeExcluded = null,
            sameResponseAmount = null,
            note = request.note,
            wishValue = request.wishValue,
            difficulty = request.difficulty,
            discarded = request.discarded,
            characterId = request.characterId,
            awardedByCharacterId = request.awardedByCharacterId,
            awardedByName = request.awardedByName,
        )
        val saved = repository.save(entity)
        return LootAwardEntityResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateLootAwardEntityRequest): LootAwardEntityResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Loot award not found with id: $id")

        val updated = existing.copy(
            note = request.note ?: existing.note,
            discarded = request.discarded ?: existing.discarded,
            wishValue = request.wishValue ?: existing.wishValue,
        )

        repository.save(updated)
        return LootAwardEntityResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Loot award not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByRaider(raiderId: Long, pageRequest: PageRequest): PagedResponse<LootAwardEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByRaiderId(raiderId, offset, pageRequest.size)
        val total = repository.countByRaiderId(raiderId)

        return PagedResponse(
            content = entities.map { LootAwardEntityResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByItem(itemId: Long, pageRequest: PageRequest): PagedResponse<LootAwardEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByItemId(itemId, offset, pageRequest.size)
        val total = repository.countByItemId(itemId)

        return PagedResponse(
            content = entities.map { LootAwardEntityResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByTier(tier: String, pageRequest: PageRequest): PagedResponse<LootAwardEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByTier(tier, offset, pageRequest.size)
        val total = repository.countByTier(tier)

        return PagedResponse(
            content = entities.map { LootAwardEntityResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByRaider(raiderId: Long): Long {
        return repository.countByRaiderId(raiderId)
    }
}
