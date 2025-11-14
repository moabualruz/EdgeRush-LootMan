package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.model.ApplicationStatus
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving guild applications
 */
@Service
class GetApplicationsUseCase(
    private val applicationRepository: ApplicationRepository
) {

    fun execute(query: GetApplicationQuery): Result<Application> = runCatching {
        applicationRepository.findById(query.applicationId)
            ?: throw ApplicationNotFoundException(query.applicationId)
    }

    fun execute(query: GetApplicationsByStatusQuery): Result<List<Application>> = runCatching {
        if (query.status != null) {
            val status = ApplicationStatus.fromString(query.status)
            applicationRepository.findByStatus(status)
        } else {
            applicationRepository.findAll()
        }
    }
}
