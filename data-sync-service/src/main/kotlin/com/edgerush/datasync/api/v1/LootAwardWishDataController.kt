package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.response.LootAwardWishDataResponse
import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.datasync.service.crud.LootAwardWishDataCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/loot-award-wish-datas")
@Tag(name = "LootAwardWishData", description = "Manage lootawardwishdata entities")
class LootAwardWishDataController(
    service: LootAwardWishDataCrudService,
) : BaseCrudController<LootAwardWishDataEntity, Long, CreateLootAwardWishDataRequest, UpdateLootAwardWishDataRequest, LootAwardWishDataResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
