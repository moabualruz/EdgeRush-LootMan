package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.lootman.domain.flps.repository.FlpsGuildModifierRepository
import com.edgerush.lootman.infrastructure.springdata.FlpsGuildModifierEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of FlpsGuildModifierRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcFlpsGuildModifierRepository(
    private val springRepository: FlpsGuildModifierEntitySpringRepository,
) : FlpsGuildModifierRepository {

    override fun findById(id: Long): FlpsGuildModifierEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<FlpsGuildModifierEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("guildId", "category", "modifierKey"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByGuildId(guildId: String, offset: Long, limit: Int): List<FlpsGuildModifierEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("category", "modifierKey"),
        )
        return springRepository.findByGuildId(guildId, pageRequest).content
    }

    override fun countByGuildId(guildId: String): Long =
        springRepository.countByGuildId(guildId)

    override fun findByGuildIdAndCategory(
        guildId: String,
        category: String,
        offset: Long,
        limit: Int,
    ): List<FlpsGuildModifierEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("modifierKey"),
        )
        return springRepository.findByGuildIdAndCategory(guildId, category, pageRequest).content
    }

    override fun countByGuildIdAndCategory(guildId: String, category: String): Long =
        springRepository.countByGuildIdAndCategory(guildId, category)

    override fun save(modifier: FlpsGuildModifierEntity): FlpsGuildModifierEntity =
        springRepository.save(modifier)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
