package com.edgerush.lootman.infrastructure.behavioral

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.lootman.domain.behavioral.repository.BehavioralActionRepository
import com.edgerush.lootman.infrastructure.springdata.BehavioralActionEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Implementation of BehavioralActionRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcBehavioralActionRepository(
    private val springRepository: BehavioralActionEntitySpringRepository,
) : BehavioralActionRepository {

    override fun findById(id: Long): BehavioralActionEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<BehavioralActionEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "appliedAt").and(Sort.by("id")),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByGuildId(guildId: String, offset: Long, limit: Int): List<BehavioralActionEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "appliedAt").and(Sort.by("id")),
        )
        return springRepository.findByGuildId(guildId, pageRequest).content
    }

    override fun countByGuildId(guildId: String): Long =
        springRepository.countByGuildId(guildId)

    override fun findActiveByGuildId(guildId: String, offset: Long, limit: Int): List<BehavioralActionEntity> {
        // Spring Data JDBC doesn't support Page with @Query, so we fetch all and paginate manually
        return springRepository.findActiveByGuildId(guildId, LocalDateTime.now())
            .drop(offset.toInt())
            .take(limit)
    }

    override fun countActiveByGuildId(guildId: String): Long =
        springRepository.countActiveByGuildId(guildId, LocalDateTime.now())

    override fun findByCharacter(
        guildId: String,
        characterName: String,
        offset: Long,
        limit: Int,
    ): List<BehavioralActionEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by(Sort.Direction.DESC, "appliedAt").and(Sort.by("id")),
        )
        return springRepository.findByGuildIdAndCharacterName(guildId, characterName, pageRequest).content
    }

    override fun countByCharacter(guildId: String, characterName: String): Long =
        springRepository.countByGuildIdAndCharacterName(guildId, characterName)

    override fun getTotalActiveDeduction(guildId: String, characterName: String): Double {
        val total = springRepository.getTotalActiveDeduction(guildId, characterName, LocalDateTime.now())
        return maxOf(0.0, minOf(1.0, total))
    }

    override fun save(entity: BehavioralActionEntity): BehavioralActionEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
