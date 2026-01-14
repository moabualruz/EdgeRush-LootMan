package com.edgerush.lootman.bot.command

import com.edgerush.lootman.bot.client.BackendApiClient
import com.edgerush.lootman.bot.client.CharacterLink
import com.edgerush.lootman.bot.client.DiscordUserLinkResponse
import com.edgerush.lootman.bot.client.IpiBreakdown
import com.edgerush.lootman.bot.client.RaiderFlpsResponse
import com.edgerush.lootman.bot.client.RmsBreakdown
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
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FlpsCommandTest {

    private val apiClient = mockk<BackendApiClient>()
    private val properties = mockk<DiscordProperties>()
    private lateinit var command: FlpsCommand

    @BeforeEach
    fun setUp() {
        command = FlpsCommand(apiClient, properties)
    }

    @Test
    fun `name should be flps`() {
        command.name shouldBe "flps"
    }

    @Test
    fun `buildCommand should create slash command with correct name`() {
        val commandData = command.buildCommand()
        commandData.name shouldBe "flps"
    }

    @Test
    fun `buildCommand should have user option`() {
        val commandData = command.buildCommand()
        val options = commandData.options
        options.any { it.name == "user" } shouldBe true
    }

    @Test
    fun `handle should send error when user has no linked character`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val user = mockk<User>()
        val hook = mockk<InteractionHook>(relaxed = true)
        val deferAction = mockk<ReplyCallbackAction>(relaxed = true)
        val embedSlot = slot<MessageEmbed>()

        every { event.user } returns user
        every { user.id } returns "123456"
        every { event.getOption("user") } returns null
        every { event.deferReply() } returns deferAction
        every { deferAction.queue() } returns Unit
        every { event.hook } returns hook

        val messageAction = mockk<WebhookMessageCreateAction<*>>(relaxed = true)
        every { hook.sendMessageEmbeds(capture(embedSlot)) } returns messageAction

        coEvery { apiClient.getDiscordUserLink("123456") } returns null

        // Act
        command.handle(event)

        // Assert
        verify { hook.sendMessageEmbeds(any<MessageEmbed>()) }
        embedSlot.captured.title shouldBe "\u274C No Character Linked"
    }

    @Test
    fun `handle should display FLPS score when character is linked`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val user = mockk<User>()
        val hook = mockk<InteractionHook>(relaxed = true)
        val deferAction = mockk<ReplyCallbackAction>(relaxed = true)
        val embedSlot = slot<MessageEmbed>()

        every { event.user } returns user
        every { user.id } returns "123456"
        every { event.getOption("user") } returns null
        every { event.deferReply() } returns deferAction
        every { deferAction.queue() } returns Unit
        every { event.hook } returns hook

        val messageAction = mockk<WebhookMessageCreateAction<*>>(relaxed = true)
        every { hook.sendMessageEmbeds(capture(embedSlot)) } returns messageAction

        val userLinks = DiscordUserLinkResponse(
            discordUserId = "123456",
            links = listOf(
                CharacterLink(
                    characterName = "TestChar",
                    realm = "TestRealm",
                    isPrimary = true,
                    linkedAt = "2024-01-01T00:00:00Z",
                    raiderId = 42L,
                ),
            ),
        )

        val flpsResponse = RaiderFlpsResponse(
            raiderId = 42L,
            characterName = "TestChar",
            characterClass = "WARRIOR",
            role = "DPS",
            flps = 0.850,
            rms = RmsBreakdown(value = 0.900, acs = 0.95, mas = 0.85, eps = 0.90),
            ipi = IpiBreakdown(value = 0.800, uv = 0.75, tierBonus = 0.85, roleMultiplier = 1.0),
            rdf = 1.0,
            eligible = true,
            ineligibilityReasons = null,
            rank = 3,
        )

        coEvery { apiClient.getDiscordUserLink("123456") } returns userLinks
        coEvery { apiClient.getRaiderFlps("guild-123", 42L) } returns flpsResponse

        every { properties.guilds } returns listOf(
            GuildConfig(guildId = "guild-123", discordServerId = "discord-123"),
        )

        // Act
        command.handle(event)

        // Assert
        verify { hook.sendMessageEmbeds(any<MessageEmbed>()) }
        embedSlot.captured.title shouldBe "FLPS Score - TestChar"
    }
}
