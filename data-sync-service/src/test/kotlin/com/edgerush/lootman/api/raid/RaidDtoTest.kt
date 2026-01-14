package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Unit tests for Raid DTOs.
 */
class RaidDtoTest : UnitTest() {

    @Nested
    inner class CreateRaidRequestTests {

        @Test
        fun `should create request with all fields`() {
            // Given/When
            val request = CreateRaidRequest(
                date = LocalDate.of(2024, 3, 15),
                startTime = LocalTime.of(20, 0),
                endTime = LocalTime.of(23, 30),
                instance = "Nerub-ar Palace",
                difficulty = "Mythic",
                optional = false,
                status = "SCHEDULED",
                totalSize = 20,
                notes = "Progression night",
                teamId = 1L,
                seasonId = 5L,
            )

            // Then
            request.date shouldBe LocalDate.of(2024, 3, 15)
            request.instance shouldBe "Nerub-ar Palace"
            request.difficulty shouldBe "Mythic"
            request.optional shouldBe false
            request.status shouldBe "SCHEDULED"
            request.totalSize shouldBe 20
            request.notes shouldBe "Progression night"
            request.teamId shouldBe 1L
        }

        @Test
        fun `should have default values`() {
            // Given/When
            val request = CreateRaidRequest()

            // Then
            request.date shouldBe null
            request.optional shouldBe false
            request.status shouldBe "SCHEDULED"
        }
    }

    @Nested
    inner class UpdateRaidRequestTests {

        @Test
        fun `should create request with partial fields`() {
            // Given/When
            val request = UpdateRaidRequest(
                status = "COMPLETED",
                presentSize = 19,
            )

            // Then
            request.status shouldBe "COMPLETED"
            request.presentSize shouldBe 19
            request.date shouldBe null
            request.instance shouldBe null
        }

        @Test
        fun `should allow all null fields`() {
            // Given/When
            val request = UpdateRaidRequest()

            // Then
            request.date shouldBe null
            request.startTime shouldBe null
            request.endTime shouldBe null
            request.instance shouldBe null
            request.difficulty shouldBe null
            request.optional shouldBe null
            request.status shouldBe null
            request.presentSize shouldBe null
            request.totalSize shouldBe null
            request.notes shouldBe null
        }
    }

    @Nested
    inner class RaidResponseTests {

        @Test
        fun `should convert entity to response`() {
            // Given
            val syncedAt = OffsetDateTime.now()
            val createdAt = OffsetDateTime.now().minusDays(7)
            val updatedAt = OffsetDateTime.now().minusHours(1)

            val entity = RaidEntity(
                raidId = 123L,
                date = LocalDate.of(2024, 3, 15),
                startTime = LocalTime.of(20, 0),
                endTime = LocalTime.of(23, 30),
                instance = "Nerub-ar Palace",
                difficulty = "Mythic",
                optional = false,
                status = "SCHEDULED",
                presentSize = 19,
                totalSize = 20,
                notes = "Progression night",
                selectionsImage = "http://example.com/image.png",
                teamId = 1L,
                seasonId = 5L,
                periodId = 10L,
                createdAt = createdAt,
                updatedAt = updatedAt,
                syncedAt = syncedAt,
            )

            // When
            val response = RaidResponse.from(entity)

            // Then
            response.raidId shouldBe 123L
            response.date shouldBe LocalDate.of(2024, 3, 15)
            response.startTime shouldBe LocalTime.of(20, 0)
            response.endTime shouldBe LocalTime.of(23, 30)
            response.instance shouldBe "Nerub-ar Palace"
            response.difficulty shouldBe "Mythic"
            response.optional shouldBe false
            response.status shouldBe "SCHEDULED"
            response.presentSize shouldBe 19
            response.totalSize shouldBe 20
            response.notes shouldBe "Progression night"
            response.selectionsImage shouldBe "http://example.com/image.png"
            response.teamId shouldBe 1L
            response.seasonId shouldBe 5L
            response.periodId shouldBe 10L
            response.createdAt shouldBe createdAt
            response.updatedAt shouldBe updatedAt
            response.syncedAt shouldBe syncedAt
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val entity = RaidEntity(
                raidId = 1L,
                date = null,
                startTime = null,
                endTime = null,
                instance = null,
                difficulty = null,
                optional = null,
                status = null,
                presentSize = null,
                totalSize = null,
                notes = null,
                selectionsImage = null,
                teamId = null,
                seasonId = null,
                periodId = null,
                createdAt = null,
                updatedAt = null,
                syncedAt = OffsetDateTime.now(),
            )

            // When
            val response = RaidResponse.from(entity)

            // Then
            response.raidId shouldBe 1L
            response.date shouldBe null
            response.startTime shouldBe null
            response.endTime shouldBe null
            response.instance shouldBe null
            response.difficulty shouldBe null
            response.optional shouldBe null
            response.status shouldBe null
            response.presentSize shouldBe null
            response.totalSize shouldBe null
            response.notes shouldBe null
            response.selectionsImage shouldBe null
            response.teamId shouldBe null
            response.seasonId shouldBe null
            response.periodId shouldBe null
            response.createdAt shouldBe null
            response.updatedAt shouldBe null
        }
    }
}
