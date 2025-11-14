package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.applications.model.CharacterInfo
import com.edgerush.datasync.entity.ApplicationAltEntity
import org.springframework.stereotype.Component

/**
 * Mapper between CharacterInfo domain model and ApplicationAltEntity
 */
@Component
class ApplicationAltMapper {

    fun toDomain(entity: ApplicationAltEntity): CharacterInfo {
        return CharacterInfo(
            name = entity.name ?: "",
            realm = entity.realm ?: "",
            region = entity.region ?: "",
            characterClass = entity.`class` ?: "",
            role = entity.role ?: "",
            race = entity.race,
            faction = entity.faction,
            level = entity.level
        )
    }

    fun toEntity(characterInfo: CharacterInfo, applicationId: Long): ApplicationAltEntity {
        return ApplicationAltEntity(
            id = null,
            applicationId = applicationId,
            name = characterInfo.name,
            realm = characterInfo.realm,
            region = characterInfo.region,
            `class` = characterInfo.characterClass,
            role = characterInfo.role,
            race = characterInfo.race,
            faction = characterInfo.faction,
            level = characterInfo.level
        )
    }
}
