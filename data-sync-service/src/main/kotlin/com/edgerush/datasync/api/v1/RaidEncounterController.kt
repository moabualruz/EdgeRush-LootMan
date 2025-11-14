package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidEncounterRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidEncounterRequest
import com.edgerush.datasync.api.dto.response.RaidEncounterResponse
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.service.crud.RaidEncounterCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raid-encounters")
@Tag(name = "RaidEncounter", description = "Manage raidencounter entities")
class RaidEncounterController(
    service: RaidEncounterCrudService,
) : BaseCrudController<RaidEncounterEntity, Long, CreateRaidEncounterRequest, UpdateRaidEncounterRequest, RaidEncounterResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
