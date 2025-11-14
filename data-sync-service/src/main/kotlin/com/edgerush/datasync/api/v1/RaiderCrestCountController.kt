package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.response.RaiderCrestCountResponse
import com.edgerush.datasync.service.crud.RaiderCrestCountCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.RaiderCrestCountEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-crest-counts")
@Tag(name = "RaiderCrestCount", description = "Manage raidercrestcount entities")
class RaiderCrestCountController(
    service: RaiderCrestCountCrudService
) : BaseCrudController<RaiderCrestCountEntity, Long, CreateRaiderCrestCountRequest, UpdateRaiderCrestCountRequest, RaiderCrestCountResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
