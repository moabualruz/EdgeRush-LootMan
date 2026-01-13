package com.edgerush.lootman.infrastructure.metrics

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for MetricsConfig.
 *
 * Verifies that custom metrics are properly registered and recorded.
 */
class MetricsConfigTest : UnitTest() {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var customMetrics: CustomMetrics

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        customMetrics = CustomMetrics(meterRegistry)
    }

    @Nested
    inner class FlpsCalculationMetrics {

        @Test
        fun `should record FLPS calculation count`() {
            // Given / When
            customMetrics.recordFlpsCalculation("test-guild", 0.85)
            customMetrics.recordFlpsCalculation("test-guild", 0.92)

            // Then
            val counter = meterRegistry.find("flps.calculations.total").counter()
            counter shouldNotBe null
            counter?.count() shouldBe 2.0
        }

        @Test
        fun `should record FLPS calculation duration`() {
            // Given / When
            customMetrics.recordFlpsCalculationDuration(150L)

            // Then
            val timer = meterRegistry.find("flps.calculation.duration").timer()
            timer shouldNotBe null
            timer?.count() shouldBe 1
        }

        @Test
        fun `should record FLPS score distribution`() {
            // Given / When
            customMetrics.recordFlpsCalculation("test-guild", 0.85)
            customMetrics.recordFlpsCalculation("test-guild", 0.92)
            customMetrics.recordFlpsCalculation("test-guild", 0.55)

            // Then
            val summary = meterRegistry.find("flps.score").summary()
            summary shouldNotBe null
            summary?.count() shouldBe 3
        }
    }

    @Nested
    inner class LootAwardMetrics {

        @Test
        fun `should record loot award count`() {
            // Given / When
            customMetrics.recordLootAward("test-guild", "MYTHIC")
            customMetrics.recordLootAward("test-guild", "HEROIC")

            // Then
            val counter = meterRegistry.find("loot.awards.total")
                .tag("tier", "MYTHIC")
                .counter()
            counter shouldNotBe null
            counter?.count() shouldBe 1.0
        }

        @Test
        fun `should record loot revoke count`() {
            // Given / When
            customMetrics.recordLootRevoke("test-guild")

            // Then
            val counter = meterRegistry.find("loot.revokes.total").counter()
            counter shouldNotBe null
            counter?.count() shouldBe 1.0
        }
    }

    @Nested
    inner class AttendanceMetrics {

        @Test
        fun `should record attendance tracking`() {
            // Given / When
            customMetrics.recordAttendanceTracked("test-guild", 25)

            // Then
            val counter = meterRegistry.find("attendance.tracked.total").counter()
            counter shouldNotBe null
            counter?.count() shouldBe 1.0
        }

        @Test
        fun `should record raiders tracked gauge`() {
            // Given / When
            customMetrics.recordAttendanceTracked("test-guild", 25)

            // Then
            val summary = meterRegistry.find("attendance.raiders.count").summary()
            summary shouldNotBe null
            summary?.count() shouldBe 1
        }
    }

    @Nested
    inner class AuditMetrics {

        @Test
        fun `should record audit log entries`() {
            // Given / When
            customMetrics.recordAuditEntry("CREATE", "Guild")
            customMetrics.recordAuditEntry("UPDATE", "Raider")

            // Then
            val createCounter = meterRegistry.find("audit.entries.total")
                .tag("operation", "CREATE")
                .counter()
            createCounter shouldNotBe null
            createCounter?.count() shouldBe 1.0

            val updateCounter = meterRegistry.find("audit.entries.total")
                .tag("operation", "UPDATE")
                .counter()
            updateCounter shouldNotBe null
            updateCounter?.count() shouldBe 1.0
        }
    }
}
