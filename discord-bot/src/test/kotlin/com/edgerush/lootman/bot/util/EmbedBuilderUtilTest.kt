package com.edgerush.lootman.bot.util

import com.edgerush.lootman.bot.client.IpiBreakdown
import com.edgerush.lootman.bot.client.LeaderboardEntry
import com.edgerush.lootman.bot.client.LootAwardEntry
import com.edgerush.lootman.bot.client.RaiderFlpsResponse
import com.edgerush.lootman.bot.client.RmsBreakdown
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.awt.Color

class EmbedBuilderUtilTest {

    @Nested
    inner class CreateFlpsEmbed {

        @Test
        fun `should create embed with correct title`() {
            val raider = createTestRaider()
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            embed.title shouldBe "FLPS Score - TestChar"
        }

        @Test
        fun `should show green color for high eligible score`() {
            val raider = createTestRaider(flps = 0.9, eligible = true)
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            embed.color shouldBe Color(46, 204, 113) // GREEN
        }

        @Test
        fun `should show red color for ineligible raider`() {
            val raider = createTestRaider(eligible = false)
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            embed.color shouldBe Color(231, 76, 60) // RED
        }

        @Test
        fun `should include RMS breakdown`() {
            val raider = createTestRaider()
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            val rmsField = embed.fields.find { it.name?.contains("RMS") == true }
            rmsField shouldNotBe null
            rmsField?.value shouldContain "ACS"
            rmsField?.value shouldContain "MAS"
            rmsField?.value shouldContain "EPS"
        }

        @Test
        fun `should include IPI breakdown`() {
            val raider = createTestRaider()
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            val ipiField = embed.fields.find { it.name?.contains("IPI") == true }
            ipiField shouldNotBe null
            ipiField?.value shouldContain "UV"
            ipiField?.value shouldContain "Tier"
            ipiField?.value shouldContain "Role"
        }

        @Test
        fun `should include eligibility status`() {
            val raider = createTestRaider(eligible = true)
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            val eligibilityField = embed.fields.find { it.name == "Eligibility" }
            eligibilityField shouldNotBe null
            eligibilityField?.value shouldContain "Eligible"
        }

        @Test
        fun `should show ineligibility reasons when not eligible`() {
            val raider = createTestRaider(
                eligible = false,
                ineligibilityReasons = listOf("Low attendance"),
            )
            val embed = EmbedBuilderUtil.createFlpsEmbed(raider)

            val eligibilityField = embed.fields.find { it.name == "Eligibility" }
            eligibilityField?.value shouldContain "Ineligible"
            eligibilityField?.value shouldContain "Low attendance"
        }

        private fun createTestRaider(
            flps: Double = 0.85,
            eligible: Boolean = true,
            ineligibilityReasons: List<String>? = null,
        ) = RaiderFlpsResponse(
            raiderId = 1L,
            characterName = "TestChar",
            characterClass = "WARRIOR",
            role = "DPS",
            flps = flps,
            rms = RmsBreakdown(value = 0.9, acs = 0.95, mas = 0.85, eps = 0.9),
            ipi = IpiBreakdown(value = 0.8, uv = 0.75, tierBonus = 0.85, roleMultiplier = 1.0),
            rdf = 1.0,
            eligible = eligible,
            ineligibilityReasons = ineligibilityReasons,
            rank = 3,
        )
    }

    @Nested
    inner class CreateLeaderboardEmbed {

        @Test
        fun `should create embed with correct title`() {
            val entries = createTestLeaderboard()
            val embed = EmbedBuilderUtil.createLeaderboardEmbed(entries, "guild-1", null, null)

            embed.title shouldBe "FLPS Leaderboard"
        }

        @Test
        fun `should include role filter in title`() {
            val entries = createTestLeaderboard()
            val embed = EmbedBuilderUtil.createLeaderboardEmbed(entries, "guild-1", "TANK", null)

            embed.title shouldBe "FLPS Leaderboard - TANK"
        }

        @Test
        fun `should display medal emojis for top 3`() {
            val entries = createTestLeaderboard()
            val embed = EmbedBuilderUtil.createLeaderboardEmbed(entries, "guild-1", null, null)

            embed.description shouldContain "\uD83E\uDD47" // Gold medal
            embed.description shouldContain "\uD83E\uDD48" // Silver medal
            embed.description shouldContain "\uD83E\uDD49" // Bronze medal
        }

        @Test
        fun `should highlight user in leaderboard`() {
            val entries = createTestLeaderboard()
            val embed = EmbedBuilderUtil.createLeaderboardEmbed(entries, "guild-1", null, 2L)

            // User with raiderId 2 should be highlighted with **
            embed.description shouldContain "**SecondPlayer**"
        }

        private fun createTestLeaderboard() = listOf(
            LeaderboardEntry(1, 1L, "FirstPlayer", "WARRIOR", "DPS", 0.95, true),
            LeaderboardEntry(2, 2L, "SecondPlayer", "MAGE", "DPS", 0.92, true),
            LeaderboardEntry(3, 3L, "ThirdPlayer", "ROGUE", "DPS", 0.89, true),
        )
    }

