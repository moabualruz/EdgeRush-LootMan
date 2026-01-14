package com.edgerush.lootman.bot.command

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandRegistryTest {

    private lateinit var registry: CommandRegistry
    private val handler1 = mockk<CommandHandler>()
    private val handler2 = mockk<CommandHandler>()

    @BeforeEach
    fun setUp() {
        every { handler1.name } returns "test1"
        every { handler1.buildCommand() } returns Commands.slash("test1", "Test command 1")

        every { handler2.name } returns "test2"
        every { handler2.buildCommand() } returns Commands.slash("test2", "Test command 2")

        registry = CommandRegistry(listOf(handler1, handler2))
    }

    @Test
    fun `registerCommands should register all handlers with JDA`() {
        // Arrange
        val jda = mockk<JDA>()
        val updateAction = mockk<CommandListUpdateAction>(relaxed = true)
        val commandsSlot = slot<Collection<SlashCommandData>>()

        every { jda.updateCommands() } returns updateAction
        every { updateAction.addCommands(capture(commandsSlot)) } returns updateAction

        // Act
        registry.registerCommands(jda)

        // Assert
        verify { jda.updateCommands() }
        verify { updateAction.addCommands(any<Collection<SlashCommandData>>()) }
        verify { updateAction.queue(any(), any()) }

        assert(commandsSlot.captured.size == 2)
        assert(commandsSlot.captured.any { it.name == "test1" })
        assert(commandsSlot.captured.any { it.name == "test2" })
    }
}
