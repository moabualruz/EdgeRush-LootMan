package com.edgerush.lootman.infrastructure.activity

import com.edgerush.datasync.entity.HistoricalActivityEntity
import com.edgerush.lootman.domain.activity.repository.HistoricalActivityRepository
import com.edgerush.lootman.infrastructure.springdata.HistoricalActivityEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of HistoricalActivityRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcHistoricalActivityRepository(
    private val springRepository: HistoricalActivityEntitySpringRepository,
) : HistoricalActivityRepository {

    override fun findById(id: Long): HistoricalActivityEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<HistoricalActivityEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<HistoricalActivityEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findByCharacterId(characterId, pageRequest).content
    }

    override fun countByCharacterId(characterId: Long): Long =
        springRepository.countByCharacterId(characterId)

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<HistoricalActivityEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "syncedAt"),
        )
        return springRepository.findByTeamId(teamId, pageRequest).content
    }

    override fun countByTeamId(teamId: Long): Long =
        springRepository.countByTeamId(teamId)

    override fun save(entity: HistoricalActivityEntity): HistoricalActivityEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
