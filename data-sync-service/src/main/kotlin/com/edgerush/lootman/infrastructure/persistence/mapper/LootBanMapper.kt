package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Mapper for converting between LootBan domain models and database entities.
 */
@Component
class LootBanMapper {
    /**
     * Maps a LootBanEntity to a LootBan domain model.
     *
     * @param entity The database entity
     * @return LootBan domain model
     */
    fun toDomain(entity: LootBanEntity): LootBan =
        LootBan(
            id = LootBanId(entity.id.toString()),
            raiderId = RaiderId(entity.characterName),
            guildId = GuildId(entity.guildId),
            reason = entity.reason,
            bannedAt = entity.bannedAt.toInstant(ZoneOffset.UTC),
            expiresAt = entity.expiresAt?.toInstant(ZoneOffset.UTC),
        )

    /**
     * Maps a LootBan domain model to a LootBanEntity.
     *
     * @param domain The domain model
     * @return LootBanEntity ready to be persisted
     */
    fun toEntity(domain: LootBan): LootBanEntity =
        LootBanEntity(
            id = domain.id.value.toLongOrNull(),
            guildId = domain.guildId.value,
            characterName = domain.raiderId.value,
            reason = domain.reason,
            bannedBy = "System", // Legacy field, not tracked in new domain model
            bannedAt = LocalDateTime.ofInstant(domain.bannedAt, ZoneOffset.UTC),
            expiresAt = domain.expiresAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) },
            isActive = true,
        )
}
