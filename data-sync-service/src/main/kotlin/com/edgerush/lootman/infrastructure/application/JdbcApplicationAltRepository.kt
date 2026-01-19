package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.lootman.domain.application.repository.ApplicationAltRepository
import com.edgerush.lootman.infrastructure.springdata.ApplicationAltEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of ApplicationAltRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcApplicationAltRepository(
    private val springRepository: ApplicationAltEntitySpringRepository,
) : ApplicationAltRepository {

    override fun findById(id: Long): ApplicationAltEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<ApplicationAltEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByApplicationId(applicationId: Long, offset: Long, limit: Int): List<ApplicationAltEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("id"),
        )
        return springRepository.findByApplicationId(applicationId, pageRequest).content
    }

    override fun countByApplicationId(applicationId: Long): Long =
        springRepository.countByApplicationId(applicationId)

    override fun save(entity: ApplicationAltEntity): ApplicationAltEntity =
        springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
