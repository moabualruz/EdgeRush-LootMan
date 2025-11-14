package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateAuditLogRequest
import com.edgerush.datasync.api.dto.request.UpdateAuditLogRequest
import com.edgerush.datasync.api.dto.response.AuditLogResponse
import com.edgerush.datasync.service.crud.AuditLogCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.AuditLogEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "AuditLog", description = "Manage auditlog entities")
class AuditLogController(
    service: AuditLogCrudService
) : BaseCrudController<AuditLogEntity, Long, CreateAuditLogRequest, UpdateAuditLogRequest, AuditLogResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
