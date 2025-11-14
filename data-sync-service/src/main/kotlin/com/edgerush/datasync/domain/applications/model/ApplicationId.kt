package com.edgerush.datasync.domain.applications.model

/**
 * Value object representing a unique application identifier
 */
@JvmInline
value class ApplicationId(val value: Long) {
    init {
        require(value > 0) { "Application ID must be positive" }
    }

    companion object {
        fun of(value: Long): ApplicationId = ApplicationId(value)
    }
}
