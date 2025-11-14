package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderPvpBracketRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderPvpBracketRequest
import com.edgerush.datasync.api.dto.response.RaiderPvpBracketResponse
import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.datasync.service.crud.RaiderPvpBracketCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-pvp-brackets")
@Tag(name = "RaiderPvpBracket", description = "Manage raiderpvpbracket entities")
class RaiderPvpBracketController(
    service: RaiderPvpBracketCrudService,
) : BaseCrudController<RaiderPvpBracketEntity, Long, CreateRaiderPvpBracketRequest, UpdateRaiderPvpBracketRequest, RaiderPvpBracketResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
