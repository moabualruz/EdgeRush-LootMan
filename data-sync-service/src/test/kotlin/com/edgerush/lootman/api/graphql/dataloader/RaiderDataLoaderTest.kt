package com.edgerush.lootman.api.graphql.dataloader

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for RaiderDataLoader.
 *
 * Tests batch loading of raiders to prevent N+1 query issues.
 */
class RaiderDataLoaderTest : UnitTest() {

    @MockK
    private lateinit var raiderRepository: RaiderRepository

    private lateinit var dataLoader: RaiderBatchLoader

    @BeforeEach
    fun setup() {
        dataLoader = RaiderBatchLoader(raiderRepository)
    }

    @Nested
    inner class BatchLoad {

        @Test
        fun `should batch load multiple raiders in single query`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(1L), RaiderId(2L), RaiderId(3L))
            val raiders = listOf(
                createTestRaider(id = 1L, name = "Raider1"),
                createTestRaider(id = 2L, name = "Raider2"),
                createTestRaider(id = 3L, name = "Raider3"),
            )
            every { raiderRepository.findByIds(raiderIds) } returns raiders

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 3
            result[0]?.characterName shouldBe "Raider1"
            result[1]?.characterName shouldBe "Raider2"
            result[2]?.characterName shouldBe "Raider3"
            verify(exactly = 1) { raiderRepository.findByIds(raiderIds) }
        }

        @Test
        fun `should return null for missing raiders`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(1L), RaiderId(2L), RaiderId(3L))
            val raiders = listOf(
                createTestRaider(id = 1L, name = "Raider1"),
                // Raider 2 is missing
                createTestRaider(id = 3L, name = "Raider3"),
            )
            every { raiderRepository.findByIds(raiderIds) } returns raiders

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 3
            result[0]?.characterName shouldBe "Raider1"
            result[1] shouldBe null  // Missing raider
            result[2]?.characterName shouldBe "Raider3"
        }

        @Test
        fun `should handle empty id list`() = runBlocking {
            // Arrange
            val raiderIds = emptyList<RaiderId>()
            every { raiderRepository.findByIds(raiderIds) } returns emptyList()

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should preserve order of requested ids`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(3L), RaiderId(1L), RaiderId(2L))
            val raiders = listOf(
                createTestRaider(id = 1L, name = "Raider1"),
                createTestRaider(id = 2L, name = "Raider2"),
                createTestRaider(id = 3L, name = "Raider3"),
            )
            every { raiderRepository.findByIds(raiderIds) } returns raiders

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 3
            result[0]?.id?.value shouldBe 3L  // First requested
            result[1]?.id?.value shouldBe 1L  // Second requested
            result[2]?.id?.value shouldBe 2L  // Third requested
        }
    }

    // Helper function
    private fun createTestRaider(
        id: Long = 1L,
        guildId: String = "test-guild",
        name: String = "TestRaider",
    ): Raider = Raider(
        id = RaiderId(id),
        guildId = GuildId(guildId),
        characterName = name,
        realm = "TestRealm",
        characterClass = CharacterClass.WARRIOR,
        role = Role.DPS,
        rank = "Raider",
        status = RaiderStatus.ACTIVE,
        joinDate = LocalDateTime.now(),
        wowauditId = id,
    )
}
