package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.response.RaiderTrackItemResponse
import com.edgerush.datasync.service.crud.RaiderTrackItemCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.RaiderTrackItemEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-track-items")
@Tag(name = "RaiderTrackItem", description = "Manage raidertrackitem entities")
class RaiderTrackItemController(
    service: RaiderTrackItemCrudService
) : BaseCrudController<RaiderTrackItemEntity, Long, CreateRaiderTrackItemRequest, UpdateRaiderTrackItemRequest, RaiderTrackItemResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
