package com.edgerush.datasync.security

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Comprehensive unit tests for AuthenticatedUser.
 *
 * Tests cover:
 * - Role-based access control (SYSTEM_ADMIN, GUILD_ADMIN)
 * - Guild access permissions
 * - Admin mode functionality
 * - Factory methods
 * - Edge cases
 */
class AuthenticatedUserTest : UnitTest() {

    @Nested
    inner class `isSystemAdmin` {

        @Test
        fun `should return true when user has SYSTEM_ADMIN role`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "admin",
                roles = listOf("SYSTEM_ADMIN"),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeTrue()
        }

        @Test
        fun `should return true when admin mode is enabled`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
                isAdminMode = true,
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeTrue()
        }

        @Test
        fun `should return false when user has no admin roles and admin mode is disabled`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "regularuser",
                roles = listOf("PUBLIC_USER", "GUILD_MEMBER"),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeFalse()
        }

        @Test
        fun `should return false when user has GUILD_ADMIN role only`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "guildadmin",
                roles = listOf("GUILD_ADMIN"),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeFalse()
        }

        @Test
        fun `should return false when user has empty roles`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "norolesuser",
                roles = emptyList(),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeFalse()
        }

        @Test
        fun `should return true when user has multiple roles including SYSTEM_ADMIN`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "superadmin",
                roles = listOf("PUBLIC_USER", "GUILD_ADMIN", "SYSTEM_ADMIN"),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeTrue()
        }
    }

    @Nested
    inner class `isGuildAdmin` {

        @Test
        fun `should return true when user has GUILD_ADMIN role`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "guildadmin",
                roles = listOf("GUILD_ADMIN"),
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeTrue()
        }

        @Test
        fun `should return true when user has SYSTEM_ADMIN role`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "admin",
                roles = listOf("SYSTEM_ADMIN"),
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeTrue()
        }

        @Test
        fun `should return true when admin mode is enabled`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
                isAdminMode = true,
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeTrue()
        }

        @Test
        fun `should return false when user has no admin roles`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "regularuser",
                roles = listOf("PUBLIC_USER", "GUILD_MEMBER"),
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeFalse()
        }

        @Test
        fun `should return false when user has empty roles`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "norolesuser",
                roles = emptyList(),
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeFalse()
        }

        @Test
        fun `should return true when user has both GUILD_ADMIN and SYSTEM_ADMIN roles`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "superadmin",
                roles = listOf("GUILD_ADMIN", "SYSTEM_ADMIN"),
            )

            // Act & Assert
            user.isGuildAdmin().shouldBeTrue()
        }
    }

    @Nested
    inner class `hasGuildAccess` {

        @Test
        fun `should return true when user is system admin`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "admin",
                roles = listOf("SYSTEM_ADMIN"),
            )

            // Act & Assert
            user.hasGuildAccess("any-guild").shouldBeTrue()
            user.hasGuildAccess("guild-123").shouldBeTrue()
            user.hasGuildAccess("random-guild-id").shouldBeTrue()
        }

        @Test
        fun `should return true when user has the guild in their guildIds`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("GUILD_ADMIN"),
                guildIds = listOf("guild-1", "guild-2", "guild-3"),
            )

            // Act & Assert
            user.hasGuildAccess("guild-1").shouldBeTrue()
            user.hasGuildAccess("guild-2").shouldBeTrue()
            user.hasGuildAccess("guild-3").shouldBeTrue()
        }

        @Test
        fun `should return false when user does not have the guild in their guildIds`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("GUILD_ADMIN"),
                guildIds = listOf("guild-1", "guild-2"),
            )

            // Act & Assert
            user.hasGuildAccess("guild-3").shouldBeFalse()
            user.hasGuildAccess("other-guild").shouldBeFalse()
        }

        @Test
        fun `should return false when user has empty guildIds and is not admin`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
                guildIds = emptyList(),
            )

            // Act & Assert
            user.hasGuildAccess("any-guild").shouldBeFalse()
        }

        @Test
        fun `should return true when admin mode is enabled regardless of guildIds`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
                guildIds = emptyList(),
                isAdminMode = true,
            )

            // Act & Assert
            user.hasGuildAccess("any-guild").shouldBeTrue()
            user.hasGuildAccess("guild-not-in-list").shouldBeTrue()
        }

        @Test
        fun `should be case-sensitive for guild IDs`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("GUILD_ADMIN"),
                guildIds = listOf("Guild-1", "GUILD-2"),
            )

            // Act & Assert
            user.hasGuildAccess("Guild-1").shouldBeTrue()
            user.hasGuildAccess("guild-1").shouldBeFalse()
            user.hasGuildAccess("GUILD-2").shouldBeTrue()
            user.hasGuildAccess("guild-2").shouldBeFalse()
        }
    }

    @Nested
    inner class `adminModeUser factory method` {

        @Test
        fun `should create user with admin mode enabled`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.isAdminMode.shouldBeTrue()
        }

        @Test
        fun `should have system admin privileges`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.isSystemAdmin().shouldBeTrue()
        }

        @Test
        fun `should have guild admin privileges`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.isGuildAdmin().shouldBeTrue()
        }

        @Test
        fun `should have access to any guild`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.hasGuildAccess("any-guild").shouldBeTrue()
            user.hasGuildAccess("random-guild-id").shouldBeTrue()
        }

        @Test
        fun `should have SYSTEM_ADMIN role`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.roles shouldContain "SYSTEM_ADMIN"
        }

        @Test
        fun `should have admin-mode as id and username`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.id shouldBe "admin-mode"
            user.username shouldBe "admin-mode"
        }

        @Test
        fun `should have empty guildIds`() {
            // Act
            val user = AuthenticatedUser.adminModeUser()

            // Assert
            user.guildIds shouldBe emptyList()
        }
    }

    @Nested
    inner class `data class functionality` {

        @Test
        fun `should support copy with modifications`() {
            // Arrange
            val original = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
                guildIds = listOf("guild-1"),
            )

            // Act
            val copied = original.copy(
                username = "newuser",
                roles = listOf("GUILD_ADMIN"),
            )

            // Assert
            copied.id shouldBe original.id
            copied.username shouldBe "newuser"
            copied.roles shouldBe listOf("GUILD_ADMIN")
            copied.guildIds shouldBe original.guildIds
        }

        @Test
        fun `should support equality comparison`() {
            // Arrange
            val user1 = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
            )
            val user2 = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
            )

            // Assert
            user1 shouldBe user2
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-123",
                username = "testuser",
                roles = listOf("GUILD_ADMIN"),
            )

            // Act
            val result = user.toString()

            // Assert
            result.shouldNotBeEmpty()
        }

        @Test
        fun `should use default values for optional parameters`() {
            // Arrange & Act
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("PUBLIC_USER"),
            )

            // Assert
            user.guildIds shouldBe emptyList()
            user.isAdminMode.shouldBeFalse()
        }
    }

    @Nested
    inner class `edge cases` {

        @Test
        fun `should handle role name with spaces`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("SYSTEM ADMIN"),
            )

            // Act & Assert - SYSTEM_ADMIN requires underscore
            user.isSystemAdmin().shouldBeFalse()
        }

        @Test
        fun `should handle case-sensitive role names`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("system_admin", "System_Admin"),
            )

            // Act & Assert - roles are case-sensitive
            user.isSystemAdmin().shouldBeFalse()
        }

        @Test
        fun `should handle duplicate roles`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("SYSTEM_ADMIN", "SYSTEM_ADMIN", "GUILD_ADMIN"),
            )

            // Act & Assert
            user.isSystemAdmin().shouldBeTrue()
            user.isGuildAdmin().shouldBeTrue()
        }

        @Test
        fun `should handle empty guild ID string`() {
            // Arrange
            val user = AuthenticatedUser(
                id = "user-1",
                username = "user",
                roles = listOf("GUILD_ADMIN"),
                guildIds = listOf(""),
            )

            // Act & Assert
            user.hasGuildAccess("").shouldBeTrue()
            user.hasGuildAccess("guild-1").shouldBeFalse()
        }
    }
}
