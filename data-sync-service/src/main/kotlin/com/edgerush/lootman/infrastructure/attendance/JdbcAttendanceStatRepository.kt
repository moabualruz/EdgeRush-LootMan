package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.domain.attendance.repository.AttendanceStatRepository
import com.edgerush.lootman.infrastructure.springdata.AttendanceStatEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of AttendanceStatRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcAttendanceStatRepository(
    private val springRepository: AttendanceStatEntitySpringRepository,
) : AttendanceStatRepository {

    override fun findById(id: Long): AttendanceStatEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<AttendanceStatEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findByCharacterId(characterId, pageRequest).content
    }

    override fun countByCharacterId(characterId: Long): Long =
        springRepository.countByCharacterId(characterId)

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun findBySeasonId(seasonId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt").and(Sort.by("id")),
        )
        return springRepository.findBySeasonId(seasonId, pageRequest).content
    }

    override fun countBySeasonId(seasonId: Long): Long =
        springRepository.countBySeasonId(seasonId)

    override fun save(entity: AttendanceStatEntity): AttendanceStatEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
