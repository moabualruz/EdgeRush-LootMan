package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.client.LeaderboardEntry
import com.edgerush.lootman.bot.client.LeaderboardResponse
import com.edgerush.lootman.bot.config.DiscordProperties
import com.edgerush.lootman.bot.config.GuildConfig
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LeaderboardCommandTest {

    private val apiClient = mockk<BackendApiClient>()
    private val properties = mockk<DiscordProperties>()
    private lateinit var command: LeaderboardCommand

    @BeforeEach
    fun setUp() {
        command = LeaderboardCommand(apiClient, properties)
    }

    @Test
    fun `name should be leaderboard`() {
        command.name shouldBe "leaderboard"
    }

    @Test
    fun `buildCommand should create slash command with correct name`() {
        val commandData = command.buildCommand()
        commandData.name shouldBe "leaderboard"
    }

    @Test
    fun `buildCommand should have role and limit options`() {
        val commandData = command.buildCommand()
        val options = commandData.options
        options.any { it.name == "role" } shouldBe true
        options.any { it.name == "limit" } shouldBe true
    }

    @Test
    fun `handle should display leaderboard`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val user = mockk<User>()
        val hook = mockk<InteractionHook>(relaxed = true)
        val deferAction = mockk<ReplyCallbackAction>(relaxed = true)
        val embedSlot = slot<MessageEmbed>()

        every { event.user } returns user
        every { user.id } returns "123456"
        every { event.getOption("role") } returns null
        every { event.getOption("limit") } returns null
        every { event.deferReply() } returns deferAction
        every { deferAction.queue() } returns Unit
        every { event.hook } returns hook

        val messageAction = mockk<WebhookMessageCreateAction<Message>>(relaxed = true)
        every { hook.sendMessageEmbeds(capture(embedSlot)) } returns messageAction

        val leaderboard = LeaderboardResponse(
            guildId = "guild-123",
            entries = listOf(
                LeaderboardEntry(
                    rank = 1,
                    raiderId = 1L,
                    characterName = "TopPlayer",
                    characterClass = "WARRIOR",
                    role = "DPS",
                    flps = 0.950,
                    eligible = true,
                ),
                LeaderboardEntry(
                    rank = 2,
                    raiderId = 2L,
                    characterName = "SecondPlayer",
                    characterClass = "MAGE",
                    role = "DPS",
                    flps = 0.920,
                    eligible = true,
                ),
            ),
            totalRaiders = 20,
        )

        coEvery { apiClient.getLeaderboard("guild-123", null, 10) } returns leaderboard
        coEvery { apiClient.getDiscordUserLink("123456") } returns null

        every { properties.guilds } returns listOf(
            GuildConfig(guildId = "guild-123", discordServerId = "discord-123"),
        )

        // Act
        command.handle(event)

        // Assert
        verify { hook.sendMessageEmbeds(any<MessageEmbed>()) }
        embedSlot.captured.title shouldBe "FLPS Leaderboard"
    }

    @Test
    fun `handle should filter by role when specified`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val user = mockk<User>()
        val hook = mockk<InteractionHook>(relaxed = true)
        val deferAction = mockk<ReplyCallbackAction>(relaxed = true)
        val roleOption = mockk<OptionMapping>()
        val embedSlot = slot<MessageEmbed>()

        every { event.user } returns user
        every { user.id } returns "123456"
        every { event.getOption("role") } returns roleOption
        every { roleOption.asString } returns "TANK"
        every { event.getOption("limit") } returns null
        every { event.deferReply() } returns deferAction
        every { deferAction.queue() } returns Unit
        every { event.hook } returns hook

        val messageAction = mockk<WebhookMessageCreateAction<Message>>(relaxed = true)
        every { hook.sendMessageEmbeds(capture(embedSlot)) } returns messageAction

        val leaderboard = LeaderboardResponse(
            guildId = "guild-123",
            entries = listOf(
                LeaderboardEntry(
                    rank = 1,
                    raiderId = 1L,
                    characterName = "TopTank",
                    characterClass = "WARRIOR",
                    role = "TANK",
                    flps = 0.900,
                    eligible = true,
                ),
            ),
            totalRaiders = 5,
        )

        coEvery { apiClient.getLeaderboard("guild-123", "TANK", 10) } returns leaderboard
        coEvery { apiClient.getDiscordUserLink("123456") } returns null

        every { properties.guilds } returns listOf(
            GuildConfig(guildId = "guild-123", discordServerId = "discord-123"),
        )

        // Act
        command.handle(event)

        // Assert
        verify { hook.sendMessageEmbeds(any<MessageEmbed>()) }
        embedSlot.captured.title shouldBe "FLPS Leaderboard - TANK"
    }
}
