package com.edgerush.lootman.api.graphql.mutation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.raider.CreateRaiderCommand
import com.edgerush.lootman.application.raider.CreateRaiderUseCase
import com.edgerush.lootman.application.raider.DeleteRaiderCommand
import com.edgerush.lootman.application.raider.DeleteRaiderUseCase
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
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for RaiderMutationResolver.
 *
 * Tests the GraphQL mutation resolver for raider operations following TDD principles.
 */
class RaiderMutationResolverTest : UnitTest() {

    @MockK
    private lateinit var createRaiderUseCase: CreateRaiderUseCase

    @MockK
    private lateinit var updateRaiderUseCase: UpdateRaiderUseCase

    @MockK
    private lateinit var deleteRaiderUseCase: DeleteRaiderUseCase

    @InjectMockKs
    private lateinit var resolver: RaiderMutationResolver

    @Nested
    inner class CreateRaiderMutation {

        @Test
        fun `should create raider successfully`() {
            // Arrange
            val input = CreateRaiderInput(
                id = "123",
                guildId = "guild-456",
                characterName = "Arthas",
                realm = "Frostmourne",
                characterClass = "DEATH_KNIGHT",
                role = "TANK",
                rank = "Officer",
                status = "ACTIVE",
            )
            val raider = createTestRaider(
                id = 123L,
                guildId = "guild-456",
                name = "Arthas",
                realm = "Frostmourne",
                characterClass = CharacterClass.DEATH_KNIGHT,
                role = Role.TANK,
            )
            val commandSlot = slot<CreateRaiderCommand>()
            every { createRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(raider)

            // Act
            val result = resolver.createRaider(input)

            // Assert
            result.id shouldBe "123"
            result.characterName shouldBe "Arthas"
            result.realm shouldBe "Frostmourne"
            result.characterClass shouldBe CharacterClass.DEATH_KNIGHT
            result.role shouldBe Role.TANK
            commandSlot.captured.id shouldBe 123L
            commandSlot.captured.guildId shouldBe "guild-456"
            commandSlot.captured.characterName shouldBe "Arthas"
        }

        @Test
        fun `should propagate exception on create failure`() {
            // Arrange
            val input = CreateRaiderInput(
                id = "123",
                guildId = "guild-456",
                characterName = "Arthas",
                realm = "Frostmourne",
                characterClass = "DEATH_KNIGHT",
                role = "TANK",
            )
            every { createRaiderUseCase.execute(any()) } returns
                Result.failure(IllegalArgumentException("Raider already exists"))

            // Act & Assert
            val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                resolver.createRaider(input)
            }
            exception.message shouldBe "Raider already exists"
        }
    }

    @Nested
    inner class UpdateRaiderMutation {

        @Test
        fun `should update raider successfully`() {
            // Arrange
            val input = UpdateRaiderInput(
                id = "123",
                characterName = "Arthas Menethil",
                role = "DPS",
                status = "INACTIVE",
            )
            val updatedRaider = createTestRaider(
                id = 123L,
                name = "Arthas Menethil",
                role = Role.DPS,
                status = RaiderStatus.INACTIVE,
            )
            val commandSlot = slot<UpdateRaiderCommand>()
            every { updateRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(updatedRaider)

            // Act
            val result = resolver.updateRaider(input)

            // Assert
            result.id shouldBe "123"
            result.characterName shouldBe "Arthas Menethil"
            result.role shouldBe Role.DPS
            result.status shouldBe RaiderStatus.INACTIVE
            commandSlot.captured.id shouldBe 123L
            commandSlot.captured.characterName shouldBe "Arthas Menethil"
            commandSlot.captured.role shouldBe "DPS"
        }

        @Test
        fun `should return error when raider not found`() {
            // Arrange
            val input = UpdateRaiderInput(
                id = "999",
                characterName = "Unknown",
            )
            every { updateRaiderUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Raider not found with id: 999"))

            // Act & Assert
            val exception = org.junit.jupiter.api.assertThrows<NoSuchElementException> {
                resolver.updateRaider(input)
            }
            exception.message shouldBe "Raider not found with id: 999"
        }

        @Test
        fun `should only update provided fields`() {
            // Arrange
            val input = UpdateRaiderInput(
                id = "123",
                role = "HEALER",
                // Other fields are null - should not be updated
            )
            val commandSlot = slot<UpdateRaiderCommand>()
            every { updateRaiderUseCase.execute(capture(commandSlot)) } returns
                Result.success(createTestRaider(id = 123L, role = Role.HEALER))

            // Act
            resolver.updateRaider(input)

            // Assert
            val captured = commandSlot.captured
            captured.id shouldBe 123L
            captured.role shouldBe "HEALER"
            captured.characterName shouldBe null
            captured.realm shouldBe null
            captured.characterClass shouldBe null
            captured.status shouldBe null
        }
    }

    @Nested
    inner class DeleteRaiderMutation {

        @Test
        fun `should delete raider successfully`() {
            // Arrange
            val commandSlot = slot<DeleteRaiderCommand>()
            every { deleteRaiderUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // Act
            val result = resolver.deleteRaider(id = "123")

            // Assert
            result shouldBe true
            commandSlot.captured.id shouldBe 123L
        }

        @Test
        fun `should return error when raider not found for delete`() {
            // Arrange
            every { deleteRaiderUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Raider not found with id: 999"))

            // Act & Assert
            val exception = org.junit.jupiter.api.assertThrows<NoSuchElementException> {
                resolver.deleteRaider(id = "999")
            }
            exception.message shouldBe "Raider not found with id: 999"
        }
    }

    // Helper function
    private fun createTestRaider(
        id: Long = 1L,
        guildId: String = "test-guild",
        name: String = "TestRaider",
        realm: String = "TestRealm",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        status: RaiderStatus = RaiderStatus.ACTIVE,
    ): Raider = Raider(
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
