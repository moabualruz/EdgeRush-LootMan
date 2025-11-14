package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderVaultSlotRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderVaultSlotRequest
import com.edgerush.datasync.api.dto.response.RaiderVaultSlotResponse
import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import com.edgerush.datasync.service.crud.RaiderVaultSlotCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-vault-slots")
@Tag(name = "RaiderVaultSlot", description = "Manage raidervaultslot entities")
class RaiderVaultSlotController(
    service: RaiderVaultSlotCrudService,
) : BaseCrudController<RaiderVaultSlotEntity, Long, CreateRaiderVaultSlotRequest, UpdateRaiderVaultSlotRequest, RaiderVaultSlotResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
