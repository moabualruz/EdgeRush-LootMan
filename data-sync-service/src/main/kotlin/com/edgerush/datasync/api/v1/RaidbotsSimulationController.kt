package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.response.RaidbotsSimulationResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsSimulationEntity
import com.edgerush.datasync.service.crud.RaidbotsSimulationCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raidbots-simulations")
@Tag(name = "RaidbotsSimulation", description = "Manage raidbotssimulation entities")
class RaidbotsSimulationController(
    service: RaidbotsSimulationCrudService,
) : BaseCrudController<RaidbotsSimulationEntity, Long, CreateRaidbotsSimulationRequest, UpdateRaidbotsSimulationRequest, RaidbotsSimulationResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
