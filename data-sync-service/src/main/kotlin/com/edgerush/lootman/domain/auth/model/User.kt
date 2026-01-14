package com.edgerush.lootman.domain.auth.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * User entity representing an authenticated user account.
 *
 * Users authenticate via Discord or Battle.net OAuth2 and can be
 * associated with a guild for access control purposes.
 */
data class User(
    val id: UserId? = null,
    val discordId: String? = null,
    val battlenetId: String? = null,
    val username: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.RAIDER,
    val guildId: GuildId? = null,
    val createdAt: Instant = Instant.now(),
    val lastLogin: Instant? = null
) {
    init {
        require(username.isNotBlank()) { "Username must not be blank" }
        require(discordId != null || battlenetId != null) {
            "User must have either Discord ID or Battle.net ID"
        }
    }

    /**
     * Creates a copy with the given ID.
     */
    fun withId(id: UserId): User = copy(id = id)

    /**
     * Updates the last login timestamp.
     */
    fun recordLogin(): User = copy(lastLogin = Instant.now())

    /**
     * Updates the user's role.
     */
    fun withRole(newRole: UserRole): User = copy(role = newRole)

    /**
     * Associates the user with a guild.
     */
    fun withGuild(guildId: GuildId?): User = copy(guildId = guildId)

    /**
     * Updates the user's profile information.
     */
    fun updateProfile(
        username: String? = null,
        email: String? = null,
        avatarUrl: String? = null
    ): User = copy(
        username = username ?: this.username,
        email = email ?: this.email,
        avatarUrl = avatarUrl ?: this.avatarUrl
    )

    /**
     * Links a Discord account to this user.
     */
    fun linkDiscord(discordId: String): User {
        require(discordId.isNotBlank()) { "Discord ID must not be blank" }
        return copy(discordId = discordId)
    }

    /**
     * Links a Battle.net account to this user.
     */
    fun linkBattlenet(battlenetId: String): User {
        require(battlenetId.isNotBlank()) { "Battle.net ID must not be blank" }
        return copy(battlenetId = battlenetId)
    }

    /**
     * Checks if the user has the specified role or higher.
     */
    fun hasRole(requiredRole: UserRole): Boolean = when (requiredRole) {
        UserRole.RAIDER -> true
        UserRole.GUILD_ADMIN -> role == UserRole.GUILD_ADMIN || role == UserRole.SYSTEM_ADMIN
        UserRole.SYSTEM_ADMIN -> role == UserRole.SYSTEM_ADMIN
    }

    companion object {
        /**
         * Creates a new user from Discord OAuth2.
         */
        fun fromDiscord(
            discordId: String,
            username: String,
            email: String? = null,
            avatarUrl: String? = null
        ): User = User(
            discordId = discordId,
            username = username,
            email = email,
            avatarUrl = avatarUrl
        )

        /**
         * Creates a new user from Battle.net OAuth2.
         */
        fun fromBattlenet(
            battlenetId: String,
            username: String
        ): User = User(
            battlenetId = battlenetId,
            username = username
        )
    }
}
