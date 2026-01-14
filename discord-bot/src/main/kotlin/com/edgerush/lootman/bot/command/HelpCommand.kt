package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.util.EmbedBuilderUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

/**
 * Handler for the /help command.
 *
 * Displays available commands and usage information.
 */
@Component
class HelpCommand : CommandHandler {

    override val name = "help"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Display help information for LootMan bot commands")

    override fun handle(event: SlashCommandInteractionEvent) {
        val embed = EmbedBuilderUtil.createHelpEmbed()
        event.replyEmbeds(embed).setEphemeral(true).queue()
    }
}

/**
 * Handler for the /about command.
 *
 * Displays information about the bot.
 */
@Component
class AboutCommand : CommandHandler {

    override val name = "about"

    override fun buildCommand(): SlashCommandData =
        Commands.slash(name, "Display information about LootMan bot")

    override fun handle(event: SlashCommandInteractionEvent) {
        val version = javaClass.`package`?.implementationVersion ?: "1.0.0-SNAPSHOT"
        val status = "Online"

        val embed = EmbedBuilderUtil.createAboutEmbed(version, status)
        event.replyEmbeds(embed).queue()
    }
}
