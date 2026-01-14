package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.client.CreateDiscordUserLinkRequest
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
 * Handler for the /link command.
 *
 * Links a Discord user to a WoW character.
 */
@Component
class LinkCommand(
    private val apiClient: BackendApiClient,
    private val properties: DiscordProperties,
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(LinkCommand::class.java)

    override val name = "link"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Link your Discord account to a WoW character")
            .addOption(OptionType.STRING, "character", "Character name", true)
            .addOption(OptionType.STRING, "realm", "Realm name", true)
            .addOption(OptionType.BOOLEAN, "primary", "Set as primary character", false)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue() // Ephemeral response

        runBlocking {
            try {
                val characterName = event.getOption("character")?.asString
                    ?: run {
                        event.hook.sendMessage("Character name is required.").queue()
                        return@runBlocking
                    }

                val realm = event.getOption("realm")?.asString
                    ?: run {
                        event.hook.sendMessage("Realm is required.").queue()
                        return@runBlocking
                    }

                val isPrimary = event.getOption("primary")?.asBoolean ?: false

                val guildId = properties.guilds.firstOrNull()?.guildId
                    ?: run {
                        event.hook.sendMessageEmbeds(
                            EmbedBuilderUtil.createErrorEmbed("Configuration Error", "No guild configured."),
                        ).queue()
                        return@runBlocking
                    }

                // Validate character exists in guild
                val raider = apiClient.getRaiderByCharacter(guildId, characterName, realm)

                if (raider == null) {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Character Not Found",
                        buildString {
                            appendLine("Character **$characterName-$realm** was not found in the guild roster.")
                            appendLine()
                            appendLine("Please ensure:")
                            appendLine("\u2022 The character name is spelled correctly")
                            appendLine("\u2022 The realm name is correct")
                            appendLine("\u2022 The character is in the guild")
                        },
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                    return@runBlocking
                }

                // Create the link
                val request = CreateDiscordUserLinkRequest(
                    discordUserId = event.user.id,
                    characterName = characterName,
                    realm = realm,
                    isPrimary = isPrimary,
                )

                val result = apiClient.createDiscordUserLink(request)

                if (result != null) {
                    val embed = EmbedBuilderUtil.createLinkSuccessEmbed(characterName, realm)
                    event.hook.sendMessageEmbeds(embed).queue()
                } else {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Link Failed",
                        "Could not link character. This may be because the character is already linked to another Discord account.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                }
            } catch (e: Exception) {
                logger.error("Error handling /link command", e)
                val embed = EmbedBuilderUtil.createErrorEmbed(
                    "Error",
                    "An unexpected error occurred. Please try again later.",
                )
                event.hook.sendMessageEmbeds(embed).queue()
            }
        }
    }
}

/**
 * Handler for the /unlink command.
 *
 * Unlinks a character from a Discord user.
 */
@Component
class UnlinkCommand(
    private val apiClient: BackendApiClient,
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(UnlinkCommand::class.java)

    override val name = "unlink"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Unlink a character from your Discord account")
            .addOption(OptionType.STRING, "character", "Character name", true)
            .addOption(OptionType.STRING, "realm", "Realm name", true)

    override fun handle(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue() // Ephemeral response

        runBlocking {
            try {
                val characterName = event.getOption("character")?.asString
                    ?: run {
                        event.hook.sendMessage("Character name is required.").queue()
                        return@runBlocking
                    }

                val realm = event.getOption("realm")?.asString
                    ?: run {
                        event.hook.sendMessage("Realm is required.").queue()
                        return@runBlocking
                    }

                val success = apiClient.deleteDiscordUserLink(
                    discordUserId = event.user.id,
                    characterName = characterName,
                    realm = realm,
                )

                if (success) {
                    val embed = EmbedBuilderUtil.createUnlinkSuccessEmbed(characterName, realm)
                    event.hook.sendMessageEmbeds(embed).queue()
                } else {
                    val embed = EmbedBuilderUtil.createErrorEmbed(
                        "Unlink Failed",
                        "Could not unlink character. Please ensure the character is linked to your account.",
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                }
            } catch (e: Exception) {
                logger.error("Error handling /unlink command", e)
                val embed = EmbedBuilderUtil.createErrorEmbed(
                    "Error",
                    "An unexpected error occurred. Please try again later.",
                )
                event.hook.sendMessageEmbeds(embed).queue()
            }
        }
    }
}
