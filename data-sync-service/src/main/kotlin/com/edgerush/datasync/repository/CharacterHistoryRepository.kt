package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.CharacterHistoryEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CharacterHistoryRepository : CrudRepository<CharacterHistoryEntity, Long> {
    fun findByCharacterIdAndTeamId(characterId: Long, teamId: Long): CharacterHistoryEntity?
    fun findByCharacterNameAndCharacterRealm(characterName: String, characterRealm: String?): CharacterHistoryEntity?
    fun deleteByCharacterId(characterId: Long)
    fun findAllByTeamId(teamId: Long): List<CharacterHistoryEntity>
}