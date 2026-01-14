package com.edgerush.lootman.api.auth

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.infrastructure.auth.InMemoryUserCharacterMappingRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for UserCharacterMappingService.
 */
class UserCharacterMappingServiceTest : UnitTest() {

    private lateinit var repository: InMemoryUserCharacterMappingRepository
    private lateinit var service: UserCharacterMappingService

    private val userId = UserId(1L)
    private val otherUserId = UserId(2L)

    @BeforeEach
    fun setup() {
        repository = InMemoryUserCharacterMappingRepository()
        service = UserCharacterMappingService(repository)
    }

    @Nested
    inner class LinkCharacter {

        @Test
        fun `should link character to user`() {
            // Given
            val request = LinkCharacterRequest(raiderId = 100L)

            // When
            val result = service.linkCharacter(userId, request)

            // Then
            result.userId shouldBe userId.value
            result.raiderId shouldBe 100L
        }

        @Test
        fun `should make first character primary automatically`() {
            // Given
            val request = LinkCharacterRequest(raiderId = 100L, isPrimary = false)

            // When
            val result = service.linkCharacter(userId, request)

            // Then
            result.isPrimary shouldBe true
        }

        @Test
        fun `should respect explicit primary flag`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))

            // When
            val result = service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L, isPrimary = true))

            // Then
            result.isPrimary shouldBe true

            // And the old primary should be cleared
            val characters = service.getCharactersForUser(userId)
            characters.find { it.raiderId == 100L }?.isPrimary shouldBe false
        }

        @Test
        fun `should throw when character already linked`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))

            // When & Then
            shouldThrow<CharacterAlreadyLinkedException> {
                service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))
            }
        }
    }

    @Nested
    inner class UnlinkCharacter {

        @Test
        fun `should unlink character`() {
            // Given
            val linked = service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))

            // When
            service.unlinkCharacter(userId, linked.id)

            // Then
            service.getCharactersForUser(userId) shouldHaveSize 0
        }

        @Test
        fun `should promote next character when primary is unlinked`() {
            // Given
            val primary = service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L))

            // When
            service.unlinkCharacter(userId, primary.id)

            // Then
            val remaining = service.getCharactersForUser(userId)
            remaining shouldHaveSize 1
            remaining.first().isPrimary shouldBe true
        }

        @Test
        fun `should throw when mapping not found`() {
            // When & Then
            shouldThrow<CharacterMappingNotFoundException> {
                service.unlinkCharacter(userId, 999L)
            }
        }

        @Test
        fun `should throw when trying to unlink another users character`() {
            // Given
            val linked = service.linkCharacter(otherUserId, LinkCharacterRequest(raiderId = 100L))

            // When & Then
            shouldThrow<CharacterMappingNotFoundException> {
                service.unlinkCharacter(userId, linked.id)
            }
        }
    }

    @Nested
    inner class SetPrimaryCharacter {

        @Test
        fun `should set character as primary`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))
            val second = service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L))

            // When
            val result = service.setPrimaryCharacter(userId, second.id)

            // Then
            result.isPrimary shouldBe true

            // And the old primary should be cleared
            val characters = service.getCharactersForUser(userId)
            characters.find { it.raiderId == 100L }?.isPrimary shouldBe false
        }

        @Test
        fun `should throw when mapping not found`() {
            // When & Then
            shouldThrow<CharacterMappingNotFoundException> {
                service.setPrimaryCharacter(userId, 999L)
            }
        }
    }

    @Nested
    inner class GetCharacters {

        @Test
        fun `should return all characters for user`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L))
            service.linkCharacter(otherUserId, LinkCharacterRequest(raiderId = 300L))

            // When
            val result = service.getCharactersForUser(userId)

            // Then
            result shouldHaveSize 2
        }

        @Test
        fun `should return primary character first`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L)) // primary
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L))

            // When
            val result = service.getCharactersForUser(userId)

            // Then
            result.first().isPrimary shouldBe true
        }
    }

    @Nested
    inner class GetPrimaryCharacter {

        @Test
        fun `should return primary character`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))

            // When
            val result = service.getPrimaryCharacterForUser(userId)

            // Then
            result.shouldNotBeNull()
            result.isPrimary shouldBe true
        }

        @Test
        fun `should return null when no characters linked`() {
            // When
            val result = service.getPrimaryCharacterForUser(userId)

            // Then
            result.shouldBeNull()
        }
    }

    @Nested
    inner class GetCharacterCount {

        @Test
        fun `should return correct count`() {
            // Given
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 100L))
            service.linkCharacter(userId, LinkCharacterRequest(raiderId = 200L))

            // When
            val result = service.getCharacterCount(userId)

            // Then
            result.count shouldBe 2
        }

        @Test
        fun `should return zero when no characters linked`() {
            // When
            val result = service.getCharacterCount(userId)

            // Then
            result.count shouldBe 0
        }
    }
}
