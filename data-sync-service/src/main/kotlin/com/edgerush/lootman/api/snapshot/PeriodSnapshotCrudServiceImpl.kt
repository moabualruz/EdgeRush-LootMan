package com.edgerush.lootman.api.snapshot

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.snapshot.repository.PeriodSnapshotRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class PeriodSnapshotCrudServiceImpl(private val repository: PeriodSnapshotRepository) : PeriodSnapshotCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<PeriodSnapshotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { PeriodSnapshotResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): PeriodSnapshotResponse =
        repository.findById(id)?.let { PeriodSnapshotResponse.from(it) }
            ?: throw NoSuchElementException("PeriodSnapshot not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreatePeriodSnapshotRequest): PeriodSnapshotResponse {
        val entity =
            PeriodSnapshotEntity(null, request.teamId, request.seasonId, request.periodId, request.currentPeriod, OffsetDateTime.now())
        return PeriodSnapshotResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdatePeriodSnapshotRequest,
    ): PeriodSnapshotResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("PeriodSnapshot not found with id: $id")
        val updated = existing.copy(currentPeriod = request.currentPeriod ?: existing.currentPeriod)
        return PeriodSnapshotResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("PeriodSnapshot not found with id: $id")
        repository.delete(id)
    }

    override fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<PeriodSnapshotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByTeamId(teamId, offset, pageRequest.size).map { PeriodSnapshotResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByTeamId(teamId),
        )
    }

    override fun countByTeamId(teamId: Long): Long = repository.countByTeamId(teamId)
}
