package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsDefaultModifierResponse
import com.edgerush.datasync.service.crud.FlpsDefaultModifierCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/flps-default-modifiers")
@Tag(name = "FlpsDefaultModifier", description = "Manage flpsdefaultmodifier entities")
class FlpsDefaultModifierController(
    service: FlpsDefaultModifierCrudService
) : BaseCrudController<FlpsDefaultModifierEntity, Long, CreateFlpsDefaultModifierRequest, UpdateFlpsDefaultModifierRequest, FlpsDefaultModifierResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
