package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.response.RaidbotsResultResponse
import com.edgerush.datasync.service.crud.RaidbotsResultCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.raidbots.RaidbotsResultEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raidbots-results")
@Tag(name = "RaidbotsResult", description = "Manage raidbotsresult entities")
class RaidbotsResultController(
    service: RaidbotsResultCrudService
) : BaseCrudController<RaidbotsResultEntity, Long, CreateRaidbotsResultRequest, UpdateRaidbotsResultRequest, RaidbotsResultResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
