package com.edgerush.lootman.api.discord

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfigId
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import com.edgerush.lootman.domain.discord.repository.DiscordNotificationConfigRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for DiscordNotificationConfigService.
 */
class DiscordNotificationConfigServiceTest : UnitTest() {
    private lateinit var repository: DiscordNotificationConfigRepository
    private lateinit var service: DiscordNotificationConfigService

    private val guildId = "guild-123"
    private val guildIdObj = GuildId(guildId)

    @BeforeEach
    fun setUp() {
        repository = mockk()
        service = DiscordNotificationConfigService(repository)
    }

    @Nested
    inner class GetConfigsForGuild {
        @Test
        fun `should return all configs for guild`() {
            val configs =
                listOf(
                    createConfig(DiscordNotificationType.LOOT_AWARD),
                    createConfig(DiscordNotificationType.PENALTY),
                )
            every { repository.findByGuildId(guildIdObj) } returns configs

            val result = service.getConfigsForGuild(guildId)

            result.guildId shouldBe guildId
            result.configs.size shouldBe 2
            result.availableTypes.size shouldBe 5
        }

        @Test
        fun `should return empty list when no configs exist`() {
            every { repository.findByGuildId(guildIdObj) } returns emptyList()

            val result = service.getConfigsForGuild(guildId)

            result.configs shouldBe emptyList()
        }
    }

    @Nested
    inner class GetConfigByType {
        @Test
        fun `should return config for type`() {
            val config = createConfig(DiscordNotificationType.LOOT_AWARD)
            every { repository.findByGuildIdAndType(guildIdObj, DiscordNotificationType.LOOT_AWARD) } returns config

            val result = service.getConfigByType(guildId, "LOOT_AWARD")

            result shouldNotBe null
            result!!.notificationType shouldBe "LOOT_AWARD"
        }

        @Test
        fun `should return null when config not found`() {
            every { repository.findByGuildIdAndType(guildIdObj, DiscordNotificationType.PENALTY) } returns null

            val result = service.getConfigByType(guildId, "PENALTY")

            result shouldBe null
        }

        @Test
        fun `should throw for invalid notification type`() {
            shouldThrow<InvalidNotificationTypeException> {
                service.getConfigByType(guildId, "INVALID_TYPE")
            }
        }
    }

    @Nested
    inner class UpsertConfig {
        @Test
        fun `should create new config when none exists`() {
            val request =
                UpsertNotificationConfigRequest(
                    discordServerId = "server-123",
                    notificationType = "LOOT_AWARD",
                    channelId = "channel-456",
                    enabled = true,
                    mentionRoleId = null,
                )
            every { repository.findByGuildIdAndType(guildIdObj, DiscordNotificationType.LOOT_AWARD) } returns null
            every { repository.save(any()) } answers {
                firstArg<DiscordNotificationConfig>().copy(id = DiscordNotificationConfigId(1L))
            }

            val result = service.upsertConfig(guildId, request)

            result.channelId shouldBe "channel-456"
            result.notificationType shouldBe "LOOT_AWARD"
            verify { repository.save(any()) }
        }

        @Test
        fun `should update existing config`() {
            val existingConfig = createConfig(DiscordNotificationType.LOOT_AWARD)
            val request =
                UpsertNotificationConfigRequest(
                    discordServerId = "server-123",
                    notificationType = "LOOT_AWARD",
                    channelId = "new-channel-789",
                    enabled = false,
                    mentionRoleId = "role-123",
                )
            every { repository.findByGuildIdAndType(guildIdObj, DiscordNotificationType.LOOT_AWARD) } returns existingConfig
            every { repository.save(any()) } answers { firstArg() }

            val result = service.upsertConfig(guildId, request)

            result.channelId shouldBe "new-channel-789"
            result.enabled shouldBe false
            result.mentionRoleId shouldBe "role-123"
        }

        @Test
        fun `should throw for invalid notification type in request`() {
            val request =
                UpsertNotificationConfigRequest(
                    discordServerId = "server-123",
                    notificationType = "INVALID",
                    channelId = "channel-456",
                )

            shouldThrow<InvalidNotificationTypeException> {
                service.upsertConfig(guildId, request)
            }
        }
    }

    @Nested
    inner class UpdateConfig {
        @Test
        fun `should update config`() {
            val configId = 1L
            val existingConfig = createConfig(DiscordNotificationType.LOOT_AWARD)
            val request =
                UpdateNotificationConfigRequest(
                    channelId = "new-channel",
                    enabled = false,
                )
            every { repository.findById(DiscordNotificationConfigId(configId)) } returns existingConfig
            every { repository.save(any()) } answers { firstArg() }

            val result = service.updateConfig(guildId, configId, request)

            result.channelId shouldBe "new-channel"
            result.enabled shouldBe false
        }

        @Test
        fun `should throw when config not found`() {
            val configId = 999L
            every { repository.findById(DiscordNotificationConfigId(configId)) } returns null

            shouldThrow<NotificationConfigNotFoundException> {
                service.updateConfig(guildId, configId, UpdateNotificationConfigRequest())
            }
        }

        @Test
        fun `should throw when config belongs to different guild`() {
            val configId = 1L
            val otherGuildConfig =
                DiscordNotificationConfig(
                    id = DiscordNotificationConfigId(configId),
                    guildId = GuildId("other-guild"),
                    discordServerId = "server-123",
                    notificationType = DiscordNotificationType.LOOT_AWARD,
                    channelId = "channel-456",
                )
            every { repository.findById(DiscordNotificationConfigId(configId)) } returns otherGuildConfig

            shouldThrow<NotificationConfigNotFoundException> {
                service.updateConfig(guildId, configId, UpdateNotificationConfigRequest())
            }
        }
    }

    @Nested
    inner class DeleteConfig {
        @Test
        fun `should delete config`() {
            val configId = 1L
            val existingConfig = createConfig(DiscordNotificationType.LOOT_AWARD)
            every { repository.findById(DiscordNotificationConfigId(configId)) } returns existingConfig
            every { repository.deleteById(DiscordNotificationConfigId(configId)) } returns Unit

            service.deleteConfig(guildId, configId)

            verify { repository.deleteById(DiscordNotificationConfigId(configId)) }
        }

        @Test
        fun `should throw when config not found`() {
            val configId = 999L
            every { repository.findById(DiscordNotificationConfigId(configId)) } returns null

            shouldThrow<NotificationConfigNotFoundException> {
                service.deleteConfig(guildId, configId)
            }
        }
    }

    @Nested
    inner class TestNotification {
        @Test
        fun `should return success when config exists`() {
            val config = createConfig(DiscordNotificationType.LOOT_AWARD)
            every { repository.findEnabledByGuildIdAndType(guildIdObj, DiscordNotificationType.LOOT_AWARD) } returns config

            val result = service.testNotification(guildId, "LOOT_AWARD")

            result.success shouldBe true
            result.message shouldBe "Test notification would be sent to channel channel-456"
        }

        @Test
        fun `should return failure when no config exists`() {
            every { repository.findEnabledByGuildIdAndType(guildIdObj, DiscordNotificationType.LOOT_AWARD) } returns null

            val result = service.testNotification(guildId, "LOOT_AWARD")

            result.success shouldBe false
        }
    }

    private fun createConfig(type: DiscordNotificationType): DiscordNotificationConfig =
        DiscordNotificationConfig(
            id = DiscordNotificationConfigId(1L),
            guildId = guildIdObj,
            discordServerId = "server-123",
            notificationType = type,
            channelId = "channel-456",
            enabled = true,
        )
}
