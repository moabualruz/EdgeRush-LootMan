package com.edgerush.lootman.api.raider

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.raider.CreateRaiderCommand
import com.edgerush.lootman.application.raider.CreateRaiderUseCase
import com.edgerush.lootman.application.raider.DeleteRaiderCommand
import com.edgerush.lootman.application.raider.DeleteRaiderUseCase
import com.edgerush.lootman.application.raider.GetRaiderQuery
import com.edgerush.lootman.application.raider.GetRaiderUseCase
import com.edgerush.lootman.application.raider.ListRaidersByGuildQuery
import com.edgerush.lootman.application.raider.ListRaidersUseCase
import com.edgerush.lootman.application.raider.UpdateRaiderCommand
import com.edgerush.lootman.application.raider.UpdateRaiderUseCase
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

/**
 * Unit tests for RaiderController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class RaiderControllerTest : UnitTest() {
    private lateinit var createRaiderUseCase: CreateRaiderUseCase
    private lateinit var updateRaiderUseCase: UpdateRaiderUseCase
    private lateinit var deleteRaiderUseCase: DeleteRaiderUseCase
    private lateinit var getRaiderUseCase: GetRaiderUseCase
    private lateinit var listRaidersUseCase: ListRaidersUseCase
    private lateinit var controller: RaiderController

    @BeforeEach
    fun setup() {
        createRaiderUseCase = mockk()
        updateRaiderUseCase = mockk()
        deleteRaiderUseCase = mockk()
        getRaiderUseCase = mockk()
        listRaidersUseCase = mockk()
        controller = RaiderController(
            createRaiderUseCase,
            updateRaiderUseCase,
            deleteRaiderUseCase,
            getRaiderUseCase,
            listRaidersUseCase
        )
    }

    @Nested
    inner class CreateRaiderTests {
        @Test
        fun `should return CREATED status with raider response`() {
            // Given
            val request = CreateRaiderRequest(
                id = 1L,
                guildId = "test-guild",
                characterName = "Testchar",
                realm = "TestRealm",
                characterClass = "WARRIOR",
                role = "DPS",
                rank = "Raider",
                status = "ACTIVE"
            )

            val raider = createRaider(id = RaiderId(1L))

            every { createRaiderUseCase.execute(any()) } returns Result.success(raider)

            // When
            val response = controller.createRaider(request)

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.id shouldBe 1L
            response.body?.characterName shouldBe "Testchar"
            response.body?.realm shouldBe "TestRealm"
            response.body?.characterClass shouldBe "WARRIOR"
            response.body?.role shouldBe "DPS"

            verify(exactly = 1) { createRaiderUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request = CreateRaiderRequest(
                id = 42L,
                guildId = "my-guild",
                characterName = "MyChar",
                realm = "MyRealm",
                characterClass = "MAGE",
                role = "DPS",
                rank = "Officer",
                status = "ACTIVE"
            )

            val commandSlot = slot<CreateRaiderCommand>()
            val raider = createRaider(
                id = RaiderId(42L),
                characterName = "MyChar",
                realm = "MyRealm",
                characterClass = CharacterClass.MAGE
            )

            every { createRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(raider)

            // When
            controller.createRaider(request)

            // Then
            commandSlot.captured.id shouldBe 42L
            commandSlot.captured.guildId shouldBe "my-guild"
            commandSlot.captured.characterName shouldBe "MyChar"
            commandSlot.captured.realm shouldBe "MyRealm"
            commandSlot.captured.characterClass shouldBe "MAGE"
            commandSlot.captured.role shouldBe "DPS"
            commandSlot.captured.rank shouldBe "Officer"
        }

        @Test
        fun `should throw exception when use case fails`() {
            // Given
            val request = CreateRaiderRequest(
                id = 1L,
                guildId = "test-guild",
                characterName = "",
                realm = "TestRealm",
                characterClass = "WARRIOR",
                role = "DPS"
            )

            every { createRaiderUseCase.execute(any()) } returns Result.failure(
                IllegalArgumentException("Character name cannot be blank")
            )

            // When/Then
            try {
                controller.createRaider(request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: IllegalArgumentException) {
                e.message shouldBe "Character name cannot be blank"
            }
        }
    }

    @Nested
    inner class GetRaiderTests {
        @Test
        fun `should return raider when found`() {
            // Given
            val raider = createRaider(id = RaiderId(123L))

            every { getRaiderUseCase.execute(any()) } returns Result.success(raider)

            // When
            val response = controller.getRaider(123L)

            // Then
            response.id shouldBe 123L
            response.characterName shouldBe "Testchar"
            response.fullName shouldBe "Testchar-TestRealm"
            response.isEligibleForLoot shouldBe true

            verify(exactly = 1) { getRaiderUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<GetRaiderQuery>()
            val raider = createRaider(id = RaiderId(456L))

            every { getRaiderUseCase.execute(capture(querySlot)) } returns Result.success(raider)

            // When
            controller.getRaider(456L)

            // Then
            querySlot.captured.id shouldBe 456L
        }

        @Test
        fun `should throw exception when raider not found`() {
            // Given
            every { getRaiderUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Raider not found with id: 999")
            )

            // When/Then
            try {
                controller.getRaider(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raider not found with id: 999"
            }
        }
    }

    @Nested
    inner class UpdateRaiderTests {
        @Test
        fun `should return updated raider`() {
            // Given
            val request = UpdateRaiderRequest(
                status = "BENCHED",
                rank = "Officer"
            )

            val updatedRaider = createRaider(
                id = RaiderId(1L),
                status = RaiderStatus.BENCHED,
                rank = "Officer"
            )

            every { updateRaiderUseCase.execute(any()) } returns Result.success(updatedRaider)

            // When
            val response = controller.updateRaider(1L, request)

            // Then
            response.id shouldBe 1L
            response.status shouldBe "BENCHED"
            response.rank shouldBe "Officer"

            verify(exactly = 1) { updateRaiderUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val request = UpdateRaiderRequest(
                characterName = "NewName",
                realm = "NewRealm",
                status = "INACTIVE"
            )

            val commandSlot = slot<UpdateRaiderCommand>()
            val updatedRaider = createRaider(
                id = RaiderId(42L),
                characterName = "NewName",
                realm = "NewRealm",
                status = RaiderStatus.INACTIVE
            )

            every { updateRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(updatedRaider)

            // When
            controller.updateRaider(42L, request)

            // Then
            commandSlot.captured.id shouldBe 42L
            commandSlot.captured.characterName shouldBe "NewName"
            commandSlot.captured.realm shouldBe "NewRealm"
            commandSlot.captured.status shouldBe "INACTIVE"
        }

        @Test
        fun `should throw exception when raider not found`() {
            // Given
            val request = UpdateRaiderRequest(status = "BENCHED")

            every { updateRaiderUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Raider not found with id: 999")
            )

            // When/Then
            try {
                controller.updateRaider(999L, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raider not found with id: 999"
            }
        }
    }

    @Nested
    inner class DeleteRaiderTests {
        @Test
        fun `should return NO_CONTENT on successful deletion`() {
            // Given
            every { deleteRaiderUseCase.execute(any()) } returns Result.success(Unit)

            // When
            val response = controller.deleteRaider(1L)

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
            response.body shouldBe null

            verify(exactly = 1) { deleteRaiderUseCase.execute(any()) }
        }

        @Test
        fun `should pass correct command to use case`() {
            // Given
            val commandSlot = slot<DeleteRaiderCommand>()

            every { deleteRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // When
            controller.deleteRaider(42L)

            // Then
            commandSlot.captured.id shouldBe 42L
        }

        @Test
        fun `should throw exception when raider not found`() {
            // Given
            every { deleteRaiderUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Raider not found with id: 999")
            )

            // When/Then
            try {
                controller.deleteRaider(999L)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Raider not found with id: 999"
            }
        }
    }

    @Nested
    inner class ListRaidersByGuildTests {
        @Test
        fun `should return list of raiders for guild`() {
            // Given
            val guildId = "test-guild"
            val raiders = listOf(
                createRaider(id = RaiderId(1L), characterName = "Raider1"),
                createRaider(id = RaiderId(2L), characterName = "Raider2"),
                createRaider(id = RaiderId(3L), characterName = "Raider3")
            )

            every { listRaidersUseCase.executeByGuild(any()) } returns Result.success(raiders)

            // When
            val response = controller.getRaidersByGuild(guildId)

            // Then
            response.count shouldBe 3
            response.raiders.size shouldBe 3
            response.raiders[0].characterName shouldBe "Raider1"
            response.raiders[1].characterName shouldBe "Raider2"
            response.raiders[2].characterName shouldBe "Raider3"

            verify(exactly = 1) { listRaidersUseCase.executeByGuild(any()) }
        }

        @Test
        fun `should pass correct query to use case`() {
            // Given
            val querySlot = slot<ListRaidersByGuildQuery>()

            every { listRaidersUseCase.executeByGuild(capture(querySlot)) } returns Result.success(emptyList())

            // When
            controller.getRaidersByGuild("my-guild")

            // Then
            querySlot.captured.guildId shouldBe "my-guild"
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // Given
            every { listRaidersUseCase.executeByGuild(any()) } returns Result.success(emptyList())

            // When
            val response = controller.getRaidersByGuild("empty-guild")

            // Then
            response.count shouldBe 0
            response.raiders shouldBe emptyList()
        }
    }

    @Nested
    inner class RaiderResponseMappingTests {
        @Test
        fun `should correctly map all raider fields to response`() {
            // Given
            val joinDate = LocalDateTime.of(2024, 1, 1, 0, 0)
            val raider = Raider(
                id = RaiderId(123L),
                guildId = GuildId("test-guild"),
                characterName = "CompleteChar",
                realm = "CompleteRealm",
                characterClass = CharacterClass.PALADIN,
                role = Role.TANK,
                rank = "Guild Master",
                status = RaiderStatus.ACTIVE,
                joinDate = joinDate,
                wowauditId = 9876L
            )

            every { getRaiderUseCase.execute(any()) } returns Result.success(raider)

            // When
            val response = controller.getRaider(123L)

            // Then
            response.id shouldBe 123L
            response.guildId shouldBe "test-guild"
            response.characterName shouldBe "CompleteChar"
            response.realm shouldBe "CompleteRealm"
            response.characterClass shouldBe "PALADIN"
            response.role shouldBe "TANK"
            response.rank shouldBe "Guild Master"
            response.status shouldBe "ACTIVE"
            response.joinDate shouldBe joinDate
            response.wowauditId shouldBe 9876L
            response.fullName shouldBe "CompleteChar-CompleteRealm"
            response.isEligibleForLoot shouldBe true
        }

        @Test
        fun `should return isEligibleForLoot false for non-active raiders`() {
            // Given
            val raider = createRaider(
                id = RaiderId(1L),
                status = RaiderStatus.BENCHED
            )

            every { getRaiderUseCase.execute(any()) } returns Result.success(raider)

            // When
            val response = controller.getRaider(1L)

            // Then
            response.isEligibleForLoot shouldBe false
        }
    }

    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.now(),
        wowauditId: Long? = null
    ): Raider = Raider(
        id = id,
        guildId = guildId,
        characterName = characterName,
        realm = realm,
        characterClass = characterClass,
        role = role,
        rank = rank,
        status = status,
        joinDate = joinDate,
        wowauditId = wowauditId
    )
}
