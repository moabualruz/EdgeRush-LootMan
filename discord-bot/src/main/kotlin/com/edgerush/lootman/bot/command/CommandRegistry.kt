package com.edgerush.lootman.bot.command

import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Registry for Discord slash commands.
 *
 * Manages registration of all slash commands with Discord.
 */
@Component
class CommandRegistry(
    private val commandHandlers: List<CommandHandler>,
) {
    private val logger = LoggerFactory.getLogger(CommandRegistry::class.java)

    /**
     * Registers all slash commands with Discord globally.
     *
     * @param jda The JDA instance to register commands with
     */
    fun registerCommands(jda: JDA) {
        logger.info("Registering ${commandHandlers.size} slash commands...")

        val commands = commandHandlers.map { it.buildCommand() }

        jda.updateCommands()
            .addCommands(commands)
            .queue(
                { logger.info("Successfully registered ${it.size} commands") },
                { logger.error("Failed to register commands", it) }
            )
    }
}
