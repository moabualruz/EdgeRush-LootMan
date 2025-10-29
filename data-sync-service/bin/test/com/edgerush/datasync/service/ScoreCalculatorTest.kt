package com.edgerush.datasync.service

import com.edgerush.datasync.model.LootAward
import com.edgerush.datasync.model.LootTier
import com.edgerush.datasync.model.RaiderInput
import com.edgerush.datasync.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class ScoreCalculatorTest {

    private val fixedClock = Clock.fixed(LocalDate.of(2025, 10, 24).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val mockDataTransformer = mock<WoWAuditDataTransformerService>()
    private val mockModifierService = mock<FlpsModifierService>()
    private val calculator = ScoreCalculator(mockDataTransformer, mockModifierService, fixedClock)

    @Test
    fun `calculates FLPS breakdowns matching walkthrough`() {
        val rosterData = readJson("mock_wowaudit.json") as Map<*, *>
        val logsData = readJson("mock_logs.json") as Map<*, *>
        val droptimizerData = readJson("mock_droptimizer.json") as Map<*, *>

        val specAverages = (logsData["spec_averages"] as Map<*, *>).mapValues { (_, value) ->
            val stats = value as Map<*, *>
            SpecAverage(
                dpa = (stats["dpa"] as Number).toDouble(),
                adt = (stats["adt_pct"] as Number).toDouble()
            )
        }

        val tierMap = (rosterData["tier_progress"] as List<*>).associate {
            val entry = it as Map<*, *>
            (entry["character"] as String) to (entry["tier_pieces_owned"] as Number).toInt()
        }

        val rosterEntries = rosterData["roster"] as List<*>
        val droptimizerSubmissions = (droptimizerData["submissions"] as List<*>)
            .associate { submission ->
                val sub = submission as Map<*, *>
                (sub["character"] as String) to Submission(
                    gain = (sub["simulated_gain"] as Number).toDouble()
                )
            }

        val baseline = (droptimizerData["baseline_dps"] as Map<*, *>).mapValues { (_, value) ->
            (value as Number).toDouble()
        }

        val players = (logsData["players"] as List<*>).associateBy({ (it as Map<*, *>)["character"] as String }) { it as Map<*, *> }

        val inputs = rosterEntries.map { entry ->
            val e = entry as Map<*, *>
            val name = e["character"] as String
            val role = Role.valueOf((e["role"] as String).uppercase())
            val attendance = (e["attendance_percent"] as Number).toInt()
            val vaultSlots = (e["vault_slots_unlocked"] as Number).toInt()
            val crestUsage = (e["crest_usage_ratio"] as Number).toDouble()
            val heroic = (e["heroic_bosses_cleared"] as Number).toInt()
            val tierPieces = tierMap[name] ?: 0
            val lastAwards = (e["last_awards"] as? List<*>)?.mapNotNull { award ->
                val aw = award as Map<*, *>
                val tier = when (aw["tier"] as String) {
                    "A" -> LootTier.A
                    "B" -> LootTier.B
                    else -> LootTier.C
                }
                val date = LocalDate.parse(aw["date"] as String)
                LootAward(tier = tier, awardedOn = date)
            } ?: emptyList()

            val logStats = players[name] ?: error("Missing log stats for $name")
            val spec = logStats["spec"] as String
            val dpa = (logStats["deaths_per_attempt"] as Number).toDouble()
            val adt = (logStats["avoidable_damage_pct"] as Number).toDouble()
            val specAvg = specAverages[spec] ?: error("Missing spec averages for $spec")
            val submission = droptimizerSubmissions[name] ?: error("Missing droptimizer for $name")
            val baselineOutput = baseline[spec] ?: error("Missing baseline for $spec")

            RaiderInput(
                name = name,
                role = role,
                attendancePercent = attendance,
                deathsPerAttempt = dpa,
                avoidableDamagePct = adt,
                specAverageDpa = specAvg.dpa,
                specAverageAdt = specAvg.adt,
                vaultSlots = vaultSlots,
                crestUsageRatio = crestUsage,
                heroicBossesCleared = heroic,
                tierPiecesOwned = tierPieces,
                simulatedGain = submission.gain,
                specBaselineOutput = baselineOutput,
                lastAwards = lastAwards
            )
        }

        val results = calculator.calculate(inputs)
        val rogue = results.first { it.name == "RogueB" }
        assertThat(rogue.flps).isCloseTo(0.583, within(0.001))
        assertThat(rogue.eligible).isTrue()

        val mage = results.first { it.name == "MageA" }
        assertThat(mage.flps).isCloseTo(0.557, within(0.001))

        val priest = results.first { it.name == "PriestD" }
        assertThat(priest.flps).isCloseTo(0.506, within(0.001))

        val hunter = results.first { it.name == "HunterC" }
        assertThat(hunter.eligible).isFalse()
    }

    private fun readJson(filename: String): Any {
        val path = Path.of("..", "docs", "data", filename).normalize()
        val content = Files.readString(path)
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        return mapper.readValue(content, Any::class.java)
    }

    private data class SpecAverage(val dpa: Double, val adt: Double)

    private data class Submission(val gain: Double)
}
