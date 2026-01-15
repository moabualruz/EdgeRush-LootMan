package com.edgerush.lootman.application.gear

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.EquipmentSlot
import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.GearSetType
import com.edgerush.lootman.domain.shared.model.ItemQuality
import com.edgerush.lootman.domain.shared.repository.GearRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving a raider's current gear.
 */
@Service
class GetCurrentGearUseCase(
    private val gearRepository: GearRepository,
) {
    fun execute(query: GetCurrentGearQuery): Result<GearSet> =
        runCatching {
            gearRepository.findCurrentGear(RaiderId(query.raiderId))
                ?: throw NoSuchElementException("Gear not found for raider: ${query.raiderId}")
        }
}

/**
 * Use case for retrieving gear by raider and type.
 */
@Service
class GetGearByTypeUseCase(
    private val gearRepository: GearRepository,
) {
    fun execute(query: GetGearByTypeQuery): Result<GearSet> =
        runCatching {
            val gearSetType = GearSetType.valueOf(query.gearSetType.uppercase())
            gearRepository.findByRaiderIdAndType(RaiderId(query.raiderId), gearSetType)
                ?: throw NoSuchElementException("Gear of type ${query.gearSetType} not found for raider: ${query.raiderId}")
        }
}

/**
 * Use case for saving raider gear.
 */
@Service
class SaveGearUseCase(
    private val gearRepository: GearRepository,
) {
    fun execute(command: SaveGearCommand): Result<GearSet> =
        runCatching {
            val gearSetType = GearSetType.valueOf(command.gearSetType.uppercase())
            val items =
                command.items.associate { item ->
                    val slot = EquipmentSlot.valueOf(item.slot.uppercase())
                    val quality = ItemQuality.valueOf(item.quality.uppercase())
                    slot to
                        GearItem(
                            itemId = ItemId(item.itemId),
                            name = item.name,
                            itemLevel = item.itemLevel,
                            quality = quality,
                            slot = slot,
                            isTierPiece = item.isTierPiece,
                            enchant = item.enchant,
                            sockets = item.sockets,
                        )
                }
            val gearSet =
                GearSet(
                    items = items,
                    gearSetType = gearSetType,
                )
            gearRepository.save(RaiderId(command.raiderId), gearSet)
        }
}

// Query and Command classes

data class GetCurrentGearQuery(
    val raiderId: Long,
)

data class GetGearByTypeQuery(
    val raiderId: Long,
    val gearSetType: String,
)

data class SaveGearCommand(
    val raiderId: Long,
    val gearSetType: String,
    val items: List<GearItemCommand>,
)

data class GearItemCommand(
    val itemId: Long,
    val name: String,
    val itemLevel: Int,
    val quality: String,
    val slot: String,
    val isTierPiece: Boolean = false,
    val enchant: String? = null,
    val sockets: Int = 0,
)
