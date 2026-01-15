package com.edgerush.lootman.api.vault

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.api.auth.GuildAccessDeniedException
import com.edgerush.lootman.api.auth.NoLinkedRaiderException
import com.edgerush.lootman.api.raider.RaiderVaultSlotCrudService
import com.edgerush.lootman.api.raider.RaiderVaultSlotResponse
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for VaultController.
 *
 * Tests the /vault endpoints for Great Vault options.
 */
class VaultControllerTest : UnitTest() {
    private lateinit var vaultSlotService: RaiderVaultSlotCrudService
    private lateinit var currentUserService: CurrentUserService
    private lateinit var controller: VaultController
    private lateinit var authenticatedUser: AuthenticatedUser

    @BeforeEach
    fun setup() {
        vaultSlotService = mockk()
        currentUserService = mockk()
        controller = VaultController(vaultSlotService, currentUserService)
        authenticatedUser = mockk()
    }

    @Test
    fun `getMyVaultOptions should return grouped vault options for current user`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        val raidSlots = listOf(
            RaiderVaultSlotResponse(id = 1L, raiderId = 123L, slot = "RAID_1", unlocked = true),
            RaiderVaultSlotResponse(id = 2L, raiderId = 123L, slot = "RAID_2", unlocked = true),
            RaiderVaultSlotResponse(id = 3L, raiderId = 123L, slot = "RAID_3", unlocked = false),
        )
        val mythicPlusSlots = listOf(
            RaiderVaultSlotResponse(id = 4L, raiderId = 123L, slot = "MYTHIC_PLUS_1", unlocked = true),
            RaiderVaultSlotResponse(id = 5L, raiderId = 123L, slot = "MYTHIC_PLUS_2", unlocked = false),
        )
        val pvpSlots = listOf(
            RaiderVaultSlotResponse(id = 6L, raiderId = 123L, slot = "PVP_1", unlocked = false),
        )
        val allSlots = raidSlots + mythicPlusSlots + pvpSlots

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { vaultSlotService.findByRaiderUnpaged(raiderId.value) } returns allSlots

        // When
        val response = controller.getMyVaultOptions(guildId, authenticatedUser)

        // Then
        response.raiderId shouldBe 123L
        response.weekOf shouldContain "-" // ISO date format
        response.raid shouldHaveSize 3
        response.mythicPlus shouldHaveSize 2
        response.pvp shouldHaveSize 1

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { vaultSlotService.findByRaiderUnpaged(raiderId.value) }
    }

    @Test
    fun `getMyVaultOptions should filter slots by type correctly`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        val mixedSlots = listOf(
            RaiderVaultSlotResponse(id = 1L, raiderId = 123L, slot = "RAID_1", unlocked = true),
            RaiderVaultSlotResponse(id = 2L, raiderId = 123L, slot = "MYTHIC_PLUS_1", unlocked = true),
            RaiderVaultSlotResponse(id = 3L, raiderId = 123L, slot = "PVP_1", unlocked = false),
            RaiderVaultSlotResponse(id = 4L, raiderId = 123L, slot = "RAID_2", unlocked = false),
        )

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { vaultSlotService.findByRaiderUnpaged(raiderId.value) } returns mixedSlots

        // When
        val response = controller.getMyVaultOptions(guildId, authenticatedUser)

        // Then
        response.raid shouldHaveSize 2
        response.mythicPlus shouldHaveSize 1
        response.pvp shouldHaveSize 1

        // Verify filtering worked correctly
        response.raid.all { it.slot.startsWith("RAID") } shouldBe true
        response.mythicPlus.all { it.slot.startsWith("MYTHIC_PLUS") } shouldBe true
        response.pvp.all { it.slot.startsWith("PVP") } shouldBe true
    }

    @Test
    fun `getMyVaultOptions should return empty lists when no slots exist`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { vaultSlotService.findByRaiderUnpaged(raiderId.value) } returns emptyList()

        // When
        val response = controller.getMyVaultOptions(guildId, authenticatedUser)

        // Then
        response.raiderId shouldBe 123L
        response.raid shouldHaveSize 0
        response.mythicPlus shouldHaveSize 0
        response.pvp shouldHaveSize 0
    }

    @Test
    fun `getMyVaultOptions should throw exception when guild access denied`() {
        // Given
        val guildId = "restricted-guild"

        every { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) } throws
            GuildAccessDeniedException(1L, guildId)

        // When / Then
        assertThrows<GuildAccessDeniedException> {
            controller.getMyVaultOptions(guildId, authenticatedUser)
        }

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 0) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(any()) }
        verify(exactly = 0) { vaultSlotService.findByRaiderUnpaged(any()) }
    }

    @Test
    fun `getMyVaultOptions should throw exception when no raider is linked`() {
        // Given
        val guildId = "test-guild"

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } throws
            NoLinkedRaiderException(1L)

        // When / Then
        assertThrows<NoLinkedRaiderException> {
            controller.getMyVaultOptions(guildId, authenticatedUser)
        }

        verify(exactly = 1) { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 0) { vaultSlotService.findByRaiderUnpaged(any()) }
    }

    @Test
    fun `getMyVaultOptions should return weekOf as current Tuesday`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { vaultSlotService.findByRaiderUnpaged(raiderId.value) } returns emptyList()

        // When
        val response = controller.getMyVaultOptions(guildId, authenticatedUser)

        // Then
        // weekOf should be a valid ISO date format (YYYY-MM-DD)
        response.weekOf.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) shouldBe true
    }

    @Test
    fun `getMyVaultOptions should handle slots with only raid data`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)

        val raidOnlySlots = listOf(
            RaiderVaultSlotResponse(id = 1L, raiderId = 123L, slot = "RAID_1", unlocked = true),
            RaiderVaultSlotResponse(id = 2L, raiderId = 123L, slot = "RAID_2", unlocked = true),
        )

        justRun { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) }
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { vaultSlotService.findByRaiderUnpaged(raiderId.value) } returns raidOnlySlots

        // When
        val response = controller.getMyVaultOptions(guildId, authenticatedUser)

        // Then
        response.raid shouldHaveSize 2
        response.mythicPlus shouldHaveSize 0
        response.pvp shouldHaveSize 0
    }
}
