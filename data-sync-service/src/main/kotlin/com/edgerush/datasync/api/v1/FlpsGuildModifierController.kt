package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsGuildModifierResponse
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.datasync.service.crud.FlpsGuildModifierCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/flps-guild-modifiers")
@Tag(name = "FlpsGuildModifier", description = "Manage flpsguildmodifier entities")
class FlpsGuildModifierController(
    service: FlpsGuildModifierCrudService,
) : BaseCrudController<FlpsGuildModifierEntity, Long, CreateFlpsGuildModifierRequest, UpdateFlpsGuildModifierRequest, FlpsGuildModifierResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
