package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderCrestCountRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Crest type is required")
    val crestType: String,
    val crestCount: Int? = null,
)

data class UpdateRaiderCrestCountRequest(
    val crestCount: Int? = null,
)

data class RaiderCrestCountResponse(
    val id: Long,
    val raiderId: Long,
    val crestType: String,
    val crestCount: Int?,
) {
    companion object {
        fun from(e: RaiderCrestCountEntity) =
            RaiderCrestCountResponse(
                e.id!!,
                e.raiderId,
                e.crestType,
                e.crestCount,
            )
    }
}

data class RaiderCrestCountExistsResponse(val exists: Boolean)

data class RaiderCrestCountCountResponse(val count: Long)
