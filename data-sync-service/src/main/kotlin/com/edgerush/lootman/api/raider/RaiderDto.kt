package com.edgerush.lootman.api.raider

import com.edgerush.lootman.domain.shared.model.Raider
import java.time.LocalDateTime

/**
 * Request DTO for creating a raider.
 */
data class CreateRaiderRequest(
    val id: Long,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val characterClass: String,
    val role: String,
    val rank: String? = null,
    val status: String = "ACTIVE",
    val joinDate: LocalDateTime? = null,
    val wowauditId: Long? = null
)

/**
 * Request DTO for updating a raider.
 */
data class UpdateRaiderRequest(
    val characterName: String? = null,
    val realm: String? = null,
    val characterClass: String? = null,
    val role: String? = null,
    val rank: String? = null,
    val status: String? = null
)

/**
 * Response DTO for a raider.
 */
data class RaiderResponse(
    val id: Long,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val characterClass: String,
    val role: String,
    val rank: String?,
    val status: String,
    val joinDate: LocalDateTime?,
    val wowauditId: Long?,
    val fullName: String,
    val isEligibleForLoot: Boolean
) {
    companion object {
        fun from(raider: Raider): RaiderResponse = RaiderResponse(
            id = raider.id.value,
            guildId = raider.guildId.value,
            characterName = raider.characterName,
            realm = raider.realm,
            characterClass = raider.characterClass.name,
            role = raider.role.name,
            rank = raider.rank,
            status = raider.status.name,
            joinDate = raider.joinDate,
            wowauditId = raider.wowauditId,
            fullName = raider.getFullName(),
            isEligibleForLoot = raider.isEligibleForLoot()
        )
    }
}

/**
 * Response DTO for a list of raiders.
 */
data class RaiderListResponse(
    val raiders: List<RaiderResponse>,
    val count: Int
) {
    companion object {
        fun from(raiders: List<Raider>): RaiderListResponse = RaiderListResponse(
            raiders = raiders.map { RaiderResponse.from(it) },
            count = raiders.size
        )
    }
}
