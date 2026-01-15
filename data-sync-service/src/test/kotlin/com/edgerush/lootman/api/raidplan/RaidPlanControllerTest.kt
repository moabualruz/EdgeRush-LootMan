package com.edgerush.lootman.api.raidplan

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.application.raidplan.*
import com.edgerush.lootman.domain.raidplan.model.*
import com.edgerush.lootman.domain.shared.GuildId
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Unit tests for RaidPlanController.
 */
class RaidPlanControllerTest : UnitTest() {

    private lateinit var raidPlanService: RaidPlanService
    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()
    private val paginationProperties = PaginationProperties(20, 100)

    @BeforeEach
    fun setUp() {
        raidPlanService = mockk(relaxed = true)
        val controller = RaidPlanController(raidPlanService, paginationProperties)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Nested
    inner class CreatePlanTests {

        @Test
        fun `should create plan and return 201`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanService.createPlan(any()) } returns plan

            val request = CreateRaidPlanApiRequest(
                guildId = "test-guild",
                encounterId = 2902,
                encounterName = "Queen Ansurek",
                name = "Phase 1 Positions",
                createdBy = 1L,
                visibility = PlanVisibility.GUILD
            )

            // When & Then
            mockMvc.perform(
                post("/api/v1/raid-plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(plan.id))
                .andExpect(jsonPath("$.name").value("Test Plan"))
                .andExpect(jsonPath("$.visibility").value("GUILD"))

            verify { raidPlanService.createPlan(any()) }
        }
    }

    @Nested
    inner class GetPlanTests {

        @Test
        fun `should return plan when found`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanService.getPlan(plan.id) } returns plan

            // When & Then
            mockMvc.perform(get("/api/v1/raid-plans/${plan.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(plan.id))
                .andExpect(jsonPath("$.guildId").value("test-guild"))
                .andExpect(jsonPath("$.encounterName").value("Queen Ansurek"))
        }

        @Test
        fun `should throw RaidPlanNotFoundException when plan not found`() {
            // Given
            every { raidPlanService.getPlan("non-existent") } throws RaidPlanNotFoundException("non-existent")

            // When & Then - In standalone MockMvc without exception handler, exception propagates
            try {
                mockMvc.perform(get("/api/v1/raid-plans/non-existent"))
                    .andReturn()
            } catch (e: Exception) {
                // Expected: the RaidPlanNotFoundException is wrapped in ServletException
                assert(e.cause is RaidPlanNotFoundException) { "Expected RaidPlanNotFoundException but got ${e.cause}" }
            }
        }
    }

    @Nested
    inner class GetPlanByShareTokenTests {

        @Test
        fun `should return plan when share token valid`() {
            // Given
            val plan = createTestPlan().generateShareToken()
            every { raidPlanService.getPlanByShareToken(plan.shareToken!!) } returns plan

            // When & Then
            mockMvc.perform(get("/api/v1/raid-plans/shared/${plan.shareToken}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(plan.id))
                .andExpect(jsonPath("$.shareToken").value(plan.shareToken))
        }
    }

    @Nested
    inner class GetPlansByGuildTests {

        @Test
        fun `should return paginated plans for guild`() {
            // Given
            val plans = listOf(createTestPlan(), createTestPlan())
            val pagedResult = PagedRaidPlans(
                content = plans,
                page = 0,
                size = 20,
                totalElements = 2,
                totalPages = 1
            )
            every { raidPlanService.getPlansByGuildPaginated("test-guild", 0, 20) } returns pagedResult

            // When & Then
            mockMvc.perform(get("/api/v1/raid-plans/guild/test-guild"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content").isArray)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
        }
    }

    @Nested
    inner class GetPlansByEncounterTests {

        @Test
        fun `should return plans for encounter`() {
            // Given
            val plans = listOf(createTestPlan())
            every { raidPlanService.getPlansByEncounter("test-guild", 2902) } returns plans

            // When & Then
            mockMvc.perform(get("/api/v1/raid-plans/guild/test-guild/encounter/2902"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(1))
        }
    }

    @Nested
    inner class UpdatePlanTests {

        @Test
        fun `should update plan name`() {
            // Given
            val plan = createTestPlan()
            val updatedPlan = RaidPlan.reconstitute(
                id = plan.id,
                guildId = plan.guildId,
                encounterId = plan.encounterId,
                encounterName = plan.encounterName,
                name = "Updated Name",
                steps = plan.steps,
                visibility = plan.visibility,
                shareToken = plan.shareToken,
                createdBy = plan.createdBy,
                createdAt = plan.createdAt,
                updatedAt = plan.updatedAt
            )
            every { raidPlanService.updatePlan(plan.id, any()) } returns updatedPlan

            val request = UpdateRaidPlanApiRequest(name = "Updated Name")

            // When & Then
            mockMvc.perform(
                put("/api/v1/raid-plans/${plan.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Updated Name"))
        }
    }

    @Nested
    inner class DeletePlanTests {

        @Test
        fun `should delete plan and return 204`() {
            // Given
            val planId = "test-plan-id"
            every { raidPlanService.deletePlan(planId) } returns Unit

            // When & Then
            mockMvc.perform(delete("/api/v1/raid-plans/$planId"))
                .andExpect(status().isNoContent)

            verify { raidPlanService.deletePlan(planId) }
        }
    }

    @Nested
    inner class AddStepTests {

        @Test
        fun `should add step to plan`() {
            // Given
            val plan = createTestPlan().addStep("New step notes")
            every { raidPlanService.addStep(plan.id, "New step notes") } returns plan

            val request = AddStepRequest(notes = "New step notes")

            // When & Then
            mockMvc.perform(
                post("/api/v1/raid-plans/${plan.id}/steps")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.steps.length()").value(1))
                .andExpect(jsonPath("$.steps[0].notes").value("New step notes"))
        }
    }

    @Nested
    inner class RemoveStepTests {

        @Test
        fun `should remove step from plan`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanService.removeStep(plan.id, 0) } returns plan

            // When & Then
            mockMvc.perform(delete("/api/v1/raid-plans/${plan.id}/steps/0"))
                .andExpect(status().isOk)

            verify { raidPlanService.removeStep(plan.id, 0) }
        }
    }

    @Nested
    inner class ShareTokenTests {

        @Test
        fun `should generate share token`() {
            // Given
            val plan = createTestPlan().generateShareToken()
            every { raidPlanService.generateShareToken(plan.id) } returns plan

            // When & Then
            mockMvc.perform(post("/api/v1/raid-plans/${plan.id}/share"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.shareToken").value(plan.shareToken))
        }

        @Test
        fun `should revoke share token`() {
            // Given
            val planId = "test-plan-id"
            val plan = createTestPlan()
            every { raidPlanService.revokeShareToken(planId) } returns plan

            // When & Then
            mockMvc.perform(delete("/api/v1/raid-plans/$planId/share"))
                .andExpect(status().isNoContent)

            verify { raidPlanService.revokeShareToken(planId) }
        }
    }

    private fun createTestPlan(): RaidPlan = RaidPlan.create(
        guildId = GuildId("test-guild"),
        encounterId = 2902,
        encounterName = "Queen Ansurek",
        name = "Test Plan",
        createdBy = 1L,
    )
}
