package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsConfigRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsConfigRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsConfigResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsConfigEntity
import com.edgerush.datasync.service.crud.WarcraftLogsConfigCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warcraft-logs-configs")
@Tag(name = "WarcraftLogsConfig", description = "Manage warcraftlogsconfig entities")
class WarcraftLogsConfigController(
    service: WarcraftLogsConfigCrudService,
) : BaseCrudController<WarcraftLogsConfigEntity, String, CreateWarcraftLogsConfigRequest, UpdateWarcraftLogsConfigRequest, WarcraftLogsConfigResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
