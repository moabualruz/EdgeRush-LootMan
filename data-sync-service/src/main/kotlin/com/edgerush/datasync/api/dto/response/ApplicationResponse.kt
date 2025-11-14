package com.edgerush.datasync.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Application details")
data class ApplicationResponse(
    @Schema(description = "Application ID", example = "1")
    val applicationId: Long,
    @Schema(description = "When the application was submitted", example = "2024-01-15T10:30:00Z")
    val appliedAt: OffsetDateTime?,
    @Schema(description = "Application status", example = "pending")
    val status: String?,
    @Schema(description = "Applied role", example = "raider")
    val role: String?,
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
    @Schema(description = "Main character level", example = "70")
    val mainCharacterLevel: Int?,
    @Schema(description = "Main character region", example = "US")
    val mainCharacterRegion: String?,
    @Schema(description = "When this record was last synced", example = "2024-01-15T10:30:00Z")
    val syncedAt: OffsetDateTime,
)
