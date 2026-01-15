package com.edgerush.lootman.api.graphql.query

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.guild.GetGuildQuery
import com.edgerush.lootman.application.guild.GetGuildUseCase
import com.edgerush.lootman.application.guild.ListGuildsUseCase
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.shared.GuildId
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
import java.time.Instant

/**
 * Unit tests for GuildQueryResolver.
 *
 * Tests the GraphQL query resolver for guild operations following TDD principles.
 */
class GuildQueryResolverTest : UnitTest() {
    @MockK
    private lateinit var getGuildUseCase: GetGuildUseCase

    @MockK
    private lateinit var listGuildsUseCase: ListGuildsUseCase

    @InjectMockKs
    private lateinit var resolver: GuildQueryResolver

    @Nested
    inner class GuildByIdQuery {
        @Test
        fun `should return guild when found by id`() {
            // Arrange
            val guild = createTestGuild(id = "guild-123")
            val querySlot = slot<GetGuildQuery>()
            every { getGuildUseCase.execute(capture(querySlot)) } returns Result.success(guild)

            // Act
            val result = resolver.guild(id = "guild-123")

            // Assert
            result.shouldNotBeNull()
            result.id shouldBe "guild-123"
            result.name shouldBe "Test Guild"
            result.region shouldBe Region.US
            querySlot.captured.id shouldBe "guild-123"
        }

        @Test
        fun `should return null when guild not found`() {
            // Arrange
            every { getGuildUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Guild not found"))

            // Act
            val result = resolver.guild(id = "non-existent")

            // Assert
            result.shouldBeNull()
        }

        @Test
        fun `should propagate exception for non-NotFound errors`() {
            // Arrange
            every { getGuildUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.guild(id = "guild-123")
                }
            exception.message shouldBe "Database error"
        }
    }

    @Nested
    inner class GuildsListQuery {
        @Test
        fun `should return all guilds`() {
            // Arrange
            val guilds =
                listOf(
                    createTestGuild(id = "guild-1", name = "Guild One"),
                    createTestGuild(id = "guild-2", name = "Guild Two"),
                    createTestGuild(id = "guild-3", name = "Guild Three"),
                )
            every { listGuildsUseCase.execute() } returns Result.success(guilds)

            // Act
            val result = resolver.guilds()

            // Assert
            result shouldHaveSize 3
            result[0].name shouldBe "Guild One"
            result[1].name shouldBe "Guild Two"
            result[2].name shouldBe "Guild Three"
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // Arrange
            every { listGuildsUseCase.execute() } returns Result.success(emptyList())

            // Act
            val result = resolver.guilds()

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { listGuildsUseCase.execute() } returns
                Result.failure(RuntimeException("Database connection failed"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.guilds()
                }
            exception.message shouldBe "Database connection failed"
        }
    }

    @Nested
    inner class GuildTypeConversion {
        @Test
        fun `should correctly convert all guild fields`() {
            // Arrange
            val guild =
                createTestGuild(
                    id = "guild-42",
                    name = "Elite Raiders",
                    region = Region.EU,
                    realm = "Silvermoon",
                    isActive = true,
                )
            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // Act
            val result = resolver.guild(id = "guild-42")

            // Assert
            result.shouldNotBeNull()
            result.id shouldBe "guild-42"
            result.name shouldBe "Elite Raiders"
            result.realm shouldBe "Silvermoon"
            result.region shouldBe Region.EU
            result.isActive shouldBe true
            result.canSync shouldBe true
        }

        @Test
        fun `should handle inactive guild sync capability`() {
            // Arrange
            val guild = createTestGuild(isActive = false)
            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // Act
            val result = resolver.guild(id = "1")

            // Assert
            result.shouldNotBeNull()
            result.canSync shouldBe false
        }

        @Test
        fun `should include settings in guild type`() {
            // Arrange
            val guild =
                createTestGuild(
                    syncEnabled = true,
                    benchmarkMode = BenchmarkMode.TOP_PERFORMER,
                )
            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // Act
            val result = resolver.guild(id = "1")

            // Assert
            result.shouldNotBeNull()
            result.settings.shouldNotBeNull()
            result.settings.syncEnabled shouldBe true
            result.settings.benchmarkMode shouldBe BenchmarkMode.TOP_PERFORMER
        }
    }

    // Helper function to create test guilds
    private fun createTestGuild(
        id: String = "test-guild",
        name: String = "Test Guild",
        realm: String? = "TestRealm",
        region: Region = Region.US,
        isActive: Boolean = true,
        syncEnabled: Boolean = true,
        benchmarkMode: BenchmarkMode = BenchmarkMode.THEORETICAL,
    ): Guild =
        Guild(
            id = GuildId(id),
            name = name,
            description = "A test guild",
            realm = realm,
            region = region,
            settings =
                GuildSettings(
                    syncEnabled = syncEnabled,
                    benchmarkMode = benchmarkMode,
                ),
            syncStatus = SyncStatus.SUCCESS,
            isActive = isActive,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
