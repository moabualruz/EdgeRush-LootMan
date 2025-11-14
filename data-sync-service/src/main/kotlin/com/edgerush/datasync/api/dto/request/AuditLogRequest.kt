package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateAuditLogRequest(
    val timestamp: LocalDateTime? = null,
    @field:NotBlank(message = "Operation is required")
    @field:Size(max = 255, message = "Operation must not exceed 255 characters")
    val operation: String? = null,
    @field:NotBlank(message = "Entity type is required")
    @field:Size(max = 255, message = "Entity type must not exceed 255 characters")
    val entityType: String? = null,
    @field:NotBlank(message = "Entity id is required")
    @field:Size(max = 255, message = "Entity id must not exceed 255 characters")
    val entityId: String? = null,
    @field:NotBlank(message = "User id is required")
    @field:Size(max = 255, message = "User id must not exceed 255 characters")
    val userId: String? = null,
    @field:NotBlank(message = "Username is required")
    @field:Size(max = 255, message = "Username must not exceed 255 characters")
    val username: String? = null,
    val isAdminMode: Boolean? = null,
    val requestId: String? = null,
)

data class UpdateAuditLogRequest(
    val timestamp: LocalDateTime? = null,
    @field:NotBlank(message = "Operation is required")
    @field:Size(max = 255, message = "Operation must not exceed 255 characters")
    val operation: String? = null,
    @field:NotBlank(message = "Entity type is required")
    @field:Size(max = 255, message = "Entity type must not exceed 255 characters")
    val entityType: String? = null,
    @field:NotBlank(message = "Entity id is required")
    @field:Size(max = 255, message = "Entity id must not exceed 255 characters")
    val entityId: String? = null,
    @field:NotBlank(message = "User id is required")
    @field:Size(max = 255, message = "User id must not exceed 255 characters")
    val userId: String? = null,
    @field:NotBlank(message = "Username is required")
    @field:Size(max = 255, message = "Username must not exceed 255 characters")
    val username: String? = null,
    val isAdminMode: Boolean? = null,
    val requestId: String? = null,
)
