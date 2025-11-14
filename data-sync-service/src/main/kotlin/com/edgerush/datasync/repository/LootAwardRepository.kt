package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.LootAwardEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface LootAwardRepository : CrudRepository<LootAwardEntity, Long>, PagingAndSortingRepository<LootAwardEntity, Long> {
    fun findByRaiderId(raiderId: Long): List<LootAwardEntity>
}
