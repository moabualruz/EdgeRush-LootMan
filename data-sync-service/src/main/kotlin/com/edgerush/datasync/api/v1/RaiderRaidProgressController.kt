package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.response.RaiderRaidProgressResponse
import com.edgerush.datasync.service.crud.RaiderRaidProgressCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-raid-progress")
@Tag(name = "RaiderRaidProgress", description = "Manage raiderraidprogress entities")
class RaiderRaidProgressController(
    service: RaiderRaidProgressCrudService
) : BaseCrudController<RaiderRaidProgressEntity, Long, CreateRaiderRaidProgressRequest, UpdateRaiderRaidProgressRequest, RaiderRaidProgressResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
