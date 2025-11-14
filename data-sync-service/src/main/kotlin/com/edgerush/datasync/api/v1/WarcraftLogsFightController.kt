package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsFightResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsFightEntity
import com.edgerush.datasync.service.crud.WarcraftLogsFightCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warcraft-logs-fights")
@Tag(name = "WarcraftLogsFight", description = "Manage warcraftlogsfight entities")
class WarcraftLogsFightController(
    service: WarcraftLogsFightCrudService,
) : BaseCrudController<WarcraftLogsFightEntity, Long, CreateWarcraftLogsFightRequest, UpdateWarcraftLogsFightRequest, WarcraftLogsFightResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
