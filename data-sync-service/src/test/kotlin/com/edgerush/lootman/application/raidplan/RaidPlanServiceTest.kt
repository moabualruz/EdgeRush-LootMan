package com.edgerush.lootman.application.raidplan

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.raidplan.model.*
import com.edgerush.lootman.domain.raidplan.repository.RaidPlanRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for RaidPlanService.
 */
class RaidPlanServiceTest : UnitTest() {
    private lateinit var raidPlanRepository: RaidPlanRepository
    private lateinit var raidPlanService: RaidPlanService

    @BeforeEach
    fun setUp() {
        raidPlanRepository = mockk(relaxed = true)
        raidPlanService = RaidPlanService(raidPlanRepository)
    }

    @Nested
    inner class CreatePlanTests {
        @Test
        fun `should create raid plan with valid data`() {
            // Given
            val request =
                CreateRaidPlanRequest(
                    guildId = "test-guild",
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Phase 1 Positions",
                    createdBy = 1L,
                    visibility = PlanVisibility.GUILD,
                )

            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.createPlan(request)

            // Then
            result.guildId.value shouldBe "test-guild"
            result.encounterId shouldBe 2902
            result.encounterName shouldBe "Queen Ansurek"
            result.name shouldBe "Phase 1 Positions"
            result.visibility shouldBe PlanVisibility.GUILD
            result.createdBy shouldBe 1L
            result.steps shouldHaveSize 0

            verify(exactly = 1) { raidPlanRepository.save(any()) }
        }

        @Test
        fun `should create private plan when specified`() {
            // Given
            val request =
                CreateRaidPlanRequest(
                    guildId = "test-guild",
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "My Private Strategy",
                    createdBy = 1L,
                    visibility = PlanVisibility.PRIVATE,
                )

            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.createPlan(request)

            // Then
            result.visibility shouldBe PlanVisibility.PRIVATE
        }
    }

    @Nested
    inner class GetPlanTests {
        @Test
        fun `should return plan when found by id`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan

            // When
            val result = raidPlanService.getPlan(plan.id)

            // Then
            result shouldBe plan
        }

        @Test
        fun `should throw exception when plan not found`() {
            // Given
            every { raidPlanRepository.findById("non-existent") } returns null

            // When & Then
            val exception =
                shouldThrow<RaidPlanNotFoundException> {
                    raidPlanService.getPlan("non-existent")
                }
            exception.planId shouldBe "non-existent"
        }
    }

    @Nested
    inner class GetPlanByShareTokenTests {
        @Test
        fun `should return plan when found by share token`() {
            // Given
            val plan = createTestPlan().generateShareToken()
            every { raidPlanRepository.findByShareToken(plan.shareToken!!) } returns plan

            // When
            val result = raidPlanService.getPlanByShareToken(plan.shareToken!!)

            // Then
            result shouldBe plan
        }

        @Test
        fun `should throw exception when share token not found`() {
            // Given
            every { raidPlanRepository.findByShareToken("invalid") } returns null

            // When & Then
            shouldThrow<RaidPlanNotFoundException> {
                raidPlanService.getPlanByShareToken("invalid")
            }
        }
    }

    @Nested
    inner class GetPlansByGuildTests {
        @Test
        fun `should return all plans for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val plans = listOf(createTestPlan(), createTestPlan())
            every { raidPlanRepository.findByGuildId(guildId) } returns plans

            // When
            val result = raidPlanService.getPlansByGuild(guildId.value)

            // Then
            result shouldHaveSize 2
        }

        @Test
        fun `should return paginated plans for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val plans = listOf(createTestPlan())
            every { raidPlanRepository.findByGuildId(guildId, 0L, 20) } returns plans
            every { raidPlanRepository.countByGuildId(guildId) } returns 1L

            // When
            val result = raidPlanService.getPlansByGuildPaginated(guildId.value, 0, 20)

            // Then
            result.content shouldHaveSize 1
            result.totalElements shouldBe 1L
        }
    }

    @Nested
    inner class UpdatePlanTests {
        @Test
        fun `should update plan name`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            val request = UpdateRaidPlanRequest(name = "Updated Name")

            // When
            val result = raidPlanService.updatePlan(plan.id, request)

            // Then
            result.name shouldBe "Updated Name"
            verify { raidPlanRepository.save(any()) }
        }

        @Test
        fun `should update plan visibility`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            val request = UpdateRaidPlanRequest(visibility = PlanVisibility.PUBLIC)

            // When
            val result = raidPlanService.updatePlan(plan.id, request)

            // Then
            result.visibility shouldBe PlanVisibility.PUBLIC
        }

        @Test
        fun `should throw exception when updating non-existent plan`() {
            // Given
            every { raidPlanRepository.findById("non-existent") } returns null

            // When & Then
            shouldThrow<RaidPlanNotFoundException> {
                raidPlanService.updatePlan("non-existent", UpdateRaidPlanRequest())
            }
        }
    }

    @Nested
    inner class AddStepTests {
        @Test
        fun `should add step to plan`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.addStep(plan.id, "Phase 1 notes")

            // Then
            result.steps shouldHaveSize 1
            result.steps[0].notes shouldBe "Phase 1 notes"
        }
    }

    @Nested
    inner class RemoveStepTests {
        @Test
        fun `should remove step from plan`() {
            // Given
            val plan = createTestPlan().addStep("Phase 1").addStep("Phase 2")
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.removeStep(plan.id, 0)

            // Then
            result.steps shouldHaveSize 1
            result.steps[0].notes shouldBe "Phase 2"
        }
    }

    @Nested
    inner class GenerateShareTokenTests {
        @Test
        fun `should generate share token`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.generateShareToken(plan.id)

            // Then
            result.shareToken shouldNotBe null
            result.shareToken!!.length shouldBe 32
        }
    }

    @Nested
    inner class RevokeShareTokenTests {
        @Test
        fun `should revoke share token`() {
            // Given
            val plan = createTestPlan().generateShareToken()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.save(any()) } answers { firstArg() }

            // When
            val result = raidPlanService.revokeShareToken(plan.id)

            // Then
            result.shareToken shouldBe null
        }
    }

    @Nested
    inner class DeletePlanTests {
        @Test
        fun `should delete plan`() {
            // Given
            val plan = createTestPlan()
            every { raidPlanRepository.findById(plan.id) } returns plan
            every { raidPlanRepository.delete(plan.id) } just Runs

            // When
            raidPlanService.deletePlan(plan.id)

            // Then
            verify(exactly = 1) { raidPlanRepository.delete(plan.id) }
        }

        @Test
        fun `should throw exception when deleting non-existent plan`() {
            // Given
            every { raidPlanRepository.findById("non-existent") } returns null

            // When & Then
            shouldThrow<RaidPlanNotFoundException> {
                raidPlanService.deletePlan("non-existent")
            }
        }
    }

    private fun createTestPlan(): RaidPlan =
        RaidPlan.create(
            guildId = GuildId("test-guild"),
            encounterId = 2902,
            encounterName = "Queen Ansurek",
            name = "Test Plan",
            createdBy = 1L,
        )
}
