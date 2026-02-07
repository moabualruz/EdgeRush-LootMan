package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.lootman.domain.application.repository.ApplicationRepository
import com.edgerush.lootman.infrastructure.springdata.ApplicationEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of ApplicationRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcApplicationRepository(
    private val springRepository: ApplicationEntitySpringRepository,
) : ApplicationRepository {
    override fun findById(id: Long): ApplicationEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "appliedAt"),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByStatus(
        status: String,
        offset: Long,
        limit: Int,
    ): List<ApplicationEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by(Sort.Direction.DESC, "appliedAt"),
            )
        return springRepository.findByStatus(status, pageRequest).content
    }

    override fun countByStatus(status: String): Long = springRepository.countByStatus(status)

    override fun save(entity: ApplicationEntity): ApplicationEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
