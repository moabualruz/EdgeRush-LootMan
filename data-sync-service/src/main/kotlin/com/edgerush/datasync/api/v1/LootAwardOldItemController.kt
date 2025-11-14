package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.response.LootAwardOldItemResponse
import com.edgerush.datasync.service.crud.LootAwardOldItemCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.LootAwardOldItemEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/loot-award-old-items")
@Tag(name = "LootAwardOldItem", description = "Manage lootawardolditem entities")
class LootAwardOldItemController(
    service: LootAwardOldItemCrudService
) : BaseCrudController<LootAwardOldItemEntity, Long, CreateLootAwardOldItemRequest, UpdateLootAwardOldItemRequest, LootAwardOldItemResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
