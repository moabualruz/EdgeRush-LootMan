package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for domain exceptions: DomainException, RaiderNotFoundException,
 * GuildNotFoundException, ItemNotFoundException, and LootBanActiveException.
 */
class DomainExceptionTest : UnitTest() {

    // region Test Fixtures

    private fun createLootBan(
        id: String = "ban-123",
        raiderId: Long = 42L,
        guildId: String = "guild-123",
        reason: String = "Test ban reason",
        bannedAt: Instant = Instant.now(),
        expiresAt: Instant? = null
    ) = LootBan(
        id = LootBanId(id),
        raiderId = RaiderId(raiderId),
        guildId = GuildId(guildId),
        reason = reason,
        bannedAt = bannedAt,
        expiresAt = expiresAt
    )

    // endregion

    @Nested
    inner class DomainExceptionBaseTests {

        @Test
        fun `should be sealed class preventing external inheritance`() {
            // This test documents that DomainException is sealed
            // Subclasses can only be defined in the same file
            val raiderNotFound = RaiderNotFoundException(RaiderId(1L))
            val guildNotFound = GuildNotFoundException(GuildId("test"))
            val itemNotFound = ItemNotFoundException(ItemId(100L))
            val lootBanActive = LootBanActiveException(RaiderId(1L), emptyList())

            // All should be instances of DomainException
            raiderNotFound.shouldBeInstanceOf<DomainException>()
            guildNotFound.shouldBeInstanceOf<DomainException>()
            itemNotFound.shouldBeInstanceOf<DomainException>()
            lootBanActive.shouldBeInstanceOf<DomainException>()
        }

        @Test
        fun `should inherit from Exception`() {
            // Arrange
            val exception = RaiderNotFoundException(RaiderId(1L))

            // Assert
            exception.shouldBeInstanceOf<Exception>()
        }
    }

    @Nested
    inner class RaiderNotFoundExceptionTests {

        @Test
        fun `should create exception with raider ID`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val exception = RaiderNotFoundException(raiderId)

            // Assert
            exception.raiderId shouldBe raiderId
        }

        @Test
        fun `should have descriptive message with raider ID value`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val exception = RaiderNotFoundException(raiderId)

