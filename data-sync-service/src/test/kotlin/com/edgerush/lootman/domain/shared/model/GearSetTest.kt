package com.edgerush.lootman.domain.shared.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GearSet, GearItem, EquipmentSlot, ItemQuality, and GearSetType.
 */
class GearSetTest : UnitTest() {
    // region Test Fixtures

    private fun createGearItem(
        itemId: Long = 12345L,
        name: String = "Test Item",
        itemLevel: Int = 489,
        quality: ItemQuality = ItemQuality.EPIC,
        slot: EquipmentSlot = EquipmentSlot.HEAD,
        isTierPiece: Boolean = false,
        enchant: String? = null,
        sockets: Int = 0,
    ) = GearItem(
        itemId = ItemId(itemId),
        name = name,
        itemLevel = itemLevel,
        quality = quality,
        slot = slot,
        isTierPiece = isTierPiece,
        enchant = enchant,
        sockets = sockets,
    )

    private fun createGearSet(
        items: Map<EquipmentSlot, GearItem> = emptyMap(),
        gearSetType: GearSetType = GearSetType.EQUIPPED,
    ) = GearSet(items = items, gearSetType = gearSetType)

    // endregion

    @Nested
    inner class GearItemTests {
        @Test
        fun `should create valid gear item with all fields`() {
            // Arrange & Act
            val item =
                createGearItem(
                    itemId = 207160L,
                    name = "Crown of the Firelands",
                    itemLevel = 489,
                    quality = ItemQuality.EPIC,
                    slot = EquipmentSlot.HEAD,
                    isTierPiece = true,
                    enchant = "Incandescent Essence",
                    sockets = 2,
                )

            // Assert
            item.itemId shouldBe ItemId(207160L)
            item.name shouldBe "Crown of the Firelands"
            item.itemLevel shouldBe 489
            item.quality shouldBe ItemQuality.EPIC
            item.slot shouldBe EquipmentSlot.HEAD
            item.isTierPiece shouldBe true
            item.enchant shouldBe "Incandescent Essence"
            item.sockets shouldBe 2
        }

        @Test
        fun `should create gear item with default values`() {
            // Arrange & Act
            val item = createGearItem()

            // Assert
            item.isTierPiece shouldBe false
            item.enchant.shouldBeNull()
            item.sockets shouldBe 0
        }

        @Test
        fun `should throw exception when item level is zero`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    createGearItem(itemLevel = 0)
                }
            exception.message shouldBe "Item level must be positive"
        }

        @Test
        fun `should throw exception when item level is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    createGearItem(itemLevel = -1)
                }
            exception.message shouldBe "Item level must be positive"
        }

        @Test
        fun `should throw exception when sockets is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    createGearItem(sockets = -1)
                }
            exception.message shouldBe "Sockets cannot be negative"
        }

        @Test
        fun `should allow zero sockets`() {
            // Arrange & Act
            val item = createGearItem(sockets = 0)

            // Assert
            item.sockets shouldBe 0
        }

        @Test
        fun `should allow item level of 1`() {
            // Arrange & Act
            val item = createGearItem(itemLevel = 1)

            // Assert
            item.itemLevel shouldBe 1
        }

        @Test
        fun `should be immutable data class`() {
            // Arrange
            val item1 = createGearItem(itemId = 100L, itemLevel = 489)
            val item2 = createGearItem(itemId = 100L, itemLevel = 489)

            // Assert
            item1 shouldBe item2
            item1.hashCode() shouldBe item2.hashCode()
        }
    }

    @Nested
    inner class GearSetAverageItemLevelTests {
        @Test
        fun `should return zero average when items map is empty`() {
            // Arrange
            val gearSet = createGearSet(items = emptyMap())

            // Act
            val average = gearSet.getAverageItemLevel()

            // Assert
            average shouldBeExactly 0.0
        }

        @Test
        fun `should calculate average item level for single item`() {
            // Arrange
            val item = createGearItem(itemLevel = 489)
            val gearSet = createGearSet(items = mapOf(EquipmentSlot.HEAD to item))

            // Act
            val average = gearSet.getAverageItemLevel()

            // Assert
            average shouldBeExactly 489.0
        }

        @Test
        fun `should calculate average item level for multiple items`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 480),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, itemLevel = 490),
                    EquipmentSlot.LEGS to createGearItem(slot = EquipmentSlot.LEGS, itemLevel = 500),
                )
            val gearSet = createGearSet(items = items)

            // Act
            val average = gearSet.getAverageItemLevel()

            // Assert
            average shouldBeExactly 490.0
        }

        @Test
        fun `should handle items with varying item levels`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, itemLevel = 450),
                    EquipmentSlot.NECK to createGearItem(slot = EquipmentSlot.NECK, itemLevel = 489),
                )
            val gearSet = createGearSet(items = items)

            // Act
            val average = gearSet.getAverageItemLevel()

            // Assert
            average shouldBeExactly 469.5
        }
    }

    @Nested
    inner class GearSetTierPieceTests {
        @Test
        fun `should return zero tier piece count when no tier pieces equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = false),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = false),
                )
            val gearSet = createGearSet(items = items)

            // Act
            val tierCount = gearSet.getTierPieceCount()

            // Assert
            tierCount shouldBe 0
        }

        @Test
        fun `should count tier pieces correctly`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = false),
                    EquipmentSlot.HANDS to createGearItem(slot = EquipmentSlot.HANDS, isTierPiece = true),
                )
            val gearSet = createGearSet(items = items)

            // Act
            val tierCount = gearSet.getTierPieceCount()

            // Assert
            tierCount shouldBe 3
        }

        @Test
        fun `should return zero tier piece count when items map is empty`() {
            // Arrange
            val gearSet = createGearSet(items = emptyMap())

            // Act
            val tierCount = gearSet.getTierPieceCount()

            // Assert
            tierCount shouldBe 0
        }
    }

    @Nested
    inner class GearSetTierBonusTests {
        @Test
        fun `should have 2-piece tier bonus when 2 or more tier pieces equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                )
            val gearSet = createGearSet(items = items)

            // Act & Assert
            gearSet.hasTierBonus(2) shouldBe true
        }

        @Test
        fun `should not have 2-piece tier bonus when only 1 tier piece equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = false),
                )
            val gearSet = createGearSet(items = items)

            // Act & Assert
            gearSet.hasTierBonus(2) shouldBe false
        }

        @Test
        fun `should have 4-piece tier bonus when 4 or more tier pieces equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = true),
                    EquipmentSlot.HANDS to createGearItem(slot = EquipmentSlot.HANDS, isTierPiece = true),
                )
            val gearSet = createGearSet(items = items)

            // Act & Assert
            gearSet.hasTierBonus(4) shouldBe true
        }

        @Test
        fun `should not have 4-piece tier bonus when only 3 tier pieces equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = true),
                    EquipmentSlot.HANDS to createGearItem(slot = EquipmentSlot.HANDS, isTierPiece = false),
                )
            val gearSet = createGearSet(items = items)

            // Act & Assert
            gearSet.hasTierBonus(4) shouldBe false
        }

        @Test
        fun `should have both 2-piece and 4-piece tier bonus when 4 tier pieces equipped`() {
            // Arrange
            val items =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(slot = EquipmentSlot.HEAD, isTierPiece = true),
                    EquipmentSlot.SHOULDER to createGearItem(slot = EquipmentSlot.SHOULDER, isTierPiece = true),
                    EquipmentSlot.CHEST to createGearItem(slot = EquipmentSlot.CHEST, isTierPiece = true),
                    EquipmentSlot.HANDS to createGearItem(slot = EquipmentSlot.HANDS, isTierPiece = true),
                )
            val gearSet = createGearSet(items = items)

            // Act & Assert
            gearSet.hasTierBonus(2) shouldBe true
            gearSet.hasTierBonus(4) shouldBe true
        }

        @Test
        fun `should throw exception for invalid tier bonus pieces of 1`() {
            // Arrange
            val gearSet = createGearSet()

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    gearSet.hasTierBonus(1)
                }
            exception.message shouldBe "Tier bonus can only be 2 or 4 pieces"
        }

        @Test
        fun `should throw exception for invalid tier bonus pieces of 3`() {
            // Arrange
            val gearSet = createGearSet()

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    gearSet.hasTierBonus(3)
                }
            exception.message shouldBe "Tier bonus can only be 2 or 4 pieces"
        }

        @Test
        fun `should throw exception for invalid tier bonus pieces of 5`() {
            // Arrange
            val gearSet = createGearSet()

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    gearSet.hasTierBonus(5)
                }
            exception.message shouldBe "Tier bonus can only be 2 or 4 pieces"
        }

        @Test
        fun `should throw exception for zero tier bonus pieces`() {
            // Arrange
            val gearSet = createGearSet()

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    gearSet.hasTierBonus(0)
                }
            exception.message shouldBe "Tier bonus can only be 2 or 4 pieces"
        }
    }

    @Nested
    inner class GearSetGetItemTests {
        @Test
        fun `should return item when slot has item equipped`() {
            // Arrange
            val headItem = createGearItem(slot = EquipmentSlot.HEAD, name = "Helm of Testing")
            val gearSet = createGearSet(items = mapOf(EquipmentSlot.HEAD to headItem))

            // Act
            val result = gearSet.getItem(EquipmentSlot.HEAD)

            // Assert
            result shouldBe headItem
        }

        @Test
        fun `should return null when slot is empty`() {
            // Arrange
            val headItem = createGearItem(slot = EquipmentSlot.HEAD)
            val gearSet = createGearSet(items = mapOf(EquipmentSlot.HEAD to headItem))

            // Act
            val result = gearSet.getItem(EquipmentSlot.CHEST)

            // Assert
            result.shouldBeNull()
        }

        @Test
        fun `should return null when items map is empty`() {
            // Arrange
            val gearSet = createGearSet(items = emptyMap())

            // Act
            val result = gearSet.getItem(EquipmentSlot.HEAD)

            // Assert
            result.shouldBeNull()
        }
    }

    @Nested
    inner class EquipmentSlotTests {
        @Test
        fun `should have all 16 equipment slots`() {
            // Act
            val slots = EquipmentSlot.entries

            // Assert
            slots.size shouldBe 16
        }

        @Test
        fun `should parse slot from string matching name exactly`() {
            // Act & Assert
            EquipmentSlot.fromString("HEAD") shouldBe EquipmentSlot.HEAD
            EquipmentSlot.fromString("FINGER1") shouldBe EquipmentSlot.FINGER_1
            EquipmentSlot.fromString("TRINKET2") shouldBe EquipmentSlot.TRINKET_2
        }

        @Test
        fun `should parse slot from string case insensitively`() {
            // Act & Assert
            EquipmentSlot.fromString("head") shouldBe EquipmentSlot.HEAD
            EquipmentSlot.fromString("Head") shouldBe EquipmentSlot.HEAD
            EquipmentSlot.fromString("finger1") shouldBe EquipmentSlot.FINGER_1
        }

        @Test
        fun `should parse slot from string with spaces instead of underscores`() {
            // Act & Assert
            EquipmentSlot.fromString("MAIN HAND") shouldBe EquipmentSlot.MAIN_HAND
            EquipmentSlot.fromString("off hand") shouldBe EquipmentSlot.OFF_HAND
            EquipmentSlot.fromString("Finger 1") shouldBe EquipmentSlot.FINGER_1
        }

        @Test
        fun `should return null for unknown slot string`() {
            // Act & Assert
            EquipmentSlot.fromString("UNKNOWN").shouldBeNull()
            EquipmentSlot.fromString("").shouldBeNull()
            EquipmentSlot.fromString("TABARD").shouldBeNull()
        }
    }

    @Nested
    inner class ItemQualityTests {
        @Test
        fun `should have all 7 quality levels`() {
            // Act
            val qualities = ItemQuality.entries

            // Assert
            qualities.size shouldBe 7
        }

        @Test
        fun `should have correct ordinal values`() {
            // Assert
            ItemQuality.POOR.ordinal shouldBe 0
            ItemQuality.COMMON.ordinal shouldBe 1
            ItemQuality.UNCOMMON.ordinal shouldBe 2
            ItemQuality.RARE.ordinal shouldBe 3
            ItemQuality.EPIC.ordinal shouldBe 4
            ItemQuality.LEGENDARY.ordinal shouldBe 5
            ItemQuality.ARTIFACT.ordinal shouldBe 6
        }

        @Test
        fun `should create quality from valid integer`() {
            // Act & Assert
            ItemQuality.fromInt(0) shouldBe ItemQuality.POOR
            ItemQuality.fromInt(3) shouldBe ItemQuality.RARE
            ItemQuality.fromInt(4) shouldBe ItemQuality.EPIC
            ItemQuality.fromInt(6) shouldBe ItemQuality.ARTIFACT
        }

        @Test
        fun `should return null for out of range integer`() {
            // Act & Assert
            ItemQuality.fromInt(-1).shouldBeNull()
            ItemQuality.fromInt(7).shouldBeNull()
            ItemQuality.fromInt(100).shouldBeNull()
        }
    }

    @Nested
    inner class GearSetTypeTests {
        @Test
        fun `should have EQUIPPED and BEST types`() {
            // Act
            val types = GearSetType.entries

            // Assert
            types.size shouldBe 2
            types shouldBe listOf(GearSetType.EQUIPPED, GearSetType.BEST)
        }

        @Test
        fun `should create gear set with EQUIPPED type`() {
            // Arrange & Act
            val gearSet = createGearSet(gearSetType = GearSetType.EQUIPPED)

            // Assert
            gearSet.gearSetType shouldBe GearSetType.EQUIPPED
        }

        @Test
        fun `should create gear set with BEST type`() {
            // Arrange & Act
            val gearSet = createGearSet(gearSetType = GearSetType.BEST)

            // Assert
            gearSet.gearSetType shouldBe GearSetType.BEST
        }
    }

    @Nested
    inner class GearSetFullEquipmentTests {
        @Test
        fun `should handle full 16 slot gear set`() {
            // Arrange
            val items =
                EquipmentSlot.entries.associateWith { slot ->
                    createGearItem(
                        itemId = 10000L + slot.ordinal,
                        slot = slot,
                        itemLevel = 480 + slot.ordinal,
                    )
                }
            val gearSet = createGearSet(items = items)

            // Act
            val itemCount = gearSet.items.size
            val averageIlvl = gearSet.getAverageItemLevel()

            // Assert
            itemCount shouldBe 16
            // Average of 480 to 495 = (480 + 495) / 2 = 487.5
            averageIlvl shouldBeExactly 487.5
        }
    }
}
