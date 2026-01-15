package com.edgerush.lootman.api.admin

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.behavioral.BehavioralActionCrudService
import com.edgerush.lootman.api.behavioral.BehavioralActionResponse
import com.edgerush.lootman.api.behavioral.CreateBehavioralActionRequest
import com.edgerush.lootman.api.behavioral.UpdateBehavioralActionRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.loot.CreateLootBanEntityRequest
import com.edgerush.lootman.api.loot.LootBanCrudService
import com.edgerush.lootman.api.loot.LootBanResponse
import com.edgerush.lootman.api.loot.UpdateLootBanEntityRequest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for AdminController.
 *
 * Tests the v1 admin endpoints for behavioral actions and loot bans.
 */
class AdminControllerTest : UnitTest() {
    private lateinit var behavioralActionService: BehavioralActionCrudService
    private lateinit var lootBanService: LootBanCrudService
    private lateinit var controller: AdminController

    @BeforeEach
    fun setup() {
        behavioralActionService = mockk()
        lootBanService = mockk()
        controller =
            AdminController(
                behavioralActionService,
                lootBanService,
            )
    }

    // Behavioral Action Tests

    @Test
    fun `getBehavioralActions should return all actions for guild`() {
        // Given
        val guildId = "test-guild"
        val actions =
            listOf(
                createBehavioralActionResponse(1L, "TestRaider", guildId),
                createBehavioralActionResponse(2L, "AnotherRaider", guildId),
            )
        val pagedResponse =
            PagedResponse(
                content = actions,
                page = 0,
                size = 50,
                totalElements = 2L,
            )

        every { behavioralActionService.findByGuild(guildId, any()) } returns pagedResponse

        // When
        val response = controller.getBehavioralActions(guildId)

        // Then
        response.size shouldBe 2
        response[0].id shouldBe 1L
        response[1].id shouldBe 2L

        verify(exactly = 1) { behavioralActionService.findByGuild(guildId, any()) }
    }

    @Test
    fun `createBehavioralAction should create and return new action`() {
        // Given
        val guildId = "test-guild"
        val request =
            AdminBehavioralActionRequest(
                raiderId = 123L,
                characterName = "TestRaider",
                actionType = "PENALTY",
                reason = "Late to raid",
                flpsModifier = -0.1,
                startDate = "2024-01-01T00:00:00Z",
                endDate = null,
            )
        val expectedResponse = createBehavioralActionResponse(1L, "TestRaider", guildId)

        every { behavioralActionService.create(any<CreateBehavioralActionRequest>()) } returns expectedResponse

        // When
        val response = controller.createBehavioralAction(guildId, request)

        // Then
        response.id shouldBe 1L
        response.characterName shouldBe "TestRaider"

        verify(exactly = 1) { behavioralActionService.create(any<CreateBehavioralActionRequest>()) }
    }

    @Test
    fun `updateBehavioralAction should update and return action`() {
        // Given
        val guildId = "test-guild"
        val actionId = 1L
        val request =
            AdminBehavioralActionUpdateRequest(
                reason = "Updated reason",
                endDate = "2024-02-01T00:00:00Z",
            )
        val expectedResponse = createBehavioralActionResponse(actionId, "TestRaider", guildId)

        every { behavioralActionService.update(actionId, any<UpdateBehavioralActionRequest>()) } returns expectedResponse

        // When
        val response = controller.updateBehavioralAction(guildId, actionId, request)

        // Then
        response.id shouldBe actionId

        verify(exactly = 1) { behavioralActionService.update(actionId, any<UpdateBehavioralActionRequest>()) }
    }

    @Test
    fun `deleteBehavioralAction should delete action`() {
        // Given
        val guildId = "test-guild"
        val actionId = 1L

        every { behavioralActionService.delete(actionId) } returns Unit

        // When
        val response = controller.deleteBehavioralAction(guildId, actionId)

        // Then
        response.statusCodeValue shouldBe 204

        verify(exactly = 1) { behavioralActionService.delete(actionId) }
    }

    // Loot Ban Tests

    @Test
    fun `getLootBans should return all bans for guild`() {
        // Given
        val guildId = "test-guild"
        val bans =
            listOf(
                createLootBanResponse(1L, "BannedRaider", guildId),
                createLootBanResponse(2L, "AnotherBannedRaider", guildId),
            )
        val pagedResponse =
            PagedResponse(
                content = bans,
                page = 0,
                size = 50,
                totalElements = 2L,
            )

        every { lootBanService.findByGuild(guildId, any()) } returns pagedResponse

        // When
        val response = controller.getLootBans(guildId)

        // Then
        response.size shouldBe 2
        response[0].id shouldBe 1L
        response[1].id shouldBe 2L

        verify(exactly = 1) { lootBanService.findByGuild(guildId, any()) }
    }

    @Test
    fun `createLootBan should create and return new ban`() {
        // Given
        val guildId = "test-guild"
        val request =
            AdminLootBanRequest(
                raiderId = 123L,
                characterName = "BannedRaider",
                reason = "Ninja looting",
                startDate = "2024-01-01T00:00:00Z",
                endDate = null,
            )
        val expectedResponse = createLootBanResponse(1L, "BannedRaider", guildId)

        every { lootBanService.create(any<CreateLootBanEntityRequest>()) } returns expectedResponse

        // When
        val response = controller.createLootBan(guildId, request)

        // Then
        response.id shouldBe 1L
        response.characterName shouldBe "BannedRaider"

        verify(exactly = 1) { lootBanService.create(any<CreateLootBanEntityRequest>()) }
    }

    @Test
    fun `updateLootBan should update and return ban`() {
        // Given
        val guildId = "test-guild"
        val banId = 1L
        val request =
            AdminLootBanUpdateRequest(
                reason = "Updated reason",
                endDate = "2024-02-01T00:00:00Z",
            )
        val expectedResponse = createLootBanResponse(banId, "BannedRaider", guildId)

        every { lootBanService.update(banId, any<UpdateLootBanEntityRequest>()) } returns expectedResponse

        // When
        val response = controller.updateLootBan(guildId, banId, request)

        // Then
        response.id shouldBe banId

        verify(exactly = 1) { lootBanService.update(banId, any<UpdateLootBanEntityRequest>()) }
    }

    @Test
    fun `deleteLootBan should delete ban`() {
        // Given
        val guildId = "test-guild"
        val banId = 1L

        every { lootBanService.delete(banId) } returns Unit

        // When
        val response = controller.deleteLootBan(guildId, banId)

        // Then
        response.statusCodeValue shouldBe 204

        verify(exactly = 1) { lootBanService.delete(banId) }
    }

    private fun createBehavioralActionResponse(
        id: Long,
        characterName: String,
        guildId: String,
    ): BehavioralActionResponse {
        return BehavioralActionResponse(
            id = id,
            guildId = guildId,
            characterName = characterName,
            actionType = "PENALTY",
            deductionAmount = 0.1,
            reason = "Test reason",
            appliedBy = "admin",
            appliedAt = LocalDateTime.now(),
            expiresAt = null,
            isActive = true,
        )
    }

    private fun createLootBanResponse(
        id: Long,
        characterName: String,
        guildId: String,
    ): LootBanResponse {
        return LootBanResponse(
            id = id,
            guildId = guildId,
            characterName = characterName,
            reason = "Test reason",
            bannedBy = "admin",
            bannedAt = LocalDateTime.now(),
            expiresAt = null,
            isActive = true,
        )
    }
}
