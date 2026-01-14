package com.edgerush.lootman.api.snapshot

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface PeriodSnapshotCrudService : CrudService<Long, CreatePeriodSnapshotRequest, UpdatePeriodSnapshotRequest, PeriodSnapshotResponse> {
    fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<PeriodSnapshotResponse>
    fun countByTeamId(teamId: Long): Long
}
