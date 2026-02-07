package com.edgerush.lootman.application.raider

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.datasync.test.fixtures.RaiderFixtures
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for Raider use cases.
 *
 * Tests use case business logic by mocking the repository layer.
 */
class RaiderUseCasesTest : UnitTest() {
    private lateinit var raiderRepository: RaiderRepository

    @BeforeEach
    fun setup() {
        raiderRepository = mockk()
    }

    @Nested
    inner class CreateRaiderUseCaseTests {
        private lateinit var useCase: CreateRaiderUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = CreateRaiderUseCase(raiderRepository)
        }

        @Test
        fun `should create raider with valid data`() {
            // Given
            val command =
                CreateRaiderCommand(
                    id = 1L,
                    characterId = 1001L,
                    guildId = "test-guild",
                    characterName = "TestChar",
                    realm = "Area52",
                    characterClass = "WARRIOR",
                    role = "DPS",
                    rank = "Raider",
                    status = "ACTIVE",
                )

            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val raider = result.getOrThrow()
            raider.id.value shouldBe 1L
            raider.guildId.value shouldBe "test-guild"
            raider.characterName shouldBe "TestChar"
            raider.realm shouldBe "Area52"
            raider.characterClass shouldBe CharacterClass.WARRIOR
            raider.role shouldBe Role.DPS
            raider.rank shouldBe "Raider"
            raider.status shouldBe RaiderStatus.ACTIVE

            verify(exactly = 1) { raiderRepository.save(any()) }
        }

        @Test
        fun `should fail with blank character name`() {
            // Given
            val command =
                CreateRaiderCommand(
                    id = 1L,
                    characterId = 1002L,
                    guildId = "test-guild",
                    characterName = "",
                    realm = "Area52",
                    characterClass = "WARRIOR",
                    role = "DPS",
                )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
            result.exceptionOrNull()?.message shouldBe "Character name cannot be blank"

            verify(exactly = 0) { raiderRepository.save(any()) }
        }

        @Test
        fun `should fail with invalid character class`() {
            // Given
            val command =
                CreateRaiderCommand(
                    id = 1L,
                    characterId = 1003L,
                    guildId = "test-guild",
                    characterName = "TestChar",
                    realm = "Area52",
                    characterClass = "INVALID_CLASS",
                    role = "DPS",
                )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        @Test
        fun `should create raider with optional fields`() {
            // Given
            val joinDate = LocalDateTime.of(2024, 1, 1, 0, 0)
            val command =
                CreateRaiderCommand(
                    id = 2L,
                    characterId = 1004L,
                    guildId = "test-guild",
                    characterName = "AltChar",
                    realm = "Illidan",
                    characterClass = "MAGE",
                    role = "DPS",
                    rank = null,
                    status = "TRIAL",
                    joinDate = joinDate,
                    wowauditId = 12345L,
                )

            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val raider = result.getOrThrow()
            raider.rank shouldBe null
            raider.status shouldBe RaiderStatus.TRIAL
            raider.joinDate shouldBe joinDate
            raider.wowauditId shouldBe 12345L
        }
    }

    @Nested
    inner class UpdateRaiderUseCaseTests {
        private lateinit var useCase: UpdateRaiderUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = UpdateRaiderUseCase(raiderRepository)
        }

        @Test
        fun `should update existing raider`() {
            // Given
            val existingRaider = createRaider(id = RaiderId(1L), characterName = "OldName")
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    characterName = "NewName",
                    status = "BENCHED",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedRaider = result.getOrThrow()
            updatedRaider.characterName shouldBe "NewName"
            updatedRaider.status shouldBe RaiderStatus.BENCHED
            // Unchanged fields should remain
            updatedRaider.realm shouldBe existingRaider.realm
            updatedRaider.characterClass shouldBe existingRaider.characterClass

            verify(exactly = 1) { raiderRepository.save(any()) }
        }

