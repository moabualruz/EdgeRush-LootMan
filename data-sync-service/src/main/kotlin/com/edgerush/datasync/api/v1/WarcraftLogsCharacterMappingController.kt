package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsCharacterMappingRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsCharacterMappingRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsCharacterMappingResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsCharacterMappingEntity
import com.edgerush.datasync.service.crud.WarcraftLogsCharacterMappingCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warcraft-logs-character-mappings")
@Tag(name = "WarcraftLogsCharacterMapping", description = "Manage warcraftlogscharactermapping entities")
class WarcraftLogsCharacterMappingController(
    service: WarcraftLogsCharacterMappingCrudService,
) : BaseCrudController<WarcraftLogsCharacterMappingEntity, Long, CreateWarcraftLogsCharacterMappingRequest, UpdateWarcraftLogsCharacterMappingRequest, WarcraftLogsCharacterMappingResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
