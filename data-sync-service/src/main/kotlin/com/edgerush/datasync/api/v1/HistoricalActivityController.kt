package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.request.UpdateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.response.HistoricalActivityResponse
import com.edgerush.datasync.entity.HistoricalActivityEntity
import com.edgerush.datasync.service.crud.HistoricalActivityCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/historical-activitys")
@Tag(name = "HistoricalActivity", description = "Manage historicalactivity entities")
class HistoricalActivityController(
    service: HistoricalActivityCrudService,
) : BaseCrudController<HistoricalActivityEntity, Long, CreateHistoricalActivityRequest, UpdateHistoricalActivityRequest, HistoricalActivityResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
