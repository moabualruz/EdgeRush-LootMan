package com.edgerush.lootman.infrastructure.guest

import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.GuestEntitySpringRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Unit tests for JdbcGuestRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcGuestRepositoryTest : UnitTest() {
    private lateinit var springRepository: GuestEntitySpringRepository
    private lateinit var repository: JdbcGuestRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcGuestRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return guest when found`() {
            // Given
            val guestId = 1L
            val entity = createGuestEntity(guestId = guestId)
            every { springRepository.findByGuestId(guestId) } returns entity

            // When
            val result = repository.findById(guestId)

            // Then
            result shouldNotBe null
            result?.guestId shouldBe guestId
            verify { springRepository.findByGuestId(guestId) }
        }

        @Test
        fun `should return null when guest not found`() {
            // Given
            val guestId = 999L
            every { springRepository.findByGuestId(guestId) } returns null

            // When
            val result = repository.findById(guestId)

            // Then
            result shouldBe null
            verify { springRepository.findByGuestId(guestId) }
        }

        @Test
        fun `should map all entity fields correctly`() {
            // Given
            val guestId = 1L
            val entity = createGuestEntity(
                guestId = guestId,
                name = "TestGuest",
                realm = "Illidan",
                clazz = "Mage",
                role = "DPS",
                blizzardId = 12345L,
                trackingSince = now,
                syncedAt = now,
            )
            every { springRepository.findByGuestId(guestId) } returns entity

            // When
            val result = repository.findById(guestId)

            // Then
            result shouldNotBe null
            result?.guestId shouldBe guestId
            result?.name shouldBe "TestGuest"
            result?.realm shouldBe "Illidan"
            result?.clazz shouldBe "Mage"
            result?.role shouldBe "DPS"
            result?.blizzardId shouldBe 12345L
            result?.trackingSince shouldBe now
            result?.syncedAt shouldBe now
            verify { springRepository.findByGuestId(guestId) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val guestId = 1L
            val entity = createGuestEntity(
                guestId = guestId,
                realm = null,
                clazz = null,
                role = null,
                blizzardId = null,
                trackingSince = null,
            )
            every { springRepository.findByGuestId(guestId) } returns entity

            // When
            val result = repository.findById(guestId)

            // Then
            result shouldNotBe null
            result?.realm shouldBe null
            result?.clazz shouldBe null
            result?.role shouldBe null
            result?.blizzardId shouldBe null
            result?.trackingSince shouldBe null
            verify { springRepository.findByGuestId(guestId) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated guests`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createGuestEntity(1L),
                createGuestEntity(2L),
            )
            val page = PageImpl(entities)

            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
            verify { springRepository.findAll(any<Pageable>()) }
        }

        @Test
        fun `should return empty list when no guests`() {
            // Given
            val page = PageImpl(emptyList<GuestEntity>())
            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findAll(any<Pageable>()) }
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            // Given
            every { springRepository.count() } returns 42L

            // When
            val result = repository.count()

            // Then
            result shouldBe 42L
            verify { springRepository.count() }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when guest exists`() {
            // Given
            val guestId = 1L
            every { springRepository.existsByGuestId(guestId) } returns true

            // When
            val result = repository.existsById(guestId)

            // Then
            result shouldBe true
            verify { springRepository.existsByGuestId(guestId) }
        }

        @Test
        fun `should return false when guest does not exist`() {
            // Given
            val guestId = 999L
            every { springRepository.existsByGuestId(guestId) } returns false

            // When
            val result = repository.existsById(guestId)

            // Then
            result shouldBe false
            verify { springRepository.existsByGuestId(guestId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save entity and return saved result`() {
            // Given
            val entity = createGuestEntity(guestId = 1L)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should handle null tracking since`() {
            // Given
            val entity = createGuestEntity(trackingSince = null)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result.trackingSince shouldBe null
            verify { springRepository.save(entity) }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete guest by id`() {
            // Given
            val guestId = 1L

            // When
            repository.delete(guestId)

            // Then
            verify { springRepository.deleteByGuestId(guestId) }
        }
    }

    // Helper methods

    private fun createGuestEntity(
        guestId: Long = 1L,
        name: String = "TestGuest",
        realm: String? = "Illidan",
        clazz: String? = "Mage",
        role: String? = "DPS",
        blizzardId: Long? = 12345L,
        trackingSince: OffsetDateTime? = now,
        syncedAt: OffsetDateTime = now,
    ): GuestEntity =
        GuestEntity(
            guestId = guestId,
            name = name,
            realm = realm,
            clazz = clazz,
            role = role,
            blizzardId = blizzardId,
            trackingSince = trackingSince,
            syncedAt = syncedAt,
        )
}
