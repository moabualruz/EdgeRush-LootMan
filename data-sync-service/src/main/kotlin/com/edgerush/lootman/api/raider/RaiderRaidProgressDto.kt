package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderRaidProgressRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Raid is required")
    val raid: String,
    @field:NotBlank(message = "Difficulty is required")
    val difficulty: String,
    val bossesDefeated: Int? = null,
)

data class UpdateRaiderRaidProgressRequest(
    val bossesDefeated: Int? = null,
)

data class RaiderRaidProgressResponse(
    val id: Long,
    val raiderId: Long,
    val raid: String,
    val difficulty: String,
    val bossesDefeated: Int?,
) {
    companion object {
        fun from(e: RaiderRaidProgressEntity) =
            RaiderRaidProgressResponse(
                e.id!!,
                e.raiderId,
                e.raid,
                e.difficulty,
                e.bossesDefeated,
            )
    }
}

data class RaiderRaidProgressExistsResponse(val exists: Boolean)

data class RaiderRaidProgressCountResponse(val count: Long)
