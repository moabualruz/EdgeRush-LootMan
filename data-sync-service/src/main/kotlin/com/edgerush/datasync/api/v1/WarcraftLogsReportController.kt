package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsReportResponse
import com.edgerush.datasync.service.crud.WarcraftLogsReportCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsReportEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/warcraft-logs-reports")
@Tag(name = "WarcraftLogsReport", description = "Manage warcraftlogsreport entities")
class WarcraftLogsReportController(
    service: WarcraftLogsReportCrudService
) : BaseCrudController<WarcraftLogsReportEntity, Long, CreateWarcraftLogsReportRequest, UpdateWarcraftLogsReportRequest, WarcraftLogsReportResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