            // Assert
            exception.message shouldBe "Raider not found: 42"
        }

        @Test
        fun `should have message with large raider ID`() {
            // Arrange
            val raiderId = RaiderId(999999999L)

            // Act
            val exception = RaiderNotFoundException(raiderId)

            // Assert
            exception.message shouldBe "Raider not found: 999999999"
        }

        @Test
        fun `should preserve raider ID for programmatic access`() {
            // Arrange
            val raiderId = RaiderId(123L)
            val exception = RaiderNotFoundException(raiderId)

            // Act
            val retrievedId = exception.raiderId

            // Assert
            retrievedId.value shouldBe 123L
        }

        @Test
        fun `should be catchable as DomainException`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act & Assert
            try {
                throw RaiderNotFoundException(raiderId)
            } catch (e: DomainException) {
                e.message shouldBe "Raider not found: 42"
            }
        }
    }

    @Nested
    inner class GuildNotFoundExceptionTests {

        @Test
        fun `should create exception with guild ID`() {
            // Arrange
            val guildId = GuildId("edge-rush")

            // Act
            val exception = GuildNotFoundException(guildId)

            // Assert
            exception.guildId shouldBe guildId
        }

        @Test
        fun `should have descriptive message with guild ID value`() {
            // Arrange
            val guildId = GuildId("edge-rush")

            // Act
            val exception = GuildNotFoundException(guildId)

            // Assert
            exception.message shouldBe "Guild not found: edge-rush"
        }

        @Test
        fun `should have message with UUID guild ID`() {
            // Arrange
            val guildId = GuildId("550e8400-e29b-41d4-a716-446655440000")

            // Act
            val exception = GuildNotFoundException(guildId)

            // Assert
            exception.message shouldBe "Guild not found: 550e8400-e29b-41d4-a716-446655440000"
        }

        @Test
        fun `should preserve guild ID for programmatic access`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val exception = GuildNotFoundException(guildId)

            // Act
            val retrievedId = exception.guildId

            // Assert
            retrievedId.value shouldBe "test-guild"
        }

        @Test
        fun `should handle special characters in guild ID`() {
            // Arrange
            val guildId = GuildId("guild-name_123.test")

            // Act
            val exception = GuildNotFoundException(guildId)

            // Assert
            exception.message shouldBe "Guild not found: guild-name_123.test"
        }

        @Test
        fun `should be catchable as DomainException`() {
            // Arrange
            val guildId = GuildId("test")

            // Act & Assert
            try {
                throw GuildNotFoundException(guildId)
            } catch (e: DomainException) {
                e.message shouldBe "Guild not found: test"
            }
        }
    }

    @Nested
    inner class ItemNotFoundExceptionTests {

        @Test
        fun `should create exception with item ID`() {
            // Arrange
            val itemId = ItemId(207160L)

            // Act
            val exception = ItemNotFoundException(itemId)

            // Assert
            exception.itemId shouldBe itemId
        }

        @Test
        fun `should have descriptive message with item ID value`() {
            // Arrange
            val itemId = ItemId(207160L)

            // Act
            val exception = ItemNotFoundException(itemId)

            // Assert
            exception.message shouldBe "Item not found: 207160"
        }

        @Test
        fun `should have message with small item ID`() {
            // Arrange
            val itemId = ItemId(1L)

            // Act
            val exception = ItemNotFoundException(itemId)

            // Assert
            exception.message shouldBe "Item not found: 1"
        }

        @Test
        fun `should preserve item ID for programmatic access`() {
            // Arrange
            val itemId = ItemId(12345L)
            val exception = ItemNotFoundException(itemId)

            // Act
            val retrievedId = exception.itemId

            // Assert
            retrievedId.value shouldBe 12345L
        }

        @Test
        fun `should be catchable as DomainException`() {
            // Arrange
            val itemId = ItemId(100L)

            // Act & Assert
            try {
                throw ItemNotFoundException(itemId)
            } catch (e: DomainException) {
                e.message shouldBe "Item not found: 100"
            }
        }
    }

    @Nested
    inner class LootBanActiveExceptionTests {

        @Test
        fun `should create exception with raider ID and empty bans list`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val exception = LootBanActiveException(raiderId, emptyList())

            // Assert
            exception.raiderId shouldBe raiderId
            exception.bans.shouldBeEmpty()
        }

        @Test
        fun `should create exception with single ban`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val ban = createLootBan(raiderId = 42L)

            // Act
            val exception = LootBanActiveException(raiderId, listOf(ban))

            // Assert
            exception.raiderId shouldBe raiderId
            exception.bans shouldHaveSize 1
            exception.bans.first() shouldBe ban
        }

        @Test
        fun `should create exception with multiple bans`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val bans = listOf(
                createLootBan(id = "ban-1", raiderId = 42L, reason = "First ban"),
                createLootBan(id = "ban-2", raiderId = 42L, reason = "Second ban"),
                createLootBan(id = "ban-3", raiderId = 42L, reason = "Third ban")
            )

            // Act
            val exception = LootBanActiveException(raiderId, bans)

            // Assert
            exception.bans shouldHaveSize 3
        }

        @Test
        fun `should have descriptive message with ban count for single ban`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val ban = createLootBan(raiderId = 42L)

            // Act
            val exception = LootBanActiveException(raiderId, listOf(ban))

            // Assert
            exception.message shouldBe "Raider 42 has 1 active loot ban(s)"
        }

        @Test
        fun `should have descriptive message with ban count for multiple bans`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val bans = listOf(
                createLootBan(id = "ban-1", raiderId = 42L),
                createLootBan(id = "ban-2", raiderId = 42L),
                createLootBan(id = "ban-3", raiderId = 42L)
            )

            // Act
            val exception = LootBanActiveException(raiderId, bans)

            // Assert
            exception.message shouldBe "Raider 42 has 3 active loot ban(s)"
        }

        @Test
        fun `should have message with zero bans`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val exception = LootBanActiveException(raiderId, emptyList())

            // Assert
            exception.message shouldBe "Raider 42 has 0 active loot ban(s)"
        }

        @Test
        fun `should preserve bans list for programmatic access`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val ban = createLootBan(raiderId = 42L, reason = "AFK during raid")

            // Act
            val exception = LootBanActiveException(raiderId, listOf(ban))

            // Assert
            exception.bans.first().reason shouldBe "AFK during raid"
        }

        @Test
        fun `should be catchable as DomainException`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val ban = createLootBan(raiderId = 42L)

            // Act & Assert
            try {
                throw LootBanActiveException(raiderId, listOf(ban))
            } catch (e: DomainException) {
                e.message shouldBe "Raider 42 has 1 active loot ban(s)"
            }
        }
    }

    @Nested
    inner class ExceptionHandlingPatternTests {

        @Test
        fun `should allow when expression matching on exception type`() {
            // Arrange
            val exceptions: List<DomainException> = listOf(
                RaiderNotFoundException(RaiderId(1L)),
                GuildNotFoundException(GuildId("test")),
                ItemNotFoundException(ItemId(100L)),
                LootBanActiveException(RaiderId(2L), emptyList())
            )

            // Act & Assert
            exceptions.forEach { exception ->
                val result = when (exception) {
                    is RaiderNotFoundException -> "raider"
                    is GuildNotFoundException -> "guild"
                    is ItemNotFoundException -> "item"
                    is LootBanActiveException -> "loot_ban"
                    is DiscordUserLinkNotFoundException -> "discord_link_not_found"
                    is DiscordUserLinkAlreadyExistsException -> "discord_link_exists"
                    is UserNotFoundException -> "user_not_found"
                    is UserNotFoundByDiscordIdException -> "user_not_found_by_discord"
                    is UserNotFoundByBattlenetIdException -> "user_not_found_by_battlenet"
                    is AuthenticationFailedException -> "auth_failed"
                    is InvalidRefreshTokenException -> "invalid_refresh_token"
                    is OAuth2AuthenticationException -> "oauth2_error"
                }
                result shouldBe when (exception) {
                    is RaiderNotFoundException -> "raider"
                    is GuildNotFoundException -> "guild"
                    is ItemNotFoundException -> "item"
                    is LootBanActiveException -> "loot_ban"
                    is DiscordUserLinkNotFoundException -> "discord_link_not_found"
                    is DiscordUserLinkAlreadyExistsException -> "discord_link_exists"
                    is UserNotFoundException -> "user_not_found"
                    is UserNotFoundByDiscordIdException -> "user_not_found_by_discord"
                    is UserNotFoundByBattlenetIdException -> "user_not_found_by_battlenet"
                    is AuthenticationFailedException -> "auth_failed"
                    is InvalidRefreshTokenException -> "invalid_refresh_token"
                    is OAuth2AuthenticationException -> "oauth2_error"
                }
            }
        }

        @Test
        fun `should allow extracting ID from caught exception`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            var extractedId: Long? = null
            try {
                throw RaiderNotFoundException(raiderId)
            } catch (e: RaiderNotFoundException) {
                extractedId = e.raiderId.value
            }

            // Assert
            extractedId shouldBe 42L
        }

        @Test
        fun `should allow catching by base type and checking specific type`() {
            // Arrange
            val guildId = GuildId("test-guild")

            // Act
            var isGuildException = false
            try {
                throw GuildNotFoundException(guildId)
            } catch (e: DomainException) {
                isGuildException = e is GuildNotFoundException
            }

            // Assert
            isGuildException shouldBe true
        }
    }

    @Nested
    inner class ExceptionCauseTests {

        @Test
        fun `RaiderNotFoundException should have null cause by default`() {
            // Arrange & Act
            val exception = RaiderNotFoundException(RaiderId(1L))

            // Assert
            exception.cause shouldBe null
        }

        @Test
        fun `GuildNotFoundException should have null cause by default`() {
            // Arrange & Act
            val exception = GuildNotFoundException(GuildId("test"))

            // Assert
            exception.cause shouldBe null
        }

        @Test
        fun `ItemNotFoundException should have null cause by default`() {
            // Arrange & Act
            val exception = ItemNotFoundException(ItemId(100L))

            // Assert
            exception.cause shouldBe null
        }

        @Test
        fun `LootBanActiveException should have null cause by default`() {
            // Arrange & Act
            val exception = LootBanActiveException(RaiderId(1L), emptyList())

            // Assert
            exception.cause shouldBe null
        }
    }
}
