package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateTeamRaidDayRequest
import com.edgerush.datasync.api.dto.request.UpdateTeamRaidDayRequest
import com.edgerush.datasync.api.dto.response.TeamRaidDayResponse
import com.edgerush.datasync.entity.TeamRaidDayEntity
import com.edgerush.datasync.service.crud.TeamRaidDayCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/team-raid-days")
@Tag(name = "TeamRaidDay", description = "Manage teamraidday entities")
class TeamRaidDayController(
    service: TeamRaidDayCrudService,
) : BaseCrudController<TeamRaidDayEntity, Long, CreateTeamRaidDayRequest, UpdateTeamRaidDayRequest, TeamRaidDayResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
