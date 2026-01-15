package com.edgerush.lootman.domain.trial.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for TrialOutcome enum.
 */
class TrialOutcomeTest : UnitTest() {

    @Test
    fun `should have PROMOTED outcome`() {
        // Assert
        TrialOutcome.PROMOTED.name shouldBe "PROMOTED"
    }

    @Test
    fun `should have FAILED outcome`() {
        // Assert
        TrialOutcome.FAILED.name shouldBe "FAILED"
    }

    @Test
    fun `should have WITHDREW outcome`() {
        // Assert
        TrialOutcome.WITHDREW.name shouldBe "WITHDREW"
    }

    @Test
    fun `should have REMOVED outcome`() {
        // Assert
        TrialOutcome.REMOVED.name shouldBe "REMOVED"
    }

    @Test
    fun `should have all expected outcomes`() {
        // Assert
        val expectedOutcomes = setOf("PROMOTED", "FAILED", "WITHDREW", "REMOVED")
        TrialOutcome.entries.map { it.name }.toSet() shouldBe expectedOutcomes
    }

    @Test
    fun `PROMOTED should be successful true`() {
        TrialOutcome.PROMOTED.isSuccessful shouldBe true
    }

    @Test
    fun `FAILED should be successful false`() {
        TrialOutcome.FAILED.isSuccessful shouldBe false
    }

    @Test
    fun `WITHDREW should be successful false`() {
        TrialOutcome.WITHDREW.isSuccessful shouldBe false
    }

    @Test
    fun `REMOVED should be successful false`() {
        TrialOutcome.REMOVED.isSuccessful shouldBe false
    }
}
