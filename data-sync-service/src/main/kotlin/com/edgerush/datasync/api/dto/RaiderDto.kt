package com.edgerush.datasync.api.dto

import com.edgerush.datasync.domain.shared.model.Raider
import java.time.OffsetDateTime

/**
 * DTO for Raider responses
 */
data class RaiderDto(
    val id: Long,
    val characterName: String,
    val realm: String,
    val region: String,
    val wowauditId: Long?,
    val clazz: String,
    val spec: String,
    val role: String,
    val rank: String?,
    val status: String,
    val note: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val joinDate: OffsetDateTime?,
    val blizzardLastModified: OffsetDateTime?,
    val lastSync: OffsetDateTime
) {
    companion object {
        fun from(raider: Raider): RaiderDto {
            return RaiderDto(
                id = raider.id.value,
                characterName = raider.characterName,
                realm = raider.realm,
                region = raider.region,
                wowauditId = raider.wowauditId,
                clazz = raider.clazz.name,
                spec = raider.spec,
                role = raider.role.name,
                rank = raider.rank,
                status = raider.status.name,
                note = raider.note,
                blizzardId = raider.blizzardId,
                trackingSince = raider.trackingSince,
                joinDate = raider.joinDate,
                blizzardLastModified = raider.blizzardLastModified,
                lastSync = raider.lastSync
            )
        }
    }
}

/**
 * Request to update raider status
 */
data class UpdateRaiderStatusRequest(
    val status: String
)
