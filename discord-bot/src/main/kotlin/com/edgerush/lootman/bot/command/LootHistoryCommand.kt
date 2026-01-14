package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.config.DiscordProperties
import com.edgerush.lootman.bot.util.EmbedBuilderUtil
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for the /loot command with subcommands.
 *
 * Provides loot history viewing functionality.
 */
@Component
class LootHistoryCommand(
    private val apiClient: BackendApiClient,
    private val properties: DiscordProperties,
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(LootHistoryCommand::class.java)

    override val name = "loot"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Loot-related commands")
            .addSubcommands(
                SubcommandData("history", "View your recent loot awards")
                    .addOption(OptionType.INTEGER, "limit", "Number of entries to show (default: 10)", false),
            )

    override fun handle(event: SlashCommandInteractionEvent) {
        val subcommand = event.subcommandName

        when (subcommand) {
            "history" -> handleHistory(event)
            else -> {
                event.reply("Unknown subcommand: $subcommand").setEphemeral(true).queue()
            }
        }
    }

    private fun handleHistory(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        runBlocking {
            try {
                val limit = event.getOption("limit")?.asInt ?: 10

                // Get user's linked character
                val userLinks = apiClient.getDiscordUserLink(event.user.id)

                if (userLinks == null || userLinks.links.isEmpty()) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "No Character Linked",
                        buildString {
                            appendLine("You don't have any characters linked to your Discord account.")
                            appendLine()
                            appendLine("Use `/link <character-name> <realm>` to link your character.")
                        },
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                val primaryLink = userLinks.links.find { it.isPrimary } ?: userLinks.links.first()
                val raiderId = primaryLink.raiderId

                if (raiderId == null) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Character Not Found",
                        "Your linked character **${primaryLink.characterName}-${primaryLink.realm}** " +
                            "was not found in the guild roster.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                val guildId = properties.guilds.firstOrNull()?.guildId
                    ?: run {
                        event.hook.sendMessageEmbeds(
                            EmbedBuilderUtil.createErrorEmbed("Configuration Error", "No guild configured."),
                        ).queue()
                        return@runBlocking
                    }

                val lootHistory = apiClient.getLootHistory(guildId, raiderId, limit)

                if (lootHistory == null) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Loot History Unavailable",
                        "Could not retrieve loot history. Please try again later.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                val embed = EmbedBuilderUtil.createLootHistoryEmbed(
                    characterName = lootHistory.characterName,
                    awards = lootHistory.awards,
                )
                event.hook.sendMessageEmbeds(embed).queue()
            } catch (e: Exception) {
                logger.error("Error handling /loot history command", e)
                val embed = EmbedBuilderUtil.createErrorEmbed(
                    "Error",
                    "An unexpected error occurred. Please try again later.",
                )
                event.hook.sendMessageEmbeds(embed).queue()
            }
        }
    }
}
