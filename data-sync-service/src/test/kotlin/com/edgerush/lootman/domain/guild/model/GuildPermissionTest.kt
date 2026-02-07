package com.edgerush.lootman.domain.guild.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for GuildPermission domain model.
 *
 * Tests verify:
 * - Permission creation and validation
 * - Factory methods
 * - Default rank lists
 * - GuildPermissionId value object
 */
class GuildPermissionTest : UnitTest() {
    @Nested
    inner class GuildPermissionCreation {
        @Test
        fun `should create permission with valid data`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Officer"
            val permissionType = GuildPermissionType.SETTINGS_ACCESS

            // When
            val permission =
                GuildPermission(
                    guildId = guildId,
                    rankName = rankName,
                    permissionType = permissionType,
                )

            // Then
            permission.guildId shouldBe guildId
            permission.rankName shouldBe "Officer"
            permission.permissionType shouldBe GuildPermissionType.SETTINGS_ACCESS
            permission.id shouldBe null
            permission.createdAt shouldNotBe null
        }

        @Test
        fun `should create permission with explicit id`() {
            // Given
            val id = GuildPermissionId(42L)
            val guildId = GuildId("test-guild")

            // When
            val permission =
                GuildPermission(
                    id = id,
                    guildId = guildId,
                    rankName = "Guild Master",
                    permissionType = GuildPermissionType.LOOT_MANAGEMENT,
                )

            // Then
            permission.id shouldBe id
            permission.id?.value shouldBe 42L
        }

        @Test
        fun `should throw exception for blank rank name`() {
            // When / Then
            shouldThrow<IllegalArgumentException> {
                GuildPermission(
                    guildId = GuildId("test-guild"),
                    rankName = "",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                )
            }.message shouldBe "Rank name cannot be blank"
        }

        @Test
        fun `should throw exception for whitespace-only rank name`() {
            // When / Then
            shouldThrow<IllegalArgumentException> {
                GuildPermission(
                    guildId = GuildId("test-guild"),
                    rankName = "   ",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                )
            }.message shouldBe "Rank name cannot be blank"
        }

