package com.edgerush.lootman.api.discord

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Unit tests for DiscordNotificationConfigController.
 */
class DiscordNotificationConfigControllerTest : UnitTest() {
    private lateinit var configService: DiscordNotificationConfigService
    private lateinit var controller: DiscordNotificationConfigController

    private val guildId = "guild-123"

    @BeforeEach
    fun setUp() {
        configService = mockk()
        controller = DiscordNotificationConfigController(configService)
    }

    @Nested
    inner class GetConfigs {
        @Test
        fun `should return configs for guild`() {
            val response =
                GuildNotificationConfigsResponse(
                    guildId = guildId,
                    configs = listOf(createConfigResponse()),
                )
            every { configService.getConfigsForGuild(guildId) } returns response

            val result = controller.getConfigs(guildId)

            result.guildId shouldBe guildId
            result.configs.size shouldBe 1
        }
    }

    @Nested
    inner class GetConfigByType {
        @Test
        fun `should return config when found`() {
            val response = createConfigResponse()
            every { configService.getConfigByType(guildId, "LOOT_AWARD") } returns response

            val result = controller.getConfigByType(guildId, "LOOT_AWARD")

            result.statusCode shouldBe HttpStatus.OK
            result.body shouldBe response
        }

        @Test
        fun `should return 204 when config not found`() {
            every { configService.getConfigByType(guildId, "LOOT_AWARD") } returns null

            val result = controller.getConfigByType(guildId, "LOOT_AWARD")

            result.statusCode shouldBe HttpStatus.NO_CONTENT
        }
    }

    @Nested
    inner class UpsertConfig {
        @Test
        fun `should create or update config`() {
            val request =
                UpsertNotificationConfigRequest(
                    discordServerId = "server-123",
                    notificationType = "LOOT_AWARD",
                    channelId = "channel-456",
                )
            val response = createConfigResponse()
            every { configService.upsertConfig(guildId, request) } returns response

            val result = controller.upsertConfig(guildId, request)

            result.id shouldBe 1L
            verify { configService.upsertConfig(guildId, request) }
        }
    }

    @Nested
    inner class UpdateConfig {
        @Test
        fun `should update config`() {
            val configId = 1L
            val request =
                UpdateNotificationConfigRequest(
                    channelId = "new-channel",
                    enabled = false,
                )
            val response = createConfigResponse()
            every { configService.updateConfig(guildId, configId, request) } returns response

            val result = controller.updateConfig(guildId, configId, request)

            result.id shouldBe 1L
            verify { configService.updateConfig(guildId, configId, request) }
        }
    }

    @Nested
    inner class DeleteConfig {
        @Test
        fun `should delete config and return 204`() {
            val configId = 1L
            every { configService.deleteConfig(guildId, configId) } returns Unit

            val result = controller.deleteConfig(guildId, configId)

            result.statusCode shouldBe HttpStatus.NO_CONTENT
            verify { configService.deleteConfig(guildId, configId) }
        }
    }

    @Nested
    inner class TestNotification {
        @Test
        fun `should return test result`() {
            val response =
                TestNotificationResponse(
                    success = true,
                    message = "Test notification sent",
                )
            every { configService.testNotification(guildId, "LOOT_AWARD") } returns response

            val result = controller.testNotification(guildId, "LOOT_AWARD")

            result.success shouldBe true
            result.message shouldBe "Test notification sent"
        }
    }

    private fun createConfigResponse(): DiscordNotificationConfigResponse =
        DiscordNotificationConfigResponse(
            id = 1L,
            guildId = guildId,
            discordServerId = "server-123",
            notificationType = "LOOT_AWARD",
            channelId = "channel-456",
            enabled = true,
            mentionRoleId = null,
            createdAt = Instant.now(),
            updatedAt = null,
        )
}
