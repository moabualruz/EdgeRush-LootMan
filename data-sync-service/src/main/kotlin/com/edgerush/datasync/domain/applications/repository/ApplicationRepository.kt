package com.edgerush.datasync.domain.applications.repository

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.model.ApplicationId
import com.edgerush.datasync.domain.applications.model.ApplicationStatus

/**
 * Repository interface for Application aggregate
 */
interface ApplicationRepository {
    fun findById(id: ApplicationId): Application?
    fun findByStatus(status: ApplicationStatus): List<Application>
    fun findAll(): List<Application>
    fun save(application: Application): Application
    fun delete(id: ApplicationId)
}
