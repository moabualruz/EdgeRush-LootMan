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
            val dto =
                ComprehensiveFlpsReportDto(
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
            val dto =
                ComprehensiveFlpsReportDto(
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
            val original =
                ComprehensiveFlpsReportDto(
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

        @Test
        fun `should have correct equals and hashCode`() {
            // Given
            val dto1 =
                ComprehensiveFlpsReportDto(
                    raiderId = "raider-1",
                    raiderName = "Raider",
                    flpsScore = 0.8,
                    eligible = true,
                )
            val dto2 =
                ComprehensiveFlpsReportDto(
                    raiderId = "raider-1",
                    raiderName = "Raider",
                    flpsScore = 0.8,
                    eligible = true,
                )
            val dto3 =
                ComprehensiveFlpsReportDto(
                    raiderId = "raider-2",
                    raiderName = "Other",
                    flpsScore = 0.5,
                    eligible = false,
                )

            // Then
            (dto1 == dto2) shouldBe true
            dto1.hashCode() shouldBe dto2.hashCode()
            (dto1 == dto3) shouldBe false
        }

        @Test
        fun `should have correct toString`() {
            // Given
            val dto =
                ComprehensiveFlpsReportDto(
                    raiderId = "raider-123",
                    raiderName = "TestRaider",
                    flpsScore = 0.85,
                    eligible = true,
                )

            // When
            val str = dto.toString()

            // Then
            str.contains("raiderId=raider-123") shouldBe true
            str.contains("raiderName=TestRaider") shouldBe true
            str.contains("flpsScore=0.85") shouldBe true
            str.contains("eligible=true") shouldBe true
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
            val dto =
                FlpsDataStatusDto(
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
            val dto =
                FlpsDataStatusDto(
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
            val original =
                FlpsDataStatusDto(
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

        @Test
        fun `should have correct equals and hashCode`() {
            // Given
            val dto1 =
                FlpsDataStatusDto(
                    message = "Status",
                    features = listOf("F1"),
                    endpoints = mapOf("key" to "value"),
                )
            val dto2 =
                FlpsDataStatusDto(
                    message = "Status",
                    features = listOf("F1"),
                    endpoints = mapOf("key" to "value"),
                )
            val dto3 =
                FlpsDataStatusDto(
                    message = "Other",
                    features = emptyList(),
                    endpoints = emptyMap(),
                )

            // Then
            (dto1 == dto2) shouldBe true
            dto1.hashCode() shouldBe dto2.hashCode()
            (dto1 == dto3) shouldBe false
        }

        @Test
        fun `should have correct toString`() {
            // Given
            val dto =
                FlpsDataStatusDto(
                    message = "Test message",
                    features = listOf("Feature1"),
                    endpoints = mapOf("endpoint" to "/api/test"),
                )

            // When
            val str = dto.toString()

            // Then
            str.contains("message=Test message") shouldBe true
            str.contains("features=") shouldBe true
            str.contains("endpoints=") shouldBe true
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
            val dto =
                PerfectScoreBenchmarkDto(
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
            val dto =
                PerfectScoreBenchmarkDto(
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
            val original =
                PerfectScoreBenchmarkDto(
                    theoretical = 1.0,
                    topPerformer = 0.9,
                )

            // When
            val copy = original.copy(topPerformer = 0.88)

            // Then
            copy.theoretical shouldBe original.theoretical
            copy.topPerformer shouldBe 0.88
        }

        @Test
        fun `should have correct equals and hashCode`() {
            // Given
            val dto1 =
                PerfectScoreBenchmarkDto(
                    theoretical = 1.0,
                    topPerformer = 0.95,
                )
            val dto2 =
                PerfectScoreBenchmarkDto(
                    theoretical = 1.0,
                    topPerformer = 0.95,
                )
            val dto3 =
                PerfectScoreBenchmarkDto(
                    theoretical = 0.9,
                    topPerformer = 0.8,
                )

            // Then
            (dto1 == dto2) shouldBe true
            dto1.hashCode() shouldBe dto2.hashCode()
            (dto1 == dto3) shouldBe false
        }

        @Test
        fun `should have correct toString`() {
            // Given
            val dto =
                PerfectScoreBenchmarkDto(
                    theoretical = 1.0,
                    topPerformer = 0.95,
                )

            // When
            val str = dto.toString()

            // Then
            str.contains("theoretical=1.0") shouldBe true
            str.contains("topPerformer=0.95") shouldBe true
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
            val response =
                FlpsStatusResponse(
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
            val response =
                FlpsStatusResponse(
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
