package com.edgerush.lootman.domain.auth.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for UserPreferences domain model.
 *
 * Tests verify:
 * - Preferences creation and validation
 * - Active character management
 * - Factory methods
 * - UserPreferencesId value object
 */
class UserPreferencesTest : UnitTest() {
    @Nested
    inner class UserPreferencesCreation {
        @Test
        fun `should create preferences with no active character`() {
            // Given
            val userId = UserId(1L)

            // When
            val preferences =
                UserPreferences(
                    userId = userId,
                    activeCharacterMappingId = null,
                    lastGuildId = null,
                )

            // Then
            preferences.userId shouldBe userId
            preferences.activeCharacterMappingId shouldBe null
            preferences.lastGuildId shouldBe null
            preferences.id shouldBe null
            preferences.updatedAt shouldNotBe null
        }

        @Test
        fun `should create preferences with active character`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(42L)
            val guildId = GuildId("test-guild")

            // When
            val preferences =
                UserPreferences(
                    userId = userId,
                    activeCharacterMappingId = mappingId,
                    lastGuildId = guildId,
                )

            // Then
            preferences.activeCharacterMappingId shouldBe mappingId
            preferences.lastGuildId shouldBe guildId
        }

        @Test
        fun `should create preferences with explicit id`() {
            // Given
            val id = UserPreferencesId(100L)
            val userId = UserId(1L)

            // When
            val preferences =
                UserPreferences(
                    id = id,
                    userId = userId,
                    activeCharacterMappingId = null,
                    lastGuildId = null,
                )

            // Then
            preferences.id shouldBe id
            preferences.id?.value shouldBe 100L
        }
    }

    @Nested
    inner class FactoryMethodTests {
        @Test
        fun `create should create preferences with no active character`() {
            // Given
            val userId = UserId(1L)

            // When
            val preferences = UserPreferences.create(userId)

            // Then
            preferences.userId shouldBe userId
            preferences.activeCharacterMappingId shouldBe null
            preferences.lastGuildId shouldBe null
            preferences.id shouldBe null
        }

        @Test
        fun `create should create preferences with active character`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(42L)
            val guildId = GuildId("test-guild")

            // When
            val preferences = UserPreferences.create(userId, mappingId, guildId)

            // Then
            preferences.userId shouldBe userId
            preferences.activeCharacterMappingId shouldBe mappingId
            preferences.lastGuildId shouldBe guildId
            preferences.id shouldBe null
        }
    }

    @Nested
    inner class WithActiveCharacterTests {
        @Test
        fun `should update active character`() {
            // Given
            val original = UserPreferences.create(UserId(1L))
            val newMappingId = UserCharacterMappingId(42L)
            val newGuildId = GuildId("new-guild")

            // When
            val updated = original.withActiveCharacter(newMappingId, newGuildId)

            // Then
            updated.activeCharacterMappingId shouldBe newMappingId
            updated.lastGuildId shouldBe newGuildId
            updated.userId shouldBe original.userId
            updated.id shouldBe original.id
        }

        @Test
        fun `should update timestamp when setting active character`() {
            // Given
            val pastTime = Instant.parse("2024-01-01T00:00:00Z")
            val original =
                UserPreferences(
                    userId = UserId(1L),
                    activeCharacterMappingId = null,
                    lastGuildId = null,
                    updatedAt = pastTime,
                )

            // When
            val updated =
                original.withActiveCharacter(
                    UserCharacterMappingId(42L),
                    GuildId("new-guild"),
                )

            // Then
            updated.updatedAt.isAfter(pastTime) shouldBe true
        }

        @Test
        fun `should replace existing active character`() {
            // Given
            val original =
                UserPreferences.create(
                    UserId(1L),
                    UserCharacterMappingId(1L),
                    GuildId("old-guild"),
                )
            val newMappingId = UserCharacterMappingId(2L)
            val newGuildId = GuildId("new-guild")

            // When
            val updated = original.withActiveCharacter(newMappingId, newGuildId)

            // Then
            updated.activeCharacterMappingId shouldBe newMappingId
            updated.lastGuildId shouldBe newGuildId
        }
    }

    @Nested
    inner class ClearActiveCharacterTests {
        @Test
        fun `should clear active character`() {
            // Given
            val original =
                UserPreferences.create(
                    UserId(1L),
                    UserCharacterMappingId(42L),
                    GuildId("test-guild"),
                )

            // When
            val cleared = original.clearActiveCharacter()

            // Then
            cleared.activeCharacterMappingId shouldBe null
            cleared.lastGuildId shouldBe original.lastGuildId // lastGuildId is preserved
            cleared.userId shouldBe original.userId
        }

        @Test
        fun `should update timestamp when clearing active character`() {
            // Given
            val pastTime = Instant.parse("2024-01-01T00:00:00Z")
            val original =
                UserPreferences(
                    userId = UserId(1L),
                    activeCharacterMappingId = UserCharacterMappingId(42L),
                    lastGuildId = GuildId("test-guild"),
                    updatedAt = pastTime,
                )

            // When
            val cleared = original.clearActiveCharacter()

            // Then
            cleared.updatedAt.isAfter(pastTime) shouldBe true
        }

        @Test
        fun `should be idempotent on already cleared preferences`() {
            // Given
            val original = UserPreferences.create(UserId(1L))

            // When
            val cleared = original.clearActiveCharacter()

            // Then
            cleared.activeCharacterMappingId shouldBe null
        }
    }

    @Nested
    inner class UserPreferencesIdTests {
        @Test
        fun `should create valid UserPreferencesId with positive value`() {
            val id = UserPreferencesId(1L)
            id.value shouldBe 1L
        }

        @Test
        fun `should create UserPreferencesId with large value`() {
            val id = UserPreferencesId(Long.MAX_VALUE)
            id.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should throw exception for zero value`() {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    UserPreferencesId(0L)
                }
            exception.message shouldBe "UserPreferencesId must be positive, got 0"
        }

        @Test
        fun `should throw exception for negative value`() {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    UserPreferencesId(-1L)
                }
            exception.message shouldBe "UserPreferencesId must be positive, got -1"
        }

        @Test
        fun `should throw exception for large negative value`() {
            val exception =
                shouldThrow<IllegalArgumentException> {
                    UserPreferencesId(-100L)
                }
            exception.message shouldBe "UserPreferencesId must be positive, got -100"
        }

        @Test
        fun `should have value equality`() {
            val id1 = UserPreferencesId(42L)
            val id2 = UserPreferencesId(42L)
            id1 shouldBe id2
        }
    }

    @Nested
    inner class DataClassBehaviorTests {
        @Test
        fun `should support copy with modified userId`() {
            // Given
            val original = UserPreferences.create(UserId(1L))

            // When
            val copied = original.copy(userId = UserId(2L))

            // Then
            copied.userId.value shouldBe 2L
            copied.activeCharacterMappingId shouldBe original.activeCharacterMappingId
        }

        @Test
        fun `should have proper equals implementation`() {
            val timestamp = Instant.parse("2024-01-01T00:00:00Z")
            val pref1 =
                UserPreferences(
                    id = UserPreferencesId(1L),
                    userId = UserId(1L),
                    activeCharacterMappingId = UserCharacterMappingId(42L),
                    lastGuildId = GuildId("test"),
                    updatedAt = timestamp,
                )
            val pref2 =
                UserPreferences(
                    id = UserPreferencesId(1L),
                    userId = UserId(1L),
                    activeCharacterMappingId = UserCharacterMappingId(42L),
                    lastGuildId = GuildId("test"),
                    updatedAt = timestamp,
                )
            pref1 shouldBe pref2
        }
    }
}
