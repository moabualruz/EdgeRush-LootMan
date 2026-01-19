package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserPreferences
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.auth.repository.UserPreferencesRepository
import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
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
import java.time.OffsetDateTime

/**
 * Unit tests for GuildContextService.
 *
 * Tests verify:
 * - Getting user guilds
 * - Getting active guild context
 * - Setting active character
 * - Permission checking
 */
class GuildContextServiceTest : UnitTest() {
    private lateinit var userCharacterMappingRepository: UserCharacterMappingRepository
    private lateinit var raiderEntityRepository: RaiderEntityRepository
    private lateinit var guildPermissionRepository: GuildPermissionRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var guildConfigurationRepository: GuildConfigurationRepository
    private lateinit var service: GuildContextService

    @BeforeEach
    fun setup() {
        userCharacterMappingRepository = mockk()
        raiderEntityRepository = mockk()
        guildPermissionRepository = mockk()
        userPreferencesRepository = mockk()
        guildConfigurationRepository = mockk()

        service = GuildContextService(
            userCharacterMappingRepository,
            raiderEntityRepository,
            guildPermissionRepository,
            userPreferencesRepository,
            guildConfigurationRepository,
        )
    }

    @Nested
    inner class GetUserGuildsTests {
        @Test
        fun `should return empty list when user has no character mappings`() {
            // Given
            val userId = UserId(1L)
            every { userCharacterMappingRepository.findByUserId(userId) } returns emptyList()

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should return guild context for user with single character`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(10L)
            val raiderId = RaiderId(100L)
            val guildId = "test-guild"

            val mapping = createMapping(mappingId, userId, raiderId)
            val raider = createRaider(100L, guildId, "Officer")
            val permission = createPermission(guildId, "Officer", GuildPermissionType.SETTINGS_ACCESS)

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildConfigurationRepository.findByGuildId(guildId) } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(GuildId(guildId), "Officer") } returns listOf(permission)
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns mapOf(
                Pair(guildId, "Officer") to listOf(GuildPermissionType.SETTINGS_ACCESS)
            )

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldHaveSize 1
            result[0].guildId shouldBe guildId
            result[0].characterName shouldBe "TestCharacter"
            result[0].rank shouldBe "Officer"
            result[0].permissions shouldContain GuildPermissionType.SETTINGS_ACCESS
            result[0].isActive shouldBe false
        }

        @Test
        fun `should mark active character correctly`() {
            // Given
            val userId = UserId(1L)
            val activeMappingId = UserCharacterMappingId(10L)
            val raiderId = RaiderId(100L)
            val guildId = "test-guild"

            val mapping = createMapping(activeMappingId, userId, raiderId)
            val raider = createRaider(100L, guildId, "Officer")
            val preferences = UserPreferences.create(userId, activeMappingId, GuildId(guildId))

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns preferences
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildConfigurationRepository.findByGuildId(guildId) } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(GuildId(guildId), "Officer") } returns emptyList()
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns emptyMap()

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldHaveSize 1
            result[0].isActive shouldBe true
        }

        @Test
        fun `should return multiple guild contexts for user with multiple characters`() {
            // Given
            val userId = UserId(1L)
            val mapping1 = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val mapping2 = createMapping(UserCharacterMappingId(20L), userId, RaiderId(200L))
            val raider1 = createRaider(100L, "guild-1", "Officer")
            val raider2 = createRaider(200L, "guild-2", "Raider", "AltCharacter")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping1, mapping2)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns raider1
            every { raiderEntityRepository.findById(200L) } returns raider2
            every { guildConfigurationRepository.findByGuildId("guild-1") } returns null
            every { guildConfigurationRepository.findByGuildId("guild-2") } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(any(), any()) } returns emptyList()
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns emptyMap()

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldHaveSize 2
            result.map { it.guildId } shouldBe listOf("guild-1", "guild-2")
        }

        @Test
        fun `should skip mappings with missing raiders`() {
            // Given
            val userId = UserId(1L)
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns null

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should skip raiders without guild ID`() {
            // Given
            val userId = UserId(1L)
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, null, "Officer")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns raider

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should return empty permissions for raiders without rank`() {
            // Given
            val userId = UserId(1L)
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", null)

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildConfigurationRepository.findByGuildId("test-guild") } returns null

            // When
            val result = service.getUserGuilds(userId)

            // Then
            result shouldHaveSize 1
            result[0].permissions shouldBe emptyList()
            result[0].rank shouldBe null
        }
    }

    @Nested
    inner class GetActiveGuildContextTests {
        @Test
        fun `should return null when user has no guilds`() {
            // Given
            val userId = UserId(1L)
            every { userCharacterMappingRepository.findByUserId(userId) } returns emptyList()

            // When
            val result = service.getActiveGuildContext(userId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should return active context when one is set`() {
            // Given
            val userId = UserId(1L)
            val activeMappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(activeMappingId, userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Officer")
            val preferences = UserPreferences.create(userId, activeMappingId, GuildId("test-guild"))

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns preferences
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildConfigurationRepository.findByGuildId("test-guild") } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(any(), any()) } returns emptyList()
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns emptyMap()

            // When
            val result = service.getActiveGuildContext(userId)

            // Then
            result shouldBe result
            result?.isActive shouldBe true
        }

        @Test
        fun `should return first context when none is active`() {
            // Given
            val userId = UserId(1L)
            val mapping1 = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val mapping2 = createMapping(UserCharacterMappingId(20L), userId, RaiderId(200L))
            val raider1 = createRaider(100L, "guild-1", "Officer")
            val raider2 = createRaider(200L, "guild-2", "Raider")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping1, mapping2)
            every { userPreferencesRepository.findByUserId(userId) } returns null
            every { raiderEntityRepository.findById(100L) } returns raider1
            every { raiderEntityRepository.findById(200L) } returns raider2
            every { guildConfigurationRepository.findByGuildId(any()) } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(any(), any()) } returns emptyList()
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns emptyMap()

