package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateSyncRunRequest
import com.edgerush.datasync.api.dto.request.UpdateSyncRunRequest
import com.edgerush.datasync.api.dto.response.SyncRunResponse
import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.datasync.service.crud.SyncRunCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sync-runs")
@Tag(name = "SyncRun", description = "Manage syncrun entities")
class SyncRunController(
    service: SyncRunCrudService,
) : BaseCrudController<SyncRunEntity, Long, CreateSyncRunRequest, UpdateSyncRunRequest, SyncRunResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
