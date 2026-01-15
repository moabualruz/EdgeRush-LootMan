package com.edgerush.lootman.domain.discord.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for DiscordNotificationConfig domain entity.
 */
class DiscordNotificationConfigTest : UnitTest() {
    private val guildId = GuildId("guild-123")
    private val discordServerId = "discord-server-456"
    private val channelId = "channel-789"

    @Nested
    inner class Creation {
        @Test
        fun `should create notification config with valid data`() {
            val config =
                DiscordNotificationConfig.create(
                    guildId = guildId,
                    discordServerId = discordServerId,
                    notificationType = DiscordNotificationType.LOOT_AWARD,
                    channelId = channelId,
                )

            config.guildId shouldBe guildId
            config.discordServerId shouldBe discordServerId
            config.notificationType shouldBe DiscordNotificationType.LOOT_AWARD
            config.channelId shouldBe channelId
            config.enabled shouldBe true
            config.mentionRoleId shouldBe null
            config.id shouldBe null
        }

        @Test
        fun `should create notification config with mention role`() {
            val roleId = "role-123"
            val config =
                DiscordNotificationConfig.create(
                    guildId = guildId,
                    discordServerId = discordServerId,
                    notificationType = DiscordNotificationType.PENALTY,
                    channelId = channelId,
                    mentionRoleId = roleId,
                )

            config.mentionRoleId shouldBe roleId
        }

        @Test
        fun `should fail when discord server ID is blank`() {
            shouldThrow<IllegalArgumentException> {
                DiscordNotificationConfig.create(
                    guildId = guildId,
                    discordServerId = "",
                    notificationType = DiscordNotificationType.LOOT_AWARD,
                    channelId = channelId,
                )
            }
        }

        @Test
        fun `should fail when channel ID is blank`() {
            shouldThrow<IllegalArgumentException> {
                DiscordNotificationConfig.create(
                    guildId = guildId,
                    discordServerId = discordServerId,
                    notificationType = DiscordNotificationType.LOOT_AWARD,
                    channelId = "",
                )
            }
        }
    }

    @Nested
    inner class StateChanges {
        @Test
        fun `should enable config`() {
            val config = createConfig().disable()
            config.enabled shouldBe false

            val enabled = config.enable()

            enabled.enabled shouldBe true
            enabled.updatedAt shouldNotBe null
        }

        @Test
        fun `should disable config`() {
            val config = createConfig()
            config.enabled shouldBe true

            val disabled = config.disable()

            disabled.enabled shouldBe false
            disabled.updatedAt shouldNotBe null
        }

        @Test
        fun `should update channel`() {
            val config = createConfig()
            val newChannelId = "new-channel-123"

            val updated = config.updateChannel(newChannelId)

            updated.channelId shouldBe newChannelId
            updated.updatedAt shouldNotBe null
        }

        @Test
        fun `should fail when updating to blank channel`() {
            val config = createConfig()

            shouldThrow<IllegalArgumentException> {
                config.updateChannel("")
            }
        }

        @Test
        fun `should update mention role`() {
            val config = createConfig()
            val newRoleId = "new-role-456"

            val updated = config.updateMentionRole(newRoleId)

            updated.mentionRoleId shouldBe newRoleId
            updated.updatedAt shouldNotBe null
        }

        @Test
        fun `should clear mention role when set to null`() {
            val config = createConfig().updateMentionRole("some-role")

            val updated = config.updateMentionRole(null)

            updated.mentionRoleId shouldBe null
        }

        @Test
        fun `should add ID with withId`() {
            val config = createConfig()
            val id = DiscordNotificationConfigId(42L)

            val withId = config.withId(id)

            withId.id shouldBe id
        }
    }

    @Nested
    inner class NotificationTypes {
        @Test
        fun `should support all notification types`() {
            val types = DiscordNotificationType.entries

            types.size shouldBe 5
            types shouldBe
                listOf(
                    DiscordNotificationType.LOOT_AWARD,
                    DiscordNotificationType.RDF_EXPIRY,
                    DiscordNotificationType.PENALTY,
                    DiscordNotificationType.LOOT_BAN,
                    DiscordNotificationType.SYNC_COMPLETE,
                )
        }

        @Test
        fun `should parse notification type from string`() {
            DiscordNotificationType.fromString("LOOT_AWARD") shouldBe DiscordNotificationType.LOOT_AWARD
            DiscordNotificationType.fromString("loot_award") shouldBe DiscordNotificationType.LOOT_AWARD
            DiscordNotificationType.fromString("Loot_Award") shouldBe DiscordNotificationType.LOOT_AWARD
        }

        @Test
        fun `should return null for invalid notification type`() {
            DiscordNotificationType.fromString("INVALID") shouldBe null
            DiscordNotificationType.fromString("") shouldBe null
        }
    }

    private fun createConfig(): DiscordNotificationConfig =
        DiscordNotificationConfig.create(
            guildId = guildId,
            discordServerId = discordServerId,
            notificationType = DiscordNotificationType.LOOT_AWARD,
            channelId = channelId,
        )
}
