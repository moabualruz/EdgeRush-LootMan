package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidSignupRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidSignupRequest
import com.edgerush.datasync.api.dto.response.RaidSignupResponse
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.service.crud.RaidSignupCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raid-signups")
@Tag(name = "RaidSignup", description = "Manage raidsignup entities")
class RaidSignupController(
    service: RaidSignupCrudService,
) : BaseCrudController<RaidSignupEntity, Long, CreateRaidSignupRequest, UpdateRaidSignupRequest, RaidSignupResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
