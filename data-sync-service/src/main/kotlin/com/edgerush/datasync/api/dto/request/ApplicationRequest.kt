package com.edgerush.datasync.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

@Schema(description = "Request to create a new application")
data class CreateApplicationRequest(
    @field:NotNull(message = "Applied at timestamp is required")
    @Schema(description = "When the application was submitted", example = "2024-01-15T10:30:00Z")
    val appliedAt: OffsetDateTime,
    @field:NotBlank(message = "Status is required")
    @Schema(description = "Application status", example = "pending", allowableValues = ["pending", "approved", "rejected", "withdrawn"])
    val status: String,
    @Schema(description = "Applied role", example = "raider")
    val role: String?,
    @field:Positive(message = "Age must be positive")
    @Schema(description = "Applicant age", example = "25")
    val age: Int?,
    @Schema(description = "Country of residence", example = "United States")
    val country: String?,
    @Schema(description = "Battle.net battletag", example = "Player#1234")
    val battletag: String?,
    @Schema(description = "Discord user ID", example = "123456789012345678")
    val discordId: String?,
    @field:NotBlank(message = "Main character name is required")
    @Schema(description = "Main character name", example = "Shadowblade")
    val mainCharacterName: String,
    @Schema(description = "Main character realm", example = "Area 52")
    val mainCharacterRealm: String?,
    @Schema(description = "Main character class", example = "Rogue")
    val mainCharacterClass: String?,
    @Schema(description = "Main character role", example = "DPS")
    val mainCharacterRole: String?,
    @Schema(description = "Main character race", example = "Night Elf")
    val mainCharacterRace: String?,
    @Schema(description = "Main character faction", example = "Alliance")
    val mainCharacterFaction: String?,
    @field:Positive(message = "Character level must be positive")
    @Schema(description = "Main character level", example = "70")
    val mainCharacterLevel: Int?,
    @Schema(description = "Main character region", example = "US")
    val mainCharacterRegion: String?,
)

@Schema(description = "Request to update an existing application")
data class UpdateApplicationRequest(
    @Schema(description = "When the application was submitted", example = "2024-01-15T10:30:00Z")
    val appliedAt: OffsetDateTime?,
    @Schema(description = "Application status", example = "approved", allowableValues = ["pending", "approved", "rejected", "withdrawn"])
    val status: String?,
    @Schema(description = "Applied role", example = "raider")
    val role: String?,
    @field:Positive(message = "Age must be positive")
    @Schema(description = "Applicant age", example = "25")
    val age: Int?,
    @Schema(description = "Country of residence", example = "United States")
    val country: String?,
    @Schema(description = "Battle.net battletag", example = "Player#1234")
    val battletag: String?,
    @Schema(description = "Discord user ID", example = "123456789012345678")
    val discordId: String?,
    @Schema(description = "Main character name", example = "Shadowblade")
    val mainCharacterName: String?,
    @Schema(description = "Main character realm", example = "Area 52")
    val mainCharacterRealm: String?,
    @Schema(description = "Main character class", example = "Rogue")
    val mainCharacterClass: String?,
    @Schema(description = "Main character role", example = "DPS")
    val mainCharacterRole: String?,
    @Schema(description = "Main character race", example = "Night Elf")
    val mainCharacterRace: String?,
    @Schema(description = "Main character faction", example = "Alliance")
    val mainCharacterFaction: String?,
    @field:Positive(message = "Character level must be positive")
    @Schema(description = "Main character level", example = "70")
    val mainCharacterLevel: Int?,
    @Schema(description = "Main character region", example = "US")
    val mainCharacterRegion: String?,
)
