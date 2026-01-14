package com.edgerush.lootman.domain.auth.model

/**
 * User roles for access control.
 */
enum class UserRole {
    /**
     * Regular raider with access to their own data.
     */
    RAIDER,

    /**
     * Guild administrator with access to guild management.
     */
    GUILD_ADMIN,

    /**
     * System administrator with full access.
     */
    SYSTEM_ADMIN;

    companion object {
        /**
         * Safely parse a role string, defaulting to RAIDER if invalid.
         */
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                RAIDER
            }
        }
    }
}
