package com.edgerush.lootman.domain.application.model

import java.util.UUID

/**
 * Value object representing an Application identifier.
 */
data class ApplicationId(val value: String) {
    init {
        require(value.isNotBlank()) { "Application ID cannot be blank" }
    }

    companion object {
        fun generate(): ApplicationId = ApplicationId(UUID.randomUUID().toString())
    }
}
