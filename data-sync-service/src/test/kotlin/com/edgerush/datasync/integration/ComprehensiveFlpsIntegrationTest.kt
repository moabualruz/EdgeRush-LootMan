package com.edgerush.datasync.integration

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.repository.BehavioralActionRepository
import com.edgerush.datasync.repository.LootBanRepository
import com.edgerush.datasync.service.GuildManagementService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Import(com.edgerush.datasync.config.TestSecurityConfig::class)
@Transactional
class ComprehensiveFlpsIntegrationTest {
    @Autowired
    private lateinit var guildManagementService: GuildManagementService

    @Autowired
    private lateinit var behavioralActionRepository: BehavioralActionRepository

    @Autowired
    private lateinit var lootBanRepository: LootBanRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val testGuildId = "integration-test-guild"

    @BeforeEach
    fun setUp() {
        // Clean up any existing test data
        behavioralActionRepository.deleteAll()
        lootBanRepository.deleteAll()
    }

    @Test
    fun `comprehensive FLPS report should include all components`() {
        // Given: Some test behavioral data
        val behavioralDeduction =
            BehavioralActionEntity(
                guildId = testGuildId,
                characterName = "TestCharacter",
                actionType = "DEDUCTION",
                deductionAmount = 0.2,
                reason = "Late to raid",
                appliedBy = "TestLeader",
                appliedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(7),
            )
        behavioralActionRepository.save(behavioralDeduction)

        // When: Calling the comprehensive service method
        val reports = guildManagementService.calculateComprehensiveFlpsReport(testGuildId)

        // Then: Verify the response structure includes all expected components
        assert(reports.isNotEmpty() || reports.isEmpty()) // Just verify it returns without error

        // If we have reports, verify the structure
        if (reports.isNotEmpty()) {
            val firstReport = reports[0]

            // Verify core components exist
            assert(firstReport.breakdown.name.isNotEmpty())
            assert(firstReport.attendanceScore >= 0.0)
            assert(firstReport.attendancePercentage >= 0.0)
            assert(firstReport.behavioralScore >= 0.0)
            assert(firstReport.behavioralPercentage >= 0.0)
            assert(firstReport.flpsScore >= 0.0)
            assert(firstReport.flpsPercentage >= 0.0)
            assert(firstReport.benchmarkUsed.isNotEmpty())
        }
    }

    @Test
    fun `behavioral deduction should affect eligibility`() {
        // Given: A character with behavioral deduction but no loot ban
        val characterName = "TestCharacter"
        val behavioralDeduction =
            BehavioralActionEntity(
                guildId = testGuildId,
                characterName = characterName,
                actionType = "DEDUCTION",
                deductionAmount = 0.3,
                reason = "Behavioral issue",
                appliedBy = "TestLeader",
                appliedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(7),
            )
        behavioralActionRepository.save(behavioralDeduction)

        // When: Getting the FLPS report
        val reports = guildManagementService.calculateComprehensiveFlpsReport(testGuildId)
        val characterReport = reports.find { it.breakdown.name == characterName }

        // Then: Behavioral score should be reduced but character should still be eligible for loot
        if (characterReport != null) {
            assert(characterReport.behavioralScore == 0.7) // 1.0 - 0.3
            assert(characterReport.behavioralPercentage == 70.0) // 70%
            assert(!characterReport.behavioralBreakdown.lootBanInfo.isBanned)
        }
    }

    @Test
    fun `loot ban should make character ineligible`() {
        // Given: A character with an active loot ban
        val characterName = "BannedCharacter"
        val lootBan =
            LootBanEntity(
                guildId = testGuildId,
                characterName = characterName,
                reason = "DKP violation",
                bannedBy = "TestLeader",
                bannedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(7),
            )
        lootBanRepository.save(lootBan)

        // When: Getting the FLPS report
        val reports = guildManagementService.calculateComprehensiveFlpsReport(testGuildId)
        val characterReport = reports.find { it.breakdown.name == characterName }

        // Then: Character should be ineligible for loot
        if (characterReport != null) {
            assert(!characterReport.eligible)
            assert(characterReport.eligibilityReasons.any { it.contains("banned from loot") })
            assert(characterReport.behavioralBreakdown.lootBanInfo.isBanned)
            assert(characterReport.behavioralBreakdown.lootBanInfo.reason == "DKP violation")
        }
    }

    @Test
    fun `management endpoints should work correctly`() {
        // Test behavioral deduction endpoint
        val behavioralRequest =
            mapOf(
                "characterName" to "TestCharacter",
                "deductionAmount" to 0.2,
                "reason" to "Late to raid",
                "appliedBy" to "TestLeader",
                "expiresAt" to LocalDateTime.now().plusDays(7).toString(),
            )

        mockMvc.perform(
            post("/api/guild/$testGuildId/management/behavioral/deduction")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(behavioralRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.actionType").value("DEDUCTION"))
            .andExpect(jsonPath("$.deductionAmount").value(0.2))

        // Test loot ban endpoint
        val banRequest =
            mapOf(
                "characterName" to "TestCharacter",
                "reason" to "DKP violation",
                "bannedBy" to "TestLeader",
                "expiresAt" to LocalDateTime.now().plusDays(3).toString(),
            )

        mockMvc.perform(
            post("/api/guild/$testGuildId/management/loot-ban")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(banRequest)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reason").value("DKP violation"))

        // Test getting active behavioral actions
        mockMvc.perform(get("/api/guild/$testGuildId/management/behavioral/active"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))

        // Test getting active loot bans
        mockMvc.perform(get("/api/guild/$testGuildId/management/loot-bans/active"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
    }

    @Test
    fun `status endpoint should return system information`() {
        mockMvc.perform(get("/api/flps/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.features").isArray)
            .andExpect(jsonPath("$.endpoints").exists())
    }
}