        @Test
        fun `should fail when raider not found`() {
            // Given
            val command =
                UpdateRaiderCommand(
                    id = 999L,
                    characterName = "NewName",
                )

            every { raiderRepository.findById(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Raider not found with id: 999"

            verify(exactly = 0) { raiderRepository.save(any()) }
        }

        @Test
        fun `should update only specified fields`() {
            // Given
            val existingRaider =
                createRaider(
                    id = RaiderId(1L),
                    characterName = "TestChar",
                    role = Role.DPS,
                    rank = "Raider",
                )
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    role = "TANK",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedRaider = result.getOrThrow()
            updatedRaider.role shouldBe Role.TANK
            updatedRaider.characterName shouldBe "TestChar" // Unchanged
            updatedRaider.rank shouldBe "Raider" // Unchanged
        }

        @Test
        fun `should update character class`() {
            // Given
            val existingRaider =
                createRaider(
                    id = RaiderId(1L),
                    characterClass = CharacterClass.WARRIOR,
                )
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    characterClass = "PALADIN",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedRaider = result.getOrThrow()
            updatedRaider.characterClass shouldBe CharacterClass.PALADIN
        }

        @Test
        fun `should update realm`() {
            // Given
            val existingRaider =
                createRaider(
                    id = RaiderId(1L),
                    realm = "OldRealm",
                )
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    realm = "NewRealm",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedRaider = result.getOrThrow()
            updatedRaider.realm shouldBe "NewRealm"
        }

        @Test
        fun `should update all fields at once`() {
            // Given
            val existingRaider =
                createRaider(
                    id = RaiderId(1L),
                    characterName = "OldName",
                    realm = "OldRealm",
                    characterClass = CharacterClass.WARRIOR,
                    role = Role.DPS,
                    rank = "Member",
                    status = RaiderStatus.ACTIVE,
                )
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    characterName = "NewName",
                    realm = "NewRealm",
                    characterClass = "MAGE",
                    role = "HEALER",
                    rank = "Officer",
                    status = "INACTIVE",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            val savedRaiderSlot = slot<Raider>()
            every { raiderRepository.save(capture(savedRaiderSlot)) } answers { savedRaiderSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val updatedRaider = result.getOrThrow()
            updatedRaider.characterName shouldBe "NewName"
            updatedRaider.realm shouldBe "NewRealm"
            updatedRaider.characterClass shouldBe CharacterClass.MAGE
            updatedRaider.role shouldBe Role.HEALER
            updatedRaider.rank shouldBe "Officer"
            updatedRaider.status shouldBe RaiderStatus.INACTIVE
        }

        @Test
        fun `should fail with invalid role`() {
            // Given
            val existingRaider = createRaider(id = RaiderId(1L))
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    role = "INVALID_ROLE",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        @Test
        fun `should fail with invalid status`() {
            // Given
            val existingRaider = createRaider(id = RaiderId(1L))
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    status = "INVALID_STATUS",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        @Test
        fun `should fail with invalid character class`() {
            // Given
            val existingRaider = createRaider(id = RaiderId(1L))
            val command =
                UpdateRaiderCommand(
                    id = 1L,
                    characterClass = "INVALID_CLASS",
                )

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }
    }

    @Nested
    inner class DeleteRaiderUseCaseTests {
        private lateinit var useCase: DeleteRaiderUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = DeleteRaiderUseCase(raiderRepository)
        }

        @Test
        fun `should delete existing raider`() {
            // Given
            val existingRaider = createRaider(id = RaiderId(1L))
            val command = DeleteRaiderCommand(id = 1L)

            every { raiderRepository.findById(RaiderId(1L)) } returns existingRaider
            every { raiderRepository.delete(RaiderId(1L)) } returns Unit

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true

            verify(exactly = 1) { raiderRepository.delete(RaiderId(1L)) }
        }

        @Test
        fun `should fail when raider not found`() {
            // Given
            val command = DeleteRaiderCommand(id = 999L)

            every { raiderRepository.findById(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Raider not found with id: 999"

            verify(exactly = 0) { raiderRepository.delete(any()) }
        }
    }

    @Nested
    inner class GetRaiderUseCaseTests {
        private lateinit var useCase: GetRaiderUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = GetRaiderUseCase(raiderRepository)
        }

        @Test
        fun `should return raider when found`() {
            // Given
            val raider = createRaider(id = RaiderId(1L), characterName = "TestChar")
            val query = GetRaiderQuery(id = 1L)

            every { raiderRepository.findById(RaiderId(1L)) } returns raider

            // When
            val result = useCase.execute(query)

            // Then
            result.isSuccess shouldBe true
            val foundRaider = result.getOrThrow()
            foundRaider.id.value shouldBe 1L
            foundRaider.characterName shouldBe "TestChar"
        }

        @Test
        fun `should fail when raider not found`() {
            // Given
            val query = GetRaiderQuery(id = 999L)

            every { raiderRepository.findById(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Raider not found with id: 999"
        }
    }

    @Nested
    inner class ListRaidersUseCaseTests {
        private lateinit var useCase: ListRaidersUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = ListRaidersUseCase(raiderRepository)
        }

        @Test
        fun `should return all raiders for guild`() {
            // Given
            val raiders =
                listOf(
                    createRaider(id = RaiderId(1L), characterName = "Raider1"),
                    createRaider(id = RaiderId(2L), characterName = "Raider2"),
                    createRaider(id = RaiderId(3L), characterName = "Raider3"),
                )
            val query = ListRaidersByGuildQuery(guildId = "test-guild")

            every { raiderRepository.findByGuildId(GuildId("test-guild")) } returns raiders

            // When
            val result = useCase.executeByGuild(query)

            // Then
            result.isSuccess shouldBe true
            val raiderList = result.getOrThrow()
            raiderList.size shouldBe 3
            raiderList[0].characterName shouldBe "Raider1"
            raiderList[1].characterName shouldBe "Raider2"
            raiderList[2].characterName shouldBe "Raider3"
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // Given
            val query = ListRaidersByGuildQuery(guildId = "empty-guild")

            every { raiderRepository.findByGuildId(GuildId("empty-guild")) } returns emptyList()

            // When
            val result = useCase.executeByGuild(query)

            // Then
            result.isSuccess shouldBe true
            result.getOrThrow().size shouldBe 0
        }

        @Test
        fun `should return paginated raiders for guild`() {
            // Given
            val raiders =
                listOf(
                    createRaider(id = RaiderId(3L), characterName = "Raider3"),
                    createRaider(id = RaiderId(4L), characterName = "Raider4"),
                )
            val query =
                ListRaidersByGuildPaginatedQuery(
                    guildId = "test-guild",
                    offset = 2L,
                    limit = 2,
                )

            every { raiderRepository.findByGuildId(GuildId("test-guild"), 2L, 2) } returns raiders
            every { raiderRepository.countByGuildId(GuildId("test-guild")) } returns 10L

            // When
            val result = useCase.executeByGuildPaginated(query)

            // Then
            result.isSuccess shouldBe true
            val paginatedResult = result.getOrThrow()
            paginatedResult.raiders.size shouldBe 2
            paginatedResult.totalCount shouldBe 10L
            paginatedResult.raiders[0].characterName shouldBe "Raider3"
            paginatedResult.raiders[1].characterName shouldBe "Raider4"
        }

        @Test
        fun `should return empty paginated list when offset exceeds total`() {
            // Given
            val query =
                ListRaidersByGuildPaginatedQuery(
                    guildId = "test-guild",
                    offset = 100L,
                    limit = 10,
                )

            every { raiderRepository.findByGuildId(GuildId("test-guild"), 100L, 10) } returns emptyList()
            every { raiderRepository.countByGuildId(GuildId("test-guild")) } returns 5L

            // When
            val result = useCase.executeByGuildPaginated(query)

            // Then
            result.isSuccess shouldBe true
            val paginatedResult = result.getOrThrow()
            paginatedResult.raiders.size shouldBe 0
            paginatedResult.totalCount shouldBe 5L
        }

        @Test
        fun `should return first page of paginated raiders`() {
            // Given
            val raiders =
                listOf(
                    createRaider(id = RaiderId(1L), characterName = "Raider1"),
                    createRaider(id = RaiderId(2L), characterName = "Raider2"),
                )
            val query =
                ListRaidersByGuildPaginatedQuery(
                    guildId = "test-guild",
                    offset = 0L,
                    limit = 2,
                )

            every { raiderRepository.findByGuildId(GuildId("test-guild"), 0L, 2) } returns raiders
            every { raiderRepository.countByGuildId(GuildId("test-guild")) } returns 10L

            // When
            val result = useCase.executeByGuildPaginated(query)

            // Then
            result.isSuccess shouldBe true
            val paginatedResult = result.getOrThrow()
            paginatedResult.raiders.size shouldBe 2
            paginatedResult.totalCount shouldBe 10L
        }

        @Test
        fun `should handle paginated query for guild with no raiders`() {
            // Given
            val query =
                ListRaidersByGuildPaginatedQuery(
                    guildId = "empty-guild",
                    offset = 0L,
                    limit = 10,
                )

            every { raiderRepository.findByGuildId(GuildId("empty-guild"), 0L, 10) } returns emptyList()
            every { raiderRepository.countByGuildId(GuildId("empty-guild")) } returns 0L

            // When
            val result = useCase.executeByGuildPaginated(query)

            // Then
            result.isSuccess shouldBe true
            val paginatedResult = result.getOrThrow()
            paginatedResult.raiders.size shouldBe 0
            paginatedResult.totalCount shouldBe 0L
        }
    }

    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        characterName: String = "TestChar",
        realm: String = "Area52",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.now(),
        wowauditId: Long? = null,
    ): Raider =
        RaiderFixtures.createRaider(
            id = id,
            guildId = guildId,
            name = characterName,
            realm = realm,
            characterClass = characterClass,
            role = role,
            rank = rank,
            status = status,
            joinDate = joinDate,
            wowauditId = wowauditId,
        )
}
