package com.edgerush.lootman.domain.guild.repository

import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository for managing guild permissions.
 */
interface GuildPermissionRepository {
    /**
     * Finds all permissions for a guild.
     */
    fun findByGuildId(guildId: GuildId): List<GuildPermission>

    /**
     * Finds all permissions for a specific rank in a guild.
     */
    fun findByGuildIdAndRankName(
        guildId: GuildId,
        rankName: String,
    ): List<GuildPermission>

    /**
     * Checks if a rank has a specific permission in a guild.
     */
    fun hasPermission(
        guildId: GuildId,
        rankName: String,
        permissionType: GuildPermissionType,
    ): Boolean

    /**
     * Finds a permission by ID.
     */
    fun findById(id: GuildPermissionId): GuildPermission?

    /**
     * Saves a permission (insert or update).
     */
    fun save(permission: GuildPermission): GuildPermission

    /**
     * Deletes a permission by ID.
     */
    fun deleteById(id: GuildPermissionId)

    /**
     * Deletes all permissions for a guild.
     */
    fun deleteByGuildId(guildId: GuildId)

    /**
     * Gets all distinct rank names that have any permission in a guild.
     */
    fun findDistinctRankNamesByGuildId(guildId: GuildId): List<String>

    /**
     * Finds permissions for multiple guild+rank combinations in a single query.
     * Returns a map from (guildId, rankName) to list of permission types.
     *
     * This is more efficient than calling findByGuildIdAndRankName multiple times.
     */
    fun findByGuildIdAndRankNames(guildRanks: List<Pair<GuildId, String>>): Map<Pair<String, String>, List<GuildPermissionType>>
}
