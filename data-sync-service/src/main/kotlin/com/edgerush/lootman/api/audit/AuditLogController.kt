package com.edgerush.lootman.api.audit

import com.edgerush.lootman.domain.audit.model.AuditOperation
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * REST controller for AuditLog read operations.
 *
 * Provides read-only endpoints for querying audit logs.
 * All endpoints require ADMIN role for security.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "AuditLog", description = "Audit log query endpoints (read-only)")
@PreAuthorize("hasRole('ADMIN')")
class AuditLogController(
    private val auditLogService: AuditLogService,
) {
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Find audit logs by entity type and ID")
    fun findByEntity(
        @Parameter(description = "Entity type (e.g., Guild, Raider)")
        @PathVariable entityType: String,
        @Parameter(description = "Entity ID")
        @PathVariable entityId: String,
    ): List<AuditLogResponse> {
        return auditLogService.findByEntity(entityType, entityId)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Find audit logs by user ID")
    fun findByUserId(
        @Parameter(description = "User ID")
        @PathVariable userId: String,
    ): List<AuditLogResponse> {
        return auditLogService.findByUserId(userId)
    }

    @GetMapping("/time-range")
    @Operation(summary = "Find audit logs within a time range")
    fun findByTimeRange(
        @Parameter(description = "Start of time range (ISO-8601 format)")
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
        @Parameter(description = "End of time range (ISO-8601 format)")
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant,
    ): List<AuditLogResponse> {
        return auditLogService.findByTimeRange(from, to)
    }

    @GetMapping("/operation/{operation}")
    @Operation(summary = "Find audit logs by operation type")
    fun findByOperation(
        @Parameter(description = "Operation type (CREATE, READ, UPDATE, DELETE)")
        @PathVariable operation: AuditOperation,
    ): List<AuditLogResponse> {
        return auditLogService.findByOperation(operation)
    }

    @GetMapping("/entity/{entityType}/{entityId}/count")
    @Operation(summary = "Count audit logs for an entity")
    fun countByEntity(
        @Parameter(description = "Entity type")
        @PathVariable entityType: String,
        @Parameter(description = "Entity ID")
        @PathVariable entityId: String,
    ): AuditLogCountResponse {
        return AuditLogCountResponse(count = auditLogService.countByEntity(entityType, entityId))
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Count audit logs for a user")
    fun countByUserId(
        @Parameter(description = "User ID")
        @PathVariable userId: String,
    ): AuditLogCountResponse {
        return AuditLogCountResponse(count = auditLogService.countByUserId(userId))
    }
}
