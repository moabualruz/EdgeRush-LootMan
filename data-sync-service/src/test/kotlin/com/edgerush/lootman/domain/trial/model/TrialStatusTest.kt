package com.edgerush.lootman.domain.trial.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for TrialStatus enum.
 */
class TrialStatusTest : UnitTest() {

    @Test
    fun `should have ACTIVE status`() {
        // Assert
        TrialStatus.ACTIVE.name shouldBe "ACTIVE"
    }

    @Test
    fun `should have PROMOTED status`() {
        // Assert
        TrialStatus.PROMOTED.name shouldBe "PROMOTED"
    }

    @Test
    fun `should have ENDED status`() {
        // Assert
        TrialStatus.ENDED.name shouldBe "ENDED"
    }

    @Test
    fun `should have EXTENDED status`() {
        // Assert
        TrialStatus.EXTENDED.name shouldBe "EXTENDED"
    }

    @Test
    fun `should have all expected statuses`() {
        // Assert
        val expectedStatuses = setOf("ACTIVE", "PROMOTED", "ENDED", "EXTENDED")
        TrialStatus.entries.map { it.name }.toSet() shouldBe expectedStatuses
    }

    @Test
    fun `ACTIVE should be terminal false`() {
        TrialStatus.ACTIVE.isTerminal shouldBe false
    }

    @Test
    fun `EXTENDED should be terminal false`() {
        TrialStatus.EXTENDED.isTerminal shouldBe false
    }

    @Test
    fun `PROMOTED should be terminal true`() {
        TrialStatus.PROMOTED.isTerminal shouldBe true
    }

    @Test
    fun `ENDED should be terminal true`() {
        TrialStatus.ENDED.isTerminal shouldBe true
    }
}
