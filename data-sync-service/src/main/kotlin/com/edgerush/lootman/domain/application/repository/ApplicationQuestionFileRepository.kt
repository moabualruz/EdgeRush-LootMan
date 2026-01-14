package com.edgerush.lootman.domain.application.repository

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity

interface ApplicationQuestionFileRepository {
    fun findById(id: Long): ApplicationQuestionFileEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<ApplicationQuestionFileEntity>
    fun count(): Long
    fun findByApplicationId(applicationId: Long, offset: Long, limit: Int): List<ApplicationQuestionFileEntity>
    fun countByApplicationId(applicationId: Long): Long
    fun save(entity: ApplicationQuestionFileEntity): ApplicationQuestionFileEntity
    fun delete(id: Long)
}
