package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PeriodSnapshotRepository : CrudRepository<PeriodSnapshotEntity, Long> {
    fun findByTeamIdAndPeriodId(teamId: Long, periodId: Long): PeriodSnapshotEntity?
}
