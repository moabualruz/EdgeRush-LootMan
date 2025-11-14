package com.edgerush.datasync.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

@Schema(description = "Request to create a new guest")
data class CreateGuestRequest(
    @field:NotBlank(message = "Name is required")
    @Schema(description = "Guest character name", example = "Guestplayer")
    val name: String,
    
    @Schema(description = "Character realm", example = "Area 52")
    val realm: String?,
    
    @Schema(description = "Character class", example = "Warrior")
    val `class`: String?,
    
    @Schema(description = "Character role", example = "Tank")
    val role: String?,
    
    @field:Positive(message = "Blizzard ID must be positive")
    @Schema(description = "Blizzard character ID", example = "123456789")
    val blizzardId: Long?,
    
    @Schema(description = "When tracking started for this guest", example = "2024-01-15T10:30:00Z")
    val trackingSince: OffsetDateTime?
)

@Schema(description = "Request to update an existing guest")
data class UpdateGuestRequest(
    @Schema(description = "Guest character name", example = "Guestplayer")
    val name: String?,
    
    @Schema(description = "Character realm", example = "Area 52")
    val realm: String?,
    
    @Schema(description = "Character class", example = "Warrior")
    val `class`: String?,
    
    @Schema(description = "Character role", example = "Tank")
    val role: String?,
    
    @field:Positive(message = "Blizzard ID must be positive")
    @Schema(description = "Blizzard character ID", example = "123456789")
    val blizzardId: Long?,
    
    @Schema(description = "When tracking started for this guest", example = "2024-01-15T10:30:00Z")
    val trackingSince: OffsetDateTime?
)
