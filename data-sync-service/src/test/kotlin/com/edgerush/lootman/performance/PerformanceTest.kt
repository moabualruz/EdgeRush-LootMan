package com.edgerush.lootman.performance

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.GuildId as AttendanceGuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId as AttendanceRaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID
import kotlin.system.measureTimeMillis

/**
 * Performance tests for EdgeRush LootMan system.
 *
 * Tests verify that key operations meet performance requirements:
 * - FLPS calculation: <1 second for 30 raiders
 * - Loot history query: <500ms for 1000 records
 * - Attendance report: <500ms for 90-day range
 * - Raid scheduling: <200ms
 */
class PerformanceTest : IntegrationTest() {
    @Autowired
    private lateinit var flpsCalculationService: FlpsCalculationService

    @Autowired
    private lateinit var lootAwardRepository: LootAwardRepository

    @Autowired
    private lateinit var attendanceRepository: AttendanceRepository

    private val testGuildId = GuildId(UUID.randomUUID().toString())
    private val testAttendanceGuildId = AttendanceGuildId(testGuildId.value)

    @BeforeEach
    fun setupPerformanceTestData() {
        // Data will be created in each test as needed
    }

    @Test
    fun `should calculate FLPS for 30 raiders in less than 1 second`() {
        // Arrange: Create test data for 30 raiders
        val raiderCount = 30
        val raiders = (1..raiderCount).map { index ->
            RaiderData(
                raiderId = RaiderId(UUID.randomUUID().toString()),
                acs = AttendanceCommitmentScore.of(0.85 + (index % 10) * 0.01),
                mas = MechanicalAdherenceScore.of(0.80 + (index % 15) * 0.01),
                eps = ExternalPreparationScore.of(0.90 + (index % 5) * 0.01),
                uv = UpgradeValue.of(0.75 + (index % 20) * 0.01),
                tb = TierBonus.of(if (index % 3 == 0) 1.15 else 1.0),
                rm = RoleMultiplier.of(if (index % 4 == 0) 1.1 else 1.0),
                rdf = RecencyDecayFactor.of(0.95 - (index % 10) * 0.05),
            )
        }

        // Act: Measure time to calculate FLPS for all raiders
        val executionTime =
            measureTimeMillis {
                raiders.forEach { raider ->
                    val rms =
                        RaiderMeritScore.fromComponents(
                            raider.acs,
                            raider.mas,
                            raider.eps,
                            0.4,
                            0.4,
                            0.2,
                        )
                    val ipi =
                        ItemPriorityIndex.fromComponents(
                            raider.uv,
                            raider.tb,
                            raider.rm,
                            0.45,
                            0.35,
                            0.20,
                        )
                    flpsCalculationService.calculateFlps(rms, ipi, raider.rdf)
                }
            }

        // Assert: Should complete in less than 1 second
        println("FLPS calculation for $raiderCount raiders took ${executionTime}ms")
        assertTrue(
            executionTime < 1000,
            "FLPS calculation for $raiderCount raiders should complete in <1000ms, but took ${executionTime}ms",
        )
    }

    @Test
    fun `should query loot history for 1000 records in less than 500ms`() {
        // Arrange: Create 1000 loot award records
        val recordCount = 1000

        println("Creating $recordCount loot award records...")
        val creationTime =
            measureTimeMillis {
                (1..recordCount).forEach { index ->
                    val lootAward =
                        LootAward.create(
                            itemId = ItemId(200000L + index),
                            raiderId = RaiderId(UUID.randomUUID().toString()),
                            guildId = testGuildId,
                            flpsScore = FlpsScore.of(0.75 + (index % 25) * 0.01),
                            tier = LootTier.HEROIC,
                        )
                    lootAwardRepository.save(lootAward)
                }
            }
        println("Created $recordCount records in ${creationTime}ms")

        // Act: Measure time to query all loot awards for the guild
        val executionTime =
            measureTimeMillis {
                val awards = lootAwardRepository.findByGuildId(testGuildId)
                assertTrue(awards.size == recordCount, "Expected $recordCount records, got ${awards.size}")
            }

        // Assert: Should complete in less than 500ms
        println("Loot history query for $recordCount records took ${executionTime}ms")
        assertTrue(
            executionTime < 500,
            "Loot history query for $recordCount records should complete in <500ms, but took ${executionTime}ms",
        )
    }