            // When
            val result = service.getActiveGuildContext(userId)

            // Then
            result?.guildId shouldBe "guild-1"
        }
    }

    @Nested
    inner class SetActiveCharacterTests {
        @Test
        fun `should set active character successfully`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(mappingId, userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Officer")
            val preferences = UserPreferences.create(userId, mappingId, GuildId("test-guild"))

            every { userCharacterMappingRepository.findById(mappingId) } returns mapping
            every { raiderEntityRepository.findById(100L) } returns raider
            every { userPreferencesRepository.updateActiveCharacter(userId, mappingId, GuildId("test-guild")) } returns preferences

            // For getActiveGuildContext call
            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns preferences
            every { guildConfigurationRepository.findByGuildId("test-guild") } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(any(), any()) } returns emptyList()
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns emptyMap()

            // When
            val result = service.setActiveCharacter(userId, mappingId)

            // Then
            result.guildId shouldBe "test-guild"
            verify { userPreferencesRepository.updateActiveCharacter(userId, mappingId, GuildId("test-guild")) }
        }

        @Test
        fun `should throw exception when mapping not found`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(999L)

            every { userCharacterMappingRepository.findById(mappingId) } returns null

            // When / Then
            shouldThrow<IllegalArgumentException> {
                service.setActiveCharacter(userId, mappingId)
            }.message shouldBe "Character mapping not found: 999"
        }

        @Test
        fun `should throw exception when mapping belongs to different user`() {
            // Given
            val userId = UserId(1L)
            val otherUserId = UserId(2L)
            val mappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(mappingId, otherUserId, RaiderId(100L))

            every { userCharacterMappingRepository.findById(mappingId) } returns mapping

            // When / Then
            shouldThrow<IllegalArgumentException> {
                service.setActiveCharacter(userId, mappingId)
            }.message shouldBe "Character mapping does not belong to user"
        }

        @Test
        fun `should throw exception when raider not found`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(mappingId, userId, RaiderId(100L))

            every { userCharacterMappingRepository.findById(mappingId) } returns mapping
            every { raiderEntityRepository.findById(100L) } returns null

            // When / Then
            shouldThrow<IllegalArgumentException> {
                service.setActiveCharacter(userId, mappingId)
            }.message shouldBe "Character not found for mapping"
        }

        @Test
        fun `should throw exception when raider has no guild`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(mappingId, userId, RaiderId(100L))
            val raider = createRaider(100L, null, "Officer")

            every { userCharacterMappingRepository.findById(mappingId) } returns mapping
            every { raiderEntityRepository.findById(100L) } returns raider

            // When / Then
            shouldThrow<IllegalArgumentException> {
                service.setActiveCharacter(userId, mappingId)
            }.message shouldBe "Character is not associated with a guild"
        }
    }

    @Nested
    inner class HasGuildPermissionTests {
        @Test
        fun `should return true when user has permission`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Officer")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildPermissionRepository.hasPermission(guildId, "Officer", GuildPermissionType.SETTINGS_ACCESS) } returns true

            // When
            val result = service.hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when user has no character in guild`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "other-guild", "Officer")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider

            // When
            val result = service.hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe false
        }

        @Test
        fun `should return false when user has no character mappings`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")

            every { userCharacterMappingRepository.findByUserId(userId) } returns emptyList()

            // When
            val result = service.hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe false
        }

        @Test
        fun `should return false when character has no rank`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", null)

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider

            // When
            val result = service.hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe false
        }

        @Test
        fun `should return false when rank does not have permission`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Raider")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildPermissionRepository.hasPermission(guildId, "Raider", GuildPermissionType.SETTINGS_ACCESS) } returns false

            // When
            val result = service.hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class IsGuildOfficerTests {
        @Test
        fun `should return true when user has SETTINGS_ACCESS permission`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Officer")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildPermissionRepository.hasPermission(guildId, "Officer", GuildPermissionType.SETTINGS_ACCESS) } returns true

            // When
            val result = service.isGuildOfficer(userId, guildId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when user does not have SETTINGS_ACCESS permission`() {
            // Given
            val userId = UserId(1L)
            val guildId = GuildId("test-guild")
            val mapping = createMapping(UserCharacterMappingId(10L), userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Raider")

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildPermissionRepository.hasPermission(guildId, "Raider", GuildPermissionType.SETTINGS_ACCESS) } returns false

            // When
            val result = service.isGuildOfficer(userId, guildId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class HasActiveGuildPermissionTests {
        @Test
        fun `should return true when active guild has permission`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(10L)
            val mapping = createMapping(mappingId, userId, RaiderId(100L))
            val raider = createRaider(100L, "test-guild", "Officer")
            val preferences = UserPreferences.create(userId, mappingId, GuildId("test-guild"))
            val permission = createPermission("test-guild", "Officer", GuildPermissionType.SETTINGS_ACCESS)

            every { userCharacterMappingRepository.findByUserId(userId) } returns listOf(mapping)
            every { userPreferencesRepository.findByUserId(userId) } returns preferences
            every { raiderEntityRepository.findById(100L) } returns raider
            every { guildConfigurationRepository.findByGuildId("test-guild") } returns null
            every { guildPermissionRepository.findByGuildIdAndRankName(GuildId("test-guild"), "Officer") } returns listOf(permission)
            every { guildPermissionRepository.findByGuildIdAndRankNames(any()) } returns mapOf(
                Pair("test-guild", "Officer") to listOf(GuildPermissionType.SETTINGS_ACCESS)
            )

            // When
            val result = service.hasActiveGuildPermission(userId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when no active guild context`() {
            // Given
            val userId = UserId(1L)
            every { userCharacterMappingRepository.findByUserId(userId) } returns emptyList()

            // When
            val result = service.hasActiveGuildPermission(userId, GuildPermissionType.SETTINGS_ACCESS)

            // Then
            result shouldBe false
        }
    }

    // Helper methods
    private fun createMapping(
        id: UserCharacterMappingId,
        userId: UserId,
        raiderId: RaiderId,
    ): UserCharacterMapping = UserCharacterMapping(
        id = id,
        userId = userId,
        raiderId = raiderId,
    )

    private fun createRaider(
        id: Long,
        guildId: String?,
        rank: String?,
        name: String = "TestCharacter",
    ): RaiderEntity = RaiderEntity(
        id = id,
        characterName = name,
        realm = "TestRealm",
        region = "EU",
        guildId = guildId,
        wowauditId = null,
        clazz = "WARRIOR",
        spec = "Arms",
        role = "DPS",
        rank = rank,
        status = "Active",
        note = null,
        blizzardId = null,
        trackingSince = null,
        joinDate = null,
        blizzardLastModified = null,
        lastSync = OffsetDateTime.now(),
    )

    private fun createPermission(
        guildId: String,
        rankName: String,
        permissionType: GuildPermissionType,
    ): GuildPermission = GuildPermission(
        id = GuildPermissionId(1L),
        guildId = GuildId(guildId),
        rankName = rankName,
        permissionType = permissionType,
    )
}
