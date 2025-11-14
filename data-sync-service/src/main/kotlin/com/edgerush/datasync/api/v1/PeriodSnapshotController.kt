package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.response.PeriodSnapshotResponse
import com.edgerush.datasync.service.crud.PeriodSnapshotCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.PeriodSnapshotEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/period-snapshots")
@Tag(name = "PeriodSnapshot", description = "Manage periodsnapshot entities")
class PeriodSnapshotController(
    service: PeriodSnapshotCrudService
) : BaseCrudController<PeriodSnapshotEntity, Long, CreatePeriodSnapshotRequest, UpdatePeriodSnapshotRequest, PeriodSnapshotResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