    @Nested
    inner class CreateLootHistoryEmbed {

        @Test
        fun `should create embed with correct title`() {
            val awards = createTestAwards()
            val embed = EmbedBuilderUtil.createLootHistoryEmbed("TestChar", awards)

            embed.title shouldBe "Recent Loot - TestChar"
        }

        @Test
        fun `should show empty message when no awards`() {
            val embed = EmbedBuilderUtil.createLootHistoryEmbed("TestChar", emptyList())

            embed.description shouldBe "No loot history found."
        }

        @Test
        fun `should show RDF expired status`() {
            val awards = listOf(
                LootAwardEntry(
                    itemId = 1L,
                    itemName = "Test Item",
                    awardedAt = "2024-01-01T00:00:00Z",
                    flpsAtAward = 0.85,
                    rdfExpired = true,
                    rdfExpiresAt = null,
                ),
            )
            val embed = EmbedBuilderUtil.createLootHistoryEmbed("TestChar", awards)

            val field = embed.fields.find { it.name == "Test Item" }
            field?.value shouldContain "RDF Expired"
        }

        @Test
        fun `should show RDF expiration date when not expired`() {
            val awards = listOf(
                LootAwardEntry(
                    itemId = 1L,
                    itemName = "Test Item",
                    awardedAt = "2024-01-01T00:00:00Z",
                    flpsAtAward = 0.85,
                    rdfExpired = false,
                    rdfExpiresAt = "2024-02-01T00:00:00Z",
                ),
            )
            val embed = EmbedBuilderUtil.createLootHistoryEmbed("TestChar", awards)

            val field = embed.fields.find { it.name == "Test Item" }
            field?.value shouldContain "RDF expires"
        }

        private fun createTestAwards() = listOf(
            LootAwardEntry(1L, "First Item", "2024-01-01T00:00:00Z", 0.85, true, null),
            LootAwardEntry(2L, "Second Item", "2024-01-10T00:00:00Z", 0.80, false, "2024-02-10T00:00:00Z"),
        )
    }

    @Nested
    inner class CreateHelpEmbed {

        @Test
        fun `should create embed with correct title`() {
            val embed = EmbedBuilderUtil.createHelpEmbed()

            embed.title shouldBe "LootMan Bot Commands"
        }

        @Test
        fun `should include flps command`() {
            val embed = EmbedBuilderUtil.createHelpEmbed()

            val field = embed.fields.find { it.name == "/flps" }
            field shouldNotBe null
        }

        @Test
        fun `should include leaderboard command`() {
            val embed = EmbedBuilderUtil.createHelpEmbed()

            val field = embed.fields.find { it.name == "/leaderboard [role]" }
            field shouldNotBe null
        }

        @Test
        fun `should include link command`() {
            val embed = EmbedBuilderUtil.createHelpEmbed()

            val field = embed.fields.find { it.name == "/link <character> <realm>" }
            field shouldNotBe null
        }
    }

    @Nested
    inner class CreateErrorEmbed {

        @Test
        fun `should create embed with error title`() {
            val embed = EmbedBuilderUtil.createErrorEmbed("Test Error", "Error description")

            embed.title shouldBe "\u274C Test Error"
        }

        @Test
        fun `should have red color`() {
            val embed = EmbedBuilderUtil.createErrorEmbed("Test Error", "Error description")

            embed.color shouldBe Color(231, 76, 60) // RED
        }

        @Test
        fun `should include description`() {
            val embed = EmbedBuilderUtil.createErrorEmbed("Test Error", "Error description")

            embed.description shouldBe "Error description"
        }
    }
}
