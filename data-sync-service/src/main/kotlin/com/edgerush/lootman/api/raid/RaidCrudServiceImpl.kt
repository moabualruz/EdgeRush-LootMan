package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raids.repository.RaidRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Implementation of RaidCrudService.
 *
 * Provides CRUD operations for Raids using the domain repository.
 */
@Service
class RaidCrudServiceImpl(
    private val raidRepository: RaidRepository,
) : RaidCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaidResponse> {
        val raids = raidRepository.findAll(pageRequest.offset, pageRequest.size)
        val total = raidRepository.count()
        return PagedResponse.of(
            content = raids.map { RaidResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findById(id: Long): RaidResponse {
        val raid = raidRepository.findById(id)
            ?: throw NoSuchElementException("Raid not found with id: $id")
        return RaidResponse.from(raid)
    }

    override fun create(request: CreateRaidRequest): RaidResponse {
        val now = OffsetDateTime.now()
        val entity = RaidEntity(
            raidId = generateRaidId(),
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            instance = request.instance,
            difficulty = request.difficulty,
            optional = request.optional,
            status = request.status,
            presentSize = null,
            totalSize = request.totalSize,
            notes = request.notes,
            selectionsImage = request.selectionsImage,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            createdAt = now,
            updatedAt = now,
            syncedAt = now,
        )
        val saved = raidRepository.save(entity)
        return RaidResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateRaidRequest): RaidResponse {
        val existing = raidRepository.findById(id)
            ?: throw NoSuchElementException("Raid not found with id: $id")

        val updated = existing.copy(
            date = request.date ?: existing.date,
            startTime = request.startTime ?: existing.startTime,
            endTime = request.endTime ?: existing.endTime,
            instance = request.instance ?: existing.instance,
            difficulty = request.difficulty ?: existing.difficulty,
            optional = request.optional ?: existing.optional,
            status = request.status ?: existing.status,
            presentSize = request.presentSize ?: existing.presentSize,
            totalSize = request.totalSize ?: existing.totalSize,
            notes = request.notes ?: existing.notes,
            selectionsImage = request.selectionsImage ?: existing.selectionsImage,
            teamId = request.teamId ?: existing.teamId,
            seasonId = request.seasonId ?: existing.seasonId,
            periodId = request.periodId ?: existing.periodId,
            updatedAt = OffsetDateTime.now(),
        )

        val saved = raidRepository.save(updated)
        return RaidResponse.from(saved)
    }

    override fun delete(id: Long) {
        if (!raidRepository.existsById(id)) {
            throw NoSuchElementException("Raid not found with id: $id")
        }
        raidRepository.delete(id)
    }

    override fun existsById(id: Long): Boolean =
        raidRepository.existsById(id)

    override fun findByTeam(teamId: Long, pageRequest: PageRequest): PagedResponse<RaidResponse> {
        val raids = raidRepository.findByTeamId(teamId, pageRequest.offset, pageRequest.size)
        val total = raidRepository.countByTeamId(teamId)
        return PagedResponse.of(
            content = raids.map { RaidResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        pageRequest: PageRequest,
    ): PagedResponse<RaidResponse> {
        val raids = raidRepository.findByDateRange(startDate, endDate, pageRequest.offset, pageRequest.size)
        val total = raidRepository.countByDateRange(startDate, endDate)
        return PagedResponse.of(
            content = raids.map { RaidResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun countByTeam(teamId: Long): Long =
        raidRepository.countByTeamId(teamId)

    override fun findUpcomingByGuild(guildId: Long, limit: Int): List<RaidResponse> =
        raidRepository.findUpcomingByGuildId(guildId, limit).map { RaidResponse.from(it) }

    override fun findPastByGuild(guildId: Long, limit: Int): List<RaidResponse> =
        raidRepository.findPastByGuildId(guildId, limit).map { RaidResponse.from(it) }

    private fun generateRaidId(): Long {
        // Generate a unique ID based on current timestamp
        // In production, this might be handled by the database or a sequence
        return System.currentTimeMillis()
    }
}
