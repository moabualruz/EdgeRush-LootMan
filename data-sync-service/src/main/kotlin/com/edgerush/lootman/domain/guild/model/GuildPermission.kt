package com.edgerush.lootman.domain.guild.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * Represents a permission granted to a guild rank.
 *
 * Guild permissions map ranks (from WoWAudit, e.g., "Guild Master", "Officer")
 * to application permissions (e.g., SETTINGS_ACCESS, LOOT_MANAGEMENT).
 *
 * This allows each guild to configure which ranks should have access to
 * which features. By default, Guild Master and Officer ranks are granted
 * SETTINGS_ACCESS and LOOT_MANAGEMENT permissions.
 */
data class GuildPermission(
    val id: GuildPermissionId? = null,
    val guildId: GuildId,
    val rankName: String,
    val permissionType: GuildPermissionType,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(rankName.isNotBlank()) { "Rank name cannot be blank" }
    }

    companion object {
        /**
         * Creates a new permission entry for a guild rank.
         */
        fun create(
            guildId: GuildId,
            rankName: String,
            permissionType: GuildPermissionType,
        ): GuildPermission =
            GuildPermission(
                guildId = guildId,
                rankName = rankName.trim(),
                permissionType = permissionType,
            )

        /**
         * Default ranks that should have SETTINGS_ACCESS permission.
         */
        val DEFAULT_SETTINGS_RANKS = listOf("Guild Master", "Officer")

        /**
         * Default ranks that should have LOOT_MANAGEMENT permission.
         */
        val DEFAULT_LOOT_MANAGEMENT_RANKS = listOf("Guild Master", "Officer")
    }
}

/**
 * Value object for GuildPermission ID.
 */
@JvmInline
value class GuildPermissionId(val value: Long) {
    init {
        require(value > 0) { "GuildPermissionId must be positive" }
    }
}
