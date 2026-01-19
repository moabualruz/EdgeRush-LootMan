package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderEntity
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateRaiderEntityRequest(
    @field:NotBlank(message = "Character name is required")
    val characterName: String,
    @field:NotBlank(message = "Realm is required")
    val realm: String,
    @field:NotBlank(message = "Region is required")
    val region: String,
    val guildId: String? = null,
    val wowauditId: Long? = null,
    @field:NotBlank(message = "Class is required")
    val clazz: String,
    @field:NotBlank(message = "Spec is required")
    val spec: String,
    @field:NotBlank(message = "Role is required")
    val role: String,
    val rank: String? = null,
    val status: String? = null,
    val note: String? = null,
    val blizzardId: Long? = null,
    val trackingSince: OffsetDateTime? = null,
    val joinDate: OffsetDateTime? = null,
)

data class UpdateRaiderEntityRequest(
    val guildId: String? = null,
    val spec: String? = null,
    val role: String? = null,
    val rank: String? = null,
    val status: String? = null,
    val note: String? = null,
)

data class RaiderEntityResponse(
    val id: Long,
    val characterName: String,
    val realm: String,
    val region: String,
    val guildId: String?,
    val wowauditId: Long?,
    val clazz: String,
    val spec: String,
    val role: String,
    val rank: String?,
    val status: String?,
    val note: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val joinDate: OffsetDateTime?,
    val blizzardLastModified: OffsetDateTime?,
    val lastSync: OffsetDateTime,
) {
    companion object {
        fun from(entity: RaiderEntity) =
            RaiderEntityResponse(
                id = entity.id!!,
                characterName = entity.characterName,
                realm = entity.realm,
                region = entity.region,
                guildId = entity.guildId,
                wowauditId = entity.wowauditId,
                clazz = entity.clazz,
                spec = entity.spec,
                role = entity.role,
                rank = entity.rank,
                status = entity.status,
                note = entity.note,
                blizzardId = entity.blizzardId,
                trackingSince = entity.trackingSince,
                joinDate = entity.joinDate,
                blizzardLastModified = entity.blizzardLastModified,
                lastSync = entity.lastSync,
            )
    }
}

data class RaiderEntityExistsResponse(val exists: Boolean)

data class RaiderEntityCountResponse(val count: Long)
