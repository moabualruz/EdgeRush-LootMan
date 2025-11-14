package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.response.RaiderStatisticsResponse
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.datasync.service.crud.RaiderStatisticsCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/raider-statistics")
@Tag(name = "RaiderStatistics", description = "Manage raiderstatistics entities")
class RaiderStatisticsController(
    service: RaiderStatisticsCrudService,
) : BaseCrudController<RaiderStatisticsEntity, Long, CreateRaiderStatisticsRequest, UpdateRaiderStatisticsRequest, RaiderStatisticsResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
