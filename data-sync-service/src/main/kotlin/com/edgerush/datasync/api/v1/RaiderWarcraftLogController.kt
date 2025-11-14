package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.response.RaiderWarcraftLogResponse
import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.datasync.service.crud.RaiderWarcraftLogCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-warcraft-logs")
@Tag(name = "RaiderWarcraftLog", description = "Manage raiderwarcraftlog entities")
class RaiderWarcraftLogController(
    service: RaiderWarcraftLogCrudService,
) : BaseCrudController<RaiderWarcraftLogEntity, Long, CreateRaiderWarcraftLogRequest, UpdateRaiderWarcraftLogRequest, RaiderWarcraftLogResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
