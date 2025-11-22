package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.ItemId
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loot_awards")
class LootAwardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var itemId: Long = 0,
    var itemName: String = "",
    var itemLevel: Int = 0,
    var characterName: String = "",
    var characterRealm: String = "",
    var awardedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): LootAward {
        val tier = when {
            itemLevel >= 639 -> LootTier.A
            itemLevel >= 626 -> LootTier.B
            else -> LootTier.C
        }
        return LootAward(
            id = LootAwardId(id),
            itemId = ItemId(itemId),
            itemName = itemName,
            itemLevel = itemLevel,
            characterName = characterName,
            characterRealm = characterRealm,
            awardedAt = awardedAt,
            tier = tier
        )
    }
}
