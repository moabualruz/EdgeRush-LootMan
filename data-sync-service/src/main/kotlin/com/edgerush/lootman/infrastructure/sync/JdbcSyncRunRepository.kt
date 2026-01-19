package com.edgerush.lootman.infrastructure.sync

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import com.edgerush.lootman.infrastructure.springdata.SyncRunEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of SyncRunRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcSyncRunRepository(
    private val springRepository: SyncRunEntitySpringRepository,
) : SyncRunRepository {

    override fun findById(id: Long): SyncRunEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<SyncRunEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "startedAt").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findBySource(source: String, offset: Long, limit: Int): List<SyncRunEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "startedAt").and(Sort.by("id")),
        )
        return springRepository.findBySource(source, pageRequest).content
    }

    override fun countBySource(source: String): Long =
        springRepository.countBySource(source)

    override fun findByStatus(status: String, offset: Long, limit: Int): List<SyncRunEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "startedAt").and(Sort.by("id")),
        )
        return springRepository.findByStatus(status, pageRequest).content
    }

    override fun countByStatus(status: String): Long =
        springRepository.countByStatus(status)

    override fun save(entity: SyncRunEntity): SyncRunEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
