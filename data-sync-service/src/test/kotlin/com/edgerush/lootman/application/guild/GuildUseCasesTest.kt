package com.edgerush.lootman.application.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.guild.repository.GuildRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for Guild use cases.
 *
 * Tests use case business logic by mocking the repository layer.
 */
class GuildUseCasesTest : UnitTest() {

    private lateinit var guildRepository: GuildRepository

    @BeforeEach
    fun setup() {
        guildRepository = mockk()
    }

    @Nested
    inner class CreateGuildUseCaseTests {
        private lateinit var useCase: CreateGuildUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = CreateGuildUseCase(guildRepository)
        }

        @Test
        fun `should create guild with valid data`() {
            // Given
            val command = CreateGuildCommand(
                id = "test-guild",
                name = "Test Guild",
                description = "A test guild",
                realm = "Area 52",
                region = "US",
                syncEnabled = true,
                syncCronExpression = "0 0 4 * * *",
                timezone = "UTC",
                benchmarkMode = "THEORETICAL"
            )

            every { guildRepository.existsById(any()) } returns false
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val guild = result.getOrThrow()
            guild.id.value shouldBe "test-guild"
            guild.name shouldBe "Test Guild"
            guild.description shouldBe "A test guild"
            guild.realm shouldBe "Area 52"
            guild.region shouldBe Region.US
            guild.settings.syncEnabled shouldBe true
            guild.settings.benchmarkMode shouldBe BenchmarkMode.THEORETICAL
            guild.syncStatus shouldBe SyncStatus.NEVER_RUN
            guild.isActive shouldBe true

            verify(exactly = 1) { guildRepository.existsById(GuildId("test-guild")) }
            verify(exactly = 1) { guildRepository.save(any()) }
        }

        @Test
        fun `should fail when guild already exists`() {
            // Given
            val command = CreateGuildCommand(
                id = "existing-guild",
                name = "Existing Guild",
                description = null,
                realm = null,
                region = "US",
                syncEnabled = true,
                syncCronExpression = "0 0 4 * * *",
                timezone = "UTC",
                benchmarkMode = "THEORETICAL"
            )

            every { guildRepository.existsById(GuildId("existing-guild")) } returns true

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Guild already exists with id: existing-guild"

            verify(exactly = 0) { guildRepository.save(any()) }
        }

        @Test
        fun `should fail with invalid region`() {
            // Given
            val command = CreateGuildCommand(
                id = "test-guild",
                name = "Test Guild",
                description = null,
                realm = null,
                region = "INVALID",
                syncEnabled = true,
                syncCronExpression = "0 0 4 * * *",
                timezone = "UTC",
                benchmarkMode = "THEORETICAL"
            )

            every { guildRepository.existsById(any()) } returns false

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Invalid region: INVALID"
        }

