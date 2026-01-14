package com.edgerush.lootman.bot.util

import com.edgerush.lootman.bot.client.LeaderboardEntry
import com.edgerush.lootman.bot.client.LootAwardEntry
import com.edgerush.lootman.bot.client.RaiderFlpsResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility object for building Discord message embeds.
 */
object EmbedBuilderUtil {

    private val GREEN = Color(46, 204, 113)
    private val YELLOW = Color(241, 196, 15)
    private val RED = Color(231, 76, 60)
    private val GOLD = Color(241, 196, 15)
    private val BLUE = Color(52, 152, 219)

    /**
     * Creates an FLPS score embed for a raider.
     */
    fun createFlpsEmbed(raider: RaiderFlpsResponse): MessageEmbed {
        val color = when {
            !raider.eligible -> RED
            raider.flps >= 0.8 -> GREEN
            raider.flps >= 0.5 -> YELLOW
            else -> RED
        }

        val eligibilityIcon = if (raider.eligible) "\u2705" else "\u274C"
        val eligibilityText = if (raider.eligible) {
            "Eligible"
        } else {
            "Ineligible: ${raider.ineligibilityReasons?.joinToString(", ") ?: "Unknown reason"}"
        }

        return EmbedBuilder()
            .setTitle("FLPS Score - ${raider.characterName}")
            .setColor(color)
            .addField("FLPS", formatScore(raider.flps) + rankText(raider.rank), true)
            .addField("Eligibility", "$eligibilityIcon $eligibilityText", true)
            .addField("\u200B", "\u200B", false) // Spacer
            .addField(
                "RMS (Raider Merit Score)",
                buildString {
                    append("**${formatScore(raider.rms.value)}**\n")
                    append("ACS: ${formatScore(raider.rms.acs)} | ")
                    append("MAS: ${formatScore(raider.rms.mas)} | ")
                    append("EPS: ${formatScore(raider.rms.eps)}")
                },
                false,
            )
            .addField(
                "IPI (Item Priority Index)",
                buildString {
                    append("**${formatScore(raider.ipi.value)}**\n")
                    append("UV: ${formatScore(raider.ipi.uv)} | ")
                    append("Tier: ${formatScore(raider.ipi.tierBonus)} | ")
                    append("Role: ${formatScore(raider.ipi.roleMultiplier)}")
                },
                false,
            )
            .addField(
                "RDF (Recency Decay Factor)",
                formatScore(raider.rdf) + if (raider.rdf < 1.0) " (Recent loot penalty)" else " (No recent loot)",
                false,
            )
            .setFooter("${raider.characterClass} | ${raider.role}")
            .setTimestamp(Instant.now())
            .build()
    }

    /**
     * Creates a leaderboard embed.
     */
    fun createLeaderboardEmbed(
        entries: List<LeaderboardEntry>,
        guildId: String,
        roleFilter: String?,
        userRaiderId: Long?,
    ): MessageEmbed {
        val title = if (roleFilter != null) {
            "FLPS Leaderboard - ${roleFilter.uppercase()}"
        } else {
            "FLPS Leaderboard"
        }

        val builder = EmbedBuilder()
            .setTitle(title)
            .setColor(GOLD)

        val leaderboardText = buildString {
            entries.forEachIndexed { index, entry ->
                val medal = when (index) {
                    0 -> "\uD83E\uDD47"
                    1 -> "\uD83E\uDD48"
                    2 -> "\uD83E\uDD49"
                    else -> "${entry.rank}."
                }
                val highlight = if (entry.raiderId == userRaiderId) "**" else ""
                val eligibleIcon = if (entry.eligible) "" else " \u274C"
                appendLine("$medal $highlight${entry.characterName}$highlight - ${formatScore(entry.flps)}$eligibleIcon")
            }
        }

        builder.setDescription(leaderboardText)

        // If user is not in top entries, show their position
        val userEntry = entries.find { it.raiderId == userRaiderId }
        if (userEntry == null && userRaiderId != null) {
            builder.addField("Your Position", "Not in top ${entries.size}", false)
        }

        builder.setTimestamp(Instant.now())
        return builder.build()
    }

