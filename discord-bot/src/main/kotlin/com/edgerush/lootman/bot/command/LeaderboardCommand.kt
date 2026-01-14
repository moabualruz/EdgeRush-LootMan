package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.config.DiscordProperties
import com.edgerush.lootman.bot.util.EmbedBuilderUtil
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for the /leaderboard command.
 *
 * Displays the FLPS leaderboard with optional role filtering.
 */
@Component
class LeaderboardCommand(
    private val apiClient: BackendApiClient,
    private val properties: DiscordProperties,
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(LeaderboardCommand::class.java)

    override val name = "leaderboard"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "View the FLPS leaderboard")
            .addOptions(
                OptionData(OptionType.STRING, "role", "Filter by role", false)
                    .addChoice("Tank", "TANK")
                    .addChoice("Healer", "HEALER")
                    .addChoice("DPS", "DPS"),
            )
            .addOption(OptionType.INTEGER, "limit", "Number of entries to show (default: 10)", false)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        runBlocking {
            try {
                val role = event.getOption("role")?.asString
                val limit = event.getOption("limit")?.asInt ?: 10

                val guildId = properties.guilds.firstOrNull()?.guildId
                    ?: run {
                        event.hook.sendMessageEmbeds(
                            EmbedBuilderUtil.createErrorEmbed("Configuration Error", "No guild configured."),
                        ).queue()
                        return@runBlocking
                    }

                val leaderboard = apiClient.getLeaderboard(guildId, role, limit)

                if (leaderboard == null) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Leaderboard Unavailable",
                        "Could not retrieve leaderboard data. Please try again later.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                // Get user's raider ID for highlighting
                val userLinks = apiClient.getDiscordUserLink(event.user.id)
                val primaryLink = userLinks?.links?.find { it.isPrimary } ?: userLinks?.links?.firstOrNull()
                val userRaiderId = primaryLink?.raiderId

                val embed = EmbedBuilderUtil.createLeaderboardEmbed(
                    entries = leaderboard.entries,
                    guildId = guildId,
                    roleFilter = role,
                    userRaiderId = userRaiderId,
                )
                event.hook.sendMessageEmbeds(embed).queue()
            } catch (e: Exception) {
                logger.error("Error handling /leaderboard command", e)
                val embed = EmbedBuilderUtil.createErrorEmbed(
                    "Error",
                    "An unexpected error occurred. Please try again later.",
                )
                event.hook.sendMessageEmbeds(embed).queue()
            }
        }
    }
}
