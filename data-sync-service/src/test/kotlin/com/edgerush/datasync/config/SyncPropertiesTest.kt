package com.edgerush.datasync.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "sync.wowaudit.base-url=https://api.test",
        "sync.wowaudit.guild-profile-uri=https://wowaudit.com/REGION/REALM/GUILD/profile",
        "sync.wowaudit.api-key=test-key-123",
        "sync.cron=0 15 5 * * *",
        "sync.run-on-startup=true"
    ]
)
class SyncPropertiesTest @Autowired constructor(
    private val properties: SyncProperties
) {

    @Test
    fun `binds properties from configuration`() {
        assertThat(properties.cron).isEqualTo("0 15 5 * * *")
        assertThat(properties.runOnStartup).isTrue()
        assertThat(properties.wowaudit.baseUrl).isEqualTo("https://api.test")
        assertThat(properties.wowaudit.guildProfileUri)
            .isEqualTo("https://wowaudit.com/REGION/REALM/GUILD/profile")
        assertThat(properties.wowaudit.apiKey).isEqualTo("test-key-123")
    }
}
