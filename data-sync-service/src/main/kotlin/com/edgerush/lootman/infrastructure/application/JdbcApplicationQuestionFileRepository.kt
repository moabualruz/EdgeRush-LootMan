package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionFileRepository
import com.edgerush.lootman.infrastructure.springdata.ApplicationQuestionFileEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of ApplicationQuestionFileRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcApplicationQuestionFileRepository(
    private val springRepository: ApplicationQuestionFileEntitySpringRepository,
) : ApplicationQuestionFileRepository {
    override fun findById(id: Long): ApplicationQuestionFileEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationQuestionFileEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("id"),
            )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long = springRepository.count()

    override fun findByApplicationId(
        applicationId: Long,
        offset: Long,
        limit: Int,
    ): List<ApplicationQuestionFileEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("questionPosition"),
            )
        return springRepository.findByApplicationId(applicationId, pageRequest).content
    }

    override fun countByApplicationId(applicationId: Long): Long = springRepository.countByApplicationId(applicationId)

    override fun save(entity: ApplicationQuestionFileEntity): ApplicationQuestionFileEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
