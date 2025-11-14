package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderRenownRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRenownRequest
import com.edgerush.datasync.api.dto.response.RaiderRenownResponse
import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.datasync.service.crud.RaiderRenownCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-renowns")
@Tag(name = "RaiderRenown", description = "Manage raiderrenown entities")
class RaiderRenownController(
    service: RaiderRenownCrudService,
) : BaseCrudController<RaiderRenownEntity, Long, CreateRaiderRenownRequest, UpdateRaiderRenownRequest, RaiderRenownResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
