package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateBehavioralActionRequest
import com.edgerush.datasync.api.dto.request.UpdateBehavioralActionRequest
import com.edgerush.datasync.api.dto.response.BehavioralActionResponse
import com.edgerush.datasync.service.crud.BehavioralActionCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.BehavioralActionEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/behavioral-actions")
@Tag(name = "BehavioralAction", description = "Manage behavioralaction entities")
class BehavioralActionController(
    service: BehavioralActionCrudService
) : BaseCrudController<BehavioralActionEntity, Long, CreateBehavioralActionRequest, UpdateBehavioralActionRequest, BehavioralActionResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
