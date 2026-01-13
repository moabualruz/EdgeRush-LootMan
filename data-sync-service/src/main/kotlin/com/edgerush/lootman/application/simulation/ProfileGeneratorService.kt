package com.edgerush.lootman.application.simulation

import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearSet
import org.springframework.stereotype.Service

/**
 * Service to generate SimulationCraft profiles from character data.
 *
 * Converts WoW character data into SimC-compatible profile format
 * that can be used to run gear upgrade simulations.
 */
@Service
class ProfileGeneratorService {

    /**
     * Mapping from EquipmentSlot to SimC slot names.
     */
    private val slotMapping = mapOf(
        EquipmentSlot.HEAD to "head",
        EquipmentSlot.NECK to "neck",
        EquipmentSlot.SHOULDER to "shoulder",
        EquipmentSlot.BACK to "back",
        EquipmentSlot.CHEST to "chest",
        EquipmentSlot.WRIST to "wrist",
        EquipmentSlot.HANDS to "hands",
        EquipmentSlot.WAIST to "waist",
        EquipmentSlot.LEGS to "legs",
        EquipmentSlot.FEET to "feet",
        EquipmentSlot.FINGER_1 to "finger1",
        EquipmentSlot.FINGER_2 to "finger2",
        EquipmentSlot.TRINKET_1 to "trinket1",
        EquipmentSlot.TRINKET_2 to "trinket2",
        EquipmentSlot.MAIN_HAND to "main_hand",
        EquipmentSlot.OFF_HAND to "off_hand"
    )

    /**
     * Generates a SimulationCraft profile for a character.
     *
     * @param characterName The character's name
     * @param characterRealm The realm name
     * @param characterClass The character's class (e.g., "warrior", "death_knight")
     * @param characterSpec The character's specialization (e.g., "fury", "frost")
     * @param characterLevel The character's level
     * @param characterRace The character's race (e.g., "human", "orc")
     * @param gear Optional gear set to include in the profile
     * @return A SimC-formatted profile string
     */
    fun generateProfile(
        characterName: String,
        characterRealm: String,
        characterClass: String,
        characterSpec: String,
        characterLevel: Int,
        characterRace: String,
        gear: GearSet?
    ): String {
        val builder = StringBuilder()

        // Character header
        builder.appendLine("""$characterClass="$characterName"""")
        builder.appendLine("level=$characterLevel")
        builder.appendLine("race=${characterRace.lowercase()}")
        builder.appendLine("spec=${characterSpec.lowercase()}")
        builder.appendLine()

        // Add gear if provided
        if (gear != null && gear.items.isNotEmpty()) {
            builder.appendLine("# Gear")
            for ((slot, item) in gear.items) {
                val simcSlot = slotMapping[slot] ?: continue
                builder.appendLine("$simcSlot=,id=${item.itemId.value},ilevel=${item.itemLevel}")
            }
        }

        return builder.toString().trimEnd()
    }

    /**
     * Generates a minimal profile for quick simulations.
     *
     * This is useful for testing or when full gear data is not available.
     *
     * @param characterName The character's name
     * @param characterClass The character's class
     * @param characterSpec The character's specialization
     * @return A minimal SimC profile string
     */
    fun generateMinimalProfile(
        characterName: String,
        characterClass: String,
        characterSpec: String
    ): String {
        return """
            |$characterClass="$characterName"
            |level=80
            |race=human
            |spec=${characterSpec.lowercase()}
        """.trimMargin()
    }
}
