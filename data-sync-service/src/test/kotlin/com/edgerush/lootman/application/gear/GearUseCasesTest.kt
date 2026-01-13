package com.edgerush.lootman.application.gear

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import com.edgerush.lootman.domain.shared.repository.GearRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for Gear use cases.
 *
 * Tests use case business logic by mocking the repository layer.
 */
class GearUseCasesTest : UnitTest() {

    private lateinit var gearRepository: GearRepository

    @BeforeEach
    fun setup() {
        gearRepository = mockk()
    }

    @Nested
    inner class GetCurrentGearUseCaseTests {
        private lateinit var useCase: GetCurrentGearUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = GetCurrentGearUseCase(gearRepository)
        }

        @Test
        fun `should return gear when found`() {
            // Given
            val gearSet = createGearSet()
            val query = GetCurrentGearQuery(raiderId = 1L)

            every { gearRepository.findCurrentGear(RaiderId(1L)) } returns gearSet

            // When
            val result = useCase.execute(query)

            // Then
            result.isSuccess shouldBe true
            val foundGear = result.getOrThrow()
            foundGear.gearSetType shouldBe GearSetType.EQUIPPED
            foundGear.items.isNotEmpty() shouldBe true
        }

        @Test
        fun `should fail when gear not found`() {
            // Given
            val query = GetCurrentGearQuery(raiderId = 999L)

            every { gearRepository.findCurrentGear(RaiderId(999L)) } returns null

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Gear not found for raider: 999"
        }
    }

    @Nested
    inner class GetGearByTypeUseCaseTests {
        private lateinit var useCase: GetGearByTypeUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = GetGearByTypeUseCase(gearRepository)
        }

        @Test
        fun `should return gear for specified type`() {
            // Given
            val gearSet = createGearSet(gearSetType = GearSetType.BEST)
            val query = GetGearByTypeQuery(raiderId = 1L, gearSetType = "BEST")

            every { gearRepository.findByRaiderIdAndType(RaiderId(1L), GearSetType.BEST) } returns gearSet

            // When
            val result = useCase.execute(query)

            // Then
            result.isSuccess shouldBe true
            val foundGear = result.getOrThrow()
            foundGear.gearSetType shouldBe GearSetType.BEST
        }

        @Test
        fun `should fail when gear type not found`() {
            // Given
            val query = GetGearByTypeQuery(raiderId = 1L, gearSetType = "BEST")

            every { gearRepository.findByRaiderIdAndType(RaiderId(1L), GearSetType.BEST) } returns null

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<NoSuchElementException>()
            result.exceptionOrNull()?.message shouldBe "Gear of type BEST not found for raider: 1"
        }

        @Test
        fun `should fail with invalid gear type`() {
            // Given
            val query = GetGearByTypeQuery(raiderId = 1L, gearSetType = "INVALID")

            // When
            val result = useCase.execute(query)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }
    }

    @Nested
    inner class SaveGearUseCaseTests {
        private lateinit var useCase: SaveGearUseCase

        @BeforeEach
        fun setupUseCase() {
            useCase = SaveGearUseCase(gearRepository)
        }

        @Test
        fun `should save gear set`() {
            // Given
            val command = SaveGearCommand(
                raiderId = 1L,
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemCommand(
                        itemId = 100L,
                        name = "Epic Sword",
                        itemLevel = 489,
                        quality = "EPIC",
                        slot = "MAIN_HAND",
                        isTierPiece = false
                    ),
                    GearItemCommand(
                        itemId = 200L,
                        name = "Tier Helm",
                        itemLevel = 489,
                        quality = "EPIC",
                        slot = "HEAD",
                        isTierPiece = true,
                        enchant = "Incandescent Essence",
                        sockets = 1
                    )
                )
            )

            val savedGearSlot = slot<GearSet>()
            every { gearRepository.save(RaiderId(1L), capture(savedGearSlot)) } answers { savedGearSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val savedGear = result.getOrThrow()
            savedGear.gearSetType shouldBe GearSetType.EQUIPPED
            savedGear.items.size shouldBe 2
            savedGear.items[EquipmentSlot.MAIN_HAND]?.name shouldBe "Epic Sword"
            savedGear.items[EquipmentSlot.HEAD]?.isTierPiece shouldBe true
            savedGear.items[EquipmentSlot.HEAD]?.enchant shouldBe "Incandescent Essence"
            savedGear.items[EquipmentSlot.HEAD]?.sockets shouldBe 1

            verify(exactly = 1) { gearRepository.save(RaiderId(1L), any()) }
        }

        @Test
        fun `should save empty gear set`() {
            // Given
            val command = SaveGearCommand(
                raiderId = 1L,
                gearSetType = "BEST",
                items = emptyList()
            )

            val savedGearSlot = slot<GearSet>()
            every { gearRepository.save(RaiderId(1L), capture(savedGearSlot)) } answers { savedGearSlot.captured }

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            val savedGear = result.getOrThrow()
            savedGear.items.size shouldBe 0
            savedGear.gearSetType shouldBe GearSetType.BEST
        }

        @Test
        fun `should fail with invalid gear type`() {
            // Given
            val command = SaveGearCommand(
                raiderId = 1L,
                gearSetType = "INVALID_TYPE",
                items = emptyList()
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }

        @Test
        fun `should fail with invalid equipment slot`() {
            // Given
            val command = SaveGearCommand(
                raiderId = 1L,
                gearSetType = "EQUIPPED",
                items = listOf(
                    GearItemCommand(
                        itemId = 100L,
                        name = "Test Item",
                        itemLevel = 489,
                        quality = "EPIC",
                        slot = "INVALID_SLOT"
                    )
                )
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        }
    }

    private fun createGearSet(
        gearSetType: GearSetType = GearSetType.EQUIPPED,
        items: Map<EquipmentSlot, GearItem> = mapOf(
            EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD)
        )
    ): GearSet = GearSet(
        items = items,
        gearSetType = gearSetType
    )

    private fun createGearItem(
        itemId: ItemId = ItemId(12345L),
        name: String = "Test Item",
        itemLevel: Int = 489,
        quality: ItemQuality = ItemQuality.EPIC,
        slot: EquipmentSlot = EquipmentSlot.HEAD,
        isTierPiece: Boolean = false,
        enchant: String? = null,
        sockets: Int = 0
    ): GearItem = GearItem(
        itemId = itemId,
        name = name,
        itemLevel = itemLevel,
        quality = quality,
        slot = slot,
        isTierPiece = isTierPiece,
        enchant = enchant,
        sockets = sockets
    )
}