        @Test
        fun `should accept rank names with leading or trailing whitespace`() {
            // Given - note: factory method trims, but direct constructor does not
            val permission =
                GuildPermission(
                    guildId = GuildId("test-guild"),
                    rankName = "  Officer  ",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                )

            // Then - whitespace is preserved when using constructor directly
            permission.rankName shouldBe "  Officer  "
        }
    }

    @Nested
    inner class FactoryMethodTests {
        @Test
        fun `create should trim rank name`() {
            // When
            val permission =
                GuildPermission.create(
                    guildId = GuildId("test-guild"),
                    rankName = "  Raider  ",
                    permissionType = GuildPermissionType.VIEW_ALL_SCORES,
                )

            // Then
            permission.rankName shouldBe "Raider"
        }

        @Test
        fun `create should set createdAt to current time`() {
            // Given
            val before = Instant.now()

            // When
            val permission =
                GuildPermission.create(
                    guildId = GuildId("test-guild"),
                    rankName = "Member",
                    permissionType = GuildPermissionType.MEMBER_MANAGEMENT,
                )

            val after = Instant.now()

            // Then
            permission.createdAt.isAfter(before.minusMillis(1)) shouldBe true
            permission.createdAt.isBefore(after.plusMillis(1)) shouldBe true
        }

        @Test
        fun `create should set id to null`() {
            // When
            val permission =
                GuildPermission.create(
                    guildId = GuildId("test-guild"),
                    rankName = "Member",
                    permissionType = GuildPermissionType.VIEW_ALL_SCORES,
                )

            // Then
            permission.id shouldBe null
        }
    }

    @Nested
    inner class DefaultRanksTests {
        @Test
        fun `DEFAULT_SETTINGS_RANKS should contain Guild Master`() {
            GuildPermission.DEFAULT_SETTINGS_RANKS shouldContain "Guild Master"
        }

        @Test
        fun `DEFAULT_SETTINGS_RANKS should contain Officer`() {
            GuildPermission.DEFAULT_SETTINGS_RANKS shouldContain "Officer"
        }

        @Test
        fun `DEFAULT_LOOT_MANAGEMENT_RANKS should contain Guild Master`() {
            GuildPermission.DEFAULT_LOOT_MANAGEMENT_RANKS shouldContain "Guild Master"
        }

        @Test
        fun `DEFAULT_LOOT_MANAGEMENT_RANKS should contain Officer`() {
            GuildPermission.DEFAULT_LOOT_MANAGEMENT_RANKS shouldContain "Officer"
        }

        @Test
        fun `DEFAULT_SETTINGS_RANKS should have exactly 2 entries`() {
            GuildPermission.DEFAULT_SETTINGS_RANKS.size shouldBe 2
        }

        @Test
        fun `DEFAULT_LOOT_MANAGEMENT_RANKS should have exactly 2 entries`() {
            GuildPermission.DEFAULT_LOOT_MANAGEMENT_RANKS.size shouldBe 2
        }
    }

    @Nested
    inner class PermissionTypeTests {
        @Test
        fun `should create permission with SETTINGS_ACCESS type`() {
            val permission = createPermission(permissionType = GuildPermissionType.SETTINGS_ACCESS)
            permission.permissionType shouldBe GuildPermissionType.SETTINGS_ACCESS
        }

        @Test
        fun `should create permission with LOOT_MANAGEMENT type`() {
            val permission = createPermission(permissionType = GuildPermissionType.LOOT_MANAGEMENT)
            permission.permissionType shouldBe GuildPermissionType.LOOT_MANAGEMENT
        }

        @Test
        fun `should create permission with MEMBER_MANAGEMENT type`() {
            val permission = createPermission(permissionType = GuildPermissionType.MEMBER_MANAGEMENT)
            permission.permissionType shouldBe GuildPermissionType.MEMBER_MANAGEMENT
        }

        @Test
        fun `should create permission with VIEW_ALL_SCORES type`() {
            val permission = createPermission(permissionType = GuildPermissionType.VIEW_ALL_SCORES)
            permission.permissionType shouldBe GuildPermissionType.VIEW_ALL_SCORES
        }
    }

    @Nested
    inner class GuildPermissionIdTests {
        @Test
        fun `should create valid GuildPermissionId with positive value`() {
            val id = GuildPermissionId(1L)
            id.value shouldBe 1L
        }

        @Test
        fun `should create GuildPermissionId with large value`() {
            val id = GuildPermissionId(Long.MAX_VALUE)
            id.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should throw exception for zero value`() {
            shouldThrow<IllegalArgumentException> {
                GuildPermissionId(0L)
            }.message shouldBe "GuildPermissionId must be positive"
        }

        @Test
        fun `should throw exception for negative value`() {
            shouldThrow<IllegalArgumentException> {
                GuildPermissionId(-1L)
            }.message shouldBe "GuildPermissionId must be positive"
        }

        @Test
        fun `should throw exception for large negative value`() {
            shouldThrow<IllegalArgumentException> {
                GuildPermissionId(-100L)
            }.message shouldBe "GuildPermissionId must be positive"
        }

        @Test
        fun `should have value equality`() {
            val id1 = GuildPermissionId(42L)
            val id2 = GuildPermissionId(42L)
            id1 shouldBe id2
        }

        @Test
        fun `should have value inequality`() {
            val id1 = GuildPermissionId(1L)
            val id2 = GuildPermissionId(2L)
            (id1 == id2) shouldBe false
        }
    }

    @Nested
    inner class DataClassBehaviorTests {
        @Test
        fun `should support copy with modified guildId`() {
            // Given
            val original = createPermission(guildId = GuildId("guild-1"))

            // When
            val copied = original.copy(guildId = GuildId("guild-2"))

            // Then
            copied.guildId.value shouldBe "guild-2"
            copied.rankName shouldBe original.rankName
            copied.permissionType shouldBe original.permissionType
        }

        @Test
        fun `should support copy with modified rankName`() {
            // Given
            val original = createPermission(rankName = "Officer")

            // When
            val copied = original.copy(rankName = "Raider")

            // Then
            copied.rankName shouldBe "Raider"
            copied.guildId shouldBe original.guildId
        }

        @Test
        fun `should have proper equals implementation`() {
            val perm1 =
                GuildPermission(
                    id = GuildPermissionId(1L),
                    guildId = GuildId("test"),
                    rankName = "Officer",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                )
            val perm2 =
                GuildPermission(
                    id = GuildPermissionId(1L),
                    guildId = GuildId("test"),
                    rankName = "Officer",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                )
            perm1 shouldBe perm2
        }
    }

    // Helper method
    private fun createPermission(
        id: GuildPermissionId? = null,
        guildId: GuildId = GuildId("test-guild"),
        rankName: String = "Officer",
        permissionType: GuildPermissionType = GuildPermissionType.SETTINGS_ACCESS,
        createdAt: Instant = Instant.now(),
    ): GuildPermission =
        GuildPermission(
            id = id,
            guildId = guildId,
            rankName = rankName,
            permissionType = permissionType,
            createdAt = createdAt,
        )
}
