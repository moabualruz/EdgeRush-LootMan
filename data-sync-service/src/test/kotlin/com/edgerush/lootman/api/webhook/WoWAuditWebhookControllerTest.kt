package com.edgerush.lootman.api.webhook

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.sync.PartialSyncUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * Unit tests for WoWAuditWebhookController.
 *
 * Tests the webhook endpoint using MockMvc.
 */
class WoWAuditWebhookControllerTest : UnitTest() {
    private lateinit var partialSyncUseCase: PartialSyncUseCase
    private lateinit var controller: WoWAuditWebhookController
    private lateinit var mockMvc: MockMvc
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

    @BeforeEach
    fun setup() {
        partialSyncUseCase = mockk()
        controller = WoWAuditWebhookController(partialSyncUseCase)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Nested
    inner class HandleCharacterUpdateTests {
        @Test
        fun `should return 202 Accepted when webhook processes successfully`() {
            // Given
            val payload =
                WoWAuditWebhookPayload(
                    eventType = "character.updated",
                    characterName = "TestChar",
                    characterRealm = "TestRealm",
                    guildId = "test-guild",
                )

            every { partialSyncUseCase.execute(any()) } returns PartialSyncResult.success("TestChar", 123L)

            // When/Then
            mockMvc.perform(
                post("/api/v1/webhooks/wowaudit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
                    .header("X-WoWAudit-Token", "test-token"),
            )
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.syncRunId").value(123))

            verify(exactly = 1) { partialSyncUseCase.execute(any()) }
        }

        @Test
        fun `should return 202 Accepted even when sync fails`() {
            // Given
            val payload =
                WoWAuditWebhookPayload(
                    eventType = "character.updated",
                    characterName = "TestChar",
                    characterRealm = "TestRealm",
                )

            every { partialSyncUseCase.execute(any()) } returns PartialSyncResult.failure("TestChar", "Sync failed")

            // When/Then
            mockMvc.perform(
                post("/api/v1/webhooks/wowaudit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)),
            )
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.accepted").value(false))
                .andExpect(jsonPath("$.message").value("Sync failed"))
        }

        @Test
        fun `should handle request without auth token`() {
            // Given - Token is optional for now
            val payload =
                WoWAuditWebhookPayload(
                    eventType = "loot.awarded",
                    characterName = "RichChar",
                    characterRealm = "RichRealm",
                    guildId = "rich-guild",
                )

            every { partialSyncUseCase.execute(any()) } returns PartialSyncResult.success("RichChar", 456L)

            // When/Then
            mockMvc.perform(
                post("/api/v1/webhooks/wowaudit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)),
            )
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.accepted").value(true))
        }
    }

    @Nested
    inner class HealthCheckTests {
        @Test
        fun `should return healthy status`() {
            // When/Then
            mockMvc.perform(get("/api/v1/webhooks/wowaudit/health"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("healthy"))
        }
    }
}
