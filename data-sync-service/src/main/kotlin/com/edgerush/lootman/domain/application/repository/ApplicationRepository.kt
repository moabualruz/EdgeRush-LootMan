package com.edgerush.lootman.domain.application.repository

import com.edgerush.datasync.entity.ApplicationEntity

interface ApplicationRepository {
    fun findById(id: Long): ApplicationEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<ApplicationEntity>
    fun count(): Long
    fun findByStatus(status: String, offset: Long, limit: Int): List<ApplicationEntity>
    fun countByStatus(status: String): Long
    fun save(entity: ApplicationEntity): ApplicationEntity
    fun delete(id: Long)
}
