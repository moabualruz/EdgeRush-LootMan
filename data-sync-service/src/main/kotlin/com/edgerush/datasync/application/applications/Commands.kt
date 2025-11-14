package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.ApplicationId

/**
 * Command to review an application
 */
data class ReviewApplicationCommand(
    val applicationId: ApplicationId,
    val action: ReviewAction
)

enum class ReviewAction {
    START_REVIEW,
    APPROVE,
    REJECT
}

/**
 * Command to withdraw an application
 */
data class WithdrawApplicationCommand(
    val applicationId: ApplicationId
)

/**
 * Query to get application by ID
 */
data class GetApplicationQuery(
    val applicationId: ApplicationId
)

/**
 * Query to get applications by status
 */
data class GetApplicationsByStatusQuery(
    val status: String?
)
