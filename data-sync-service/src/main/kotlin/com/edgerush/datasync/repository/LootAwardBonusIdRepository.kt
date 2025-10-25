package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LootAwardBonusIdRepository : CrudRepository<LootAwardBonusIdEntity, Long> {
    fun deleteByLootAwardId(lootAwardId: Long)
}
