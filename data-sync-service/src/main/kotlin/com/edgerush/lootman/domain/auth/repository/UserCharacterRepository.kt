package com.edgerush.lootman.domain.auth.repository

import com.edgerush.lootman.domain.auth.model.UserCharacter
import com.edgerush.lootman.domain.auth.model.UserId

interface UserCharacterRepository {
    fun save(character: UserCharacter): UserCharacter

    fun saveAll(characters: List<UserCharacter>): List<UserCharacter>

    fun findAllByUserId(userId: UserId): List<UserCharacter>

    fun deleteAllByUserId(userId: UserId)
}
