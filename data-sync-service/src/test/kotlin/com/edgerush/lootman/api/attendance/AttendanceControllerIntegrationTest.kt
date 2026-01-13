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

    @Test
    fun `should get attendance record by ID and return 200 OK`() {
        // Given - First create a record
        val createRequest = TrackAttendanceRequest(
            raiderId = 99001L,
            guildId = "get-record-test-guild",
            instance = "Test Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 7,
            totalRaids = 10
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/attendance/track",
            createEntity,
            TrackAttendanceResponse::class.java
        )

        val recordId = createResponse.body!!.recordId

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/$recordId",
            TrackAttendanceResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(recordId, response.body!!.recordId)
        assertEquals(createRequest.raiderId, response.body!!.raiderId)
        assertEquals(createRequest.instance, response.body!!.instance)
    }

    @Test
    fun `should return 404 Not Found when getting non-existent attendance record`() {
        // Given
        val nonExistentId = "non-existent-record-id"

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/$nonExistentId",
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should update attendance record and return 200 OK`() {
        // Given - First create a record
        val createRequest = TrackAttendanceRequest(
            raiderId = 99002L,
            guildId = "update-record-test-guild",
            instance = "Original Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 5,
            totalRaids = 10
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/attendance/track",
            createEntity,
            TrackAttendanceResponse::class.java
        )

        val recordId = createResponse.body!!.recordId

        // When - Update the record
        val updateRequest = UpdateAttendanceRequest(
            instance = "Updated Instance",
            attendedRaids = 9,
            totalRaids = 10
        )

        val updateEntity = HttpEntity(updateRequest, headers)
        val response = restTemplate.exchange(
            "/api/v1/attendance/$recordId",
            org.springframework.http.HttpMethod.PUT,
            updateEntity,
            TrackAttendanceResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(recordId, response.body!!.recordId)
        assertEquals("Updated Instance", response.body!!.instance)
        assertEquals(9, response.body!!.attendedRaids)
        assertEquals(0.9, response.body!!.attendancePercentage, 0.01)
    }

    @Test
    fun `should return 404 Not Found when updating non-existent attendance record`() {
        // Given
        val nonExistentId = "non-existent-update-record-id"
        val updateRequest = UpdateAttendanceRequest(
            instance = "Updated Instance"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val updateEntity = HttpEntity(updateRequest, headers)

        // When
        val response = restTemplate.exchange(
            "/api/v1/attendance/$nonExistentId",
            org.springframework.http.HttpMethod.PUT,
            updateEntity,
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should delete attendance record and return 204 No Content`() {
        // Given - First create a record
        val createRequest = TrackAttendanceRequest(
            raiderId = 99003L,
            guildId = "delete-record-test-guild",
            instance = "Delete Test Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 6,
            totalRaids = 10
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/attendance/track",
            createEntity,
            TrackAttendanceResponse::class.java
        )

        val recordId = createResponse.body!!.recordId

        // When
        val response = restTemplate.exchange(
            "/api/v1/attendance/$recordId",
            org.springframework.http.HttpMethod.DELETE,
            null,
            Void::class.java
        )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // Verify the record is actually deleted
        val getResponse = restTemplate.getForEntity(
            "/api/v1/attendance/$recordId",
            String::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @Test
    fun `should return 404 Not Found when deleting non-existent attendance record`() {
        // Given
        val nonExistentId = "non-existent-delete-record-id"

        // When
        val response = restTemplate.exchange(
            "/api/v1/attendance/$nonExistentId",
            org.springframework.http.HttpMethod.DELETE,
            null,
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should get raider attendance history and return 200 OK`() {
        // Given - First create some records
        val raiderId = 99004L
        val guildId = "raider-history-test-guild"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 30)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // Create first record
        val request1 = TrackAttendanceRequest(
            raiderId = raiderId,
            guildId = guildId,
            instance = "Instance 1",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 7),
            attendedRaids = 8,
            totalRaids = 10
        )
        restTemplate.postForEntity(
            "/api/v1/attendance/track",
            HttpEntity(request1, headers),
            TrackAttendanceResponse::class.java
        )

        // Create second record
        val request2 = TrackAttendanceRequest(
            raiderId = raiderId,
            guildId = guildId,
            instance = "Instance 2",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 8),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 9,
            totalRaids = 10
        )
        restTemplate.postForEntity(
            "/api/v1/attendance/track",
            HttpEntity(request2, headers),
            TrackAttendanceResponse::class.java
        )

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/raider/$raiderId?guildId=$guildId&startDate=$startDate&endDate=$endDate",
            RaiderAttendanceHistoryResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(raiderId, response.body!!.raiderId)
        assertEquals(guildId, response.body!!.guildId)
        assertTrue(response.body!!.totalRecords >= 2)
        assertTrue(response.body!!.records.isNotEmpty())
    }

    @Test
    fun `should get guild attendance summary and return 200 OK`() {
        // Given - First create some records for different raiders
        val guildId = "guild-summary-test-guild"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 30)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // Create record for first raider
        val request1 = TrackAttendanceRequest(
            raiderId = 99005L,
            guildId = guildId,
            instance = "Summary Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 10,
            totalRaids = 10
        )
        restTemplate.postForEntity(
            "/api/v1/attendance/track",
            HttpEntity(request1, headers),
            TrackAttendanceResponse::class.java
        )

        // Create record for second raider
        val request2 = TrackAttendanceRequest(
            raiderId = 99006L,
            guildId = guildId,
            instance = "Summary Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 5,
            totalRaids = 10
        )
        restTemplate.postForEntity(
            "/api/v1/attendance/track",
            HttpEntity(request2, headers),
            TrackAttendanceResponse::class.java
        )

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/guild/$guildId/summary?startDate=$startDate&endDate=$endDate",
            GuildAttendanceSummaryResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(guildId, response.body!!.guildId)
        assertTrue(response.body!!.totalRecords >= 2)
        assertTrue(response.body!!.uniqueRaiders >= 2)
        assertTrue(response.body!!.overallAttendancePercentage >= 0.0)
        assertTrue(response.body!!.overallAttendancePercentage <= 1.0)
    }

    @Test
    fun `should return empty history for raider with no attendance records`() {
        // Given
        val raiderId = 99999L
        val guildId = "empty-history-guild"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/raider/$raiderId?guildId=$guildId&startDate=$startDate&endDate=$endDate",
            RaiderAttendanceHistoryResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(0, response.body!!.totalRecords)
        assertTrue(response.body!!.records.isEmpty())
    }

    @Test
    fun `should return empty summary for guild with no attendance records`() {
        // Given
        val guildId = "empty-summary-guild"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/attendance/guild/$guildId/summary?startDate=$startDate&endDate=$endDate",
            GuildAttendanceSummaryResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(0, response.body!!.totalRecords)
        assertEquals(0, response.body!!.uniqueRaiders)
        assertEquals(0.0, response.body!!.overallAttendancePercentage, 0.001)
    }
}
