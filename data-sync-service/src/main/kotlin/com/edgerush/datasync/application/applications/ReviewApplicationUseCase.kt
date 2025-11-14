package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.Application
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import com.edgerush.datasync.domain.applications.service.ApplicationReviewService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use case for reviewing guild applications
 */
@Service
class ReviewApplicationUseCase(
    private val applicationRepository: ApplicationRepository,
    private val reviewService: ApplicationReviewService
) {

    @Transactional
    fun execute(command: ReviewApplicationCommand): Result<Application> = runCatching {
        val application = applicationRepository.findById(command.applicationId)
            ?: throw ApplicationNotFoundException(command.applicationId)

        val updatedApplication = when (command.action) {
            ReviewAction.START_REVIEW -> application.startReview()
            ReviewAction.APPROVE -> {
                // Verify application meets requirements before approval
                if (!reviewService.meetsBasicRequirements(application)) {
                    throw ApplicationRequirementsNotMetException(command.applicationId)
                }
                application.approve()
            }
            ReviewAction.REJECT -> application.reject()
        }

        applicationRepository.save(updatedApplication)
    }
}

class ApplicationNotFoundException(val applicationId: com.edgerush.datasync.domain.applications.model.ApplicationId) :
    Exception("Application not found: ${applicationId.value}")

class ApplicationRequirementsNotMetException(val applicationId: com.edgerush.datasync.domain.applications.model.ApplicationId) :
    Exception("Application ${applicationId.value} does not meet basic requirements for approval")
