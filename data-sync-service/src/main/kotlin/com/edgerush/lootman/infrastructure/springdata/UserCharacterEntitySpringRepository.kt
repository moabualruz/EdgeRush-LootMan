package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.UserCharacterEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserCharacterEntitySpringRepository :
    CrudRepository<UserCharacterEntity, Long>,
    PagingAndSortingRepository<UserCharacterEntity, Long> {

    fun findByUserId(userId: Long): List<UserCharacterEntity>
    fun findByUserIdOrderByLevelDescCharacterNameAsc(userId: Long): List<UserCharacterEntity>
    fun deleteAllByUserId(userId: Long)
    fun findByUserIdAndCharacterNameIgnoreCaseAndRealmIgnoreCase(
        userId: Long,
        characterName: String,
        realm: String,
    ): UserCharacterEntity?
}
