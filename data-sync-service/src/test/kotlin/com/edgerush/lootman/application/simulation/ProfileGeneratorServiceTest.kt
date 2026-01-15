package com.edgerush.lootman.application.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileGeneratorServiceTest : UnitTest() {
    private lateinit var generator: ProfileGeneratorService

    @BeforeEach
    fun setUp() {
        generator = ProfileGeneratorService()
    }

    private fun createGearItem(
        slot: EquipmentSlot,
        itemId: Long = 12345L,
        name: String = "Test Item",
        itemLevel: Int = 639,
        isTierPiece: Boolean = false,
    ): GearItem {
        return GearItem(
            itemId = ItemId(itemId),
            name = name,
            itemLevel = itemLevel,
            quality = ItemQuality.EPIC,
            slot = slot,
            isTierPiece = isTierPiece,
        )
    }

    private fun createGearSet(items: Map<EquipmentSlot, GearItem>): GearSet {
        return GearSet(
            items = items,
            gearSetType = GearSetType.EQUIPPED,
        )
    }

    @Nested
    inner class GenerateProfile {
        @Test
        fun `should generate basic profile with character info`() {
            // Arrange
            val characterName = "Testchar"
            val characterRealm = "TestRealm"
            val characterClass = "warrior"
            val characterSpec = "fury"
            val characterLevel = 80
            val characterRace = "human"

            // Act
            val profile =
                generator.generateProfile(
                    characterName = characterName,
                    characterRealm = characterRealm,
                    characterClass = characterClass,
                    characterSpec = characterSpec,
                    characterLevel = characterLevel,
                    characterRace = characterRace,
                    gear = null,
                )

            // Assert
            profile shouldContain """$characterClass="$characterName""""
            profile shouldContain "level=$characterLevel"
            profile shouldContain "race=$characterRace"
            profile shouldContain "spec=$characterSpec"
        }

        @Test
        fun `should handle class names with spaces`() {
            // Arrange
            val characterClass = "death_knight"

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = characterClass,
                    characterSpec = "frost",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = null,
                )

            // Assert
            profile shouldContain """death_knight="Testchar""""
        }

        @Test
        fun `should include gear when provided`() {
            // Arrange
            val gear =
                createGearSet(
                    mapOf(
                        EquipmentSlot.HEAD to createGearItem(EquipmentSlot.HEAD, itemId = 12345L),
                        EquipmentSlot.NECK to createGearItem(EquipmentSlot.NECK, itemId = 12346L),
                    ),
                )

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = gear,
                )

            // Assert
            profile shouldContain "head=,id=12345"
            profile shouldContain "neck=,id=12346"
        }

        @Test
        fun `should exclude gear section when no gear provided`() {
            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = null,
                )

            // Assert
            profile shouldNotContain "head="
            profile shouldNotContain "# Gear"
        }
    }

    @Nested
    inner class SlotMapping {
        @Test
        fun `should map all equipment slots to simc slot names`() {
            // Arrange
            val allSlots =
                mapOf(
                    EquipmentSlot.HEAD to createGearItem(EquipmentSlot.HEAD, itemId = 1L),
                    EquipmentSlot.NECK to createGearItem(EquipmentSlot.NECK, itemId = 2L),
                    EquipmentSlot.SHOULDER to createGearItem(EquipmentSlot.SHOULDER, itemId = 3L),
                    EquipmentSlot.BACK to createGearItem(EquipmentSlot.BACK, itemId = 4L),
                    EquipmentSlot.CHEST to createGearItem(EquipmentSlot.CHEST, itemId = 5L),
                    EquipmentSlot.WRIST to createGearItem(EquipmentSlot.WRIST, itemId = 6L),
                    EquipmentSlot.HANDS to createGearItem(EquipmentSlot.HANDS, itemId = 7L),
                    EquipmentSlot.WAIST to createGearItem(EquipmentSlot.WAIST, itemId = 8L),
                    EquipmentSlot.LEGS to createGearItem(EquipmentSlot.LEGS, itemId = 9L),
                    EquipmentSlot.FEET to createGearItem(EquipmentSlot.FEET, itemId = 10L),
                    EquipmentSlot.FINGER_1 to createGearItem(EquipmentSlot.FINGER_1, itemId = 11L),
                    EquipmentSlot.FINGER_2 to createGearItem(EquipmentSlot.FINGER_2, itemId = 12L),
                    EquipmentSlot.TRINKET_1 to createGearItem(EquipmentSlot.TRINKET_1, itemId = 13L),
                    EquipmentSlot.TRINKET_2 to createGearItem(EquipmentSlot.TRINKET_2, itemId = 14L),
                    EquipmentSlot.MAIN_HAND to createGearItem(EquipmentSlot.MAIN_HAND, itemId = 15L),
                    EquipmentSlot.OFF_HAND to createGearItem(EquipmentSlot.OFF_HAND, itemId = 16L),
                )
            val gear = createGearSet(allSlots)

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = gear,
                )

            // Assert - verify simc slot names
            profile shouldContain "head=,id=1"
            profile shouldContain "neck=,id=2"
            profile shouldContain "shoulder=,id=3"
            profile shouldContain "back=,id=4"
            profile shouldContain "chest=,id=5"
            profile shouldContain "wrist=,id=6"
            profile shouldContain "hands=,id=7"
            profile shouldContain "waist=,id=8"
            profile shouldContain "legs=,id=9"
            profile shouldContain "feet=,id=10"
            profile shouldContain "finger1=,id=11"
            profile shouldContain "finger2=,id=12"
            profile shouldContain "trinket1=,id=13"
            profile shouldContain "trinket2=,id=14"
            profile shouldContain "main_hand=,id=15"
            profile shouldContain "off_hand=,id=16"
        }
    }

    @Nested
    inner class ItemLevelInclusion {
        @Test
        fun `should include item level in gear lines`() {
            // Arrange
            val gear =
                createGearSet(
                    mapOf(
                        EquipmentSlot.HEAD to
                            createGearItem(
                                EquipmentSlot.HEAD,
                                itemId = 12345L,
                                itemLevel = 639,
                            ),
                    ),
                )

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = gear,
                )

            // Assert
            profile shouldContain "head=,id=12345,ilevel=639"
        }
    }

    @Nested
    inner class GenerateMinimalProfile {
        @Test
        fun `should generate minimal profile with just basic info`() {
            // Arrange
            val characterName = "Minimal"
            val characterClass = "mage"
            val characterSpec = "fire"

            // Act
            val profile =
                generator.generateMinimalProfile(
                    characterName = characterName,
                    characterClass = characterClass,
                    characterSpec = characterSpec,
                )

            // Assert
            profile shouldContain """$characterClass="$characterName""""
            profile shouldContain "level=80"
            profile shouldContain "race=human"
            profile shouldContain "spec=$characterSpec"
        }

        @Test
        fun `should lowercase spec in minimal profile`() {
            // Act
            val profile =
                generator.generateMinimalProfile(
                    characterName = "Test",
                    characterClass = "warrior",
                    characterSpec = "FURY",
                )

            // Assert
            profile shouldContain "spec=fury"
        }
    }

    @Nested
    inner class EmptyGearHandling {
        @Test
        fun `should exclude gear section when gear set is empty`() {
            // Arrange
            val emptyGear = createGearSet(emptyMap())

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = emptyGear,
                )

            // Assert
            profile shouldNotContain "# Gear"
            profile shouldNotContain "head="
        }
    }

    @Nested
    inner class ProfileValidation {
        @Test
        fun `generated profile should be valid simc syntax`() {
            // Arrange
            val gear =
                createGearSet(
                    mapOf(
                        EquipmentSlot.HEAD to createGearItem(EquipmentSlot.HEAD),
                    ),
                )

            // Act
            val profile =
                generator.generateProfile(
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    characterLevel = 80,
                    characterRace = "human",
                    gear = gear,
                )

            // Assert - basic syntax validation
            profile shouldNotBe null
            profile.isNotBlank() shouldBe true
            // Each line should either be empty, a comment, or a key=value pair
            profile.lines().forEach { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    line shouldContain "="
                }
            }
        }
    }
}
