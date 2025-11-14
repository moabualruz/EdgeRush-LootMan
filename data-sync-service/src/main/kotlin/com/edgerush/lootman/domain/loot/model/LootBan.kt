package com.edgerush.lootman.domain.loot.model

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant
import java.util.UUID

/**
 * Entity representing a loot ban.
 *
 * A loot ban temporarily prevents a raider from receiving loot.
 */
data class LootBan(
    val id: LootBanId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val reason: String,
    val bannedAt: Instant,
    val expiresAt: Instant?,
) {
    fun isActive(now: Instant = Instant.now()): Boolean {
        return expiresAt == null || now.isBefore(expiresAt)
    }

    companion object {
        fun create(
            raiderId: RaiderId,
            guildId: GuildId,
            reason: String,
            expiresAt: Instant?,
        ): LootBan =
            LootBan(
                id = LootBanId.generate(),
                raiderId = raiderId,
                guildId = guildId,
                reason = reason,
                bannedAt = Instant.now(),
                expiresAt = expiresAt,
            )
    }
}

data class LootBanId(val value: String) {
    init {
        require(value.isNotBlank()) { "LootBan ID cannot be blank" }
    }

    companion object {
        fun generate(): LootBanId = LootBanId(UUID.randomUUID().toString())
    }
}
