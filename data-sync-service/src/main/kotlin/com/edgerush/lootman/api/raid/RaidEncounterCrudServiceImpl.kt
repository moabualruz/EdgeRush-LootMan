package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raids.repository.RaidEncounterRepository
import org.springframework.stereotype.Service

/**
 * Implementation of RaidEncounterCrudService.
 *
 * Provides CRUD operations for RaidEncounters using the domain repository.
 */
@Service
class RaidEncounterCrudServiceImpl(
    private val encounterRepository: RaidEncounterRepository,
) : RaidEncounterCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaidEncounterResponse> {
        val encounters = encounterRepository.findAll(pageRequest.offset, pageRequest.size)
        val total = encounterRepository.count()
        return PagedResponse.of(
            content = encounters.map { RaidEncounterResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findById(id: Long): RaidEncounterResponse {
        val encounter =
            encounterRepository.findById(id)
                ?: throw NoSuchElementException("Encounter not found with id: $id")
        return RaidEncounterResponse.from(encounter)
    }

    override fun create(request: CreateRaidEncounterRequest): RaidEncounterResponse {
        val entity =
            RaidEncounterEntity(
                id = null,
                raidId = request.raidId,
                encounterId = request.encounterId,
                name = request.name,
                enabled = request.enabled,
                extra = request.extra,
                notes = request.notes,
            )
        val saved = encounterRepository.save(entity)
        return RaidEncounterResponse.from(saved)
    }

    override fun update(
        id: Long,
        request: UpdateRaidEncounterRequest,
    ): RaidEncounterResponse {
        val existing =
            encounterRepository.findById(id)
                ?: throw NoSuchElementException("Encounter not found with id: $id")

        val updated =
            existing.copy(
                encounterId = request.encounterId ?: existing.encounterId,
                name = request.name ?: existing.name,
                enabled = request.enabled ?: existing.enabled,
                extra = request.extra ?: existing.extra,
                notes = request.notes ?: existing.notes,
            )

        val saved = encounterRepository.save(updated)
        return RaidEncounterResponse.from(saved)
    }

    override fun delete(id: Long) {
        if (!encounterRepository.existsById(id)) {
            throw NoSuchElementException("Encounter not found with id: $id")
        }
        encounterRepository.delete(id)
    }

    override fun existsById(id: Long): Boolean = encounterRepository.existsById(id)

    override fun findByRaid(
        raidId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaidEncounterResponse> {
        val encounters = encounterRepository.findByRaidId(raidId, pageRequest.offset, pageRequest.size)
        val total = encounterRepository.countByRaidId(raidId)
        return PagedResponse.of(
            content = encounters.map { RaidEncounterResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findEnabledByRaid(
        raidId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<RaidEncounterResponse> {
        val encounters = encounterRepository.findEnabledByRaidId(raidId, pageRequest.offset, pageRequest.size)
        val total = encounterRepository.countEnabledByRaidId(raidId)
        return PagedResponse.of(
            content = encounters.map { RaidEncounterResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun countByRaid(raidId: Long): Long = encounterRepository.countByRaidId(raidId)
}
