package com.edgerush.lootman.api.guest

import com.edgerush.datasync.entity.GuestEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class CreateGuestRequest(
    @field:NotNull val guestId: Long,
    @field:NotBlank val name: String,
    val realm: String? = null,
    val clazz: String? = null,
    val role: String? = null,
    val blizzardId: Long? = null,
    val trackingSince: OffsetDateTime? = null,
)

data class UpdateGuestRequest(
    val name: String? = null,
    val realm: String? = null,
    val clazz: String? = null,
    val role: String? = null,
)

data class GuestResponse(
    val guestId: Long,
    val name: String,
    val realm: String?,
    val clazz: String?,
    val role: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val syncedAt: OffsetDateTime,
) {
    companion object {
        fun from(e: GuestEntity) = GuestResponse(e.guestId, e.name, e.realm, e.clazz, e.role, e.blizzardId, e.trackingSince, e.syncedAt)
    }
}

data class GuestExistsResponse(val exists: Boolean)

data class GuestCountResponse(val count: Long)
