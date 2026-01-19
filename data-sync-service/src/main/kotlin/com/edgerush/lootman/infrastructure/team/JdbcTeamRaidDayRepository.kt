package com.edgerush.lootman.infrastructure.team

import com.edgerush.datasync.entity.TeamRaidDayEntity
import com.edgerush.lootman.domain.team.repository.TeamRaidDayRepository
import com.edgerush.lootman.infrastructure.springdata.TeamRaidDayEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of TeamRaidDayRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcTeamRaidDayRepository(
    private val springRepository: TeamRaidDayEntitySpringRepository,
) : TeamRaidDayRepository {

    override fun findById(id: Long): TeamRaidDayEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<TeamRaidDayEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<TeamRaidDayEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun save(entity: TeamRaidDayEntity): TeamRaidDayEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
