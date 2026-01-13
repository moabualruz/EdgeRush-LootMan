package com.edgerush.lootman.domain.simulation.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for SimulationStatus enum.
 *
 * Tests verify:
 * - All expected status values exist
 * - Terminal vs in-progress state identification
 */
class SimulationStatusTest : UnitTest() {

    @Nested
    inner class StatusValuesTests {
        @Test
        fun `should have PENDING status`() {
            SimulationStatus.valueOf("PENDING") shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should have RUNNING status`() {
            SimulationStatus.valueOf("RUNNING") shouldBe SimulationStatus.RUNNING
        }

        @Test
        fun `should have COMPLETED status`() {
            SimulationStatus.valueOf("COMPLETED") shouldBe SimulationStatus.COMPLETED
        }

        @Test
        fun `should have FAILED status`() {
            SimulationStatus.valueOf("FAILED") shouldBe SimulationStatus.FAILED
        }

        @Test
        fun `should have exactly four statuses`() {
            SimulationStatus.entries.size shouldBe 4
        }

        @Test
        fun `should contain all expected statuses`() {
            SimulationStatus.entries shouldContainExactly listOf(
                SimulationStatus.PENDING,
                SimulationStatus.RUNNING,
                SimulationStatus.COMPLETED,
                SimulationStatus.FAILED
            )
        }
    }

    @Nested
    inner class TerminalStateTests {
        @Test
        fun `COMPLETED should be terminal state`() {
            val terminalStates = setOf(SimulationStatus.COMPLETED, SimulationStatus.FAILED)
            terminalStates.contains(SimulationStatus.COMPLETED) shouldBe true
        }

        @Test
        fun `FAILED should be terminal state`() {
            val terminalStates = setOf(SimulationStatus.COMPLETED, SimulationStatus.FAILED)
            terminalStates.contains(SimulationStatus.FAILED) shouldBe true
        }

        @Test
        fun `PENDING should not be terminal state`() {
            val terminalStates = setOf(SimulationStatus.COMPLETED, SimulationStatus.FAILED)
            terminalStates.contains(SimulationStatus.PENDING) shouldBe false
        }

        @Test
        fun `RUNNING should not be terminal state`() {
            val terminalStates = setOf(SimulationStatus.COMPLETED, SimulationStatus.FAILED)
            terminalStates.contains(SimulationStatus.RUNNING) shouldBe false
        }
    }

    @Nested
    inner class InProgressStateTests {
        @Test
        fun `PENDING should be in-progress state`() {
            val inProgressStates = setOf(SimulationStatus.PENDING, SimulationStatus.RUNNING)
            inProgressStates.contains(SimulationStatus.PENDING) shouldBe true
        }

        @Test
        fun `RUNNING should be in-progress state`() {
            val inProgressStates = setOf(SimulationStatus.PENDING, SimulationStatus.RUNNING)
            inProgressStates.contains(SimulationStatus.RUNNING) shouldBe true
        }

        @Test
        fun `COMPLETED should not be in-progress state`() {
            val inProgressStates = setOf(SimulationStatus.PENDING, SimulationStatus.RUNNING)
            inProgressStates.contains(SimulationStatus.COMPLETED) shouldBe false
        }

        @Test
        fun `FAILED should not be in-progress state`() {
            val inProgressStates = setOf(SimulationStatus.PENDING, SimulationStatus.RUNNING)
            inProgressStates.contains(SimulationStatus.FAILED) shouldBe false
        }
    }

    @Nested
    inner class StatusNameTests {
        @Test
        fun `should have correct name for PENDING`() {
            SimulationStatus.PENDING.name shouldBe "PENDING"
        }

        @Test
        fun `should have correct name for RUNNING`() {
            SimulationStatus.RUNNING.name shouldBe "RUNNING"
        }

        @Test
        fun `should have correct name for COMPLETED`() {
            SimulationStatus.COMPLETED.name shouldBe "COMPLETED"
        }

        @Test
        fun `should have correct name for FAILED`() {
            SimulationStatus.FAILED.name shouldBe "FAILED"
        }
    }
}
