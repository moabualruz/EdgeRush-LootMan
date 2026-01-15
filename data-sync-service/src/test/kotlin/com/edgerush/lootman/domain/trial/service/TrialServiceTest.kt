package com.edgerush.lootman.domain.trial.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import com.edgerush.lootman.domain.trial.repository.TrialRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for TrialService.
 */
class TrialServiceTest : UnitTest() {
    private lateinit var trialRepository: TrialRepository
    private lateinit var trialService: TrialService

    private val guildId = GuildId("test-guild")

    @BeforeEach
    fun setup() {
        trialRepository = mockk(relaxed = true)
        trialService = TrialService(trialRepository)
    }

    @Nested
    inner class CreateTrialTests {
        @Test
        fun `should create new trial for approved application`() {
            // Arrange
            val applicationId = ApplicationId("app-123")
            val savedTrial = slot<Trial>()
            every { trialRepository.findByApplicationId(applicationId) } returns null
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val trial =
                trialService.createTrial(
                    applicationId = applicationId,
                    guildId = guildId,
                    raidsRequired = 8,
                )

            // Assert
            trial.applicationId shouldBe applicationId
            trial.guildId shouldBe guildId
            trial.raidsRequired shouldBe 8
            trial.status shouldBe TrialStatus.ACTIVE
            verify { trialRepository.save(any()) }
        }

        @Test
        fun `should throw exception when trial already exists for application`() {
            // Arrange
            val applicationId = ApplicationId("app-123")
            val existingTrial = createValidTrial(applicationId = "app-123")
            every { trialRepository.findByApplicationId(applicationId) } returns existingTrial

            // Act & Assert
            shouldThrow<IllegalStateException> {
                trialService.createTrial(
                    applicationId = applicationId,
                    guildId = guildId,
                    raidsRequired = 8,
                )
            }.message shouldBe "Trial already exists for application app-123"
        }

        @Test
        fun `should create trial with optional raider ID`() {
            // Arrange
            val applicationId = ApplicationId("app-123")
            val savedTrial = slot<Trial>()
            every { trialRepository.findByApplicationId(applicationId) } returns null
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val trial =
                trialService.createTrial(
                    applicationId = applicationId,
                    guildId = guildId,
                    raidsRequired = 8,
                    raiderId = 12345L,
                )

            // Assert
            trial.raiderId shouldBe 12345L
        }
    }

    @Nested
    inner class GetTrialTests {
        @Test
        fun `should get trial by ID`() {
            // Arrange
            val trial = createValidTrial()
            every { trialRepository.findById(trial.id) } returns trial

            // Act
            val result = trialService.getTrial(trial.id)

            // Assert
            result shouldBe trial
        }

        @Test
        fun `should return null for non-existent trial`() {
            // Arrange
            every { trialRepository.findById(any()) } returns null

            // Act
            val result = trialService.getTrial(TrialId("non-existent"))

            // Assert
            result shouldBe null
        }

        @Test
        fun `should get trial by application ID`() {
            // Arrange
            val applicationId = ApplicationId("app-123")
            val trial = createValidTrial(applicationId = "app-123")
            every { trialRepository.findByApplicationId(applicationId) } returns trial

            // Act
            val result = trialService.getTrialByApplicationId(applicationId)

            // Assert
            result shouldBe trial
        }

        @Test
        fun `should get active trials for guild`() {
            // Arrange
            val trials =
                listOf(
                    createValidTrial(applicationId = "app-1"),
                    createValidTrial(applicationId = "app-2"),
                )
            every { trialRepository.findActiveTrialsByGuildId(guildId) } returns trials

            // Act
            val result = trialService.getActiveTrials(guildId)

            // Assert
            result.size shouldBe 2
        }
    }

