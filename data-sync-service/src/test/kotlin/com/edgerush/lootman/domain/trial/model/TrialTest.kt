package com.edgerush.lootman.domain.trial.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for Trial entity.
 */
class TrialTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid trial with required fields`() {
            // Arrange & Act
            val trial =
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = GuildId("guild-123"),
                    raidsRequired = 8,
                )

            // Assert
            trial shouldNotBe null
            trial.applicationId shouldBe ApplicationId("app-123")
            trial.guildId shouldBe GuildId("guild-123")
            trial.raidsRequired shouldBe 8
            trial.status shouldBe TrialStatus.ACTIVE
            trial.raidsAttended shouldBe 0
            trial.attendanceRate shouldBe null
            trial.averagePerformance shouldBe null
            trial.deathsPerRaid shouldBe null
        }

        @Test
        fun `should generate unique ID on create`() {
            // Arrange & Act
            val trial1 = createValidTrial()
            val trial2 = createValidTrial()

            // Assert
            trial1.id shouldNotBe trial2.id
        }

        @Test
        fun `should set createdAt on create`() {
            // Arrange
            val before = Instant.now()

            // Act
            val trial = createValidTrial()
            val after = Instant.now()

            // Assert
            (trial.createdAt >= before) shouldBe true
            (trial.createdAt <= after) shouldBe true
        }

        @Test
        fun `should set startDate on create`() {
            // Arrange
            val before = Instant.now()

            // Act
            val trial = createValidTrial()
            val after = Instant.now()

            // Assert
            (trial.startDate >= before) shouldBe true
            (trial.startDate <= after) shouldBe true
        }

        @Test
        fun `should calculate expected end date based on raids required`() {
            // Arrange & Act
            val trial =
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = GuildId("guild-123"),
                    raidsRequired = 8,
                )

            // Assert - assuming ~2 raids per week, 8 raids = ~4 weeks
            val fourWeeksFromNow = Instant.now().plus(28, ChronoUnit.DAYS)
            trial.expectedEndDate shouldNotBe null
            // Should be approximately 4 weeks out (within a day tolerance)
            val diff = ChronoUnit.DAYS.between(trial.startDate, trial.expectedEndDate)
            diff shouldBe 28 // 4 weeks
        }

        @Test
        fun `should set status to ACTIVE on create`() {
            // Arrange & Act
            val trial = createValidTrial()

            // Assert
            trial.status shouldBe TrialStatus.ACTIVE
        }

        @Test
        fun `should have null outcome on create`() {
            // Arrange & Act
            val trial = createValidTrial()

            // Assert
            trial.outcome shouldBe null
            trial.outcomeReason shouldBe null
        }

        @Test
        fun `should have null endDate on create`() {
            // Arrange & Act
            val trial = createValidTrial()

            // Assert
            trial.endDate shouldBe null
        }

        @Test
        fun `should allow optional raider ID`() {
            // Arrange & Act
            val trial =
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = GuildId("guild-123"),
                    raidsRequired = 8,
                    raiderId = 12345L,
                )

            // Assert
            trial.raiderId shouldBe 12345L
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when raids required is zero`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = GuildId("guild-123"),
                    raidsRequired = 0,
                )
            }.message shouldBe "Raids required must be positive"
        }

        @Test
        fun `should throw exception when raids required is negative`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = GuildId("guild-123"),
                    raidsRequired = -5,
                )
            }.message shouldBe "Raids required must be positive"
        }
    }

    @Nested
    inner class MetricsUpdateTests {
        @Test
        fun `should update attendance metrics`() {
            // Arrange
            val trial = createValidTrial()

            // Act
            val updated =
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 72.5,
                    deathsPerRaid = 1.2,
                )

            // Assert
            updated.raidsAttended shouldBe 4
            updated.attendanceRate shouldBe 0.85
            updated.averagePerformance shouldBe 72.5
            updated.deathsPerRaid shouldBe 1.2
        }

        @Test
        fun `should throw exception when attendance rate is negative`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = -0.1,
                    averagePerformance = 72.5,
                    deathsPerRaid = 1.2,
                )
            }.message shouldBe "Attendance rate must be between 0 and 1"
        }

        @Test
        fun `should throw exception when attendance rate is greater than 1`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 1.5,
                    averagePerformance = 72.5,
                    deathsPerRaid = 1.2,
                )
            }.message shouldBe "Attendance rate must be between 0 and 1"
        }

        @Test
        fun `should throw exception when average performance is out of range`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 150.0,
                    deathsPerRaid = 1.2,
                )
            }.message shouldBe "Average performance must be between 0 and 100"
        }

        @Test
        fun `should throw exception when deaths per raid is negative`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 72.5,
                    deathsPerRaid = -1.0,
                )
            }.message shouldBe "Deaths per raid cannot be negative"
        }

        @Test
        fun `should throw exception when updating terminated trial`() {
            // Arrange
            val trial = createValidTrial().promote("officer-123", "Great performance")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                trial.updateMetrics(
                    raidsAttended = 5,
                    attendanceRate = 0.90,
                    averagePerformance = 80.0,
                    deathsPerRaid = 0.5,
                )
            }.message shouldBe "Cannot update metrics on a completed trial"
        }

        @Test
        fun `should update lastUpdated when updating metrics`() {
            // Arrange
            val trial = createValidTrial()
            val originalUpdatedAt = trial.lastUpdated

            // Small delay to ensure time difference
            Thread.sleep(10)

            // Act
            val updated =
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 72.5,
                    deathsPerRaid = 1.2,
                )

            // Assert
            updated.lastUpdated shouldNotBe originalUpdatedAt
            (updated.lastUpdated > originalUpdatedAt) shouldBe true
        }
    }

    @Nested
    inner class PromotionTests {
        @Test
        fun `should promote trial`() {
            // Arrange
            val trial = createValidTrial()
            val promoterId = "officer-123"
            val reason = "Excellent performance during trial"

            // Act
            val promoted = trial.promote(promoterId, reason)

            // Assert
            promoted.status shouldBe TrialStatus.PROMOTED
            promoted.outcome shouldBe TrialOutcome.PROMOTED
            promoted.outcomeReason shouldBe reason
            promoted.promotedBy shouldBe promoterId
            promoted.promotedAt shouldNotBe null
            promoted.endDate shouldNotBe null
        }

        @Test
        fun `should throw exception when promoting non-active trial`() {
            // Arrange
            val trial = createValidTrial().endTrial("officer-123", TrialOutcome.FAILED, "Poor attendance")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                trial.promote("officer-456", "Actually they were great")
            }.message shouldBe "Can only promote active or extended trials"
        }

        @Test
        fun `should allow promoting extended trial`() {
            // Arrange
            val trial = createValidTrial().extend("officer-123", 4, "Needs more time")

            // Act
            val promoted = trial.promote("officer-123", "Improved significantly")

            // Assert
            promoted.status shouldBe TrialStatus.PROMOTED
            promoted.outcome shouldBe TrialOutcome.PROMOTED
        }
    }

    @Nested
    inner class ExtensionTests {
        @Test
        fun `should extend trial`() {
            // Arrange
            val trial = createValidTrial()
            val originalExpectedEnd = trial.expectedEndDate
            val extenderId = "officer-123"
            val additionalRaids = 4
            val reason = "Needs more evaluation time"

            // Act
            val extended = trial.extend(extenderId, additionalRaids, reason)

            // Assert
            extended.status shouldBe TrialStatus.EXTENDED
            extended.raidsRequired shouldBe (8 + additionalRaids)
            extended.outcomeReason shouldBe reason
            extended.expectedEndDate shouldNotBe originalExpectedEnd
        }

        @Test
        fun `should throw exception when extending non-active trial`() {
            // Arrange
            val trial = createValidTrial().promote("officer-123", "Great job")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                trial.extend("officer-456", 4, "Actually needs more time")
            }.message shouldBe "Can only extend active or already extended trials"
        }

        @Test
        fun `should throw exception when additional raids is not positive`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.extend("officer-123", 0, "More time needed")
            }.message shouldBe "Additional raids must be positive"
        }

        @Test
        fun `should allow extending already extended trial`() {
            // Arrange
            val trial =
                createValidTrial()
                    .extend("officer-123", 4, "First extension")

            // Act
            val extendedAgain = trial.extend("officer-123", 2, "Second extension")

            // Assert
            extendedAgain.status shouldBe TrialStatus.EXTENDED
            extendedAgain.raidsRequired shouldBe (8 + 4 + 2)
        }
    }

    @Nested
    inner class EndTrialTests {
        @Test
        fun `should end trial with failed outcome`() {
            // Arrange
            val trial = createValidTrial()
            val officerId = "officer-123"
            val reason = "Poor attendance and performance"

            // Act
            val ended = trial.endTrial(officerId, TrialOutcome.FAILED, reason)

            // Assert
            ended.status shouldBe TrialStatus.ENDED
            ended.outcome shouldBe TrialOutcome.FAILED
            ended.outcomeReason shouldBe reason
            ended.endDate shouldNotBe null
        }

        @Test
        fun `should end trial with withdrew outcome`() {
            // Arrange
            val trial = createValidTrial()

            // Act
            val ended = trial.endTrial("officer-123", TrialOutcome.WITHDREW, "Player decided to leave")

            // Assert
            ended.status shouldBe TrialStatus.ENDED
            ended.outcome shouldBe TrialOutcome.WITHDREW
        }

        @Test
        fun `should end trial with removed outcome`() {
            // Arrange
            val trial = createValidTrial()

            // Act
            val ended = trial.endTrial("officer-123", TrialOutcome.REMOVED, "Behavioral issues")

            // Assert
            ended.status shouldBe TrialStatus.ENDED
            ended.outcome shouldBe TrialOutcome.REMOVED
        }

        @Test
        fun `should throw exception when ending with PROMOTED outcome`() {
            // Arrange
            val trial = createValidTrial()

            // Act & Assert
            shouldThrow<IllegalArgumentException> {
                trial.endTrial("officer-123", TrialOutcome.PROMOTED, "Good job")
            }.message shouldBe "Use promote() method for PROMOTED outcome"
        }

        @Test
        fun `should throw exception when ending already terminated trial`() {
            // Arrange
            val trial = createValidTrial().endTrial("officer-123", TrialOutcome.FAILED, "Poor performance")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                trial.endTrial("officer-456", TrialOutcome.WITHDREW, "Actually they left")
            }.message shouldBe "Can only end active or extended trials"
        }
    }

    @Nested
    inner class ProgressTests {
        @Test
        fun `should calculate progress percentage`() {
            // Arrange
            val trial =
                createValidTrial().updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.9,
                    averagePerformance = 75.0,
                    deathsPerRaid = 1.0,
                )

            // Act
            val progress = trial.progressPercentage

            // Assert
            progress shouldBe 50.0 // 4 out of 8 raids
        }

        @Test
        fun `should cap progress at 100 percent`() {
            // Arrange
            val trial =
                createValidTrial().updateMetrics(
                    raidsAttended = 10, // More than required 8
                    attendanceRate = 0.95,
                    averagePerformance = 80.0,
                    deathsPerRaid = 0.5,
                )

            // Act
            val progress = trial.progressPercentage

            // Assert
            progress shouldBe 100.0
        }

        @Test
        fun `should return zero progress for new trial`() {
            // Arrange
            val trial = createValidTrial()

            // Act
            val progress = trial.progressPercentage

            // Assert
            progress shouldBe 0.0
        }

        @Test
        fun `should check if trial is complete`() {
            // Arrange
            val incomplete =
                createValidTrial().updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 72.0,
                    deathsPerRaid = 1.0,
                )
            val complete =
                createValidTrial().updateMetrics(
                    raidsAttended = 8,
                    attendanceRate = 0.90,
                    averagePerformance = 75.0,
                    deathsPerRaid = 0.8,
                )

            // Assert
            incomplete.isComplete shouldBe false
            complete.isComplete shouldBe true
        }
    }

    @Nested
    inner class ReconstructTests {
        @Test
        fun `should reconstruct trial from persisted data`() {
            // Arrange
            val id = TrialId("trial-123")
            val applicationId = ApplicationId("app-456")
            val guildId = GuildId("guild-789")
            val startDate = Instant.now().minus(14, ChronoUnit.DAYS)
            val expectedEndDate = Instant.now().plus(14, ChronoUnit.DAYS)
            val createdAt = startDate

            // Act
            val trial =
                Trial.reconstruct(
                    id = id,
                    applicationId = applicationId,
                    raiderId = 12345L,
                    guildId = guildId,
                    status = TrialStatus.EXTENDED,
                    startDate = startDate,
                    endDate = null,
                    expectedEndDate = expectedEndDate,
                    raidsAttended = 6,
                    raidsRequired = 12,
                    attendanceRate = 0.85,
                    averagePerformance = 72.5,
                    deathsPerRaid = 1.2,
                    outcome = null,
                    outcomeReason = "Extended for additional evaluation",
                    promotedBy = null,
                    promotedAt = null,
                    createdAt = createdAt,
                    lastUpdated = Instant.now(),
                )

            // Assert
            trial.id shouldBe id
            trial.applicationId shouldBe applicationId
            trial.raiderId shouldBe 12345L
            trial.guildId shouldBe guildId
            trial.status shouldBe TrialStatus.EXTENDED
            trial.raidsAttended shouldBe 6
            trial.raidsRequired shouldBe 12
            trial.attendanceRate shouldBe 0.85
            trial.averagePerformance shouldBe 72.5
            trial.deathsPerRaid shouldBe 1.2
            trial.outcomeReason shouldBe "Extended for additional evaluation"
        }
    }

    private fun createValidTrial(): Trial =
        Trial.create(
            applicationId = ApplicationId("app-123"),
            guildId = GuildId("guild-123"),
            raidsRequired = 8,
        )
}
