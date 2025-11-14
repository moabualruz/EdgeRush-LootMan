package com.edgerush.datasync.security

data class AuthenticatedUser(
    val id: String,
    val username: String,
    val roles: List<String>,
    val guildIds: List<String> = emptyList(),
    val isAdminMode: Boolean = false,
) {
    fun isSystemAdmin(): Boolean = roles.contains("SYSTEM_ADMIN") || isAdminMode

    fun isGuildAdmin(): Boolean = roles.contains("GUILD_ADMIN") || isSystemAdmin()

    fun hasGuildAccess(guildId: String): Boolean = isSystemAdmin() || guildIds.contains(guildId)

    companion object {
        fun adminModeUser() =
            AuthenticatedUser(
                id = "admin-mode",
                username = "admin-mode",
                roles = listOf("SYSTEM_ADMIN"),
                guildIds = emptyList(),
                isAdminMode = true,
            )
    }
}
