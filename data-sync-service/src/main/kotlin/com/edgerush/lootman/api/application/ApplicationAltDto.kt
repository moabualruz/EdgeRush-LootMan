package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationAltEntity
import jakarta.validation.constraints.NotNull

data class CreateApplicationAltRequest(
    @field:NotNull(message = "Application ID is required")
    val applicationId: Long,
    val name: String? = null,
    val realm: String? = null,
    val region: String? = null,
    val clazz: String? = null,
    val role: String? = null,
    val level: Int? = null,
    val faction: String? = null,
    val race: String? = null,
)

data class UpdateApplicationAltRequest(
    val name: String? = null,
    val realm: String? = null,
    val region: String? = null,
    val clazz: String? = null,
    val role: String? = null,
    val level: Int? = null,
)

data class ApplicationAltResponse(
    val id: Long,
    val applicationId: Long,
    val name: String?,
    val realm: String?,
    val region: String?,
    val clazz: String?,
    val role: String?,
    val level: Int?,
    val faction: String?,
    val race: String?,
) {
    companion object {
        fun from(e: ApplicationAltEntity) = ApplicationAltResponse(
            e.id!!, e.applicationId, e.name, e.realm, e.region, e.clazz, e.role, e.level, e.faction, e.race
        )
    }
}

data class ApplicationAltExistsResponse(val exists: Boolean)
