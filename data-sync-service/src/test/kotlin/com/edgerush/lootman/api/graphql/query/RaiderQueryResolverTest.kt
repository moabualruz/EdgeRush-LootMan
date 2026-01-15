package com.edgerush.lootman.api.graphql.query

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.raider.GetRaiderQuery
import com.edgerush.lootman.application.raider.GetRaiderUseCase
import com.edgerush.lootman.application.raider.ListRaidersByGuildQuery
import com.edgerush.lootman.application.raider.ListRaidersUseCase
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for RaiderQueryResolver.
 *
 * Tests the GraphQL query resolver for raider operations following TDD principles.
 * Each test verifies a single behavior using the Arrange-Act-Assert pattern.
 */
class RaiderQueryResolverTest : UnitTest() {
    @MockK
    private lateinit var getRaiderUseCase: GetRaiderUseCase

    @MockK
    private lateinit var listRaidersUseCase: ListRaidersUseCase

    @InjectMockKs
    private lateinit var resolver: RaiderQueryResolver

    @Nested
    inner class RaiderByIdQuery {
        @Test
        fun `should return raider when found by id`() {
            // Arrange
            val raider = createTestRaider(id = 123L)
            val querySlot = slot<GetRaiderQuery>()
            every { getRaiderUseCase.execute(capture(querySlot)) } returns Result.success(raider)

            // Act
            val result = resolver.raider(id = "123")

            // Assert
            result.shouldNotBeNull()
            result.id shouldBe "123"
            result.characterName shouldBe "TestRaider"
            result.characterClass shouldBe CharacterClass.WARRIOR
            querySlot.captured.id shouldBe 123L
        }

        @Test
        fun `should return null when raider not found`() {
            // Arrange
            every { getRaiderUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Raider not found"))

            // Act
            val result = resolver.raider(id = "999")

            // Assert
            result.shouldBeNull()
        }

        @Test
        fun `should propagate exception for non-NotFound errors`() {
            // Arrange
            every { getRaiderUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.raider(id = "123")
                }
            exception.message shouldBe "Database error"
        }
    }

    @Nested
    inner class RaidersByGuildQuery {
        @Test
        fun `should return list of raiders for guild`() {
            // Arrange
            val raiders =
                listOf(
                    createTestRaider(id = 1L, name = "Raider1"),
                    createTestRaider(id = 2L, name = "Raider2"),
                    createTestRaider(id = 3L, name = "Raider3"),
                )
            val querySlot = slot<ListRaidersByGuildQuery>()
            every { listRaidersUseCase.executeByGuild(capture(querySlot)) } returns Result.success(raiders)

            // Act
            val result = resolver.raiders(guildId = "test-guild-123")

            // Assert
            result shouldHaveSize 3
            result[0].characterName shouldBe "Raider1"
            result[1].characterName shouldBe "Raider2"
            result[2].characterName shouldBe "Raider3"
            querySlot.captured.guildId shouldBe "test-guild-123"
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // Arrange
            every { listRaidersUseCase.executeByGuild(any()) } returns Result.success(emptyList())

            // Act
            val result = resolver.raiders(guildId = "empty-guild")

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { listRaidersUseCase.executeByGuild(any()) } returns
                Result.failure(RuntimeException("Database connection failed"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.raiders(guildId = "test-guild")
                }
            exception.message shouldBe "Database connection failed"
        }
    }

    @Nested
    inner class RaiderTypeConversion {
        @Test
        fun `should correctly convert all raider fields`() {
            // Arrange
            val raider =
                createTestRaider(
                    id = 42L,
                    name = "Arthas",
                    realm = "Frostmourne",
                    characterClass = CharacterClass.DEATH_KNIGHT,
                    role = Role.TANK,
                    status = RaiderStatus.ACTIVE,
                )
            every { getRaiderUseCase.execute(any()) } returns Result.success(raider)

            // Act
            val result = resolver.raider(id = "42")

            // Assert
            result.shouldNotBeNull()
            result.id shouldBe "42"
            result.characterName shouldBe "Arthas"
            result.realm shouldBe "Frostmourne"
            result.characterClass shouldBe CharacterClass.DEATH_KNIGHT
            result.role shouldBe Role.TANK
            result.status shouldBe RaiderStatus.ACTIVE
            result.fullName shouldBe "Arthas-Frostmourne"
            result.isEligibleForLoot shouldBe true
        }

        @Test
        fun `should handle inactive raider eligibility`() {
            // Arrange
            val raider = createTestRaider(status = RaiderStatus.INACTIVE)
            every { getRaiderUseCase.execute(any()) } returns Result.success(raider)

            // Act
            val result = resolver.raider(id = "1")

            // Assert
            result.shouldNotBeNull()
            result.isEligibleForLoot shouldBe false
        }
    }

    // Helper function to create test raiders
    private fun createTestRaider(
        id: Long = 1L,
        guildId: String = "test-guild",
        name: String = "TestRaider",
        realm: String = "TestRealm",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        status: RaiderStatus = RaiderStatus.ACTIVE,
    ): Raider =
        Raider(
            id = RaiderId(id),
            guildId = GuildId(guildId),
            characterName = name,
            realm = realm,
            characterClass = characterClass,
            role = role,
            rank = "Raider",
            status = status,
            joinDate = LocalDateTime.now(),
            wowauditId = id,
        )
}
