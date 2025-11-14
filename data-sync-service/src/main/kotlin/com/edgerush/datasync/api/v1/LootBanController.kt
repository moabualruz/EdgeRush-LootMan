package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootBanRequest
import com.edgerush.datasync.api.dto.request.UpdateLootBanRequest
import com.edgerush.datasync.api.dto.response.LootBanResponse
import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.service.crud.LootBanCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/loot-bans")
@Tag(name = "LootBan", description = "Manage lootban entities")
class LootBanController(
    service: LootBanCrudService,
) : BaseCrudController<LootBanEntity, Long, CreateLootBanRequest, UpdateLootBanRequest, LootBanResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
