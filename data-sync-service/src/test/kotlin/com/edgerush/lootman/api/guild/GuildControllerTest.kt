package com.edgerush.lootman.api.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.guild.CreateGuildCommand
import com.edgerush.lootman.application.guild.CreateGuildUseCase
import com.edgerush.lootman.application.guild.DeleteGuildCommand
import com.edgerush.lootman.application.guild.DeleteGuildUseCase
import com.edgerush.lootman.application.guild.GetGuildQuery
import com.edgerush.lootman.application.guild.GetGuildUseCase
import com.edgerush.lootman.application.guild.ListGuildsUseCase
import com.edgerush.lootman.application.guild.UpdateGuildCommand
import com.edgerush.lootman.application.guild.UpdateGuildUseCase
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Unit tests for GuildController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class GuildControllerTest : UnitTest() {
    private lateinit var createGuildUseCase: CreateGuildUseCase
    private lateinit var updateGuildUseCase: UpdateGuildUseCase
    private lateinit var deleteGuildUseCase: DeleteGuildUseCase
    private lateinit var getGuildUseCase: GetGuildUseCase
    private lateinit var listGuildsUseCase: ListGuildsUseCase
    private lateinit var controller: GuildController

    @BeforeEach
    fun setup() {
        createGuildUseCase = mockk()
        updateGuildUseCase = mockk()
        deleteGuildUseCase = mockk()
        getGuildUseCase = mockk()
        listGuildsUseCase = mockk()
        controller =
            GuildController(
                createGuildUseCase,
                updateGuildUseCase,
                deleteGuildUseCase,
                getGuildUseCase,
                listGuildsUseCase,
            )
    }

    @Nested
    inner class CreateGuildTests {
        @Test
        fun `should return CREATED status with guild response`() {
            // Given
            val request =
                CreateGuildRequest(
                    id = "test-guild",
                    name = "Test Guild",
                    description = "A test guild",
                    realm = "Area 52",
                    region = "US",
                )

            val guild = createGuild(id = GuildId("test-guild"))

            every { createGuildUseCase.execute(any()) } returns Result.success(guild)

            // When
            val response = controller.createGuild(request)

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.id shouldBe "test-guild"
            response.body?.name shouldBe "Test Guild"
            response.body?.region shouldBe "US"

            verify(exactly = 1) { createGuildUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request =
                CreateGuildRequest(
                    id = "my-guild",
                    name = "My Guild",
                    description = "Description",
                    realm = "Illidan",
                    region = "US",
                    syncEnabled = false,
                    benchmarkMode = "TOP_PERFORMER",
                )

            val commandSlot = slot<CreateGuildCommand>()
            val guild = createGuild(id = GuildId("my-guild"), name = "My Guild")

            every { createGuildUseCase.execute(capture(commandSlot)) } returns Result.success(guild)

            // When
            controller.createGuild(request)

            // Then
            commandSlot.captured.id shouldBe "my-guild"
            commandSlot.captured.name shouldBe "My Guild"
            commandSlot.captured.description shouldBe "Description"
            commandSlot.captured.realm shouldBe "Illidan"
            commandSlot.captured.region shouldBe "US"
            commandSlot.captured.syncEnabled shouldBe false
            commandSlot.captured.benchmarkMode shouldBe "TOP_PERFORMER"
        }

        @Test
        fun `should throw exception when use case fails`() {
            // Given
            val request =
                CreateGuildRequest(
                    id = "test-guild",
                    name = "",
                    region = "US",
                )

            every { createGuildUseCase.execute(any()) } returns
                Result.failure(
                    IllegalArgumentException("Guild name cannot be blank"),
                )

            // When/Then
            try {
                controller.createGuild(request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: IllegalArgumentException) {
                e.message shouldBe "Guild name cannot be blank"
            }
        }
    }

    @Nested
    inner class GetGuildTests {
        @Test
        fun `should return guild when found`() {
            // Given
            val guild = createGuild(id = GuildId("test-guild"))

            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // When
            val response = controller.getGuild("test-guild")

            // Then
            response.id shouldBe "test-guild"
            response.name shouldBe "Test Guild"
            response.isActive shouldBe true

            verify(exactly = 1) { getGuildUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<GetGuildQuery>()
            val guild = createGuild(id = GuildId("my-guild"))

            every { getGuildUseCase.execute(capture(querySlot)) } returns Result.success(guild)

            // When
            controller.getGuild("my-guild")

            // Then
            querySlot.captured.id shouldBe "my-guild"
        }

        @Test
        fun `should throw exception when guild not found`() {
            // Given
            every { getGuildUseCase.execute(any()) } returns
                Result.failure(
                    NoSuchElementException("Guild not found with id: unknown-guild"),
                )

            // When/Then
            try {
                controller.getGuild("unknown-guild")
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Guild not found with id: unknown-guild"
            }
        }
    }

    @Nested
    inner class UpdateGuildTests {
        @Test
        fun `should return updated guild`() {
            // Given
            val request =
                UpdateGuildRequest(
                    name = "Updated Guild",
                    description = "New description",
                )

            val updatedGuild =
                createGuild(
                    id = GuildId("test-guild"),
                    name = "Updated Guild",
                    description = "New description",
                )

            every { updateGuildUseCase.execute(any()) } returns Result.success(updatedGuild)

            // When
            val response = controller.updateGuild("test-guild", request)

            // Then
            response.id shouldBe "test-guild"
            response.name shouldBe "Updated Guild"
            response.description shouldBe "New description"

            verify(exactly = 1) { updateGuildUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request =
                UpdateGuildRequest(
                    name = "New Name",
                    syncEnabled = false,
                    benchmarkMode = "CUSTOM",
                    customBenchmarkRms = 0.95,
                    customBenchmarkIpi = 0.90,
                )

            val commandSlot = slot<UpdateGuildCommand>()
            val updatedGuild = createGuild(id = GuildId("test-guild"), name = "New Name")

            every { updateGuildUseCase.execute(capture(commandSlot)) } returns Result.success(updatedGuild)

            // When
            controller.updateGuild("test-guild", request)

            // Then
            commandSlot.captured.id shouldBe "test-guild"
            commandSlot.captured.name shouldBe "New Name"
            commandSlot.captured.syncEnabled shouldBe false
            commandSlot.captured.benchmarkMode shouldBe "CUSTOM"
            commandSlot.captured.customBenchmarkRms shouldBe 0.95
            commandSlot.captured.customBenchmarkIpi shouldBe 0.90
        }

        @Test
        fun `should throw exception when guild not found`() {
            // Given
            val request = UpdateGuildRequest(name = "New Name")

            every { updateGuildUseCase.execute(any()) } returns
                Result.failure(
                    NoSuchElementException("Guild not found with id: unknown-guild"),
                )

            // When/Then
            try {
                controller.updateGuild("unknown-guild", request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Guild not found with id: unknown-guild"
            }
        }
    }

    @Nested
    inner class DeleteGuildTests {
        @Test
        fun `should return NO_CONTENT on successful deletion`() {
            // Given
            every { deleteGuildUseCase.execute(any()) } returns Result.success(Unit)

            // When
            val response = controller.deleteGuild("test-guild")

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
            response.body shouldBe null

            verify(exactly = 1) { deleteGuildUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val commandSlot = slot<DeleteGuildCommand>()

            every { deleteGuildUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // When
            controller.deleteGuild("my-guild")

            // Then
            commandSlot.captured.id shouldBe "my-guild"
        }

        @Test
        fun `should throw exception when guild not found`() {
            // Given
            every { deleteGuildUseCase.execute(any()) } returns
                Result.failure(
                    NoSuchElementException("Guild not found with id: unknown-guild"),
                )

            // When/Then
            try {
                controller.deleteGuild("unknown-guild")
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Guild not found with id: unknown-guild"
            }
        }
    }

    @Nested
    inner class ListGuildsTests {
        @Test
        fun `should return list of all guilds`() {
            // Given
            val guilds =
                listOf(
                    createGuild(id = GuildId("guild-1"), name = "Guild One"),
                    createGuild(id = GuildId("guild-2"), name = "Guild Two"),
                    createGuild(id = GuildId("guild-3"), name = "Guild Three"),
                )

            every { listGuildsUseCase.execute() } returns Result.success(guilds)

            // When
            val response = controller.listGuilds()

            // Then
            response.count shouldBe 3
            response.guilds.size shouldBe 3
            response.guilds[0].name shouldBe "Guild One"
            response.guilds[1].name shouldBe "Guild Two"
            response.guilds[2].name shouldBe "Guild Three"

            verify(exactly = 1) { listGuildsUseCase.execute() }
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // Given
            every { listGuildsUseCase.execute() } returns Result.success(emptyList())

            // When
            val response = controller.listGuilds()

            // Then
            response.count shouldBe 0
            response.guilds shouldBe emptyList()
        }

        @Test
        fun `should return list of active guilds only`() {
            // Given
            val activeGuilds =
                listOf(
                    createGuild(id = GuildId("active-1"), name = "Active Guild 1"),
                    createGuild(id = GuildId("active-2"), name = "Active Guild 2"),
                )

            every { listGuildsUseCase.executeActiveOnly() } returns Result.success(activeGuilds)

            // When
            val response = controller.listActiveGuilds()

            // Then
            response.count shouldBe 2
            response.guilds.size shouldBe 2

            verify(exactly = 1) { listGuildsUseCase.executeActiveOnly() }
        }

        @Test
        fun `should throw exception when listGuilds use case fails`() {
            // Given
            every { listGuildsUseCase.execute() } returns
                Result.failure(
                    RuntimeException("Database connection failed"),
                )

            // When/Then
            try {
                controller.listGuilds()
                throw AssertionError("Expected exception was not thrown")
            } catch (e: RuntimeException) {
                e.message shouldBe "Database connection failed"
            }
        }

        @Test
        fun `should throw exception when listActiveGuilds use case fails`() {
            // Given
            every { listGuildsUseCase.executeActiveOnly() } returns
                Result.failure(
                    RuntimeException("Database query failed"),
                )

            // When/Then
            try {
                controller.listActiveGuilds()
                throw AssertionError("Expected exception was not thrown")
            } catch (e: RuntimeException) {
                e.message shouldBe "Database query failed"
            }
        }
    }

    @Nested
    inner class GuildResponseMappingTests {
        @Test
        fun `should correctly map all guild fields to response`() {
            // Given
            val createdAt = Instant.parse("2024-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2024-06-01T00:00:00Z")
            val guild =
                Guild(
                    id = GuildId("full-guild"),
                    name = "Full Guild",
                    description = "Complete guild configuration",
                    realm = "Area 52",
                    region = Region.US,
                    settings =
                        GuildSettings(
                            syncEnabled = true,
                            syncCronExpression = "0 0 5 * * *",
                            syncRunOnStartup = true,
                            timezone = "America/New_York",
                            benchmarkMode = BenchmarkMode.CUSTOM,
                            customBenchmarkRms = 0.95,
                            customBenchmarkIpi = 0.90,
                        ),
                    syncStatus = SyncStatus.SUCCESS,
                    isActive = true,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // When
            val response = controller.getGuild("full-guild")

            // Then
            response.id shouldBe "full-guild"
            response.name shouldBe "Full Guild"
            response.description shouldBe "Complete guild configuration"
            response.realm shouldBe "Area 52"
            response.region shouldBe "US"
            response.syncEnabled shouldBe true
            response.syncCronExpression shouldBe "0 0 5 * * *"
            response.timezone shouldBe "America/New_York"
            response.benchmarkMode shouldBe "CUSTOM"
            response.customBenchmarkRms shouldBe 0.95
            response.customBenchmarkIpi shouldBe 0.90
            response.syncStatus shouldBe "SUCCESS"
            response.isActive shouldBe true
            response.canSync shouldBe true
        }

        @Test
        fun `should return canSync false when guild is inactive`() {
            // Given
            val guild =
                createGuild(
                    id = GuildId("inactive-guild"),
                    isActive = false,
                )

            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // When
            val response = controller.getGuild("inactive-guild")

            // Then
            response.isActive shouldBe false
            response.canSync shouldBe false
        }

        @Test
        fun `should return canSync false when sync is disabled`() {
            // Given
            val guild =
                createGuild(
                    id = GuildId("sync-disabled"),
                    settings = GuildSettings(syncEnabled = false),
                )

            every { getGuildUseCase.execute(any()) } returns Result.success(guild)

            // When
            val response = controller.getGuild("sync-disabled")

            // Then
            response.syncEnabled shouldBe false
            response.canSync shouldBe false
        }
    }

    private fun createGuild(
        id: GuildId = GuildId("test-guild"),
        name: String = "Test Guild",
        description: String? = "Test guild description",
        realm: String? = "Area 52",
        region: Region = Region.US,
        settings: GuildSettings = GuildSettings.default(),
        syncStatus: SyncStatus = SyncStatus.NEVER_RUN,
        isActive: Boolean = true,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Guild =
        Guild(
            id = id,
            name = name,
            description = description,
            realm = realm,
            region = region,
            settings = settings,
            syncStatus = syncStatus,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
