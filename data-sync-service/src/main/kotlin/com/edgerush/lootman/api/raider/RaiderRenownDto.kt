package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderRenownEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderRenownRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Faction is required")
    val faction: String,
    val level: Int? = null,
)

data class UpdateRaiderRenownRequest(
    val level: Int? = null,
)

data class RaiderRenownResponse(
    val id: Long,
    val raiderId: Long,
    val faction: String,
    val level: Int?,
) {
    companion object {
        fun from(e: RaiderRenownEntity) = RaiderRenownResponse(
            e.id!!, e.raiderId, e.faction, e.level
        )
    }
}

data class RaiderRenownExistsResponse(val exists: Boolean)
data class RaiderRenownCountResponse(val count: Long)
