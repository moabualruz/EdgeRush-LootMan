package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsPerformanceResponse
import com.edgerush.datasync.service.crud.WarcraftLogsPerformanceCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsPerformanceEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warcraft-logs-performances")
@Tag(name = "WarcraftLogsPerformance", description = "Manage warcraftlogsperformance entities")
class WarcraftLogsPerformanceController(
    service: WarcraftLogsPerformanceCrudService
) : BaseCrudController<WarcraftLogsPerformanceEntity, Long, CreateWarcraftLogsPerformanceRequest, UpdateWarcraftLogsPerformanceRequest, WarcraftLogsPerformanceResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
