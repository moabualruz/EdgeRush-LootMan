package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootAwardStatus
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Component
import java.time.ZoneOffset

/**
 * Mapper for converting between LootAward domain models and database entities.
 */
@Component
class LootAwardMapper {
    /**
     * Maps a LootAwardEntity to a LootAward domain model.
     *
     * @param entity The database entity
     * @param guildId The guild identifier (not stored in legacy table)
     * @return LootAward domain model
     */
    fun toDomain(
        entity: LootAwardEntity,
        guildId: GuildId,
    ): LootAward =
        LootAward(
            id = LootAwardId(entity.id.toString()),
            itemId = ItemId(entity.itemId),
            raiderId = RaiderId(entity.raiderId.toString()),
            guildId = guildId,
            awardedAt = entity.awardedAt.toInstant(),
            flpsScore = FlpsScore.of(entity.flps),
            tier = LootTier.valueOf(entity.tier),
            status = LootAwardStatus.ACTIVE, // Legacy table doesn't track status
        )

    /**
     * Maps a LootAward domain model to a LootAwardEntity.
     *
     * @param domain The domain model
     * @return LootAwardEntity ready to be persisted
     */
    fun toEntity(domain: LootAward): LootAwardEntity =
        LootAwardEntity(
            id = domain.id.value.toLongOrNull(),
            raiderId = domain.raiderId.value.toLong(),
            itemId = domain.itemId.value,
            itemName = "", // Not tracked in new domain model
            tier = domain.tier.name,
            flps = domain.flpsScore.value,
            rdf = 1.0, // Legacy field, not used in new model
            awardedAt = domain.awardedAt.atOffset(ZoneOffset.UTC),
            rclootcouncilId = null,
            icon = null,
            slot = null,
            quality = null,
            responseTypeId = null,
            responseTypeName = null,
            responseTypeRgba = null,
            responseTypeExcluded = null,
            propagatedResponseTypeId = null,
            propagatedResponseTypeName = null,
            propagatedResponseTypeRgba = null,
            propagatedResponseTypeExcluded = null,
            sameResponseAmount = null,
            note = null,
            wishValue = null,
            difficulty = null,
            discarded = null,
            characterId = null,
            awardedByCharacterId = null,
            awardedByName = null,
        )
}
