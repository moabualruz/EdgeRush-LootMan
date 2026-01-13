package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for InMemoryGearRepository.
 *
 * Following TDD - these tests are written before the implementation.
 */
class InMemoryGearRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryGearRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryGearRepository()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save and return the gear set`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet()

            // Act
            val saved = repository.save(raiderId, gearSet)

            // Assert
            saved shouldBe gearSet
        }

        @Test
        fun `should persist gear set to storage`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet()

            // Act
            repository.save(raiderId, gearSet)
            val retrieved = repository.findByRaiderIdAndType(raiderId, gearSet.gearSetType)

            // Assert
            retrieved shouldBe gearSet
        }

        @Test
        fun `should replace existing gear set for same raider and type`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val originalGearSet = createGearSet(items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 400)
            ))
            repository.save(raiderId, originalGearSet)

            val newGearSet = createGearSet(items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 450),
                EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, itemLevel = 445)
            ))

            // Act
            repository.save(raiderId, newGearSet)
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved shouldBe newGearSet
            retrieved?.items?.size shouldBe 2
            retrieved?.items?.get(EquipmentSlot.HEAD)?.itemLevel shouldBe 450
        }

        @Test
        fun `should save different gear set types for same raider`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val equippedGear = createGearSet(gearSetType = GearSetType.EQUIPPED, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 400)
            ))
            val bestGear = createGearSet(gearSetType = GearSetType.BEST, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 450)
            ))

            // Act
            repository.save(raiderId, equippedGear)
            repository.save(raiderId, bestGear)

            // Assert
            repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)?.items?.get(EquipmentSlot.HEAD)?.itemLevel shouldBe 400
            repository.findByRaiderIdAndType(raiderId, GearSetType.BEST)?.items?.get(EquipmentSlot.HEAD)?.itemLevel shouldBe 450
        }

        @Test
        fun `should save gear sets for different raiders`() {
            // Arrange
            val raider1 = RaiderId(1L)
            val raider2 = RaiderId(2L)
            val gearSet1 = createGearSet(items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, name = "Raider1 Helm")
            ))
            val gearSet2 = createGearSet(items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, name = "Raider2 Helm")
            ))

            // Act
            repository.save(raider1, gearSet1)
            repository.save(raider2, gearSet2)

            // Assert
            repository.findByRaiderIdAndType(raider1, GearSetType.EQUIPPED)?.items?.get(EquipmentSlot.HEAD)?.name shouldBe "Raider1 Helm"
            repository.findByRaiderIdAndType(raider2, GearSetType.EQUIPPED)?.items?.get(EquipmentSlot.HEAD)?.name shouldBe "Raider2 Helm"
        }
    }

    @Nested
    inner class FindCurrentGearTests {
        @Test
        fun `should return equipped gear set`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val equippedGear = createGearSet(gearSetType = GearSetType.EQUIPPED)
            repository.save(raiderId, equippedGear)

            // Act
            val retrieved = repository.findCurrentGear(raiderId)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe equippedGear
        }

        @Test
        fun `should return null when no equipped gear exists`() {
            // Arrange
            val raiderId = RaiderId(1L)

            // Act
            val retrieved = repository.findCurrentGear(raiderId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return equipped gear even when best gear also exists`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val equippedGear = createGearSet(gearSetType = GearSetType.EQUIPPED, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, name = "Equipped Helm")
            ))
            val bestGear = createGearSet(gearSetType = GearSetType.BEST, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, name = "Best Helm")
            ))

            repository.save(raiderId, equippedGear)
            repository.save(raiderId, bestGear)

            // Act
            val retrieved = repository.findCurrentGear(raiderId)

            // Assert
            retrieved?.items?.get(EquipmentSlot.HEAD)?.name shouldBe "Equipped Helm"
        }

        @Test
        fun `should return null when only best gear exists`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val bestGear = createGearSet(gearSetType = GearSetType.BEST)
            repository.save(raiderId, bestGear)

            // Act
            val retrieved = repository.findCurrentGear(raiderId)

            // Assert
            retrieved shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdAndTypeTests {
        @Test
        fun `should return gear set when found`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet()
            repository.save(raiderId, gearSet)

            // Act
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe gearSet
        }

        @Test
        fun `should return null when gear set not found for raider`() {
            // Arrange
            val nonExistentRaiderId = RaiderId(9999L)

            // Act
            val retrieved = repository.findByRaiderIdAndType(nonExistentRaiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null when gear set type not found for raider`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val equippedGear = createGearSet(gearSetType = GearSetType.EQUIPPED)
            repository.save(raiderId, equippedGear)

            // Act
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.BEST)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return correct gear set type`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val equippedGear = createGearSet(gearSetType = GearSetType.EQUIPPED, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 400)
            ))
            val bestGear = createGearSet(gearSetType = GearSetType.BEST, items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 450)
            ))

            repository.save(raiderId, equippedGear)
            repository.save(raiderId, bestGear)

            // Act & Assert
            repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)?.items?.get(EquipmentSlot.HEAD)?.itemLevel shouldBe 400
            repository.findByRaiderIdAndType(raiderId, GearSetType.BEST)?.items?.get(EquipmentSlot.HEAD)?.itemLevel shouldBe 450
        }
    }

    @Nested
    inner class ConcurrencyTests {
        @Test
        fun `should handle concurrent saves without data loss`() {
            // Arrange
            val gearSets = (1..100).map { index ->
                RaiderId(index.toLong()) to createGearSet(items = mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 400 + index)
                ))
            }

            // Act - simulate concurrent saves
            gearSets.parallelStream().forEach { (raiderId, gearSet) ->
                repository.save(raiderId, gearSet)
            }

            // Assert - all gear sets should be saved
            gearSets.forEach { (raiderId, gearSet) ->
                repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED) shouldBe gearSet
            }
        }
    }

    @Nested
    inner class GearItemTests {
        @Test
        fun `should correctly store gear items with tier pieces`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet(items = mapOf(
                EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = false)
            ))

            // Act
            repository.save(raiderId, gearSet)
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved?.getTierPieceCount() shouldBe 2
        }

        @Test
        fun `should correctly store gear items with enchants and sockets`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet(items = mapOf(
                EquipmentSlot.NECK to createGearItem(
                    slot = EquipmentSlot.NECK,
                    enchant = "Haste +200",
                    sockets = 3
                )
            ))

            // Act
            repository.save(raiderId, gearSet)
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved?.items?.get(EquipmentSlot.NECK)?.enchant shouldBe "Haste +200"
            retrieved?.items?.get(EquipmentSlot.NECK)?.sockets shouldBe 3
        }

        @Test
        fun `should correctly store empty gear set`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val gearSet = createGearSet(items = emptyMap())

            // Act
            repository.save(raiderId, gearSet)
            val retrieved = repository.findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)

            // Assert
            retrieved shouldNotBe null
            retrieved?.items?.size shouldBe 0
        }
    }

    private fun createGearSet(
        items: Map<EquipmentSlot, GearItem> = mapOf(
            EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD)
        ),
        gearSetType: GearSetType = GearSetType.EQUIPPED
    ): GearSet = GearSet(
        items = items,
        gearSetType = gearSetType
    )

    private fun createGearItem(
        itemId: ItemId = ItemId(12345L),
        name: String = "Test Item",
        itemLevel: Int = 450,
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
