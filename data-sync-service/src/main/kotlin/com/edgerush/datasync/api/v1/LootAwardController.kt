package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootAwardRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardRequest
import com.edgerush.datasync.api.dto.response.LootAwardResponse
import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.datasync.service.crud.LootAwardCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/loot-awards")
@Tag(name = "LootAward", description = "Manage lootaward entities")
class LootAwardController(
    service: LootAwardCrudService,
) : BaseCrudController<LootAwardEntity, Long, CreateLootAwardRequest, UpdateLootAwardRequest, LootAwardResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
