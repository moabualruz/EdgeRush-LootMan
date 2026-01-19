package com.edgerush.lootman.infrastructure.team

import com.edgerush.datasync.entity.TeamMetadataEntity
import com.edgerush.lootman.domain.team.repository.TeamMetadataRepository
import com.edgerush.lootman.infrastructure.springdata.TeamMetadataEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of TeamMetadataRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcTeamMetadataRepository(
    private val springRepository: TeamMetadataEntitySpringRepository,
) : TeamMetadataRepository {

    override fun findById(teamId: Long): TeamMetadataEntity? =
        springRepository.findByTeamId(teamId)

    override fun existsById(teamId: Long): Boolean =
        springRepository.existsByTeamId(teamId)

    override fun findAll(offset: Long, limit: Int): List<TeamMetadataEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("teamId")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByGuildId(guildId: Long, offset: Long, limit: Int): List<TeamMetadataEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("teamId")),
        )
        return springRepository.findByGuildId(guildId, pageRequest).content
    }

    override fun countByGuildId(guildId: Long): Long =
        springRepository.countByGuildId(guildId)

    override fun findByRegion(region: String, offset: Long, limit: Int): List<TeamMetadataEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("teamId")),
        )
        return springRepository.findByRegion(region, pageRequest).content
    }

    override fun countByRegion(region: String): Long =
        springRepository.countByRegion(region)

    override fun save(entity: TeamMetadataEntity): TeamMetadataEntity =
        springRepository.save(entity)

    override fun delete(teamId: Long) {
        springRepository.deleteByTeamId(teamId)
    }
}
