package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.response.RaidbotsConfigResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsConfigEntity
import com.edgerush.datasync.service.crud.RaidbotsConfigCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raidbots-configs")
@Tag(name = "RaidbotsConfig", description = "Manage raidbotsconfig entities")
class RaidbotsConfigController(
    service: RaidbotsConfigCrudService,
) : BaseCrudController<RaidbotsConfigEntity, String, CreateRaidbotsConfigRequest, UpdateRaidbotsConfigRequest, RaidbotsConfigResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
