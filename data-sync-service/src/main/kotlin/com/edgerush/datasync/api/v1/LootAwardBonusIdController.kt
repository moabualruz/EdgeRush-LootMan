package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.response.LootAwardBonusIdResponse
import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import com.edgerush.datasync.service.crud.LootAwardBonusIdCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/loot-award-bonus-ids")
@Tag(name = "LootAwardBonusId", description = "Manage lootawardbonusid entities")
class LootAwardBonusIdController(
    service: LootAwardBonusIdCrudService,
) : BaseCrudController<LootAwardBonusIdEntity, Long, CreateLootAwardBonusIdRequest, UpdateLootAwardBonusIdRequest, LootAwardBonusIdResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
