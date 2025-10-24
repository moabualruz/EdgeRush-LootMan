package com.edgerush.datasync.client

import com.edgerush.datasync.config.SyncProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class WoWAuditClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: WoWAuditClient

    @BeforeEach
    fun setUp() {
        server = MockWebServer().apply { start() }
        val baseUrl = server.url("/").toString().removeSuffix("/")
        val props = SyncProperties(
            cron = "0 0 4 * * *",
            wowaudit = SyncProperties.WoWAudit(
                baseUrl = baseUrl,
                guildProfileUri = "https://wowaudit.com/REGION/REALM/GUILD/profile",
                apiKey = "test-key"
            )
        )
        val webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer test-key")
            .build()
        client = WoWAuditClient(webClient, props)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchRoster returns body on success`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"ok\":true}"))

        StepVerifier.create(client.fetchRoster())
            .expectNext("{\"ok\":true}")
            .verifyComplete()

        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("GET")
        assertThat(recorded.path).isEqualTo("/guild/characters")
    }

    @Test
    fun `fetchRoster throws on 429`() {
        server.enqueue(MockResponse().setResponseCode(429).setBody("Slow down"))

        StepVerifier.create(client.fetchRoster())
            .expectErrorSatisfies { ex ->
                assertThat(ex).isInstanceOf(WoWAuditRateLimitException::class.java)
                assertThat(ex.message).contains("Slow down")
            }
            .verify()
    }

    @Test
    fun `fetchRoster throws on 500`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Server oops"))

        StepVerifier.create(client.fetchRoster())
            .expectErrorSatisfies { ex ->
                assertThat(ex).isInstanceOf(WoWAuditServerException::class.java)
                assertThat(ex.message).contains("Server oops")
            }
            .verify()
    }

    @Test
    fun `requires guild profile uri`() {
        val props = SyncProperties(
            cron = "0 0 4 * * *",
            wowaudit = SyncProperties.WoWAudit(
                baseUrl = server.url("/").toString().removeSuffix("/"),
                guildProfileUri = null,
                apiKey = "test-key"
            )
        )
        val webClient = WebClient.create(server.url("/").toString())
        val brokenClient = WoWAuditClient(webClient, props)

        assertThatThrownBy { brokenClient.fetchRoster().block() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("guild-profile-uri")
    }
}
