package com.edgerush.lootman.bot.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * Interface for slash command handlers.
 *
 * Each command handler is responsible for:
 * - Defining the command structure (name, description, options)
 * - Handling command execution
 */
interface CommandHandler {
    /**
     * The name of the command (e.g., "flps", "leaderboard").
     */
    val name: String

    /**
     * Builds the command data for registration with Discord.
     *
     * @return SlashCommandData defining the command structure
     */
    fun buildCommand(): SlashCommandData

    /**
     * Handles the command execution.
     *
     * @param event The slash command interaction event
     */
    fun handle(event: SlashCommandInteractionEvent)
}
