package com.edgerush.lootman.bot.command

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.junit.jupiter.api.Test

class HelpCommandTest {

    private val command = HelpCommand()

    @Test
    fun `name should be help`() {
        command.name shouldBe "help"
    }

    @Test
    fun `buildCommand should create slash command with correct name`() {
        val commandData = command.buildCommand()
        commandData.name shouldBe "help"
    }

    @Test
    fun `handle should reply with help embed`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val replyAction = mockk<ReplyCallbackAction>(relaxed = true)
        val embedSlot = slot<MessageEmbed>()

        every { event.replyEmbeds(capture(embedSlot)) } returns replyAction
        every { replyAction.setEphemeral(true) } returns replyAction

        // Act
        command.handle(event)

        // Assert
        verify { event.replyEmbeds(any<MessageEmbed>()) }
        verify { replyAction.setEphemeral(true) }
        verify { replyAction.queue() }

        embedSlot.captured.title shouldBe "LootMan Bot Commands"
    }
}

class AboutCommandTest {

    private val command = AboutCommand()

    @Test
    fun `name should be about`() {
        command.name shouldBe "about"
    }

    @Test
    fun `buildCommand should create slash command with correct name`() {
        val commandData = command.buildCommand()
        commandData.name shouldBe "about"
    }

    @Test
    fun `handle should reply with about embed`() {
        // Arrange
        val event = mockk<SlashCommandInteractionEvent>(relaxed = true)
        val replyAction = mockk<ReplyCallbackAction>(relaxed = true)
        val embedSlot = slot<MessageEmbed>()

        every { event.replyEmbeds(capture(embedSlot)) } returns replyAction

        // Act
        command.handle(event)

        // Assert
        verify { event.replyEmbeds(any<MessageEmbed>()) }
        verify { replyAction.queue() }

        embedSlot.captured.title shouldBe "About LootMan Bot"
    }
}