    @Nested
    inner class UpdateMetricsTests {
        @Test
        fun `should update trial metrics`() {
            // Arrange
            val trial = createValidTrial()
            val savedTrial = slot<Trial>()
            every { trialRepository.findById(trial.id) } returns trial
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val updated =
                trialService.updateMetrics(
                    trialId = trial.id,
                    raidsAttended = 5,
                    attendanceRate = 0.85,
                    averagePerformance = 75.0,
                    deathsPerRaid = 1.2,
                )

            // Assert
            updated.raidsAttended shouldBe 5
            updated.attendanceRate shouldBe 0.85
            updated.averagePerformance shouldBe 75.0
            updated.deathsPerRaid shouldBe 1.2
        }

        @Test
        fun `should throw exception when trial not found`() {
            // Arrange
            every { trialRepository.findById(any()) } returns null

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trialService.updateMetrics(
                    trialId = TrialId("non-existent"),
                    raidsAttended = 5,
                    attendanceRate = 0.85,
                    averagePerformance = 75.0,
                    deathsPerRaid = 1.2,
                )
            }.message shouldBe "Trial not found: non-existent"
        }
    }

    @Nested
    inner class PromoteTests {
        @Test
        fun `should promote trial raider`() {
            // Arrange
            val trial =
                createValidTrial().updateMetrics(
                    raidsAttended = 8,
                    attendanceRate = 0.9,
                    averagePerformance = 80.0,
                    deathsPerRaid = 0.5,
                )
            val savedTrial = slot<Trial>()
            every { trialRepository.findById(trial.id) } returns trial
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val promoted =
                trialService.promoteTrial(
                    trialId = trial.id,
                    promoterId = "officer-123",
                    reason = "Excellent performance",
                )

            // Assert
            promoted.status shouldBe TrialStatus.PROMOTED
            promoted.outcome shouldBe TrialOutcome.PROMOTED
            promoted.promotedBy shouldBe "officer-123"
            promoted.outcomeReason shouldBe "Excellent performance"
        }

        @Test
        fun `should throw exception when trial not found for promotion`() {
            // Arrange
            every { trialRepository.findById(any()) } returns null

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trialService.promoteTrial(
                    trialId = TrialId("non-existent"),
                    promoterId = "officer-123",
                    reason = "Great job",
                )
            }.message shouldBe "Trial not found: non-existent"
        }
    }

    @Nested
    inner class ExtendTests {
        @Test
        fun `should extend trial`() {
            // Arrange
            val trial = createValidTrial()
            val savedTrial = slot<Trial>()
            every { trialRepository.findById(trial.id) } returns trial
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val extended =
                trialService.extendTrial(
                    trialId = trial.id,
                    extenderId = "officer-123",
                    additionalRaids = 4,
                    reason = "Needs more evaluation",
                )

            // Assert
            extended.status shouldBe TrialStatus.EXTENDED
            extended.raidsRequired shouldBe 12
            extended.outcomeReason shouldBe "Needs more evaluation"
        }

        @Test
        fun `should throw exception when trial not found for extension`() {
            // Arrange
            every { trialRepository.findById(any()) } returns null

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trialService.extendTrial(
                    trialId = TrialId("non-existent"),
                    extenderId = "officer-123",
                    additionalRaids = 4,
                    reason = "More time",
                )
            }.message shouldBe "Trial not found: non-existent"
        }
    }

    @Nested
    inner class EndTrialTests {
        @Test
        fun `should end trial with failed outcome`() {
            // Arrange
            val trial = createValidTrial()
            val savedTrial = slot<Trial>()
            every { trialRepository.findById(trial.id) } returns trial
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val ended =
                trialService.endTrial(
                    trialId = trial.id,
                    officerId = "officer-123",
                    outcome = TrialOutcome.FAILED,
                    reason = "Poor attendance",
                )

            // Assert
            ended.status shouldBe TrialStatus.ENDED
            ended.outcome shouldBe TrialOutcome.FAILED
            ended.outcomeReason shouldBe "Poor attendance"
        }

        @Test
        fun `should end trial with withdrew outcome`() {
            // Arrange
            val trial = createValidTrial()
            val savedTrial = slot<Trial>()
            every { trialRepository.findById(trial.id) } returns trial
            every { trialRepository.save(capture(savedTrial)) } answers { savedTrial.captured }

            // Act
            val ended =
                trialService.endTrial(
                    trialId = trial.id,
                    officerId = "officer-123",
                    outcome = TrialOutcome.WITHDREW,
                    reason = "Player decided to leave",
                )

            // Assert
            ended.status shouldBe TrialStatus.ENDED
            ended.outcome shouldBe TrialOutcome.WITHDREW
        }

        @Test
        fun `should throw exception when trial not found`() {
            // Arrange
            every { trialRepository.findById(any()) } returns null

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trialService.endTrial(
                    trialId = TrialId("non-existent"),
                    officerId = "officer-123",
                    outcome = TrialOutcome.FAILED,
                    reason = "Not found",
                )
            }.message shouldBe "Trial not found: non-existent"
        }
    }

    @Nested
    inner class ListTrialsTests {
        @Test
        fun `should list all trials for guild`() {
            // Arrange
            val trials =
                listOf(
                    createValidTrial(applicationId = "app-1"),
                    createValidTrial(applicationId = "app-2"),
                )
            every { trialRepository.findByGuildId(guildId, 0, 50) } returns trials

            // Act
            val result = trialService.listTrials(guildId)

            // Assert
            result.size shouldBe 2
        }

        @Test
        fun `should list trials by status`() {
            // Arrange
            val activeTrials = listOf(createValidTrial(applicationId = "app-1"))
            every { trialRepository.findByGuildIdAndStatus(guildId, TrialStatus.ACTIVE, 0, 50) } returns activeTrials

            // Act
            val result = trialService.listTrialsByStatus(guildId, TrialStatus.ACTIVE)

            // Assert
            result.size shouldBe 1
        }

        @Test
        fun `should support pagination`() {
            // Arrange
            val trials = listOf(createValidTrial())
            every { trialRepository.findByGuildId(guildId, 10, 5) } returns trials

            // Act
            val result = trialService.listTrials(guildId, offset = 10, limit = 5)

            // Assert
            verify { trialRepository.findByGuildId(guildId, 10, 5) }
            result.size shouldBe 1
        }
    }

    @Nested
    inner class CountTrialsTests {
        @Test
        fun `should count trials for guild`() {
            // Arrange
            every { trialRepository.countByGuildId(guildId) } returns 5L

            // Act
            val count = trialService.countTrials(guildId)

            // Assert
            count shouldBe 5L
        }

        @Test
        fun `should count trials by status`() {
            // Arrange
            every { trialRepository.countByGuildIdAndStatus(guildId, TrialStatus.ACTIVE) } returns 3L

            // Act
            val count = trialService.countTrialsByStatus(guildId, TrialStatus.ACTIVE)

            // Assert
            count shouldBe 3L
        }
    }

    private fun createValidTrial(
        applicationId: String = "app-${System.nanoTime()}",
        guildId: String = this.guildId.value,
    ): Trial =
        Trial.create(
            applicationId = ApplicationId(applicationId),
            guildId = GuildId(guildId),
            raidsRequired = 8,
        )
}
