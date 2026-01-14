package com.edgerush.lootman.bot.config

import com.edgerush.lootman.bot.listener.ReadyListener
import com.edgerush.lootman.bot.listener.SlashCommandListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for JDA (Java Discord API).
 *
 * Sets up the Discord bot connection with required intents and event listeners.
 */
@Configuration
class JdaConfig(
    private val discordProperties: DiscordProperties,
    private val readyListener: ReadyListener,
    private val slashCommandListener: SlashCommandListener,
) {
    private val logger = LoggerFactory.getLogger(JdaConfig::class.java)

    /**
     * Creates and configures the JDA instance.
     *
     * @return Configured JDA instance
     */
    @Bean
    fun jda(): JDA {
        logger.info("Initializing Discord bot...")

        if (discordProperties.bot.token.isBlank()) {
            logger.warn("Discord bot token not configured. Bot will not start.")
            throw IllegalStateException("Discord bot token must be configured")
        }

        return JDABuilder.createDefault(discordProperties.bot.token)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGES,
            )
            .addEventListeners(readyListener, slashCommandListener)
            .build()
            .awaitReady()
            .also {
                logger.info("Discord bot connected as: ${it.selfUser.name}")
            }
    }
}
