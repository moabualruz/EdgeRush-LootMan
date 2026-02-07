package com.edgerush.lootman.api.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

/**
 * Unit tests for GuildPermissionController.
 */
class GuildPermissionControllerTest : UnitTest() {
    private lateinit var guildPermissionRepository: GuildPermissionRepository
    private lateinit var guildContextService: GuildContextService
    private lateinit var userIdExtractor: UserIdExtractor
    private lateinit var controller: GuildPermissionController

    private val testUserId = UserId(1L)
    private val testAuth = "Bearer test-token"
    private val testGuildId = "test-guild"

    @BeforeEach
    fun setup() {
        guildPermissionRepository = mockk()
        guildContextService = mockk()
        userIdExtractor = mockk()
        controller =
            GuildPermissionController(
                guildPermissionRepository,
                guildContextService,
                userIdExtractor,
            )

        every { userIdExtractor.extractUserId(testAuth) } returns testUserId
    }

    @Nested
    inner class GetPermissionsTests {
        @Test
        fun `should return permissions when user has settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val permissions =
                listOf(
                    createPermission(1L, "Officer", GuildPermissionType.SETTINGS_ACCESS),
                    createPermission(2L, "Officer", GuildPermissionType.LOOT_MANAGEMENT),
                )
            every { guildPermissionRepository.findByGuildId(GuildId(testGuildId)) } returns permissions

            // When
            val result = controller.getPermissions(testGuildId, testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize 2
            result.body?.get(0)?.rankName shouldBe "Officer"
            result.body?.get(0)?.permissionType shouldBe "SETTINGS_ACCESS"
        }

        @Test
        fun `should throw 403 when user has no settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns false

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.getPermissions(testGuildId, testAuth)
                }
            exception.statusCode shouldBe HttpStatus.FORBIDDEN
        }

        @Test
        fun `should return empty list when no permissions configured`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true
            every { guildPermissionRepository.findByGuildId(GuildId(testGuildId)) } returns emptyList()

            // When
            val result = controller.getPermissions(testGuildId, testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize 0
        }
    }

    @Nested
    inner class GetRanksWithPermissionsTests {
        @Test
        fun `should return ranks with permissions`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true
            every {
                guildPermissionRepository.findDistinctRankNamesByGuildId(GuildId(testGuildId))
            } returns listOf("Guild Master", "Officer", "Raider")

            // When
            val result = controller.getRanksWithPermissions(testGuildId, testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize 3
            result.body shouldContain "Guild Master"
            result.body shouldContain "Officer"
            result.body shouldContain "Raider"
        }

        @Test
        fun `should throw 403 when user has no settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns false

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.getRanksWithPermissions(testGuildId, testAuth)
                }
            exception.statusCode shouldBe HttpStatus.FORBIDDEN
        }
    }

    @Nested
    inner class AddPermissionTests {
        @Test
        fun `should add permission when user has settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val request =
                AddPermissionRequest(
                    rankName = "Raider",
                    permissionType = "VIEW_ALL_SCORES",
                )

            val savedPermission = createPermission(1L, "Raider", GuildPermissionType.VIEW_ALL_SCORES)
            every { guildPermissionRepository.save(any()) } returns savedPermission

            // When
            val result = controller.addPermission(testGuildId, testAuth, request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.rankName shouldBe "Raider"
            result.body?.permissionType shouldBe "VIEW_ALL_SCORES"
        }

        @Test
        fun `should throw 403 when user has no settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns false

            val request =
                AddPermissionRequest(
                    rankName = "Raider",
                    permissionType = "VIEW_ALL_SCORES",
                )

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.addPermission(testGuildId, testAuth, request)
                }
            exception.statusCode shouldBe HttpStatus.FORBIDDEN
        }

        @Test
        fun `should throw 400 for invalid permission type`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val request =
                AddPermissionRequest(
                    rankName = "Raider",
                    permissionType = "INVALID_TYPE",
                )

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.addPermission(testGuildId, testAuth, request)
                }
            exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            exception.reason shouldBe "Invalid permission type: INVALID_TYPE"
        }

        @Test
        fun `should call repository save with correct permission`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val request =
                AddPermissionRequest(
                    rankName = "Officer",
                    permissionType = "LOOT_MANAGEMENT",
                )

            val savedPermission = createPermission(1L, "Officer", GuildPermissionType.LOOT_MANAGEMENT)
            every { guildPermissionRepository.save(any()) } returns savedPermission

            // When
            controller.addPermission(testGuildId, testAuth, request)

            // Then
            verify(exactly = 1) {
                guildPermissionRepository.save(
                    match { permission ->
                        permission.guildId.value == testGuildId &&
                            permission.rankName == "Officer" &&
                            permission.permissionType == GuildPermissionType.LOOT_MANAGEMENT
                    },
                )
            }
        }
    }

    @Nested
    inner class RemovePermissionTests {
        @Test
        fun `should remove permission when found and user has access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val permission = createPermission(1L, "Officer", GuildPermissionType.SETTINGS_ACCESS)
            every { guildPermissionRepository.findById(GuildPermissionId(1L)) } returns permission
            every { guildPermissionRepository.deleteById(GuildPermissionId(1L)) } returns Unit

            // When
            val result = controller.removePermission(testGuildId, 1L, testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            verify(exactly = 1) { guildPermissionRepository.deleteById(GuildPermissionId(1L)) }
        }

        @Test
        fun `should throw 403 when user has no settings access`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns false

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.removePermission(testGuildId, 1L, testAuth)
                }
            exception.statusCode shouldBe HttpStatus.FORBIDDEN
        }

        @Test
        fun `should throw 404 when permission not found`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true
            every { guildPermissionRepository.findById(GuildPermissionId(999L)) } returns null

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.removePermission(testGuildId, 999L, testAuth)
                }
            exception.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        @Test
        fun `should throw 404 when permission belongs to different guild`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val permission =
                GuildPermission(
                    id = GuildPermissionId(1L),
                    guildId = GuildId("different-guild"),
                    rankName = "Officer",
                    permissionType = GuildPermissionType.SETTINGS_ACCESS,
                )
            every { guildPermissionRepository.findById(GuildPermissionId(1L)) } returns permission

            // When & Then
            val exception =
                shouldThrow<ResponseStatusException> {
                    controller.removePermission(testGuildId, 1L, testAuth)
                }
            exception.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetPermissionTypesTests {
        @Test
        fun `should return all permission types`() {
            // When
            val result = controller.getPermissionTypes()

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize GuildPermissionType.entries.size
        }

        @Test
        fun `should include SETTINGS_ACCESS type with description`() {
            // When
            val result = controller.getPermissionTypes()

            // Then
            val settingsType = result.body?.find { it.name == "SETTINGS_ACCESS" }
            settingsType?.description shouldBe "Access to guild settings page"
        }

        @Test
        fun `should include LOOT_MANAGEMENT type with description`() {
            // When
            val result = controller.getPermissionTypes()

            // Then
            val lootType = result.body?.find { it.name == "LOOT_MANAGEMENT" }
            lootType?.description shouldBe "Manage loot distribution"
        }

        @Test
        fun `should include MEMBER_MANAGEMENT type with description`() {
            // When
            val result = controller.getPermissionTypes()

            // Then
            val memberType = result.body?.find { it.name == "MEMBER_MANAGEMENT" }
            memberType?.description shouldBe "Manage guild members"
        }

        @Test
        fun `should include VIEW_ALL_SCORES type with description`() {
            // When
            val result = controller.getPermissionTypes()

            // Then
            val scoresType = result.body?.find { it.name == "VIEW_ALL_SCORES" }
            scoresType?.description shouldBe "View all member FLPS scores"
        }

        @Test
        fun `should not require authentication`() {
            // Note: This endpoint doesn't call userIdExtractor or check permissions
            // When
            val result = controller.getPermissionTypes()

            // Then
            result.statusCode shouldBe HttpStatus.OK
            // No verification of userIdExtractor - it shouldn't be called
        }
    }

    @Nested
    inner class ResponseMappingTests {
        @Test
        fun `should map permission to response correctly`() {
            // Given
            every {
                guildContextService.hasGuildPermission(testUserId, GuildId(testGuildId), GuildPermissionType.SETTINGS_ACCESS)
            } returns true

            val permission =
                GuildPermission(
                    id = GuildPermissionId(42L),
                    guildId = GuildId(testGuildId),
                    rankName = "Guild Master",
                    permissionType = GuildPermissionType.LOOT_MANAGEMENT,
                    createdAt = Instant.parse("2024-01-15T10:30:00Z"),
                )
            every { guildPermissionRepository.findByGuildId(GuildId(testGuildId)) } returns listOf(permission)

            // When
            val result = controller.getPermissions(testGuildId, testAuth)

            // Then
            val response = result.body?.first()
            response?.id shouldBe 42L
            response?.guildId shouldBe testGuildId
            response?.rankName shouldBe "Guild Master"
            response?.permissionType shouldBe "LOOT_MANAGEMENT"
            response?.createdAt shouldBe "2024-01-15T10:30:00Z"
        }
    }

    // Helper method to create test permissions
    private fun createPermission(
        id: Long,
        rankName: String,
        permissionType: GuildPermissionType,
        guildId: String = testGuildId,
    ): GuildPermission =
        GuildPermission(
            id = GuildPermissionId(id),
            guildId = GuildId(guildId),
            rankName = rankName,
            permissionType = permissionType,
            createdAt = Instant.now(),
        )
}
