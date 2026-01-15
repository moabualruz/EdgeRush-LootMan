package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderPvpBracketRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Bracket is required")
    val bracket: String,
    val rating: Int? = null,
    val seasonPlayed: Int? = null,
    val weekPlayed: Int? = null,
    val maxRating: Int? = null,
)

data class UpdateRaiderPvpBracketRequest(
    val rating: Int? = null,
    val seasonPlayed: Int? = null,
    val weekPlayed: Int? = null,
    val maxRating: Int? = null,
)

data class RaiderPvpBracketResponse(
    val id: Long,
    val raiderId: Long,
    val bracket: String,
    val rating: Int?,
    val seasonPlayed: Int?,
    val weekPlayed: Int?,
    val maxRating: Int?,
) {
    companion object {
        fun from(e: RaiderPvpBracketEntity) =
            RaiderPvpBracketResponse(
                e.id!!,
                e.raiderId,
                e.bracket,
                e.rating,
                e.seasonPlayed,
                e.weekPlayed,
                e.maxRating,
            )
    }
}

data class RaiderPvpBracketExistsResponse(val exists: Boolean)

data class RaiderPvpBracketCountResponse(val count: Long)
