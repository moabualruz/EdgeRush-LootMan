package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateLootAwardRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardRequest
import com.edgerush.datasync.api.dto.response.LootAwardResponse
import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.datasync.service.crud.LootAwardCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

/**
 * REST controller for LootAward CRUD operations (legacy API).
 *
 * This controller maintains backward compatibility with the legacy CRUD API.
 *
 * Note: For new loot award operations, prefer using the /api/v1/loot endpoints
 * which provide a more domain-focused API with the new domain models.
 */
@RestController
@RequestMapping("/api/v1/loot-awards")
@Tag(name = "LootAward", description = "Manage lootaward entities (legacy CRUD API)")
class LootAwardController(
    service: LootAwardCrudService,
) : BaseCrudController<LootAwardEntity, Long, CreateLootAwardRequest, UpdateLootAwardRequest, LootAwardResponse>(
        service,
    ) {
    // This controller uses the legacy CRUD service for backward compatibility
    // Custom endpoints can be added here if needed
}

