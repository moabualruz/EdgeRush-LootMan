package com.edgerush.lootman.infrastructure.snapshot

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.lootman.domain.snapshot.repository.PeriodSnapshotRepository
import com.edgerush.lootman.infrastructure.springdata.PeriodSnapshotEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of PeriodSnapshotRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcPeriodSnapshotRepository(
    private val springRepository: PeriodSnapshotEntitySpringRepository,
) : PeriodSnapshotRepository {

    override fun findById(id: Long): PeriodSnapshotEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<PeriodSnapshotEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "fetchedAt"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<PeriodSnapshotEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "fetchedAt"),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun save(entity: PeriodSnapshotEntity): PeriodSnapshotEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