        @Test
        fun `should fail with invalid benchmark mode`() {
            // Given
            val command = CreateGuildCommand(
                id = "test-guild",
                name = "Test Guild",
                description = null,
                realm = null,
                region = "US",
                syncEnabled = true,
                syncCronExpression = "0 0 4 * * *",
                timezone = "UTC",
                benchmarkMode = "INVALID_MODE"
            )

            every { guildRepository.existsById(any()) } returns false

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Invalid benchmark mode: INVALID_MODE"
        }
    }

    @Nested
    inner class UpdateGuildUseCaseTests {
        private lateinit var useCase: UpdateGuildUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = UpdateGuildUseCase(guildRepository)
        }

        @Test
        fun `should update existing guild`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"), name = "Old Name")
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = "New Name",
                description = "New description",
                realm = null,
                region = null,
                syncEnabled = false,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.name shouldBe "New Name"
            updatedGuild.description shouldBe "New description"
            updatedGuild.settings.syncEnabled shouldBe false
            updatedGuild.updatedAt shouldNotBe existingGuild.updatedAt

            verify(exactly = 1) { guildRepository.save(any()) }
        }

        @Test
        fun `should fail when guild not found`() {
            // Given
            val command = UpdateGuildCommand(
                id = "non-existent",
                name = "New Name",
                description = null,
                realm = null,
                region = null,
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("non-existent")) } returns null

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Guild not found with id: non-existent"

            verify(exactly = 0) { guildRepository.save(any()) }
        }

        @Test
        fun `should update custom benchmark values`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"))
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = null,
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = "CUSTOM",
                customBenchmarkRms = 0.95,
                customBenchmarkIpi = 0.90,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.settings.benchmarkMode shouldBe BenchmarkMode.CUSTOM
            updatedGuild.settings.customBenchmarkRms shouldBe 0.95
            updatedGuild.settings.customBenchmarkIpi shouldBe 0.90
        }

        @Test
        fun `should fail with invalid region on update`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"))
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = "INVALID_REGION",
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Invalid region: INVALID_REGION"

            verify(exactly = 0) { guildRepository.save(any()) }
        }

        @Test
        fun `should fail with invalid benchmark mode on update`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"))
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = null,
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = "INVALID_MODE",
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Invalid benchmark mode: INVALID_MODE"

            verify(exactly = 0) { guildRepository.save(any()) }
        }

        @Test
        fun `should update region`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"), region = Region.US)
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = "EU",
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.region shouldBe Region.EU
        }

        @Test
        fun `should update realm`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"), realm = "Old Realm")
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = "New Realm",
                region = null,
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.realm shouldBe "New Realm"
        }

        @Test
        fun `should update isActive flag`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"), isActive = true)
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = null,
                syncEnabled = null,
                syncCronExpression = null,
                timezone = null,
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = false
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.isActive shouldBe false
        }

        @Test
        fun `should update sync cron expression and timezone`() {
            // Given
            val existingGuild = createGuild(id = GuildId("test-guild"))
            val command = UpdateGuildCommand(
                id = "test-guild",
                name = null,
                description = null,
                realm = null,
                region = null,
                syncEnabled = null,
                syncCronExpression = "0 0 6 * * *",
                timezone = "America/New_York",
                benchmarkMode = null,
                customBenchmarkRms = null,
                customBenchmarkIpi = null,
                isActive = null
            )

            every { guildRepository.findById(GuildId("test-guild")) } returns existingGuild
            val savedGuildSlot = slot<Guild>()
            every { guildRepository.save(capture(savedGuildSlot)) } answers { savedGuildSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedGuild = result.getOrThrow()
            updatedGuild.settings.syncCronExpression shouldBe "0 0 6 * * *"
            updatedGuild.settings.timezone shouldBe "America/New_York"
        }
    }

    @Nested
    inner class DeleteGuildUseCaseTests {
        private lateinit var useCase: DeleteGuildUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = DeleteGuildUseCase(guildRepository)
        }

        @Test
        fun `should delete existing guild`() {
            // Given
            val command = DeleteGuildCommand(id = "test-guild")

            every { guildRepository.deleteById(GuildId("test-guild")) } returns true

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true

            verify(exactly = 1) { guildRepository.deleteById(GuildId("test-guild")) }
        }

        @Test
        fun `should fail when guild not found`() {
            // Given
            val command = DeleteGuildCommand(id = "non-existent")

            every { guildRepository.deleteById(GuildId("non-existent")) } returns false

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Guild not found with id: non-existent"
        }
    }

    @Nested
    inner class GetGuildUseCaseTests {
        private lateinit var useCase: GetGuildUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = GetGuildUseCase(guildRepository)
        }

        @Test
        fun `should return guild when found`() {
            // Given
            val guild = createGuild(id = GuildId("test-guild"), name = "Test Guild")
            val query = GetGuildQuery(id = "test-guild")

            every { guildRepository.findById(GuildId("test-guild")) } returns guild

            // When
            val result = useCase.execute(query)

            // Then
            result.isSuccess shouldBe true
            val foundGuild = result.getOrThrow()
            foundGuild.id.value shouldBe "test-guild"
            foundGuild.name shouldBe "Test Guild"
        }

        @Test
        fun `should fail when guild not found`() {
            // Given
            val query = GetGuildQuery(id = "non-existent")

            every { guildRepository.findById(GuildId("non-existent")) } returns null

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Guild not found with id: non-existent"
        }
    }

    @Nested
    inner class ListGuildsUseCaseTests {
        private lateinit var useCase: ListGuildsUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = ListGuildsUseCase(guildRepository)
        }

        @Test
        fun `should return all guilds`() {
            // Given
            val guilds = listOf(
                createGuild(id = GuildId("guild-1"), name = "Guild One"),
                createGuild(id = GuildId("guild-2"), name = "Guild Two"),
                createGuild(id = GuildId("guild-3"), name = "Guild Three", isActive = false)
            )

            every { guildRepository.findAll() } returns guilds

            // When
            val result = useCase.execute()

            // Then
            result.isSuccess shouldBe true
            val guildList = result.getOrThrow()
            guildList.size shouldBe 3
            guildList[0].name shouldBe "Guild One"
            guildList[1].name shouldBe "Guild Two"
            guildList[2].name shouldBe "Guild Three"
        }

        @Test
        fun `should return only active guilds`() {
            // Given
            val activeGuilds = listOf(
                createGuild(id = GuildId("active-1"), name = "Active Guild 1"),
                createGuild(id = GuildId("active-2"), name = "Active Guild 2")
            )

            every { guildRepository.findAllActive() } returns activeGuilds

            // When
            val result = useCase.executeActiveOnly()

            // Then
            result.isSuccess shouldBe true
            val guildList = result.getOrThrow()
            guildList.size shouldBe 2
            guildList.all { it.isActive } shouldBe true
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // Given
            every { guildRepository.findAll() } returns emptyList()

            // When
            val result = useCase.execute()

            // Then
            result.isSuccess shouldBe true
            result.getOrThrow().size shouldBe 0
        }

        @Test
        fun `should return empty list when no active guilds exist`() {
            // Given
            every { guildRepository.findAllActive() } returns emptyList()

            // When
            val result = useCase.executeActiveOnly()

            // Then
            result.isSuccess shouldBe true
            result.getOrThrow().size shouldBe 0
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
        updatedAt: Instant = Instant.now()
    ): Guild = Guild(
        id = id,
        name = name,
        description = description,
        realm = realm,
        region = region,
        settings = settings,
        syncStatus = syncStatus,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
