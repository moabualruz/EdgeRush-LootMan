package com.edgerush.lootman.api.trial

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import com.edgerush.lootman.domain.trial.service.TrialService
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
 * Unit tests for TrialController.
 */
class TrialControllerTest : UnitTest() {

    private lateinit var trialService: TrialService
    private lateinit var controller: TrialController

    private val guildId = "test-guild"

    @BeforeEach
    fun setup() {
        trialService = mockk()
        controller = TrialController(trialService)
    }

    @Nested
    inner class CreateTrialTests {

        @Test
        fun `should create new trial and return 201`() {
            // Arrange
            val request = CreateTrialRequest(
                applicationId = "app-123",
                guildId = guildId,
                raidsRequired = 8,
            )
            val trial = createValidTrial(applicationId = "app-123")
            every { trialService.createTrial(any(), any(), any(), any()) } returns trial

            // Act
            val response = controller.createTrial(request)

            // Assert
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.applicationId shouldBe "app-123"
            response.body?.status shouldBe TrialStatus.ACTIVE
        }

        @Test
        fun `should return 409 when trial already exists`() {
            // Arrange
            val request = CreateTrialRequest(
                applicationId = "app-123",
                guildId = guildId,
                raidsRequired = 8,
            )
            every { trialService.createTrial(any(), any(), any(), any()) } throws
                IllegalStateException("Trial already exists")

            // Act
            val response = controller.createTrial(request)

            // Assert
            response.statusCode shouldBe HttpStatus.CONFLICT
        }
    }