    /**
     * Creates a loot history embed.
     */
    fun createLootHistoryEmbed(
        characterName: String,
        awards: List<LootAwardEntry>,
    ): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("Recent Loot - $characterName")
            .setColor(BLUE)

        if (awards.isEmpty()) {
            builder.setDescription("No loot history found.")
        } else {
            awards.forEach { award ->
                val rdfStatus = if (award.rdfExpired) {
                    "\u2705 RDF Expired"
                } else {
                    "\u23F3 RDF expires: ${formatDate(award.rdfExpiresAt)}"
                }
                builder.addField(
                    award.itemName,
                    "Awarded: ${formatDate(award.awardedAt)}\nFLPS: ${formatScore(award.flpsAtAward)}\n$rdfStatus",
                    false,
                )
            }
        }

        builder.setTimestamp(Instant.now())
        return builder.build()
    }

    /**
     * Creates a character link success embed.
     */
    fun createLinkSuccessEmbed(characterName: String, realm: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle("Character Linked")
            .setColor(GREEN)
            .setDescription("\u2705 Successfully linked **$characterName-$realm** to your Discord account.")
            .addField("What's Next?", "Use `/flps` to check your FLPS score!", false)
            .setTimestamp(Instant.now())
            .build()
    }

    /**
     * Creates an unlink success embed.
     */
    fun createUnlinkSuccessEmbed(characterName: String, realm: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle("Character Unlinked")
            .setColor(YELLOW)
            .setDescription("\u2705 Successfully unlinked **$characterName-$realm** from your Discord account.")
            .setTimestamp(Instant.now())
            .build()
    }

    /**
     * Creates an error embed.
     */
    fun createErrorEmbed(title: String, description: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle("\u274C $title")
            .setColor(RED)
            .setDescription(description)
            .setTimestamp(Instant.now())
            .build()
    }

    /**
     * Creates a help embed.
     */
    fun createHelpEmbed(): MessageEmbed {
        return EmbedBuilder()
            .setTitle("LootMan Bot Commands")
            .setColor(BLUE)
            .setDescription("Here are the available commands:")
            .addField(
                "/flps",
                "Check your current FLPS score and breakdown",
                false,
            )
            .addField(
                "/leaderboard [role]",
                "View the FLPS leaderboard. Optional: filter by role (tank, healer, dps)",
                false,
            )
            .addField(
                "/loot history",
                "View your recent loot awards and RDF status",
                false,
            )
            .addField(
                "/link <character> <realm>",
                "Link your Discord account to a WoW character",
                false,
            )
            .addField(
                "/unlink <character> <realm>",
                "Unlink a character from your Discord account",
                false,
            )
            .addField(
                "/help",
                "Display this help message",
                false,
            )
            .setFooter("LootMan - Fair Loot Distribution")
            .setTimestamp(Instant.now())
            .build()
    }

    /**
     * Creates an about embed.
     */
    fun createAboutEmbed(version: String, status: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle("About LootMan Bot")
            .setColor(BLUE)
            .setDescription("EdgeRush LootMan - Progression-first guild operations platform")
            .addField("Version", version, true)
            .addField("Status", status, true)
            .addField(
                "Features",
                buildString {
                    appendLine("\u2022 FLPS-based loot priority")
                    appendLine("\u2022 Real-time leaderboards")
                    appendLine("\u2022 Loot history tracking")
                    appendLine("\u2022 RDF notifications")
                },
                false,
            )
            .setTimestamp(Instant.now())
            .build()
    }

    private fun formatScore(score: Double): String = String.format("%.3f", score)

    private fun rankText(rank: Int?): String = rank?.let { " (Rank #$it)" } ?: ""

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return "Unknown"
        return try {
            val instant = Instant.parse(dateStr)
            val dateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        } catch (e: Exception) {
            dateStr
        }
    }
}
