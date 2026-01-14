package com.edgerush.lootman.bot.listener

import com.edgerush.lootman.bot.command.CommandRegistry
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Listener for bot ready events.
 *
 * Handles bot initialization tasks like registering slash commands.
 */
@Component
class ReadyListener(
    private val commandRegistry: CommandRegistry,
) : ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(ReadyListener::class.java)

    override fun onReady(event: ReadyEvent) {
        logger.info("Bot is ready! Connected to ${event.guildTotalCount} guilds")

        // Register slash commands globally
        commandRegistry.registerCommands(event.jda)

        logger.info("Slash commands registered")
    }
}
