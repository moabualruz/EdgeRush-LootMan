package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.config.DiscordProperties
import com.edgerush.lootman.bot.util.EmbedBuilderUtil
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handler for the /flps command.
 *
 * Displays the user's current FLPS score with detailed breakdown.
 */
@Component
class FlpsCommand(
    private val apiClient: BackendApiClient,
    private val properties: DiscordProperties,
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(FlpsCommand::class.java)

    override val name = "flps"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Check your FLPS score and breakdown")
            .addOption(OptionType.USER, "user", "Check another user's FLPS (officer only)", false)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        runBlocking {
            try {
                val targetUser = event.getOption("user")?.asUser ?: event.user
                val discordUserId = targetUser.id

                // First, get the user's linked character
                val userLinks = apiClient.getDiscordUserLink(discordUserId)

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

                // Get primary or first linked character
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

                // Fetch FLPS data
                val guildId = properties.guilds.firstOrNull()?.guildId
                    ?: run {
                        event.hook.sendMessageEmbeds(
                            EmbedBuilderUtil.createErrorEmbed("Configuration Error", "No guild configured."),
                        ).queue()
                        return@runBlocking
                    }

                val flpsData = apiClient.getRaiderFlps(guildId, raiderId)

                if (flpsData == null) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "FLPS Data Unavailable",
                        "Could not retrieve FLPS data. Please try again later.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                val embed = EmbedBuilderUtil.createFlpsEmbed(flpsData)
                event.hook.sendMessageEmbeds(embed).queue()
            } catch (e: Exception) {
                logger.error("Error handling /flps command", e)
                val embed = EmbedBuilderUtil.createErrorEmbed(
                    "Error",
                    "An unexpected error occurred. Please try again later.",
                )
                event.hook.sendMessageEmbeds(embed).queue()
            }
        }
    }
}
