package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WishlistSnapshotRepository : CrudRepository<WishlistSnapshotEntity, Long> {
    fun deleteByCharacterNameAndCharacterRealm(
        characterName: String,
        characterRealm: String,
    )
}
