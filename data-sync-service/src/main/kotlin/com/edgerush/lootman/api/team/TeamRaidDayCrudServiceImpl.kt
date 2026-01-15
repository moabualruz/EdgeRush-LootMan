package com.edgerush.lootman.api.team

import com.edgerush.datasync.entity.TeamRaidDayEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.team.repository.TeamRaidDayRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of TeamRaidDayCrudService.
 *
 * Provides CRUD operations for team raid days.
 */
@Service
class TeamRaidDayCrudServiceImpl(
    private val repository: TeamRaidDayRepository,
) : TeamRaidDayCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<TeamRaidDayResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { TeamRaidDayResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): TeamRaidDayResponse {
        val entity =
            repository.findById(id)
                ?: throw NoSuchElementException("Team raid day not found with id: $id")
        return TeamRaidDayResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateTeamRaidDayRequest): TeamRaidDayResponse {
        val entity =
            TeamRaidDayEntity(
                teamId = request.teamId,
                weekDay = request.weekDay,
                startTime = request.startTime,
                endTime = request.endTime,
                currentInstance = request.currentInstance,
                difficulty = request.difficulty,
                activeFrom = request.activeFrom,
                syncedAt = OffsetDateTime.now(),
            )
        val saved = repository.save(entity)
        return TeamRaidDayResponse.from(saved)
    }

    override fun update(
        id: Long,
        request: UpdateTeamRaidDayRequest,
    ): TeamRaidDayResponse {
        val existing =
            repository.findById(id)
                ?: throw NoSuchElementException("Team raid day not found with id: $id")

        val updated =
            existing.copy(
                weekDay = request.weekDay ?: existing.weekDay,
                startTime = request.startTime ?: existing.startTime,
                endTime = request.endTime ?: existing.endTime,
                currentInstance = request.currentInstance ?: existing.currentInstance,
                difficulty = request.difficulty ?: existing.difficulty,
                activeFrom = request.activeFrom ?: existing.activeFrom,
            )

        repository.save(updated)
        return TeamRaidDayResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Team raid day not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<TeamRaidDayResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByTeamId(teamId, offset, pageRequest.size)
        val total = repository.countByTeamId(teamId)

        return PagedResponse(
            content = entities.map { TeamRaidDayResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByTeamId(teamId: Long): Long {
        return repository.countByTeamId(teamId)
    }
}
