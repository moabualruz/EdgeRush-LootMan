package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRequest
import com.edgerush.datasync.api.dto.response.RaiderResponse
import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.service.crud.RaiderCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raiders")
@Tag(name = "Raider", description = "Manage raider entities")
class RaiderController(
    service: RaiderCrudService,
) : BaseCrudController<RaiderEntity, Long, CreateRaiderRequest, UpdateRaiderRequest, RaiderResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
