package com.edgerush.lootman.domain.application.model

/**
 * Enumeration of application statuses.
 */
enum class ApplicationStatus {
    /** Application submitted and awaiting review */
    PENDING,

    /** Application is currently under review by officers */
    UNDER_REVIEW,

    /** Application has been approved */
    APPROVED,

    /** Application has been rejected */
    REJECTED,

    /** Application was withdrawn by the applicant */
    WITHDRAWN
}
