package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.RaidRole
import com.edgerush.datasync.domain.raids.model.RaidSignup
import com.edgerush.datasync.domain.raids.model.RaiderId
import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.stereotype.Component

/**
 * Mapper for converting between RaidSignup domain model and RaidSignupEntity.
 */
@Component
class RaidSignupMapper {
    
    fun toDomain(entity: RaidSignupEntity): RaidSignup {
        val raiderId = RaiderId(entity.characterId ?: 0L)
        val role = RaidRole.fromString(entity.characterRole) ?: RaidRole.DPS
        val status = RaidSignup.SignupStatus.fromString(entity.status) ?: RaidSignup.SignupStatus.CONFIRMED
        val selected = entity.selected ?: false
        
        return if (entity.id != null) {
            RaidSignup.reconstitute(
                id = entity.id,
                raiderId = raiderId,
                role = role,
                status = status,
                comment = entity.comment,
                selected = selected
            )
        } else {
            val signup = RaidSignup.create(
                raiderId = raiderId,
                role = role,
                status = status,
                comment = entity.comment
            )
            if (selected) signup.select() else signup
        }
    }
    
    fun toEntity(signup: RaidSignup, raidId: Long): RaidSignupEntity {
        return RaidSignupEntity(
            id = signup.id,
            raidId = raidId,
            characterId = signup.raiderId.value,
            characterName = null,
            characterRealm = null,
            characterRegion = null,
            characterClass = null,
            characterRole = signup.role.name,
            characterGuest = null,
            status = signup.status.name,
            comment = signup.comment,
            selected = signup.selected
        )
    }
    
    fun toEntities(signups: List<RaidSignup>, raidId: Long): List<RaidSignupEntity> {
        return signups.map { toEntity(it, raidId) }
    }
    
    fun toDomainList(entities: List<RaidSignupEntity>): List<RaidSignup> {
        return entities.map { toDomain(it) }
    }
}