    @Test
    fun `should generate attendance report for 90-day range in less than 500ms`() {
        // Arrange: Create attendance records for 30 raiders over 90 days
        val raiderCount = 30
        val dayCount = 90
        val raidsPerWeek = 3

        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(dayCount.toLong())

        println("Creating attendance records for $raiderCount raiders over $dayCount days...")
        val creationTime =
            measureTimeMillis {
                (1..raiderCount).forEach { raiderIndex ->
                    val raiderId = AttendanceRaiderId(raiderIndex.toLong())

                    // Create aggregated attendance record for the period
                    // Simulate 3 raids per week over 90 days
                    val totalRaids = (dayCount / 7) * raidsPerWeek
                    val attendedRaids = if (raiderIndex % 10 == 0) (totalRaids * 0.7).toInt() else (totalRaids * 0.95).toInt()

                    val record =
                        AttendanceRecord.create(
                            raiderId = raiderId,
                            guildId = testAttendanceGuildId,
                            instance = "Nerub-ar Palace",
                            encounter = null,
                            startDate = startDate,
                            endDate = endDate,
                            attendedRaids = attendedRaids,
                            totalRaids = totalRaids,
                        )
                    attendanceRepository.save(record)
                }
            }
        println("Created records in ${creationTime}ms")

        // Act: Measure time to query attendance for 90-day range
        val executionTime =
            measureTimeMillis {
                val records = attendanceRepository.findByGuildIdAndDateRange(testAttendanceGuildId, startDate, endDate)
                assertTrue(records.isNotEmpty(), "Expected attendance records, got none")
            }

        // Assert: Should complete in less than 500ms
        println("Attendance report query for 90-day range took ${executionTime}ms")
        assertTrue(
            executionTime < 500,
            "Attendance report for 90-day range should complete in <500ms, but took ${executionTime}ms",
        )
    }

    @Test
    fun `should perform raid scheduling operations in less than 200ms`() {
        // Arrange: Prepare data for raid scheduling simulation
        val raiderCount = 30
        val raiders =
            (1..raiderCount).map {
                RaiderId(UUID.randomUUID().toString())
            }

        // Act: Measure time for typical raid scheduling operations
        // This simulates checking availability, calculating scores, and organizing roster
        val executionTime =
            measureTimeMillis {
                // Simulate checking raider availability (database query simulation)
                val availableRaiders = raiders.filter { it.value.toString().hashCode() % 10 != 0 }

                // Simulate calculating FLPS for available raiders
                availableRaiders.forEach { _ ->
                    val acs = AttendanceCommitmentScore.of(0.85)
                    val mas = MechanicalAdherenceScore.of(0.80)
                    val eps = ExternalPreparationScore.of(0.90)
                    val uv = UpgradeValue.of(0.75)
                    val tb = TierBonus.of(1.0)
                    val rm = RoleMultiplier.of(1.0)
                    val rdf = RecencyDecayFactor.of(1.0)

                    val rms = RaiderMeritScore.fromComponents(acs, mas, eps, 0.4, 0.4, 0.2)
                    val ipi = ItemPriorityIndex.fromComponents(uv, tb, rm, 0.45, 0.35, 0.20)
                    flpsCalculationService.calculateFlps(rms, ipi, rdf)
                }

                // Simulate roster organization (sorting and grouping)
                val sortedRaiders = availableRaiders.sortedBy { it.value.toString() }
                val tanks = sortedRaiders.take(2)
                val healers = sortedRaiders.drop(2).take(5)
                val dps = sortedRaiders.drop(7)

                assertTrue(tanks.size == 2, "Expected 2 tanks")
                assertTrue(healers.size == 5, "Expected 5 healers")
                assertTrue(dps.isNotEmpty(), "Expected DPS raiders")
            }

        // Assert: Should complete in less than 200ms
        println("Raid scheduling operations took ${executionTime}ms")
        assertTrue(
            executionTime < 200,
            "Raid scheduling operations should complete in <200ms, but took ${executionTime}ms",
        )
    }

    /**
     * Data class to hold raider test data for FLPS calculations.
     */
    private data class RaiderData(
        val raiderId: RaiderId,
        val acs: AttendanceCommitmentScore,
        val mas: MechanicalAdherenceScore,
        val eps: ExternalPreparationScore,
        val uv: UpgradeValue,
        val tb: TierBonus,
        val rm: RoleMultiplier,
        val rdf: RecencyDecayFactor,
    )
}
