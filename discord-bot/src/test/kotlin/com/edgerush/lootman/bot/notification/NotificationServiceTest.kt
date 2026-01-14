package com.edgerush.lootman.bot.notification

import com.edgerush.lootman.bot.config.DiscordProperties
import com.edgerush.lootman.bot.config.GuildConfig
import com.edgerush.lootman.bot.config.NotificationChannels
import com.edgerush.lootman.bot.config.NotificationsConfig
import com.edgerush.lootman.bot.config.RdfExpiryNotificationConfig
import com.edgerush.lootman.bot.config.PenaltyNotificationConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.function.Consumer

class NotificationServiceTest {

    private val jda = mockk<JDA>()
    private val properties = mockk<DiscordProperties>()
    private lateinit var service: NotificationService

    @BeforeEach
    fun setUp() {
        service = NotificationService(jda, properties)
    }

    @Nested
    inner class SendLootAwardNotification {

        @Test
        fun `should send notification to configured channel`() {
            // Arrange
            val channel = mockk<TextChannel>()
            val messageAction = mockk<MessageCreateAction>(relaxed = true)

            every { properties.guilds } returns listOf(
                GuildConfig(
                    guildId = "guild-123",
                    discordServerId = "discord-123",
                    notificationChannels = NotificationChannels(
                        lootAwards = "channel-123",
                    ),
                ),
            )
            every { jda.getTextChannelById("channel-123") } returns channel
            every { channel.sendMessageEmbeds(any<MessageEmbed>()) } returns messageAction
            every { messageAction.queue(any<Consumer<Message>>(), any<Consumer<Throwable>>()) } returns Unit

            val notification = LootAwardNotification(
                guildId = "guild-123",
                itemName = "Test Item",
                recipientName = "TestPlayer",
                flpsScore = 0.85,
                rationale = "Highest FLPS",
                runnerUps = listOf("Player2" to 0.82, "Player3" to 0.80),
            )

            // Act
            service.sendLootAwardNotification(notification)

            // Assert
            verify { channel.sendMessageEmbeds(any<MessageEmbed>()) }
            verify { messageAction.queue(any<Consumer<Message>>(), any<Consumer<Throwable>>()) }
        }

        @Test
        fun `should not send when channel not configured`() {
            // Arrange
            every { properties.guilds } returns listOf(
                GuildConfig(
                    guildId = "guild-123",
                    discordServerId = "discord-123",
                    notificationChannels = NotificationChannels(
                        lootAwards = null, // Not configured
                    ),
                ),
            )

            val notification = LootAwardNotification(
                guildId = "guild-123",
                itemName = "Test Item",
                recipientName = "TestPlayer",
                flpsScore = 0.85,
                rationale = null,
                runnerUps = emptyList(),
            )

            // Act
            service.sendLootAwardNotification(notification)

            // Assert - no channel lookup should happen
            verify(exactly = 0) { jda.getTextChannelById(any<String>()) }
        }
    }

    @Nested
    inner class SendRdfExpiryNotification {

        @Test
        fun `should send DM when enabled`() {
            // Arrange
            val user = mockk<User>()
            val privateChannel = mockk<PrivateChannel>()
            val channelAction = mockk<CacheRestAction<PrivateChannel>>(relaxed = true)
            val messageAction = mockk<MessageCreateAction>(relaxed = true)

            every { properties.notifications } returns NotificationsConfig(
                rdfExpiry = RdfExpiryNotificationConfig(enabled = true, dmUsers = true),
            )
            every { jda.getUserById("user-123") } returns user
            every { user.openPrivateChannel() } returns channelAction
            every { channelAction.queue(any<Consumer<PrivateChannel>>(), any<Consumer<Throwable>>()) } answers {
                val successCallback = firstArg<Consumer<PrivateChannel>>()
                successCallback.accept(privateChannel)
            }
            every { privateChannel.sendMessageEmbeds(any<MessageEmbed>()) } returns messageAction

            val notification = RdfExpiryNotification(
                discordUserId = "user-123",
                itemName = "Test Item",
                newFlps = 0.85,
                previousFlps = 0.75,
            )

            // Act
            service.sendRdfExpiryNotification(notification)

            // Assert
            verify { user.openPrivateChannel() }
        }

        @Test
        fun `should not send DM when disabled`() {
            // Arrange
            every { properties.notifications } returns NotificationsConfig(
                rdfExpiry = RdfExpiryNotificationConfig(enabled = true, dmUsers = false),
            )

            val notification = RdfExpiryNotification(
                discordUserId = "user-123",
                itemName = "Test Item",
                newFlps = 0.85,
                previousFlps = 0.75,
            )

            // Act
            service.sendRdfExpiryNotification(notification)

            // Assert - no user lookup should happen
            verify(exactly = 0) { jda.getUserById(any<String>()) }
        }
    }

    @Nested
    inner class SendPenaltyNotification {

        @Test
        fun `should send DM for behavioral action when enabled`() {
            // Arrange
            val user = mockk<User>()
            val privateChannel = mockk<PrivateChannel>()
            val channelAction = mockk<CacheRestAction<PrivateChannel>>(relaxed = true)
            val messageAction = mockk<MessageCreateAction>(relaxed = true)

            every { properties.notifications } returns NotificationsConfig(
                penalties = PenaltyNotificationConfig(enabled = true, dmUsers = true),
            )
            every { jda.getUserById("user-123") } returns user
            every { user.openPrivateChannel() } returns channelAction
            every { channelAction.queue(any<Consumer<PrivateChannel>>(), any<Consumer<Throwable>>()) } answers {
                val successCallback = firstArg<Consumer<PrivateChannel>>()
                successCallback.accept(privateChannel)
            }
            every { privateChannel.sendMessageEmbeds(any<MessageEmbed>()) } returns messageAction

            val notification = PenaltyNotification(
                discordUserId = "user-123",
                type = PenaltyType.BEHAVIORAL_ACTION,
                reason = "Test reason",
                duration = "2 weeks",
                flpsImpact = "-10%",
                appealInstructions = "Contact an officer",
            )

            // Act
            service.sendPenaltyNotification(notification)

            // Assert
            verify { user.openPrivateChannel() }
        }

        @Test
        fun `should not send DM when disabled`() {
            // Arrange
            every { properties.notifications } returns NotificationsConfig(
                penalties = PenaltyNotificationConfig(enabled = true, dmUsers = false),
            )

            val notification = PenaltyNotification(
                discordUserId = "user-123",
                type = PenaltyType.LOOT_BAN,
                reason = "Test reason",
                duration = null,
                flpsImpact = null,
                appealInstructions = null,
            )

            // Act
            service.sendPenaltyNotification(notification)

            // Assert - no user lookup should happen
            verify(exactly = 0) { jda.getUserById(any<String>()) }
        }
    }
}
