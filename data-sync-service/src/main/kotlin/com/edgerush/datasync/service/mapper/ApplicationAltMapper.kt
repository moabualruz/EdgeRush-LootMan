package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateApplicationAltRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationAltRequest
import com.edgerush.datasync.api.dto.response.ApplicationAltResponse
import com.edgerush.datasync.entity.ApplicationAltEntity
import org.springframework.stereotype.Component

@Component
class ApplicationAltMapper {
    fun toEntity(request: CreateApplicationAltRequest): ApplicationAltEntity {
        return ApplicationAltEntity(
            id = null,
            applicationId = request.applicationId ?: 0L,
            name = request.name,
            realm = request.realm,
            region = request.region,
            `class` = request.`class`,
            role = request.role,
            level = request.level,
            faction = request.faction,
            race = request.race,
        )
    }

    fun updateEntity(
        entity: ApplicationAltEntity,
        request: UpdateApplicationAltRequest,
    ): ApplicationAltEntity {
        return entity.copy(
            applicationId = request.applicationId ?: entity.applicationId,
            name = request.name ?: entity.name,
            realm = request.realm ?: entity.realm,
            region = request.region ?: entity.region,
            `class` = request.`class` ?: entity.`class`,
            role = request.role ?: entity.role,
            level = request.level ?: entity.level,
            faction = request.faction ?: entity.faction,
            race = request.race ?: entity.race,
        )
    }

    fun toResponse(entity: ApplicationAltEntity): ApplicationAltResponse {
        return ApplicationAltResponse(
            id = entity.id!!,
            applicationId = entity.applicationId,
            name = entity.name!!,
            realm = entity.realm!!,
            region = entity.region!!,
            `class` = entity.`class`!!,
            role = entity.role!!,
            level = entity.level!!,
            faction = entity.faction!!,
            race = entity.race!!,
        )
    }
}
