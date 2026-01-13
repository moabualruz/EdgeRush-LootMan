package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for FLPS DTO classes.
 *
 * These tests ensure all DTO properties are properly accessible
 * and that data class copy/equality operations work correctly.
 */
class FlpsDtoTest : UnitTest() {

    @Nested
    inner class ComprehensiveFlpsReportDtoTest {

        @Test
        fun `should create DTO with all properties`() {
            // Given
            val raiderId = "raider-123"
            val raiderName = "TestRaider"
            val flpsScore = 0.85
            val eligible = true

            // When
            val dto = ComprehensiveFlpsReportDto(
                raiderId = raiderId,
                raiderName = raiderName,
                flpsScore = flpsScore,
                eligible = eligible,
            )

            // Then
            dto.raiderId shouldBe raiderId
            dto.raiderName shouldBe raiderName
            dto.flpsScore shouldBe flpsScore
            dto.eligible shouldBe eligible
        }

        @Test
        fun `should have correct property access for ineligible raider`() {
            // Given
            val dto = ComprehensiveFlpsReportDto(
                raiderId = "inactive-raider",
                raiderName = "InactivePlayer",
                flpsScore = 0.25,
                eligible = false,
            )

            // Then
            dto.raiderId shouldBe "inactive-raider"
            dto.raiderName shouldBe "InactivePlayer"
            dto.flpsScore shouldBe 0.25
            dto.eligible shouldBe false
        }

        @Test
        fun `should support data class copy operation`() {
            // Given
            val original = ComprehensiveFlpsReportDto(
                raiderId = "original",
                raiderName = "OriginalName",
                flpsScore = 0.5,
                eligible = true,
            )

            // When
            val copy = original.copy(flpsScore = 0.75)

            // Then
            copy.raiderId shouldBe original.raiderId
            copy.raiderName shouldBe original.raiderName
            copy.flpsScore shouldBe 0.75
            copy.eligible shouldBe original.eligible
        }
    }

    @Nested
    inner class FlpsDataStatusDtoTest {

        @Test
        fun `should create DTO with all properties`() {
            // Given
            val message = "System is operational"
            val features = listOf("Feature1", "Feature2", "Feature3")
            val endpoints = mapOf("endpoint1" to "/api/v1/endpoint1", "endpoint2" to "/api/v1/endpoint2")

            // When
            val dto = FlpsDataStatusDto(
                message = message,
                features = features,
                endpoints = endpoints,
            )

            // Then
            dto.message shouldBe message
            dto.features shouldBe features
            dto.endpoints shouldBe endpoints
        }

        @Test
        fun `should have correct property access for empty collections`() {
            // Given
            val dto = FlpsDataStatusDto(
                message = "Minimal status",
                features = emptyList(),
                endpoints = emptyMap(),
            )

            // Then
            dto.message shouldBe "Minimal status"
            dto.features shouldBe emptyList()
            dto.endpoints shouldBe emptyMap()
        }

        @Test
        fun `should support data class copy operation`() {
            // Given
            val original = FlpsDataStatusDto(
                message = "Original message",
                features = listOf("Feature1"),
                endpoints = mapOf("key" to "value"),
            )

            // When
            val copy = original.copy(message = "Updated message")

            // Then
            copy.message shouldBe "Updated message"
            copy.features shouldBe original.features
            copy.endpoints shouldBe original.endpoints
        }
    }

    @Nested
    inner class PerfectScoreBenchmarkDtoTest {

        @Test
        fun `should create DTO with all properties`() {
            // Given
            val theoretical = 1.0
            val topPerformer = 0.95

            // When
            val dto = PerfectScoreBenchmarkDto(
                theoretical = theoretical,
                topPerformer = topPerformer,
            )

            // Then
            dto.theoretical shouldBe theoretical
            dto.topPerformer shouldBe topPerformer
        }

        @Test
        fun `should have correct property access for different values`() {
            // Given
            val dto = PerfectScoreBenchmarkDto(
                theoretical = 0.99,
                topPerformer = 0.85,
            )

            // Then
            dto.theoretical shouldBe 0.99
            dto.topPerformer shouldBe 0.85
        }

        @Test
        fun `should support data class copy operation`() {
            // Given
            val original = PerfectScoreBenchmarkDto(
                theoretical = 1.0,
                topPerformer = 0.9,
            )

            // When
            val copy = original.copy(topPerformer = 0.88)

            // Then
            copy.theoretical shouldBe original.theoretical
            copy.topPerformer shouldBe 0.88
        }
    }

    @Nested
    inner class FlpsStatusResponseTest {

        @Test
        fun `should create response with all properties`() {
            // Given
            val message = "Status response"
            val features = listOf("DDD", "TDD", "Coverage")
            val endpoints = mapOf("report" to "/api/report", "status" to "/api/status")

            // When
            val response = FlpsStatusResponse(
                message = message,
                features = features,
                endpoints = endpoints,
            )

            // Then
            response.message shouldBe message
            response.features shouldBe features
            response.endpoints shouldBe endpoints
        }

        @Test
        fun `should have correct property access`() {
            // Given
            val response = FlpsStatusResponse(
                message = "Test message",
                features = listOf("Feature A"),
                endpoints = mapOf("key" to "/api/endpoint"),
            )

            // Then
            response.message shouldBe "Test message"
            response.features.size shouldBe 1
            response.features[0] shouldBe "Feature A"
            response.endpoints["key"] shouldBe "/api/endpoint"
        }
    }
}
