package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LootAwardOldItemRepository : CrudRepository<LootAwardOldItemEntity, Long> {
    fun deleteByLootAwardId(lootAwardId: Long)
}
