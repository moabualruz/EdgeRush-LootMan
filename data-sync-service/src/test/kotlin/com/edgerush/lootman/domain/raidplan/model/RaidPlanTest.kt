package com.edgerush.lootman.domain.raidplan.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for RaidPlan aggregate root.
 */
class RaidPlanTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid raid plan with required fields`() {
            // Arrange & Act
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Phase 1 Positions",
                    createdBy = 1L,
                )

            // Assert
            plan.id shouldNotBe null
            plan.guildId.value shouldBe "test-guild"
            plan.encounterId shouldBe 2902
            plan.encounterName shouldBe "Queen Ansurek"
            plan.name shouldBe "Phase 1 Positions"
            plan.visibility shouldBe PlanVisibility.GUILD
            plan.createdBy shouldBe 1L
            plan.steps.shouldBeEmpty()
        }

        @Test
        fun `should create raid plan with private visibility`() {
            // Arrange & Act
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "My Private Plan",
                    createdBy = 1L,
                    visibility = PlanVisibility.PRIVATE,
                )

            // Assert
            plan.visibility shouldBe PlanVisibility.PRIVATE
        }

        @Test
        fun `should create raid plan with public visibility`() {
            // Arrange & Act
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Community Strategy",
                    createdBy = 1L,
                    visibility = PlanVisibility.PUBLIC,
                )

            // Assert
            plan.visibility shouldBe PlanVisibility.PUBLIC
        }

        @Test
        fun `should generate unique IDs for different plans`() {
            // Arrange & Act
            val plan1 =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Plan 1",
                    createdBy = 1L,
                )
            val plan2 =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Plan 2",
                    createdBy = 1L,
                )

            // Assert
            plan1.id shouldNotBe plan2.id
        }

        @Test
        fun `should set timestamps on creation`() {
            // Arrange
            val before = Instant.now()

            // Act
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Test Plan",
                    createdBy = 1L,
                )

            // Assert
            val after = Instant.now()
            plan.createdAt shouldNotBe null
            plan.updatedAt shouldNotBe null
            plan.createdAt shouldBe plan.updatedAt
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when name is blank`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaidPlan.create(
                        guildId = GuildId("test-guild"),
                        encounterId = 2902,
                        encounterName = "Queen Ansurek",
                        name = "",
                        createdBy = 1L,
                    )
                }
            exception.message shouldBe "Plan name cannot be blank"
        }

        @Test
        fun `should throw exception when name is only whitespace`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaidPlan.create(
                        guildId = GuildId("test-guild"),
                        encounterId = 2902,
                        encounterName = "Queen Ansurek",
                        name = "   ",
                        createdBy = 1L,
                    )
                }
            exception.message shouldBe "Plan name cannot be blank"
        }

        @Test
        fun `should throw exception when encounter name is blank`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaidPlan.create(
                        guildId = GuildId("test-guild"),
                        encounterId = 2902,
                        encounterName = "",
                        name = "Test Plan",
                        createdBy = 1L,
                    )
                }
            exception.message shouldBe "Encounter name cannot be blank"
        }

        @Test
        fun `should throw exception when encounter ID is not positive`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaidPlan.create(
                        guildId = GuildId("test-guild"),
                        encounterId = 0,
                        encounterName = "Queen Ansurek",
                        name = "Test Plan",
                        createdBy = 1L,
                    )
                }
            exception.message shouldBe "Encounter ID must be positive"
        }

        @Test
        fun `should throw exception when createdBy is not positive`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    RaidPlan.create(
                        guildId = GuildId("test-guild"),
                        encounterId = 2902,
                        encounterName = "Queen Ansurek",
                        name = "Test Plan",
                        createdBy = 0L,
                    )
                }
            exception.message shouldBe "Created by user ID must be positive"
        }
    }

    @Nested
    inner class StepManagementTests {
        @Test
        fun `should add step to plan`() {
            // Arrange
            val plan = createTestPlan()

            // Act
            val updatedPlan = plan.addStep(notes = "Initial positions")

            // Assert
            updatedPlan.steps shouldHaveSize 1
            updatedPlan.steps[0].order shouldBe 0
            updatedPlan.steps[0].notes shouldBe "Initial positions"
        }

        @Test
        fun `should add multiple steps in order`() {
            // Arrange
            val plan = createTestPlan()

            // Act
            val updatedPlan =
                plan
                    .addStep(notes = "Phase 1")
                    .addStep(notes = "Phase 2")
                    .addStep(notes = "Phase 3")

            // Assert
            updatedPlan.steps shouldHaveSize 3
            updatedPlan.steps[0].order shouldBe 0
            updatedPlan.steps[0].notes shouldBe "Phase 1"
            updatedPlan.steps[1].order shouldBe 1
            updatedPlan.steps[1].notes shouldBe "Phase 2"
            updatedPlan.steps[2].order shouldBe 2
            updatedPlan.steps[2].notes shouldBe "Phase 3"
        }

        @Test
        fun `should remove step by order`() {
            // Arrange
            val plan =
                createTestPlan()
                    .addStep(notes = "Phase 1")
                    .addStep(notes = "Phase 2")
                    .addStep(notes = "Phase 3")

            // Act
            val updatedPlan = plan.removeStep(1)

            // Assert
            updatedPlan.steps shouldHaveSize 2
            updatedPlan.steps[0].notes shouldBe "Phase 1"
            updatedPlan.steps[1].notes shouldBe "Phase 3"
            // Re-ordered
            updatedPlan.steps[0].order shouldBe 0
            updatedPlan.steps[1].order shouldBe 1
        }

        @Test
        fun `should throw exception when removing non-existent step`() {
            // Arrange
            val plan = createTestPlan().addStep(notes = "Phase 1")

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    plan.removeStep(5)
                }
            exception.message shouldBe "Step with order 5 does not exist"
        }

        @Test
        fun `should update step notes`() {
            // Arrange
            val plan =
                createTestPlan()
                    .addStep(notes = "Initial notes")

            // Act
            val updatedPlan = plan.updateStep(0, notes = "Updated notes")

            // Assert
            updatedPlan.steps[0].notes shouldBe "Updated notes"
        }
    }

    @Nested
    inner class ShareTokenTests {
        @Test
        fun `should generate share token`() {
            // Arrange
            val plan = createTestPlan()

            // Act
            val updatedPlan = plan.generateShareToken()

            // Assert
            updatedPlan.shareToken shouldNotBe null
            updatedPlan.shareToken!!.length shouldBe 32
        }

        @Test
        fun `should revoke share token`() {
            // Arrange
            val plan = createTestPlan().generateShareToken()

            // Act
            val updatedPlan = plan.revokeShareToken()

            // Assert
            updatedPlan.shareToken shouldBe null
        }
    }

    @Nested
    inner class VisibilityTests {
        @Test
        fun `should change visibility to public`() {
            // Arrange
            val plan = createTestPlan()

            // Act
            val updatedPlan = plan.changeVisibility(PlanVisibility.PUBLIC)

            // Assert
            updatedPlan.visibility shouldBe PlanVisibility.PUBLIC
        }

        @Test
        fun `should change visibility to private`() {
            // Arrange
            val plan = createTestPlan()

            // Act
            val updatedPlan = plan.changeVisibility(PlanVisibility.PRIVATE)

            // Assert
            updatedPlan.visibility shouldBe PlanVisibility.PRIVATE
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
