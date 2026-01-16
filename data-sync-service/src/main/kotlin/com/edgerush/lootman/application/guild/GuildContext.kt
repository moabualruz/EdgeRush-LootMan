package com.edgerush.lootman.application.guild

import com.edgerush.lootman.domain.guild.model.GuildPermissionType

/**
 * Represents a user's context within a guild.
 *
 * Contains information about the user's character in the guild,
 * their rank, and what permissions they have.
 */
data class GuildContext(
    val guildId: String,
    val guildName: String,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val characterMappingId: Long,
    val raiderId: Long,
    val rank: String?,
    val permissions: List<GuildPermissionType>,
    val isActive: Boolean,
) {
    /**
     * Checks if the user has a specific permission in this guild context.
     */
    fun hasPermission(permission: GuildPermissionType): Boolean = permissions.contains(permission)

    /**
     * Checks if the user can access guild settings.
     */
    fun canAccessSettings(): Boolean = hasPermission(GuildPermissionType.SETTINGS_ACCESS)

    /**
     * Checks if the user can manage loot.
     */
    fun canManageLoot(): Boolean = hasPermission(GuildPermissionType.LOOT_MANAGEMENT)

    /**
     * Checks if the user can manage members.
     */
    fun canManageMembers(): Boolean = hasPermission(GuildPermissionType.MEMBER_MANAGEMENT)

    /**
     * Checks if the user can view all FLPS scores.
     */
    fun canViewAllScores(): Boolean = hasPermission(GuildPermissionType.VIEW_ALL_SCORES)
}
