package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.attendance.repository.AttendanceStatRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of AttendanceStatCrudService.
 *
 * Provides CRUD operations for attendance stats.
 */
@Service
class AttendanceStatCrudServiceImpl(
    private val repository: AttendanceStatRepository,
) : AttendanceStatCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<AttendanceStatResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { AttendanceStatResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): AttendanceStatResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Attendance stat not found with id: $id")
        return AttendanceStatResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateAttendanceStatRequest): AttendanceStatResponse {
        val entity = AttendanceStatEntity(
            instance = request.instance,
            encounter = request.encounter,
            startDate = request.startDate,
            endDate = request.endDate,
            characterId = request.characterId,
            characterName = request.characterName,
            characterRealm = request.characterRealm,
            characterClass = request.characterClass,
            characterRole = request.characterRole,
            characterRegion = request.characterRegion,
            attendedAmountOfRaids = request.attendedAmountOfRaids,
            totalAmountOfRaids = request.totalAmountOfRaids,
            attendedPercentage = request.attendedPercentage,
            selectedAmountOfEncounters = request.selectedAmountOfEncounters,
            totalAmountOfEncounters = request.totalAmountOfEncounters,
            selectedPercentage = request.selectedPercentage,
            teamId = request.teamId,
            seasonId = request.seasonId,
            periodId = request.periodId,
            syncedAt = OffsetDateTime.now(),
        )
        val saved = repository.save(entity)
        return AttendanceStatResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateAttendanceStatRequest): AttendanceStatResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Attendance stat not found with id: $id")

        val updated = existing.copy(
            instance = request.instance ?: existing.instance,
            encounter = request.encounter ?: existing.encounter,
            startDate = request.startDate ?: existing.startDate,
            endDate = request.endDate ?: existing.endDate,
            attendedAmountOfRaids = request.attendedAmountOfRaids ?: existing.attendedAmountOfRaids,
            totalAmountOfRaids = request.totalAmountOfRaids ?: existing.totalAmountOfRaids,
            attendedPercentage = request.attendedPercentage ?: existing.attendedPercentage,
            selectedAmountOfEncounters = request.selectedAmountOfEncounters ?: existing.selectedAmountOfEncounters,
            totalAmountOfEncounters = request.totalAmountOfEncounters ?: existing.totalAmountOfEncounters,
            selectedPercentage = request.selectedPercentage ?: existing.selectedPercentage,
        )

        repository.save(updated)
        return AttendanceStatResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Attendance stat not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findByCharacterId(characterId: Long, pageRequest: PageRequest): PagedResponse<AttendanceStatResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByCharacterId(characterId, offset, pageRequest.size)
        val total = repository.countByCharacterId(characterId)

        return PagedResponse(
            content = entities.map { AttendanceStatResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<AttendanceStatResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByTeamId(teamId, offset, pageRequest.size)
        val total = repository.countByTeamId(teamId)

        return PagedResponse(
            content = entities.map { AttendanceStatResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findBySeasonId(seasonId: Long, pageRequest: PageRequest): PagedResponse<AttendanceStatResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findBySeasonId(seasonId, offset, pageRequest.size)
        val total = repository.countBySeasonId(seasonId)

        return PagedResponse(
            content = entities.map { AttendanceStatResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByCharacterId(characterId: Long): Long {
        return repository.countByCharacterId(characterId)
    }
}
