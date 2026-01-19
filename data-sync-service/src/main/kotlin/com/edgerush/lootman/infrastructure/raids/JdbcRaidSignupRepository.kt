package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.lootman.domain.raids.repository.RaidSignupRepository
import com.edgerush.lootman.infrastructure.springdata.RaidSignupEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of RaidSignupRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaidSignupRepository(
    private val springRepository: RaidSignupEntitySpringRepository,
) : RaidSignupRepository {

    override fun findById(id: Long): RaidSignupEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<RaidSignupEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("raidId").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("characterName").and(Sort.by("id")),
        )
        return springRepository.findByRaidId(raidId, pageRequest).content
    }

    override fun countByRaidId(raidId: Long): Long =
        springRepository.countByRaidId(raidId)

    override fun findSelectedByRaidId(raidId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("characterName").and(Sort.by("id")),
        )
        return springRepository.findByRaidIdAndSelectedTrue(raidId, pageRequest).content
    }

    override fun countSelectedByRaidId(raidId: Long): Long =
        springRepository.countByRaidIdAndSelectedTrue(raidId)

    override fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<RaidSignupEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "raidId").and(Sort.by("id")),
        )
        return springRepository.findByCharacterId(characterId, pageRequest).content
    }

    override fun countByCharacterId(characterId: Long): Long =
        springRepository.countByCharacterId(characterId)

    override fun save(signup: RaidSignupEntity): RaidSignupEntity =
        springRepository.save(signup)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
