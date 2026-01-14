package com.edgerush.lootman.bot.listener

import com.edgerush.lootman.bot.command.CommandHandler
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Listener for slash command interactions.
 *
 * Routes slash commands to their appropriate handlers.
 */
@Component
class SlashCommandListener(
    private val commandHandlers: List<CommandHandler>,
) : ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(SlashCommandListener::class.java)
    private val handlerMap: Map<String, CommandHandler> by lazy {
        commandHandlers.associateBy { it.name }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandName = event.name
        logger.debug("Received slash command: /$commandName from ${event.user.name}")

        val handler = handlerMap[commandName]
        if (handler == null) {
            logger.warn("No handler found for command: /$commandName")
            event.reply("Unknown command").setEphemeral(true).queue()
            return
        }

        try {
            handler.handle(event)
        } catch (e: Exception) {
            logger.error("Error handling command /$commandName", e)
            if (!event.isAcknowledged) {
                event.reply("An error occurred while processing your command.")
                    .setEphemeral(true)
                    .queue()
            }
        }
    }
}
