package com.edgerush.datasync.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class SyncPropertiesTest {
    @Configuration
    @EnableConfigurationProperties(SyncProperties::class)
    class TestConfiguration

    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration::class.java)
            .withPropertyValues(
                "sync.wowaudit.base-url=https://api.test",
                "sync.wowaudit.guild-profile-uri=https://wowaudit.com/REGION/REALM/GUILD/profile",
                "sync.wowaudit.api-key=test-key-123",
                "sync.cron=0 15 5 * * *",
                "sync.run-on-startup=true",
            )

    @Test
    fun `binds properties from configuration`() {
        contextRunner.run { context ->
            val properties = context.getBean(SyncProperties::class.java)
            assertThat(properties.cron).isEqualTo("0 15 5 * * *")
            assertThat(properties.runOnStartup).isTrue()
            assertThat(properties.wowaudit.baseUrl).isEqualTo("https://api.test")
            assertThat(properties.wowaudit.guildProfileUri)
                .isEqualTo("https://wowaudit.com/REGION/REALM/GUILD/profile")
            assertThat(properties.wowaudit.apiKey).isEqualTo("test-key-123")
        }
    }
}
