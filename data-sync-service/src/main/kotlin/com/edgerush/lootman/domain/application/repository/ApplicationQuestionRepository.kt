package com.edgerush.lootman.domain.application.repository

import com.edgerush.datasync.entity.ApplicationQuestionEntity

interface ApplicationQuestionRepository {
    fun findById(id: Long): ApplicationQuestionEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationQuestionEntity>

    fun count(): Long

    fun findByApplicationId(
        applicationId: Long,
        offset: Long,
        limit: Int,
    ): List<ApplicationQuestionEntity>

    fun countByApplicationId(applicationId: Long): Long

    fun save(entity: ApplicationQuestionEntity): ApplicationQuestionEntity

    fun delete(id: Long)
}
