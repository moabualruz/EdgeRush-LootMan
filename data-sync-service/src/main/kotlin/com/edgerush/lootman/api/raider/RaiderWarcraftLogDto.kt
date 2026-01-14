package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderWarcraftLogRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Difficulty is required")
    val difficulty: String,
    val score: Int? = null,
)

data class UpdateRaiderWarcraftLogRequest(
    val score: Int? = null,
)

data class RaiderWarcraftLogResponse(
    val id: Long,
    val raiderId: Long,
    val difficulty: String,
    val score: Int?,
) {
    companion object {
        fun from(e: RaiderWarcraftLogEntity) = RaiderWarcraftLogResponse(
            e.id!!, e.raiderId, e.difficulty, e.score
        )
    }
}

data class RaiderWarcraftLogExistsResponse(val exists: Boolean)
data class RaiderWarcraftLogCountResponse(val count: Long)
