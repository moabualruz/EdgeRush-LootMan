package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.repository.CharacterRepository
import com.edgerush.lootman.infrastructure.springdata.RaiderEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaiderEntityRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaiderEntityRepository(
    private val springRepository: RaiderEntitySpringRepository,
    private val characterRepository: CharacterRepository,
) : RaiderEntityRepository {
    override fun findById(id: Long): RaiderEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "lastSync").and(Sort.by("id")),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByRealm(
        realm: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "lastSync").and(Sort.by("id")),
            )
        return springRepository.findByRealm(realm, pageRequest).content
    }

    override fun countByRealm(realm: String): Long = springRepository.countByRealm(realm)

    override fun findByRegion(
        region: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "lastSync").and(Sort.by("id")),
            )
        return springRepository.findByRegion(region, pageRequest).content
    }

    override fun countByRegion(region: String): Long = springRepository.countByRegion(region)

    override fun save(entity: RaiderEntity): RaiderEntity {
        // Ensure character exists and get character_id
        val characterClass = CharacterClass.fromString(entity.clazz)
        val characterId =
            entity.characterId ?: characterRepository.getOrCreateCharacterId(
                name = entity.characterName,
                realm = entity.realm,
                region = entity.region,
                characterClass = characterClass,
            ).value

        val entityWithCharacterId =
            if (entity.characterId == null) {
                entity.copy(characterId = characterId)
            } else {
                entity
            }

        return springRepository.save(entityWithCharacterId)
    }

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }

    override fun findByCharacterNameAndRealm(
        characterName: String,
        realm: String,
    ): RaiderEntity? = springRepository.findByCharacterNameAndRealm(characterName, realm)

    override fun findByCharacterNameAndRealmNormalized(
        characterName: String,
        realm: String,
    ): RaiderEntity? = springRepository.findByCharacterNameAndRealmNormalized(characterName, realm)

    override fun findByBlizzardId(blizzardId: Long): RaiderEntity? = springRepository.findByBlizzardId(blizzardId)

    override fun findByWowauditId(wowauditId: Long): RaiderEntity? = springRepository.findByWowauditId(wowauditId)

    override fun findByWowauditIds(wowauditIds: List<Long>): List<RaiderEntity> {
        if (wowauditIds.isEmpty()) return emptyList()
        return springRepository.findByWowauditIdIn(wowauditIds)
    }

    override fun findByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "lastSync").and(Sort.by("id")),
            )
        return springRepository.findByGuildId(guildId, pageRequest).content
    }

    override fun countByGuildId(guildId: String): Long = springRepository.countByGuildId(guildId)
}
