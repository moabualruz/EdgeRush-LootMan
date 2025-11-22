package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.*
import com.edgerush.lootman.domain.shared.repository.GearRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import jakarta.persistence.*

@Entity
@Table(name = "raider_gear_items")
class GearItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var raiderId: Long = 0,
    var slot: String = "",
    var gearSet: String = "",
    var itemId: Long = 0,
    var name: String = "",
    var itemLevel: Int = 0,
    var quality: Int = 0,
    var enchant: String? = null,
    var sockets: Int = 0
) {
    fun toDomain(): GearItem {
        return GearItem(
            itemId = ItemId(itemId),
            name = name,
            itemLevel = itemLevel,
            quality = ItemQuality.fromInt(quality) ?: ItemQuality.RARE,
            slot = EquipmentSlot.fromString(slot) ?: EquipmentSlot.CHEST,
            isTierPiece = false,
            enchant = enchant,
            sockets = sockets
        )
    }
}

interface GearJpaRepository : JpaRepository<GearItemEntity, Long> {
    fun findByRaiderIdAndGearSet(raiderId: Long, gearSet: String): List<GearItemEntity>
}

@Repository
class JpaGearRepositoryImpl(
    private val jpaRepository: GearJpaRepository
) : GearRepository {

    override fun findCurrentGear(raiderId: RaiderId): GearSet? {
        return findByRaiderIdAndType(raiderId, GearSetType.EQUIPPED)
    }

    override fun findByRaiderIdAndType(raiderId: RaiderId, gearSetType: GearSetType): GearSet? {
        val gearSetName = when (gearSetType) {
            GearSetType.EQUIPPED -> "equipped"
            GearSetType.BEST -> "best"
        }

        val items = jpaRepository.findByRaiderIdAndGearSet(raiderId.value, gearSetName)
            .map { it.toDomain() }

        return if (items.isNotEmpty()) {
            GearSet(
                items = items.associateBy { it.slot },
                gearSetType = gearSetType
            )
        } else {
            null
        }
    }

    override fun save(raiderId: RaiderId, gearSet: GearSet): GearSet {
        throw UnsupportedOperationException("Save not yet implemented")
    }
}
