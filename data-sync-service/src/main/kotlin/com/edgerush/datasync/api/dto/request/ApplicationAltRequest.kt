package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateApplicationAltRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,

    val name: String? = null,

    val realm: String? = null,

    val region: String? = null,

    val `class`: String? = null,

    val role: String? = null,

    val level: Int? = null,

    val faction: String? = null,

    val race: String? = null
)

data class UpdateApplicationAltRequest(
    @field:Min(value = 0, message = "Application id must be positive")
    val applicationId: Long? = null,

    val name: String? = null,

    val realm: String? = null,

    val region: String? = null,

    val `class`: String? = null,

    val role: String? = null,

    val level: Int? = null,

    val faction: String? = null,

    val race: String? = null
)
