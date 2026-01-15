package com.edgerush.lootman.api.character

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

/**
 * Unit tests for CharacterHistoryController.
 */
class CharacterHistoryControllerTest : UnitTest() {
    private lateinit var characterHistoryService: CharacterHistoryCrudService
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var controller: CharacterHistoryController

    @BeforeEach
    fun setup() {
        characterHistoryService = mockk()
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        controller = CharacterHistoryController(characterHistoryService, paginationProperties)
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paged response with default pagination`() {
            // Given
            val expectedResponse =
                PagedResponse(
                    content = listOf(createCharacterHistoryResponse(id = 1L)),
                    page = 0,
                    size = 20,
                    totalElements = 1,
                )
            every { characterHistoryService.findAll(any()) } returns expectedResponse

            // When
            val result = controller.findAll(page = 0, size = null)

            // Then
            result shouldBe expectedResponse
            verify {
                characterHistoryService.findAll(match { it.page == 0 && it.size == 20 })
            }
        }

        @Test
        fun `should cap page size at max`() {
            // Given
            val slot = slot<PageRequest>()
            val expectedResponse =
                PagedResponse(
                    content = emptyList<CharacterHistoryResponse>(),
                    page = 0,
                    size = 100,
                    totalElements = 0,
                )
            every { characterHistoryService.findAll(capture(slot)) } returns expectedResponse

            // When
            controller.findAll(page = 0, size = 500)

            // Then
            slot.captured.size shouldBe 100
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return character history when found`() {
            // Given
            val expected = createCharacterHistoryResponse(id = 123L, characterName = "Testchar")
            every { characterHistoryService.findById(123L) } returns expected

            // When
            val result = controller.findById(123L)

            // Then
            result.id shouldBe 123L
            result.characterName shouldBe "Testchar"
            verify(exactly = 1) { characterHistoryService.findById(123L) }
        }

        @Test
        fun `should propagate exception when not found`() {
            // Given
            every { characterHistoryService.findById(999L) } throws NoSuchElementException("Character history not found with id: 999")

            // When/Then
            try {
                controller.findById(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Character history not found with id: 999"
            }
        }
    }

    @Nested
    inner class CreateTests {
        @Test
        fun `should return created character history with 201 status`() {
            // Given
            val request =
                CreateCharacterHistoryRequest(
                    characterId = 100L,
                    characterName = "Testchar",
                    characterRealm = "Silvermoon",
                    characterRegion = "EU",
                    teamId = 1L,
                    seasonId = 1L,
                    periodId = 1L,
                    historyJson = """{"level":80}""",
                    bestGearJson = """{"ilvl":619}""",
                )

            val created =
                createCharacterHistoryResponse(
                    id = 1L,
                    characterId = 100L,
                    characterName = "Testchar",
                )
            every { characterHistoryService.create(request) } returns created

            // When
            val result = controller.create(request)

            // Then
            result.statusCode shouldBe HttpStatus.CREATED
            result.body?.id shouldBe 1L
            result.body?.characterName shouldBe "Testchar"
            verify(exactly = 1) { characterHistoryService.create(request) }
        }
    }

    @Nested
    inner class UpdateTests {
        @Test
        fun `should return updated character history`() {
            // Given
            val request =
                UpdateCharacterHistoryRequest(
                    historyJson = """{"level":80,"updated":true}""",
                    bestGearJson = """{"ilvl":625}""",
                )

            val updated =
                createCharacterHistoryResponse(
                    id = 1L,
                    historyJson = """{"level":80,"updated":true}""",
                    bestGearJson = """{"ilvl":625}""",
                )
            every { characterHistoryService.update(1L, request) } returns updated

            // When
            val result = controller.update(1L, request)

            // Then
            result.id shouldBe 1L
            result.historyJson shouldBe """{"level":80,"updated":true}"""
            verify(exactly = 1) { characterHistoryService.update(1L, request) }
        }

        @Test
        fun `should propagate exception when character history not found`() {
            // Given
            val request = UpdateCharacterHistoryRequest(historyJson = """{}""")

            every { characterHistoryService.update(999L, request) } throws NoSuchElementException("Character history not found with id: 999")

            // When/Then
            try {
                controller.update(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Character history not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should return 204 No Content on success`() {
            // Given
            every { characterHistoryService.delete(1L) } returns Unit

            // When
            val result = controller.delete(1L)

            // Then
            result.statusCode shouldBe HttpStatus.NO_CONTENT
            result.body shouldBe null
            verify(exactly = 1) { characterHistoryService.delete(1L) }
        }

        @Test
        fun `should propagate exception when character history not found`() {
            // Given
            every { characterHistoryService.delete(999L) } throws NoSuchElementException("Character history not found with id: 999")

            // When/Then
            try {
                controller.delete(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Character history not found with id: 999"
            }
        }
    }

    @Nested
    inner class ExistsTests {
        @Test
        fun `should return exists true when character history exists`() {
            // Given
            every { characterHistoryService.existsById(1L) } returns true

            // When
            val result = controller.exists(1L)

            // Then
            result.exists shouldBe true
            verify(exactly = 1) { characterHistoryService.existsById(1L) }
        }

        @Test
        fun `should return exists false when character history does not exist`() {
            // Given
            every { characterHistoryService.existsById(999L) } returns false

            // When
            val result = controller.exists(999L)

            // Then
            result.exists shouldBe false
        }
    }

    @Nested
    inner class FindByCharacterIdTests {
        @Test
        fun `should return character history for a character`() {
            // Given
            val characterId = 100L
            val histories =
                listOf(
                    createCharacterHistoryResponse(id = 1L, characterId = characterId),
                    createCharacterHistoryResponse(id = 2L, characterId = characterId),
                )
            val expectedResponse =
                PagedResponse(
                    content = histories,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { characterHistoryService.findByCharacterId(characterId, any()) } returns expectedResponse

            // When
            val result = controller.findByCharacterId(characterId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.characterId == characterId } shouldBe true
        }
    }

    @Nested
    inner class FindByTeamIdTests {
        @Test
        fun `should return character history for a team`() {
            // Given
            val teamId = 1L
            val histories =
                listOf(
                    createCharacterHistoryResponse(id = 1L, teamId = teamId),
                    createCharacterHistoryResponse(id = 2L, teamId = teamId),
                )
            val expectedResponse =
                PagedResponse(
                    content = histories,
                    page = 0,
                    size = 20,
                    totalElements = 2,
                )
            every { characterHistoryService.findByTeamId(teamId, any()) } returns expectedResponse

            // When
            val result = controller.findByTeamId(teamId, page = 0, size = null)

            // Then
            result.totalElements shouldBe 2
            result.content.all { it.teamId == teamId } shouldBe true
        }
    }

    @Nested
    inner class CountByCharacterIdTests {
        @Test
        fun `should return count for character`() {
            // Given
            val characterId = 100L
            every { characterHistoryService.countByCharacterId(characterId) } returns 5L

            // When
            val result = controller.countByCharacterId(characterId)

            // Then
            result.count shouldBe 5L
            verify(exactly = 1) { characterHistoryService.countByCharacterId(characterId) }
        }
    }

    private fun createCharacterHistoryResponse(
        id: Long = 1L,
        characterId: Long = 100L,
        characterName: String = "Testchar",
        characterRealm: String? = "Silvermoon",
        characterRegion: String? = "EU",
        teamId: Long? = 1L,
        seasonId: Long? = 1L,
        periodId: Long? = 1L,
        historyJson: String = """{"level":80}""",
        bestGearJson: String? = """{"ilvl":619}""",
        syncedAt: OffsetDateTime = OffsetDateTime.now(),
    ): CharacterHistoryResponse =
        CharacterHistoryResponse(
            id = id,
            characterId = characterId,
            characterName = characterName,
            characterRealm = characterRealm,
            characterRegion = characterRegion,
            teamId = teamId,
            seasonId = seasonId,
            periodId = periodId,
            historyJson = historyJson,
            bestGearJson = bestGearJson,
            syncedAt = syncedAt,
        )
}
