package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.response.RaiderGearItemResponse
import com.edgerush.datasync.service.crud.RaiderGearItemCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.RaiderGearItemEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-gear-items")
@Tag(name = "RaiderGearItem", description = "Manage raidergearitem entities")
class RaiderGearItemController(
    service: RaiderGearItemCrudService
) : BaseCrudController<RaiderGearItemEntity, Long, CreateRaiderGearItemRequest, UpdateRaiderGearItemRequest, RaiderGearItemResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
