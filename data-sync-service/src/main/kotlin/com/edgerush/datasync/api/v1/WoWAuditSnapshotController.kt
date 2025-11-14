package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.response.WoWAuditSnapshotResponse
import com.edgerush.datasync.service.crud.WoWAuditSnapshotCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.WoWAuditSnapshotEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/wo-waudit-snapshots")
@Tag(name = "WoWAuditSnapshot", description = "Manage wowauditsnapshot entities")
class WoWAuditSnapshotController(
    service: WoWAuditSnapshotCrudService
) : BaseCrudController<WoWAuditSnapshotEntity, Long, CreateWoWAuditSnapshotRequest, UpdateWoWAuditSnapshotRequest, WoWAuditSnapshotResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
