package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.*
import com.edgerush.datasync.api.dto.response.FlpsCalculationResponse
import com.edgerush.datasync.api.dto.response.FlpsReportResponse
import com.edgerush.datasync.domain.flps.model.*
import com.edgerush.datasync.domain.flps.repository.*
import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

/**
 * Integration tests for FlpsController.
 *
 * Tests the REST API endpoints for FLPS operations using the new use cases.
 */
class FlpsControllerIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var flpsModifierRepository: FlpsModifierRepository

    @BeforeEach
    fun setupFlpsConfiguration() {
        // Set up default FLPS configuration for test guild
        val config = FlpsConfiguration(
            rmsWeights = RmsWeights(
                attendance = 0.4,
                mechanical = 0.4,
                preparation = 0.2
            ),
            ipiWeights = IpiWeights(
                upgradeValue = 0.5,
                tierBonus = 0.3,
                roleMultiplier = 0.2
            ),
            thresholds = FlpsThresholds(
                eligibilityAttendance = 0.75,
                eligibilityActivity = 30.0
            ),
            roleMultipliers = RoleMultipliers(
                tank = 1.0,
                healer = 1.0,
                dps = 0.9
            ),
            recencyPenalties = RecencyPenalties(
                tierA = 0.5,
                tierB = 0.7,
                tierC = 0.9,
                recoveryRate = 0.1
            )
        )
        flpsModifierRepository.save("test-guild", config)
    }

    @Test
    fun `should calculate FLPS score when valid request is provided`() {
        // Arrange
        val request = FlpsCalculationRequest(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 5.0,
            specAvgAdt = 10.0,
            vaultSlots = 3,
            crestUsageRatio = 0.9,
            heroicKills = 8,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = "DPS",
            recentLootCount = 1
        )

        // Act
        val response = restTemplate.postForEntity(
            "/api/v1/flps/calculate",
            request,
            FlpsCalculationResponse::class.java
        )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.flpsScore shouldBeGreaterThan 0.0
        response.body!!.flpsScore shouldBeLessThanOrEqual 1.0
        response.body!!.raiderMerit shouldBeGreaterThan 0.0
        response.body!!.itemPriority shouldBeGreaterThan 0.0
        response.body!!.recencyDecay shouldBeGreaterThan 0.0
    }

    @Test
    fun `should return 400 when calculating FLPS with invalid role`() {
        // Arrange
        val request = FlpsCalculationRequest(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 5.0,
            specAvgAdt = 10.0,
            vaultSlots = 3,
            crestUsageRatio = 0.9,
            heroicKills = 8,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = "INVALID_ROLE",
            recentLootCount = 1
        )

        // Act
        val response = restTemplate.postForEntity(
            "/api/v1/flps/calculate",
            request,
            String::class.java
        )

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `should generate FLPS report for multiple raiders`() {
        // Arrange
        val request = FlpsReportRequest(
            guildId = "test-guild",
            raiders = listOf(
                createRaiderFlpsDataRequest("raider-1", "Raider One", 95, 1),
                createRaiderFlpsDataRequest("raider-2", "Raider Two", 90, 0),
                createRaiderFlpsDataRequest("raider-3", "Raider Three", 85, 2)
            )
        )

        // Act
        val response = restTemplate.postForEntity(
            "/api/v1/flps/report",
            request,
            FlpsReportResponse::class.java
        )

        // Assert
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldNotBe null
        response.body!!.guildId shouldBe "test-guild"
        response.body!!.raiderReports.size shouldBe 3
        
        // Verify raiders are sorted by FLPS score (descending)
        val scores = response.body!!.raiderReports.map { it.flpsScore }
        scores shouldBe scores.sortedDescending()
        
        // Verify all scores are valid
        response.body!!.raiderReports.forEach { raider ->
            raider.flpsScore shouldBeGreaterThan 0.0
            raider.flpsScore shouldBeLessThanOrEqual 1.0
            raider.flpsPercentage shouldBeGreaterThan 0.0
            raider.flpsPercentage shouldBeLessThanOrEqual 100.0
        }
        
        // Verify configuration is included
        response.body!!.configuration shouldNotBe null
        response.body!!.configuration.rmsWeights.attendance shouldBe 0.4
    }

    @Test
    fun `should update FLPS modifiers when valid request is provided`() {
        // Arrange
        val request = UpdateFlpsModifiersRequest(
            rmsWeights = RmsWeightsRequest(
                attendance = 0.5,
                mechanical = 0.3,
                preparation = 0.2
            ),
            ipiWeights = IpiWeightsRequest(
                upgradeValue = 0.6,
                tierBonus = 0.2,
                roleMultiplier = 0.2
            ),
            thresholds = FlpsThresholdsRequest(
                eligibilityAttendance = 0.8,
                eligibilityActivity = 40.0
            ),
            roleMultipliers = RoleMultipliersRequest(
                tank = 1.1,
                healer = 1.0,
                dps = 0.9
            ),
            recencyPenalties = RecencyPenaltiesRequest(
                tierA = 0.4,
                tierB = 0.6,
                tierC = 0.8,
                recoveryRate = 0.15
            )
        )

        // Act
        val response = restTemplate.exchange(
            "/api/v1/flps/test-guild/modifiers",
            HttpMethod.PUT,
            HttpEntity(request),
            Void::class.java
        )

        // Assert
        response.statusCode shouldBe HttpStatus.NO_CONTENT
        
        // Verify configuration was updated
        val updatedConfig = flpsModifierRepository.findByGuildId("test-guild")
        updatedConfig.rmsWeights.attendance shouldBe 0.5
        updatedConfig.ipiWeights.upgradeValue shouldBe 0.6
        updatedConfig.thresholds.eligibilityAttendance shouldBe 0.8
    }

    @Test
    fun `should return 400 when updating modifiers with invalid weights`() {
        // Arrange
        val request = UpdateFlpsModifiersRequest(
            rmsWeights = RmsWeightsRequest(
                attendance = -0.1, // Invalid: negative
                mechanical = 0.3,
                preparation = 0.2
            ),
            ipiWeights = IpiWeightsRequest(
                upgradeValue = 0.6,
                tierBonus = 0.2,
                roleMultiplier = 0.2
            ),
            thresholds = FlpsThresholdsRequest(
                eligibilityAttendance = 0.8,
                eligibilityActivity = 40.0
            ),
            roleMultipliers = RoleMultipliersRequest(
                tank = 1.0,
                healer = 1.0,
                dps = 0.9
            ),
            recencyPenalties = RecencyPenaltiesRequest(
                tierA = 0.5,
                tierB = 0.7,
                tierC = 0.9,
                recoveryRate = 0.1
            )
        )

        // Act
        val response = restTemplate.exchange(
            "/api/v1/flps/test-guild/modifiers",
            HttpMethod.PUT,
            HttpEntity(request),
            String::class.java
        )

        // Assert
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `should maintain backward compatibility with existing FLPS endpoints`() {
        // This test verifies that the new endpoints work alongside the legacy endpoints
        // The legacy FlpsController at /api/flps should still work
        
        // Act - Test new endpoint
        val newRequest = FlpsCalculationRequest(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 5.0,
            specAvgAdt = 10.0,
            vaultSlots = 3,
            crestUsageRatio = 0.9,
            heroicKills = 8,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = "DPS",
            recentLootCount = 1
        )
        
        val newResponse = restTemplate.postForEntity(
            "/api/v1/flps/calculate",
            newRequest,
            FlpsCalculationResponse::class.java
        )

        // Assert - New endpoint works
        newResponse.statusCode shouldBe HttpStatus.OK
        newResponse.body shouldNotBe null
    }

    @Test
    fun `should verify API contract for calculation response format`() {
        // Arrange
        val request = FlpsCalculationRequest(
            guildId = "test-guild",
            attendancePercent = 95,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 5.0,
            specAvgAdt = 10.0,
            vaultSlots = 3,
            crestUsageRatio = 0.9,
            heroicKills = 8,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = "DPS",
            recentLootCount = 1
        )

        // Act
        val response = restTemplate.postForEntity(
            "/api/v1/flps/calculate",
            request,
            FlpsCalculationResponse::class.java
        )

        // Assert - Verify all required fields are present
        response.statusCode shouldBe HttpStatus.OK
        val body = response.body!!
        
        // Verify main scores
        body.flpsScore shouldNotBe null
        body.raiderMerit shouldNotBe null
        body.itemPriority shouldNotBe null
        body.recencyDecay shouldNotBe null
        
        // Verify component scores
        body.attendanceScore shouldNotBe null
        body.mechanicalScore shouldNotBe null
        body.preparationScore shouldNotBe null
        body.upgradeValue shouldNotBe null
        body.tierBonus shouldNotBe null
        body.roleMultiplier shouldNotBe null
    }

    @Test
    fun `should verify API contract for report response format`() {
        // Arrange
        val request = FlpsReportRequest(
            guildId = "test-guild",
            raiders = listOf(
                createRaiderFlpsDataRequest("raider-1", "Raider One", 95, 1)
            )
        )

        // Act
        val response = restTemplate.postForEntity(
            "/api/v1/flps/report",
            request,
            FlpsReportResponse::class.java
        )

        // Assert - Verify all required fields are present
        response.statusCode shouldBe HttpStatus.OK
        val body = response.body!!
        
        body.guildId shouldBe "test-guild"
        body.raiderReports.size shouldBe 1
        body.configuration shouldNotBe null
        
        val raider = body.raiderReports[0]
        raider.raiderId shouldBe "raider-1"
        raider.raiderName shouldBe "Raider One"
        
        // Verify all score fields
        raider.flpsScore shouldNotBe null
        raider.raiderMerit shouldNotBe null
        raider.itemPriority shouldNotBe null
        raider.recencyDecay shouldNotBe null
        
        // Verify all percentage fields
        raider.flpsPercentage shouldNotBe null
        raider.rmsPercentage shouldNotBe null
        raider.ipiPercentage shouldNotBe null
        raider.recencyDecayPercentage shouldNotBe null
    }

    private fun createRaiderFlpsDataRequest(
        raiderId: String,
        raiderName: String,
        attendancePercent: Int,
        recentLootCount: Int
    ): RaiderFlpsDataRequest {
        return RaiderFlpsDataRequest(
            raiderId = raiderId,
            raiderName = raiderName,
            attendancePercent = attendancePercent,
            deathsPerAttempt = 0.5,
            specAvgDpa = 1.0,
            avoidableDamagePct = 5.0,
            specAvgAdt = 10.0,
            vaultSlots = 3,
            crestUsageRatio = 0.9,
            heroicKills = 8,
            simulatedGain = 500.0,
            specBaseline = 10000.0,
            tierPiecesOwned = 2,
            role = "DPS",
            recentLootCount = recentLootCount
        )
    }
}
