package com.edgerush.lootman.bot.notification

import com.edgerush.lootman.bot.config.DiscordProperties
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.awt.Color
import java.time.Instant

/**
 * Service for sending Discord notifications.
 *
 * Handles loot award notifications, RDF expiry notifications,
 * and penalty/ban notifications.
 */
@Service
class NotificationService(
    private val jda: JDA,
    private val properties: DiscordProperties,
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    private val GOLD = Color(241, 196, 15)
    private val GREEN = Color(46, 204, 113)
    private val RED = Color(231, 76, 60)

    /**
     * Sends a loot award notification to the configured channel.
     */
    fun sendLootAwardNotification(notification: LootAwardNotification) {
        val guildConfig = properties.guilds.find { it.guildId == notification.guildId }
        if (guildConfig == null) {
            logger.warn("No guild configuration found for ${notification.guildId}")
            return
        }

        val channelId = guildConfig.notificationChannels.lootAwards
        if (channelId.isNullOrBlank()) {
            logger.debug("Loot award notifications not configured for guild ${notification.guildId}")
            return
        }

        val channel = jda.getTextChannelById(channelId)
        if (channel == null) {
            logger.error("Could not find channel $channelId for loot award notifications")
            return
        }

        val embed = buildLootAwardEmbed(notification)
        channel.sendMessageEmbeds(embed).queue(
            { logger.info("Sent loot award notification for ${notification.itemName}") },
            { logger.error("Failed to send loot award notification", it) },
        )
    }

    /**
     * Sends an RDF expiry notification as a direct message.
     */
    fun sendRdfExpiryNotification(notification: RdfExpiryNotification) {
        if (!properties.notifications.rdfExpiry.dmUsers) {
            logger.debug("RDF expiry DMs disabled")
            return
        }

        val user = jda.getUserById(notification.discordUserId)
        if (user == null) {
            logger.warn("Could not find Discord user ${notification.discordUserId} for RDF notification")
            return
        }

        val embed = buildRdfExpiryEmbed(notification)
        user.openPrivateChannel().queue(
            { privateChannel ->
                privateChannel.sendMessageEmbeds(embed).queue(
                    { logger.info("Sent RDF expiry notification to ${user.name}") },
                    { logger.warn("Could not send DM to ${user.name} - DMs may be disabled") },
                )
            },
            { logger.warn("Could not open private channel with ${user.name}") },
        )
    }

    /**
     * Sends a penalty notification as a direct message.
     */
    fun sendPenaltyNotification(notification: PenaltyNotification) {
        if (!properties.notifications.penalties.dmUsers) {
            logger.debug("Penalty DMs disabled")
            return
        }

        val user = jda.getUserById(notification.discordUserId)
        if (user == null) {
            logger.warn("Could not find Discord user ${notification.discordUserId} for penalty notification")
            return
        }

        val embed = buildPenaltyEmbed(notification)
        user.openPrivateChannel().queue(
            { privateChannel ->
                privateChannel.sendMessageEmbeds(embed).queue(
                    { logger.info("Sent penalty notification to ${user.name}") },
                    { logger.warn("Could not send DM to ${user.name} - DMs may be disabled") },
                )
            },
            { logger.warn("Could not open private channel with ${user.name}") },
        )
    }

    /**
     * Sends a penalty lifted notification.
     */
    fun sendPenaltyLiftedNotification(notification: PenaltyLiftedNotification) {
        if (!properties.notifications.penalties.dmUsers) {
            logger.debug("Penalty DMs disabled")
            return
        }

        val user = jda.getUserById(notification.discordUserId)
        if (user == null) {
            logger.warn("Could not find Discord user ${notification.discordUserId} for penalty lifted notification")
            return
        }

        val embed = buildPenaltyLiftedEmbed(notification)
        user.openPrivateChannel().queue(
            { privateChannel ->
                privateChannel.sendMessageEmbeds(embed).queue(
                    { logger.info("Sent penalty lifted notification to ${user.name}") },
                    { logger.warn("Could not send DM to ${user.name} - DMs may be disabled") },
                )
            },
            { logger.warn("Could not open private channel with ${user.name}") },
        )
    }

    private fun buildLootAwardEmbed(notification: LootAwardNotification): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("\uD83C\uDF81 Loot Awarded")
            .setColor(GOLD)
            .addField("Item", notification.itemName, true)
            .addField("Recipient", notification.recipientName, true)
            .addField("FLPS", String.format("%.3f", notification.flpsScore), true)

        if (!notification.rationale.isNullOrBlank()) {
            builder.addField("Rationale", notification.rationale, false)
        }

        if (notification.runnerUps.isNotEmpty()) {
            val runnerUpText = notification.runnerUps.joinToString("\n") { (name, score) ->
                "\u2022 $name - ${String.format("%.3f", score)}"
            }
            builder.addField("Runner-ups", runnerUpText, false)
        }

        builder.setTimestamp(Instant.now())
        return builder.build()
    }

    private fun buildRdfExpiryEmbed(notification: RdfExpiryNotification): MessageEmbed {
        return EmbedBuilder()
            .setTitle("\u2705 RDF Expired")
            .setColor(GREEN)
            .setDescription("Your RDF penalty for **${notification.itemName}** has expired!")
            .addField("New FLPS", String.format("%.3f", notification.newFlps), true)
            .addField("Previous FLPS", String.format("%.3f", notification.previousFlps), true)
            .addField("Status", "Back in full contention for loot", false)
            .setTimestamp(Instant.now())
            .build()
    }

    private fun buildPenaltyEmbed(notification: PenaltyNotification): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle(
                when (notification.type) {
                    PenaltyType.BEHAVIORAL_ACTION -> "\u26A0\uFE0F Behavioral Action Applied"
                    PenaltyType.LOOT_BAN -> "\uD83D\uDEAB Loot Ban Applied"
                },
            )
            .setColor(RED)
            .addField("Reason", notification.reason, false)
            .addField("Duration", notification.duration ?: "Until further notice", true)
            .addField("FLPS Impact", notification.flpsImpact ?: "Varies by item", true)

        if (!notification.appealInstructions.isNullOrBlank()) {
            builder.addField("Appeals", notification.appealInstructions, false)
        }

        builder.setTimestamp(Instant.now())
        return builder.build()
    }

    private fun buildPenaltyLiftedEmbed(notification: PenaltyLiftedNotification): MessageEmbed {
        return EmbedBuilder()
            .setTitle("\u2705 ${notification.type.displayName} Lifted")
            .setColor(GREEN)
            .setDescription("Your ${notification.type.displayName.lowercase()} has been lifted.")
            .addField("Original Reason", notification.originalReason, false)
            .addField("Status", "Eligibility restored", false)
            .setTimestamp(Instant.now())
            .build()
    }
}

// Notification DTOs

data class LootAwardNotification(
    val guildId: String,
    val itemName: String,
    val recipientName: String,
    val flpsScore: Double,
    val rationale: String?,
    val runnerUps: List<Pair<String, Double>>,
)

data class RdfExpiryNotification(
    val discordUserId: String,
    val itemName: String,
    val newFlps: Double,
    val previousFlps: Double,
)

data class PenaltyNotification(
    val discordUserId: String,
    val type: PenaltyType,
    val reason: String,
    val duration: String?,
    val flpsImpact: String?,
    val appealInstructions: String?,
)

data class PenaltyLiftedNotification(
    val discordUserId: String,
    val type: PenaltyType,
    val originalReason: String,
)

enum class PenaltyType(val displayName: String) {
    BEHAVIORAL_ACTION("Behavioral Action"),
    LOOT_BAN("Loot Ban"),
}
