package com.edgerush.lootman.api.sync

import com.edgerush.datasync.entity.SyncRunEntity
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

/**
 * Request DTO for creating a sync run.
 */
data class CreateSyncRunRequest(
    @field:NotBlank(message = "Source is required")
    val source: String,
    @field:NotBlank(message = "Status is required")
    val status: String,
    val message: String? = null,
)

/**
 * Request DTO for updating a sync run.
 */
data class UpdateSyncRunRequest(
    val status: String? = null,
    val completedAt: OffsetDateTime? = null,
    val message: String? = null,
)

/**
 * Response DTO for sync run.
 */
data class SyncRunResponse(
    val id: Long,
    val source: String,
    val status: String,
    val startedAt: OffsetDateTime,
    val completedAt: OffsetDateTime?,
    val message: String?,
) {
    companion object {
        fun from(entity: SyncRunEntity): SyncRunResponse =
            SyncRunResponse(
                id = entity.id!!,
                source = entity.source,
                status = entity.status,
                startedAt = entity.startedAt,
                completedAt = entity.completedAt,
                message = entity.message,
            )
    }
}

/**
 * Response DTO for exists check.
 */
data class SyncRunExistsResponse(val exists: Boolean)

/**
 * Response DTO for count.
 */
data class SyncRunCountResponse(val count: Long)
