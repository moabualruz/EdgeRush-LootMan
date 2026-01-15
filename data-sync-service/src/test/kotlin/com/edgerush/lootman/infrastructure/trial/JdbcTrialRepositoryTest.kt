package com.edgerush.lootman.infrastructure.trial

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for JdbcTrialRepository.
 */
class JdbcTrialRepositoryTest : IntegrationTest() {
    @Autowired
    private lateinit var repository: JdbcTrialRepository

    private val guildId = GuildId("test-guild-123")

    @BeforeEach
    fun setup() {
        // Clean up test data
        cleanupTestTrials()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new trial`() {
            // Arrange
            val trial = createValidTrial()

            // Act
            val saved = repository.save(trial)

            // Assert
            saved shouldBe trial
            repository.findById(trial.id) shouldBe trial
        }

        @Test
        fun `should update existing trial`() {
            // Arrange
            val trial = createValidTrial()
            repository.save(trial)

            val updated =
                trial.updateMetrics(
                    raidsAttended = 4,
                    attendanceRate = 0.85,
                    averagePerformance = 75.0,
                    deathsPerRaid = 1.2,
                )

            // Act
            val saved = repository.save(updated)

            // Assert
            saved.raidsAttended shouldBe 4
            saved.attendanceRate shouldBe 0.85
            saved.averagePerformance shouldBe 75.0
            saved.deathsPerRaid shouldBe 1.2
        }

        @Test
        fun `should save trial with promoted status`() {
            // Arrange
            val trial =
                createValidTrial()
                    .updateMetrics(raidsAttended = 8, attendanceRate = 0.9, averagePerformance = 80.0, deathsPerRaid = 0.5)
                    .promote("officer-123", "Excellent performance")

            // Act
            val saved = repository.save(trial)

            // Assert
            val loaded = repository.findById(trial.id)
            loaded shouldNotBe null
            loaded!!.status shouldBe TrialStatus.PROMOTED
            loaded.outcome shouldBe TrialOutcome.PROMOTED
            loaded.promotedBy shouldBe "officer-123"
            loaded.outcomeReason shouldBe "Excellent performance"
        }

        @Test
        fun `should save trial with ended status`() {
            // Arrange
            val trial =
                createValidTrial()
                    .endTrial("officer-123", TrialOutcome.FAILED, "Poor attendance")

            // Act
            repository.save(trial)

            // Assert
            val loaded = repository.findById(trial.id)
            loaded shouldNotBe null
            loaded!!.status shouldBe TrialStatus.ENDED
            loaded.outcome shouldBe TrialOutcome.FAILED
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should find existing trial by ID`() {
            // Arrange
            val trial = createValidTrial()
            repository.save(trial)

            // Act
            val found = repository.findById(trial.id)

            // Assert
            found shouldNotBe null
            found!!.id shouldBe trial.id
            found.applicationId shouldBe trial.applicationId
            found.guildId shouldBe trial.guildId
        }

        @Test
        fun `should return null for non-existent ID`() {
            // Act
            val found = repository.findById(TrialId("non-existent-id"))

            // Assert
            found shouldBe null
        }
    }

