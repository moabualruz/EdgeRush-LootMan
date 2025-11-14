package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaiderEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RaiderRepository : CrudRepository<RaiderEntity, Long>, PagingAndSortingRepository<RaiderEntity, Long> {
    fun findByCharacterNameAndRealm(
        name: String,
        realm: String,
    ): Optional<RaiderEntity>

    fun findByWowauditId(wowauditId: Long): Optional<RaiderEntity>
}