    @Nested
    inner class GetTrialTests {

        @Test
        fun `should get trial by ID`() {
            // Arrange
            val trial = createValidTrial()
            every { trialService.getTrial(trial.id) } returns trial

            // Act
            val response = controller.getTrial(trial.id.value)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.id shouldBe trial.id.value
        }

        @Test
        fun `should return 404 when trial not found`() {
            // Arrange
            every { trialService.getTrial(any()) } returns null

            // Act
            val response = controller.getTrial("non-existent")

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class ListTrialsTests {

        @Test
        fun `should list trials for guild`() {
            // Arrange
            val trials = listOf(
                createValidTrial(applicationId = "app-1"),
                createValidTrial(applicationId = "app-2"),
            )
            every { trialService.listTrials(GuildId(guildId), 0, 50) } returns trials
            every { trialService.countTrials(GuildId(guildId)) } returns 2L

            // Act
            val response = controller.listTrials(guildId, null, 0, 50)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body!!.trials shouldHaveSize 2
            response.body!!.total shouldBe 2L
        }

        @Test
        fun `should filter trials by status`() {
            // Arrange
            val trials = listOf(createValidTrial())
            every { trialService.listTrialsByStatus(GuildId(guildId), TrialStatus.ACTIVE, 0, 50) } returns trials
            every { trialService.countTrialsByStatus(GuildId(guildId), TrialStatus.ACTIVE) } returns 1L

            // Act
            val response = controller.listTrials(guildId, TrialStatus.ACTIVE, 0, 50)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body!!.trials shouldHaveSize 1
        }

        @Test
        fun `should get active trials`() {
            // Arrange
            val trials = listOf(createValidTrial())
            every { trialService.getActiveTrials(GuildId(guildId)) } returns trials

            // Act
            val response = controller.getActiveTrials(guildId)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldHaveSize 1
        }
    }

    @Nested
    inner class UpdateMetricsTests {

        @Test
        fun `should update trial metrics`() {
            // Arrange
            val trial = createValidTrial()
            val updated = trial.updateMetrics(
                raidsAttended = 5,
                attendanceRate = 0.85,
                averagePerformance = 75.0,
                deathsPerRaid = 1.2,
            )
            val request = UpdateMetricsRequest(
                raidsAttended = 5,
                attendanceRate = 0.85,
                averagePerformance = 75.0,
                deathsPerRaid = 1.2,
            )
            every { trialService.updateMetrics(trial.id, 5, 0.85, 75.0, 1.2) } returns updated

            // Act
            val response = controller.updateMetrics(trial.id.value, request)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.raidsAttended shouldBe 5
        }

        @Test
        fun `should return 404 when trial not found`() {
            // Arrange
            val request = UpdateMetricsRequest(5, 0.85, 75.0, 1.2)
            every { trialService.updateMetrics(any(), any(), any(), any(), any()) } throws
                IllegalArgumentException("Trial not found")

            // Act
            val response = controller.updateMetrics("non-existent", request)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class PromoteTests {

        @Test
        fun `should promote trial`() {
            // Arrange
            val trial = createValidTrial()
            val promoted = trial.promote("officer-123", "Great job")
            val request = PromoteTrialRequest(
                promoterId = "officer-123",
                reason = "Great job",
            )
            every { trialService.promoteTrial(trial.id, "officer-123", "Great job") } returns promoted

            // Act
            val response = controller.promoteTrial(trial.id.value, request)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.status shouldBe TrialStatus.PROMOTED
            response.body?.outcome shouldBe TrialOutcome.PROMOTED
        }

        @Test
        fun `should return 404 when trial not found`() {
            // Arrange
            val request = PromoteTrialRequest("officer", "reason")
            every { trialService.promoteTrial(any(), any(), any()) } throws
                IllegalArgumentException("Trial not found")

            // Act
            val response = controller.promoteTrial("non-existent", request)

            // Assert
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        @Test
        fun `should return 400 when trial cannot be promoted`() {
            // Arrange
            val request = PromoteTrialRequest("officer", "reason")
            every { trialService.promoteTrial(any(), any(), any()) } throws
                IllegalStateException("Cannot promote")

            // Act
            val response = controller.promoteTrial("trial-id", request)

            // Assert
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
        }
    }

    @Nested
    inner class ExtendTests {

        @Test
        fun `should extend trial`() {
            // Arrange
            val trial = createValidTrial()
            val extended = trial.extend("officer-123", 4, "Needs more time")
            val request = ExtendTrialRequest(
                extenderId = "officer-123",
                additionalRaids = 4,
                reason = "Needs more time",
            )
            every { trialService.extendTrial(trial.id, "officer-123", 4, "Needs more time") } returns extended

            // Act
            val response = controller.extendTrial(trial.id.value, request)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.status shouldBe TrialStatus.EXTENDED
            response.body?.raidsRequired shouldBe 12
        }
    }

    @Nested
    inner class EndTrialTests {

        @Test
        fun `should end trial`() {
            // Arrange
            val trial = createValidTrial()
            val ended = trial.endTrial("officer-123", TrialOutcome.FAILED, "Poor attendance")
            val request = EndTrialRequest(
                officerId = "officer-123",
                outcome = TrialOutcome.FAILED,
                reason = "Poor attendance",
            )
            every { trialService.endTrial(trial.id, "officer-123", TrialOutcome.FAILED, "Poor attendance") } returns ended

            // Act
            val response = controller.endTrial(trial.id.value, request)

            // Assert
            response.statusCode shouldBe HttpStatus.OK
            response.body?.status shouldBe TrialStatus.ENDED
            response.body?.outcome shouldBe TrialOutcome.FAILED
        }
    }

    @Nested
    inner class DeleteTrialTests {

        @Test
        fun `should delete trial and return 204`() {
            // Arrange
            val trialId = TrialId("trial-123")
            every { trialService.deleteTrial(trialId) } returns Unit

            // Act
            val response = controller.deleteTrial(trialId.value)

            // Assert
            response.statusCode shouldBe HttpStatus.NO_CONTENT
            verify { trialService.deleteTrial(trialId) }
        }
    }

    private fun createValidTrial(
        applicationId: String = "app-${System.nanoTime()}",
    ): Trial = Trial.create(
        applicationId = ApplicationId(applicationId),
        guildId = GuildId(guildId),
        raidsRequired = 8,
    )
}
