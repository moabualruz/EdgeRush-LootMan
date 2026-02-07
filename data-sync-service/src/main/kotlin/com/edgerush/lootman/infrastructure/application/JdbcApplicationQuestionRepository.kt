package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionRepository
import com.edgerush.lootman.infrastructure.springdata.ApplicationQuestionEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of ApplicationQuestionRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcApplicationQuestionRepository(
    private val springRepository: ApplicationQuestionEntitySpringRepository,
) : ApplicationQuestionRepository {
    override fun findById(id: Long): ApplicationQuestionEntity? = springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean = springRepository.existsById(id)

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationQuestionEntity> {
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
    ): List<ApplicationQuestionEntity> {
        val pageRequest =
            PageRequest.of(
                (offset / limit).toInt(),
                limit,
                Sort.by("position"),
            )
        return springRepository.findByApplicationId(applicationId, pageRequest).content
    }

    override fun countByApplicationId(applicationId: Long): Long = springRepository.countByApplicationId(applicationId)

    override fun save(entity: ApplicationQuestionEntity): ApplicationQuestionEntity = springRepository.save(entity)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
