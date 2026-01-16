package com.edgerush.lootman.api.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.guild.GuildContext
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
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

/**
 * Unit tests for GuildContextController.
 */
class GuildContextControllerTest : UnitTest() {
    private lateinit var guildContextService: GuildContextService
    private lateinit var userIdExtractor: UserIdExtractor
    private lateinit var controller: GuildContextController

    private val testUserId = UserId(1L)
    private val testAuth = "Bearer test-token"

    @BeforeEach
    fun setup() {
        guildContextService = mockk()
        userIdExtractor = mockk()
        controller = GuildContextController(guildContextService, userIdExtractor)

        every { userIdExtractor.extractUserId(testAuth) } returns testUserId
    }

    @Nested
    inner class GetUserGuildsTests {
        @Test
        fun `should return user guilds`() {
            // Given
            val contexts = listOf(
                createGuildContext(
                    guildId = "guild-1",
                    guildName = "First Guild",
                    characterName = "TestChar",
                    isActive = true,
                ),
                createGuildContext(
                    guildId = "guild-2",
                    guildName = "Second Guild",
                    characterName = "AltChar",
                    isActive = false,
                ),
            )
            every { guildContextService.getUserGuilds(testUserId) } returns contexts

            // When
            val result = controller.getUserGuilds(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize 2
            result.body?.get(0)?.guildId shouldBe "guild-1"
            result.body?.get(0)?.guildName shouldBe "First Guild"
            result.body?.get(0)?.isActive shouldBe true
            result.body?.get(1)?.guildId shouldBe "guild-2"
            result.body?.get(1)?.isActive shouldBe false
        }

        @Test
        fun `should return empty list when user has no guilds`() {
            // Given
            every { guildContextService.getUserGuilds(testUserId) } returns emptyList()

            // When
            val result = controller.getUserGuilds(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body shouldHaveSize 0
        }

        @Test
        fun `should include permissions in response`() {
            // Given
            val context = createGuildContext(
                permissions = listOf(
                    GuildPermissionType.SETTINGS_ACCESS,
                    GuildPermissionType.LOOT_MANAGEMENT,
                ),
            )
            every { guildContextService.getUserGuilds(testUserId) } returns listOf(context)

            // When
            val result = controller.getUserGuilds(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.get(0)?.permissions shouldHaveSize 2
            result.body?.get(0)?.permissions shouldContain "SETTINGS_ACCESS"
            result.body?.get(0)?.permissions shouldContain "LOOT_MANAGEMENT"
        }

        @Test
        fun `should extract user ID from authorization header`() {
            // Given
            every { guildContextService.getUserGuilds(testUserId) } returns emptyList()

            // When
            controller.getUserGuilds(testAuth)

            // Then
            verify(exactly = 1) { userIdExtractor.extractUserId(testAuth) }
        }
    }

    @Nested
    inner class GetActiveGuildContextTests {
        @Test
        fun `should return active guild context`() {
            // Given
            val context = createGuildContext(
                guildId = "active-guild",
                guildName = "Active Guild",
                characterName = "MainChar",
                rank = "Officer",
                isActive = true,
            )
            every { guildContextService.getActiveGuildContext(testUserId) } returns context

            // When
            val result = controller.getActiveGuildContext(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.guildId shouldBe "active-guild"
            result.body?.characterName shouldBe "MainChar"
            result.body?.rank shouldBe "Officer"
            result.body?.isActive shouldBe true
        }

        @Test
        fun `should return no content when no active context`() {
            // Given
            every { guildContextService.getActiveGuildContext(testUserId) } returns null

            // When
            val result = controller.getActiveGuildContext(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
        }

        @Test
        fun `should include all character details in response`() {
            // Given
            val context = createGuildContext(
                characterName = "Thrall",
                characterRealm = "Tarren Mill",
                characterClass = "Shaman",
                characterMappingId = 42L,
                raiderId = 100L,
            )
            every { guildContextService.getActiveGuildContext(testUserId) } returns context

            // When
            val result = controller.getActiveGuildContext(testAuth)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.characterName shouldBe "Thrall"
            result.body?.characterRealm shouldBe "Tarren Mill"
            result.body?.characterClass shouldBe "Shaman"
            result.body?.characterMappingId shouldBe 42L
            result.body?.raiderId shouldBe 100L
        }
    }

    @Nested
    inner class SetActiveCharacterTests {
        @Test
        fun `should set active character and return context`() {
            // Given
            val mappingId = 42L
            val request = SetActiveCharacterRequest(characterMappingId = mappingId)
            val context = createGuildContext(
                guildId = "guild-1",
                characterName = "NewActive",
                characterMappingId = mappingId,
                isActive = true,
            )
            every {
                guildContextService.setActiveCharacter(testUserId, UserCharacterMappingId(mappingId))
            } returns context

            // When
            val result = controller.setActiveCharacter(testAuth, request)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.characterMappingId shouldBe mappingId
            result.body?.isActive shouldBe true
        }

        @Test
        fun `should call service with correct mapping ID`() {
            // Given
            val mappingId = 123L
            val request = SetActiveCharacterRequest(characterMappingId = mappingId)
            val context = createGuildContext(characterMappingId = mappingId)
            every {
                guildContextService.setActiveCharacter(testUserId, UserCharacterMappingId(mappingId))
            } returns context

            // When
            controller.setActiveCharacter(testAuth, request)

            // Then
            verify(exactly = 1) {
                guildContextService.setActiveCharacter(testUserId, UserCharacterMappingId(mappingId))
            }
        }

        @Test
        fun `should return guild and permissions after switch`() {
            // Given
            val request = SetActiveCharacterRequest(characterMappingId = 42L)
            val context = createGuildContext(
                guildId = "switched-guild",
                guildName = "Switched Guild",
                permissions = listOf(GuildPermissionType.SETTINGS_ACCESS),
            )
            every {
                guildContextService.setActiveCharacter(testUserId, any())
            } returns context

            // When
            val result = controller.setActiveCharacter(testAuth, request)

            // Then
            result.statusCode shouldBe HttpStatus.OK
            result.body?.guildId shouldBe "switched-guild"
            result.body?.guildName shouldBe "Switched Guild"
            result.body?.permissions shouldContain "SETTINGS_ACCESS"
        }
    }

    @Nested
    inner class ResponseMappingTests {
        @Test
        fun `should map all GuildContext fields to response`() {
            // Given
            val context = GuildContext(
                guildId = "test-guild",
                guildName = "Test Guild",
                characterName = "TestChar",
                characterRealm = "Tarren Mill",
                characterClass = "Warrior",
                characterMappingId = 1L,
                raiderId = 10L,
                rank = "Guild Master",
                permissions = listOf(
                    GuildPermissionType.SETTINGS_ACCESS,
                    GuildPermissionType.LOOT_MANAGEMENT,
                    GuildPermissionType.MEMBER_MANAGEMENT,
                ),
                isActive = true,
            )
            every { guildContextService.getUserGuilds(testUserId) } returns listOf(context)

            // When
            val result = controller.getUserGuilds(testAuth)

            // Then
            val response = result.body?.first()
            response?.guildId shouldBe "test-guild"
            response?.guildName shouldBe "Test Guild"
            response?.characterName shouldBe "TestChar"
            response?.characterRealm shouldBe "Tarren Mill"
            response?.characterClass shouldBe "Warrior"
            response?.characterMappingId shouldBe 1L
            response?.raiderId shouldBe 10L
            response?.rank shouldBe "Guild Master"
            response?.permissions shouldHaveSize 3
            response?.isActive shouldBe true
        }

        @Test
        fun `should handle null rank in response`() {
            // Given
            val context = createGuildContext(rank = null)
            every { guildContextService.getActiveGuildContext(testUserId) } returns context

            // When
            val result = controller.getActiveGuildContext(testAuth)

            // Then
            result.body?.rank shouldBe null
        }

        @Test
        fun `should map empty permissions list`() {
            // Given
            val context = createGuildContext(permissions = emptyList())
            every { guildContextService.getUserGuilds(testUserId) } returns listOf(context)

            // When
            val result = controller.getUserGuilds(testAuth)

            // Then
            result.body?.first()?.permissions shouldHaveSize 0
        }
    }

    // Helper method to create test GuildContext
    private fun createGuildContext(
        guildId: String = "test-guild",
        guildName: String = "Test Guild",
        characterName: String = "TestChar",
        characterRealm: String = "Test Realm",
        characterClass: String = "Warrior",
        characterMappingId: Long = 1L,
        raiderId: Long = 1L,
        rank: String? = "Member",
        permissions: List<GuildPermissionType> = emptyList(),
        isActive: Boolean = false,
    ): GuildContext = GuildContext(
        guildId = guildId,
        guildName = guildName,
        characterName = characterName,
        characterRealm = characterRealm,
        characterClass = characterClass,
        characterMappingId = characterMappingId,
        raiderId = raiderId,
        rank = rank,
        permissions = permissions,
        isActive = isActive,
    )
}
