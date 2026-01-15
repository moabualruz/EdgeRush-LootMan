package com.edgerush.lootman.domain.auth.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for User entity.
 */
class UserTest : UnitTest() {
    @Nested
    inner class Creation {
        @Test
        fun `should create user from Discord`() {
            // Act
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "testuser",
                    email = "test@example.com",
                    avatarUrl = "https://cdn.discordapp.com/avatars/123/abc.png",
                )

            // Assert
            user.discordId shouldBe "123456789012345678"
            user.username shouldBe "testuser"
            user.email shouldBe "test@example.com"
            user.avatarUrl shouldBe "https://cdn.discordapp.com/avatars/123/abc.png"
            user.role shouldBe UserRole.RAIDER
        }

        @Test
        fun `should create user from Battle net`() {
            // Act
            val user =
                User.fromBattlenet(
                    battlenetId = "12345",
                    username = "Player#1234",
                )

            // Assert
            user.battlenetId shouldBe "12345"
            user.username shouldBe "Player#1234"
            user.role shouldBe UserRole.RAIDER
        }

        @Test
        fun `should create local user without OAuth`() {
            // Act
            val user =
                User.fromLocal(
                    username = "testuser",
                    email = "test@example.com",
                    passwordHash = "hashedpassword",
                )

            // Assert
            user.discordId shouldBe null
            user.battlenetId shouldBe null
            user.username shouldBe "testuser"
            user.email shouldBe "test@example.com"
            user.passwordHash shouldBe "hashedpassword"
            user.role shouldBe UserRole.RAIDER
        }

        @Test
        fun `should allow user with just username`() {
            // Act (Users can exist without OAuth or password - useful for admin-created accounts)
            val user = User(username = "testuser")

            // Assert
            user.username shouldBe "testuser"
            user.discordId shouldBe null
            user.battlenetId shouldBe null
            user.passwordHash shouldBe null
        }

        @Test
        fun `should require non-blank username`() {
            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                User(discordId = "123", username = "  ")
            }
        }
    }

    @Nested
    inner class Modifications {
        @Test
        fun `should update profile`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "oldname",
                )

            // Act
            val updated =
                user.updateProfile(
                    username = "newname",
                    email = "new@example.com",
                    avatarUrl = "https://new-avatar.png",
                )

            // Assert
            updated.username shouldBe "newname"
            updated.email shouldBe "new@example.com"
            updated.avatarUrl shouldBe "https://new-avatar.png"
        }

        @Test
        fun `should record login`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "testuser",
                )

            // Act
            val updated = user.recordLogin()

            // Assert
            updated.lastLogin.shouldNotBeNull()
        }

        @Test
        fun `should change role`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "testuser",
                )

            // Act
            val updated = user.withRole(UserRole.GUILD_ADMIN)

            // Assert
            updated.role shouldBe UserRole.GUILD_ADMIN
        }

        @Test
        fun `should associate with guild`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "testuser",
                )
            val guildId = GuildId("test-guild")

            // Act
            val updated = user.withGuild(guildId)

            // Assert
            updated.guildId shouldBe guildId
        }

        @Test
        fun `should link Discord account`() {
            // Arrange
            val user =
                User.fromBattlenet(
                    battlenetId = "12345",
                    username = "Player#1234",
                )

            // Act
            val updated = user.linkDiscord("123456789012345678")

            // Assert
            updated.discordId shouldBe "123456789012345678"
            updated.battlenetId shouldBe "12345"
        }

        @Test
        fun `should link Battle net account`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123456789012345678",
                    username = "testuser",
                )

            // Act
            val updated = user.linkBattlenet("12345")

            // Assert
            updated.discordId shouldBe "123456789012345678"
            updated.battlenetId shouldBe "12345"
        }
    }

    @Nested
    inner class RoleChecks {
        @Test
        fun `raider should have raider role`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123",
                    username = "test",
                )

            // Assert
            user.hasRole(UserRole.RAIDER) shouldBe true
            user.hasRole(UserRole.GUILD_ADMIN) shouldBe false
            user.hasRole(UserRole.SYSTEM_ADMIN) shouldBe false
        }

        @Test
        fun `guild admin should have raider and guild admin roles`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123",
                    username = "test",
                ).withRole(UserRole.GUILD_ADMIN)

            // Assert
            user.hasRole(UserRole.RAIDER) shouldBe true
            user.hasRole(UserRole.GUILD_ADMIN) shouldBe true
            user.hasRole(UserRole.SYSTEM_ADMIN) shouldBe false
        }

        @Test
        fun `system admin should have all roles`() {
            // Arrange
            val user =
                User.fromDiscord(
                    discordId = "123",
                    username = "test",
                ).withRole(UserRole.SYSTEM_ADMIN)

            // Assert
            user.hasRole(UserRole.RAIDER) shouldBe true
            user.hasRole(UserRole.GUILD_ADMIN) shouldBe true
            user.hasRole(UserRole.SYSTEM_ADMIN) shouldBe true
        }
    }
}
