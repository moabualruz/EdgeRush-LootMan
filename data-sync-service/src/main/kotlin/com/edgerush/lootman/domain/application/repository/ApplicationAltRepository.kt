package com.edgerush.lootman.domain.application.repository

import com.edgerush.datasync.entity.ApplicationAltEntity

interface ApplicationAltRepository {
    fun findById(id: Long): ApplicationAltEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<ApplicationAltEntity>

    fun count(): Long

    fun findByApplicationId(
        applicationId: Long,
        offset: Long,
        limit: Int,
    ): List<ApplicationAltEntity>

    fun countByApplicationId(applicationId: Long): Long

    fun save(entity: ApplicationAltEntity): ApplicationAltEntity

    fun delete(id: Long)
}
