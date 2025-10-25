package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LootAwardWishDataRepository : CrudRepository<LootAwardWishDataEntity, Long> {
    fun deleteByLootAwardId(lootAwardId: Long)
}
