package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.lootman.domain.raids.repository.RaidRepository
import com.edgerush.lootman.infrastructure.springdata.RaidEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Implementation of RaidRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcRaidRepository(
    private val springRepository: RaidEntitySpringRepository,
) : RaidRepository {

    override fun findById(raidId: Long): RaidEntity? =
        springRepository.findByRaidId(raidId)

    override fun existsById(raidId: Long): Boolean =
        springRepository.existsByRaidId(raidId)

    override fun findAll(offset: Long, limit: Int): List<RaidEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "raidId")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<RaidEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "raidId")),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        offset: Long,
        limit: Int,
    ): List<RaidEntity> {
        // Spring Data JDBC doesn't support Page with @Query, so we fetch all and paginate manually
        return springRepository.findByDateBetween(startDate, endDate)
            .drop(offset.toInt())
            .take(limit)
    }

    override fun countByDateRange(startDate: LocalDate, endDate: LocalDate): Long =
        springRepository.countByDateBetween(startDate, endDate)

    override fun save(raid: RaidEntity): RaidEntity =
        springRepository.save(raid)

    override fun delete(raidId: Long) {
        springRepository.deleteByRaidId(raidId)
    }

    override fun findUpcomingByGuildId(guildId: Long, limit: Int): List<RaidEntity> =
        springRepository.findUpcomingByGuildId(guildId, limit)

    override fun findPastByGuildId(guildId: Long, limit: Int): List<RaidEntity> =
        springRepository.findPastByGuildId(guildId, limit)
}
