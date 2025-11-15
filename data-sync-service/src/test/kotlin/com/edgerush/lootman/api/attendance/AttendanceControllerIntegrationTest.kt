package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate

/**
 * Integration tests for Attendance API endpoints.
 *
 * Tests verify:
 * - Tracking attendance records
 * - Retrieving attendance reports
 * - Query parameter handling
 * - Error handling for invalid inputs
 * - Backward compatibility with existing endpoints
 */
class AttendanceControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `should track attendance and return 201 Created`() {
        // Given
        val request =
            TrackAttendanceRequest(
                raiderId = 12345L,
                guildId = "test-guild-123",
                instance = "Nerub-ar Palace",
                encounter = null, // Overall instance attendance
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/attendance/track",
                entity,
                TrackAttendanceResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(request.raiderId, response.body!!.raiderId)
        assertEquals(request.guildId, response.body!!.guildId)
        assertEquals(request.instance, response.body!!.instance)
        assertEquals(request.attendedRaids, response.body!!.attendedRaids)
        assertEquals(request.totalRaids, response.body!!.totalRaids)
        assertEquals(0.8, response.body!!.attendancePercentage, 0.01)
        assertNotNull(response.body!!.recordId)
        assertNotNull(response.body!!.recordedAt)
    }

    @Test
    fun `should track encounter-specific attendance and return 201 Created`() {
        // Given
        val request =
            TrackAttendanceRequest(
                raiderId = 12345L,
                guildId = "test-guild-123",
                instance = "Nerub-ar Palace",
                encounter = "Queen Ansurek",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 6,
                totalRaids = 8,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/attendance/track",
                entity,
                TrackAttendanceResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(request.encounter, response.body!!.encounter)
        assertEquals(0.75, response.body!!.attendancePercentage, 0.01)
    }

    @Test
    fun `should get overall attendance report and return 200 OK`() {
        // Given
        val raiderId = 12345L
        val guildId = "test-guild-123"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/attendance/raiders/$raiderId/report?guildId=$guildId&startDate=$startDate&endDate=$endDate",
                AttendanceReportResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(raiderId, response.body!!.raiderId)
        assertEquals(guildId, response.body!!.guildId)
        assertEquals(startDate, response.body!!.startDate)
        assertEquals(endDate, response.body!!.endDate)
        assertNotNull(response.body!!.stats)
        assertTrue(response.body!!.stats.attendancePercentage >= 0.0)
        assertTrue(response.body!!.stats.attendancePercentage <= 1.0)
    }

    @Test
    fun `should get instance-specific attendance report and return 200 OK`() {
        // Given
        val raiderId = 12345L
        val guildId = "test-guild-123"
        val instance = "Nerub-ar Palace"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/attendance/raiders/$raiderId/report?guildId=$guildId&startDate=$startDate&endDate=$endDate&instance=$instance",
                AttendanceReportResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(instance, response.body!!.instance)
        assertNotNull(response.body!!.stats)
    }

    @Test
    fun `should get encounter-specific attendance report and return 200 OK`() {
        // Given
        val raiderId = 12345L
        val guildId = "test-guild-123"
        val instance = "Nerub-ar Palace"
        val encounter = "Queen Ansurek"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/attendance/raiders/$raiderId/report?guildId=$guildId&startDate=$startDate&endDate=$endDate&instance=$instance&encounter=$encounter",
                AttendanceReportResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(instance, response.body!!.instance)
        assertEquals(encounter, response.body!!.encounter)
        assertNotNull(response.body!!.stats)
    }

    @Test
    fun `should return 400 Bad Request when tracking attendance with invalid dates`() {
        // Given
        val request =
            TrackAttendanceRequest(
                raiderId = 12345L,
                guildId = "test-guild-123",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 14),
                endDate = LocalDate.of(2024, 11, 1), // End before start
                attendedRaids = 8,
                totalRaids = 10,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/attendance/track",
                entity,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should return 400 Bad Request when tracking attendance with negative raids`() {
        // Given
        val request =
            TrackAttendanceRequest(
                raiderId = 12345L,
                guildId = "test-guild-123",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = -1, // Invalid
                totalRaids = 10,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/attendance/track",
                entity,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should return 400 Bad Request when tracking attendance with attended greater than total`() {
        // Given
        val request =
            TrackAttendanceRequest(
                raiderId = 12345L,
                guildId = "test-guild-123",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 15, // Greater than total
                totalRaids = 10,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/attendance/track",
                entity,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should return 400 Bad Request when querying encounter without instance`() {
        // Given
        val raiderId = 12345L
        val guildId = "test-guild-123"
        val encounter = "Queen Ansurek"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        // When - Note: no instance parameter
        val response =
            restTemplate.getForEntity(
                "/api/v1/attendance/raiders/$raiderId/report?guildId=$guildId&startDate=$startDate&endDate=$endDate&encounter=$encounter",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should return 400 Bad Request when missing required query parameters`() {
        // Given
        val raiderId = 12345L

        // When - Missing guildId, startDate, endDate
        val response =
            restTemplate.getForEntity(
                "/api/v1/attendance/raiders/$raiderId/report",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
