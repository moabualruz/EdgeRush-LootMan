package com.edgerush.lootman.domain.guild.model

/**
 * Types of permissions that can be granted to guild ranks.
 *
 * Permissions are configurable per guild, allowing each guild to decide
 * which ranks should have access to which features.
 */
enum class GuildPermissionType {
    /**
     * Access to guild settings page.
     * Allows viewing and modifying guild configuration including:
     * - WoWAudit API settings
     * - FLPS configuration
     * - Permission settings
     */
    SETTINGS_ACCESS,

    /**
     * Access to loot management features.
     * Allows:
     * - Distributing loot
     * - Managing loot bans
     * - Viewing all FLPS scores for loot decisions
     */
    LOOT_MANAGEMENT,

    /**
     * Access to member management features.
     * Allows:
     * - Managing guild roster
     * - Adding/removing members from tracking
     * - Managing behavioral actions
     */
    MEMBER_MANAGEMENT,

    /**
     * Access to view all member FLPS scores.
     * Normally members can only see their own score.
     * This permission allows viewing the full leaderboard with scores.
     */
    VIEW_ALL_SCORES,
}
