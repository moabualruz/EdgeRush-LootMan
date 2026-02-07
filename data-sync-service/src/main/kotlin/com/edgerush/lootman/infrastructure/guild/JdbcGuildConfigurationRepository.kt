package com.edgerush.lootman.infrastructure.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.infrastructure.springdata.GuildConfigurationEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of GuildConfigurationRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcGuildConfigurationRepository(
    private val springRepository: GuildConfigurationEntitySpringRepository,
) : GuildConfigurationRepository {
    override fun findById(id: Long): GuildConfigurationEntity? = springRepository.findById(id).orElse(null)

    override fun findByGuildId(guildId: String): GuildConfigurationEntity? = springRepository.findByGuildId(guildId)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<GuildConfigurationEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("guildName").and(Sort.by("id")),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findActive(
        offset: Long,
        limit: Int,
    ): List<GuildConfigurationEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("guildName").and(Sort.by("id")),
            )
        return springRepository.findByIsActiveTrue(pageRequest).content
    }

    override fun countActive(): Long = springRepository.countByIsActiveTrue()

    override fun save(entity: GuildConfigurationEntity): GuildConfigurationEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
