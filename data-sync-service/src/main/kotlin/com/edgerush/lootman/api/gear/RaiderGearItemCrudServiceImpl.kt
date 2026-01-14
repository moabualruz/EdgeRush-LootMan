package com.edgerush.lootman.api.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.gear.repository.RaiderGearItemRepository
import org.springframework.stereotype.Service

@Service
class RaiderGearItemCrudServiceImpl(private val repository: RaiderGearItemRepository) : RaiderGearItemCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderGearItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { RaiderGearItemResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): RaiderGearItemResponse = repository.findById(id)?.let { RaiderGearItemResponse.from(it) }
        ?: throw NoSuchElementException("RaiderGearItem not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderGearItemRequest): RaiderGearItemResponse {
        val entity = RaiderGearItemEntity(
            null, request.raiderId, request.gearSet, request.slot, request.itemId, request.itemLevel,
            request.quality, request.enchant, request.enchantQuality, request.upgradeLevel, request.sockets, request.name
        )
        return RaiderGearItemResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateRaiderGearItemRequest): RaiderGearItemResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderGearItem not found with id: $id")
        val updated = existing.copy(
            itemId = request.itemId ?: existing.itemId,
            itemLevel = request.itemLevel ?: existing.itemLevel,
            quality = request.quality ?: existing.quality,
            enchant = request.enchant ?: existing.enchant,
            enchantQuality = request.enchantQuality ?: existing.enchantQuality,
            upgradeLevel = request.upgradeLevel ?: existing.upgradeLevel,
            sockets = request.sockets ?: existing.sockets,
            name = request.name ?: existing.name
        )
        return RaiderGearItemResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderGearItem not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(raiderId: Long, pageRequest: PageRequest): PagedResponse<RaiderGearItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByRaiderId(raiderId, offset, pageRequest.size).map { RaiderGearItemResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByRaiderId(raiderId))
    }

    override fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String, pageRequest: PageRequest): PagedResponse<RaiderGearItemResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findByRaiderIdAndGearSet(raiderId, gearSet, offset, pageRequest.size).map { RaiderGearItemResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.countByRaiderIdAndGearSet(raiderId, gearSet))
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)

    override fun countByRaiderIdAndGearSet(raiderId: Long, gearSet: String): Long = repository.countByRaiderIdAndGearSet(raiderId, gearSet)
}
