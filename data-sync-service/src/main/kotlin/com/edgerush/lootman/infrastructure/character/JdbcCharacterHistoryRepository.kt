package com.edgerush.lootman.infrastructure.character

import com.edgerush.datasync.entity.CharacterHistoryEntity
import com.edgerush.lootman.domain.character.repository.CharacterHistoryRepository
import com.edgerush.lootman.infrastructure.springdata.CharacterHistoryEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of CharacterHistoryRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcCharacterHistoryRepository(
    private val springRepository: CharacterHistoryEntitySpringRepository,
) : CharacterHistoryRepository {
    override fun findById(id: Long): CharacterHistoryEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByCharacterId(
        characterId: Long,
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
            )
        return springRepository.findByCharacterId(characterId, pageRequest).content
    }

    override fun countByCharacterId(characterId: Long): Long = springRepository.countByCharacterId(characterId)

    override fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<CharacterHistoryEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
            )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long = springRepository.countByTeamId(teamId)

    override fun save(entity: CharacterHistoryEntity): CharacterHistoryEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