    @Nested
    inner class FindByApplicationIdTests {
        @Test
        fun `should find trial by application ID`() {
            // Arrange
            val trial = createValidTrial()
            repository.save(trial)

            // Act
            val found = repository.findByApplicationId(trial.applicationId)

            // Assert
            found shouldNotBe null
            found!!.applicationId shouldBe trial.applicationId
        }

        @Test
        fun `should return null when no trial exists for application`() {
            // Act
            val found = repository.findByApplicationId(ApplicationId("non-existent-app"))

            // Assert
            found shouldBe null
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should find all trials for guild`() {
            // Arrange
            val trial1 = createValidTrial(applicationId = "app-1")
            val trial2 = createValidTrial(applicationId = "app-2")
            val trial3 = createValidTrial(applicationId = "app-3", guildId = "other-guild")
            repository.save(trial1)
            repository.save(trial2)
            repository.save(trial3)

            // Act
            val found = repository.findByGuildId(guildId)

            // Assert
            found shouldHaveSize 2
            found.map { it.applicationId.value }.toSet() shouldBe setOf("app-1", "app-2")
        }

        @Test
        fun `should return empty list for guild with no trials`() {
            // Act
            val found = repository.findByGuildId(GuildId("empty-guild"))

            // Assert
            found.shouldBeEmpty()
        }

        @Test
        fun `should support pagination`() {
            // Arrange
            (1..5).forEach { i ->
                repository.save(createValidTrial(applicationId = "app-$i"))
            }

            // Act
            val page1 = repository.findByGuildId(guildId, offset = 0, limit = 2)
            val page2 = repository.findByGuildId(guildId, offset = 2, limit = 2)

            // Assert
            page1 shouldHaveSize 2
            page2 shouldHaveSize 2
        }
    }

    @Nested
    inner class FindByGuildIdAndStatusTests {
        @Test
        fun `should find trials by status`() {
            // Arrange
            val activeTrial = createValidTrial(applicationId = "app-active")
            val promotedTrial =
                createValidTrial(applicationId = "app-promoted")
                    .promote("officer-123", "Great job")
            repository.save(activeTrial)
            repository.save(promotedTrial)

            // Act
            val active = repository.findByGuildIdAndStatus(guildId, TrialStatus.ACTIVE)
            val promoted = repository.findByGuildIdAndStatus(guildId, TrialStatus.PROMOTED)

            // Assert
            active shouldHaveSize 1
            active.first().applicationId.value shouldBe "app-active"
            promoted shouldHaveSize 1
            promoted.first().applicationId.value shouldBe "app-promoted"
        }
    }

    @Nested
    inner class FindActiveTrialsTests {
        @Test
        fun `should find active and extended trials`() {
            // Arrange
            val activeTrial = createValidTrial(applicationId = "app-active")
            val extendedTrial =
                createValidTrial(applicationId = "app-extended")
                    .extend("officer-123", 4, "Needs more time")
            val promotedTrial =
                createValidTrial(applicationId = "app-promoted")
                    .promote("officer-123", "Great job")
            repository.save(activeTrial)
            repository.save(extendedTrial)
            repository.save(promotedTrial)

            // Act
            val found = repository.findActiveTrialsByGuildId(guildId)

            // Assert
            found shouldHaveSize 2
            found.map { it.status }.toSet() shouldBe setOf(TrialStatus.ACTIVE, TrialStatus.EXTENDED)
        }
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should find trial by raider ID`() {
            // Arrange
            val trial =
                Trial.create(
                    applicationId = ApplicationId("app-123"),
                    guildId = guildId,
                    raidsRequired = 8,
                    raiderId = 12345L,
                )
            repository.save(trial)

            // Act
            val found = repository.findByRaiderId(12345L)

            // Assert
            found shouldNotBe null
            found!!.raiderId shouldBe 12345L
        }

        @Test
        fun `should return null when raider has no trial`() {
            // Act
            val found = repository.findByRaiderId(99999L)

            // Assert
            found shouldBe null
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should count trials for guild`() {
            // Arrange
            (1..3).forEach { i ->
                repository.save(createValidTrial(applicationId = "app-$i"))
            }

            // Act
            val count = repository.countByGuildId(guildId)

            // Assert
            count shouldBe 3
        }

        @Test
        fun `should count trials by status`() {
            // Arrange
            val activeTrial = createValidTrial(applicationId = "app-1")
            val promotedTrial =
                createValidTrial(applicationId = "app-2")
                    .promote("officer-123", "Good job")
            repository.save(activeTrial)
            repository.save(promotedTrial)

            // Act
            val activeCount = repository.countByGuildIdAndStatus(guildId, TrialStatus.ACTIVE)
            val promotedCount = repository.countByGuildIdAndStatus(guildId, TrialStatus.PROMOTED)

            // Assert
            activeCount shouldBe 1
            promotedCount shouldBe 1
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete trial by ID`() {
            // Arrange
            val trial = createValidTrial()
            repository.save(trial)

            // Act
            repository.deleteById(trial.id)

            // Assert
            repository.findById(trial.id) shouldBe null
        }

        @Test
        fun `should not throw when deleting non-existent trial`() {
            // Act & Assert (should not throw)
            repository.deleteById(TrialId("non-existent"))
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return true for existing trial`() {
            // Arrange
            val trial = createValidTrial()
            repository.save(trial)

            // Act
            val exists = repository.existsById(trial.id)

            // Assert
            exists shouldBe true
        }

        @Test
        fun `should return false for non-existent trial`() {
            // Act
            val exists = repository.existsById(TrialId("non-existent"))

            // Assert
            exists shouldBe false
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

    private fun cleanupTestTrials() {
        // Clean up trials from test guild
        repository.findByGuildId(guildId).forEach {
            repository.deleteById(it.id)
        }
        repository.findByGuildId(GuildId("other-guild")).forEach {
            repository.deleteById(it.id)
        }
    }
}
