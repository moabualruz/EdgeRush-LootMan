package com.edgerush.datasync.api.dto.response


data class ApplicationAltResponse(
    val id: Long?,
    val applicationId: Long,
    val name: String?,
    val realm: String?,
    val region: String?,
    val `class`: String?,
    val role: String?,
    val level: Int?,
    val faction: String?,
    val race: String?
)
