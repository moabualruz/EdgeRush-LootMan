package com.edgerush.datasync.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Guest details")
data class GuestResponse(
    @Schema(description = "Guest ID", example = "1")
    val guestId: Long,
    
    @Schema(description = "Guest character name", example = "Guestplayer")
    val name: String,
    
    @Schema(description = "Character realm", example = "Area 52")
    val realm: String?,
    
    @Schema(description = "Character class", example = "Warrior")
    val `class`: String?,
    
    @Schema(description = "Character role", example = "Tank")
    val role: String?,
    
    @Schema(description = "Blizzard character ID", example = "123456789")
    val blizzardId: Long?,
    
    @Schema(description = "When tracking started for this guest", example = "2024-01-15T10:30:00Z")
    val trackingSince: OffsetDateTime?,
    
    @Schema(description = "When this record was last synced", example = "2024-01-15T10:30:00Z")
    val syncedAt: OffsetDateTime
)
