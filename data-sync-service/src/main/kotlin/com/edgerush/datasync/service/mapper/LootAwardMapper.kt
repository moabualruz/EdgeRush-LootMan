package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateLootAwardRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardRequest
import com.edgerush.datasync.api.dto.response.LootAwardResponse
import com.edgerush.datasync.entity.LootAwardEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class LootAwardMapper {
    fun toEntity(request: CreateLootAwardRequest): LootAwardEntity {
        return LootAwardEntity(
            id = null,
            raiderId = request.raiderId ?: 0L,
            itemId = request.itemId ?: 0L,
            itemName = request.itemName ?: "",
            tier = request.tier ?: "",
            flps = request.flps ?: 0.0,
            rdf = request.rdf ?: 0.0,
            awardedAt = request.awardedAt ?: OffsetDateTime.now(),
            rclootcouncilId = request.rclootcouncilId,
            icon = request.icon,
            slot = request.slot,
            quality = request.quality,
            responseTypeId = request.responseTypeId,
            responseTypeName = request.responseTypeName,
            responseTypeRgba = request.responseTypeRgba,
            responseTypeExcluded = request.responseTypeExcluded,
            propagatedResponseTypeId = request.propagatedResponseTypeId,
            propagatedResponseTypeName = request.propagatedResponseTypeName,
            propagatedResponseTypeRgba = request.propagatedResponseTypeRgba,
            propagatedResponseTypeExcluded = request.propagatedResponseTypeExcluded,
            sameResponseAmount = request.sameResponseAmount,
            note = request.note,
            wishValue = request.wishValue,
            difficulty = request.difficulty,
            discarded = request.discarded,
            characterId = request.characterId,
            awardedByCharacterId = request.awardedByCharacterId,
            awardedByName = request.awardedByName,
        )
    }

    fun updateEntity(
        entity: LootAwardEntity,
        request: UpdateLootAwardRequest,
    ): LootAwardEntity {
        return entity.copy(
            raiderId = request.raiderId ?: entity.raiderId,
            itemId = request.itemId ?: entity.itemId,
            itemName = request.itemName ?: entity.itemName,
            tier = request.tier ?: entity.tier,
            flps = request.flps ?: entity.flps,
            rdf = request.rdf ?: entity.rdf,
            awardedAt = request.awardedAt ?: entity.awardedAt,
            rclootcouncilId = request.rclootcouncilId ?: entity.rclootcouncilId,
            icon = request.icon ?: entity.icon,
            slot = request.slot ?: entity.slot,
            quality = request.quality ?: entity.quality,
            responseTypeId = request.responseTypeId ?: entity.responseTypeId,
            responseTypeName = request.responseTypeName ?: entity.responseTypeName,
            responseTypeRgba = request.responseTypeRgba ?: entity.responseTypeRgba,
            responseTypeExcluded = request.responseTypeExcluded ?: entity.responseTypeExcluded,
            propagatedResponseTypeId = request.propagatedResponseTypeId ?: entity.propagatedResponseTypeId,
            propagatedResponseTypeName = request.propagatedResponseTypeName ?: entity.propagatedResponseTypeName,
            propagatedResponseTypeRgba = request.propagatedResponseTypeRgba ?: entity.propagatedResponseTypeRgba,
            propagatedResponseTypeExcluded = request.propagatedResponseTypeExcluded ?: entity.propagatedResponseTypeExcluded,
            sameResponseAmount = request.sameResponseAmount ?: entity.sameResponseAmount,
            note = request.note ?: entity.note,
            wishValue = request.wishValue ?: entity.wishValue,
            difficulty = request.difficulty ?: entity.difficulty,
            discarded = request.discarded ?: entity.discarded,
            characterId = request.characterId ?: entity.characterId,
            awardedByCharacterId = request.awardedByCharacterId ?: entity.awardedByCharacterId,
            awardedByName = request.awardedByName ?: entity.awardedByName,
        )
    }

    fun toResponse(entity: LootAwardEntity): LootAwardResponse {
        return LootAwardResponse(
            id = entity.id!!,
            raiderId = entity.raiderId,
            itemId = entity.itemId,
            itemName = entity.itemName,
            tier = entity.tier,
            flps = entity.flps,
            rdf = entity.rdf,
            awardedAt = entity.awardedAt,
            rclootcouncilId = entity.rclootcouncilId!!,
            icon = entity.icon!!,
            slot = entity.slot!!,
            quality = entity.quality!!,
            responseTypeId = entity.responseTypeId!!,
            responseTypeName = entity.responseTypeName!!,
            responseTypeRgba = entity.responseTypeRgba!!,
            responseTypeExcluded = entity.responseTypeExcluded!!,
            propagatedResponseTypeId = entity.propagatedResponseTypeId!!,
            propagatedResponseTypeName = entity.propagatedResponseTypeName!!,
            propagatedResponseTypeRgba = entity.propagatedResponseTypeRgba!!,
            propagatedResponseTypeExcluded = entity.propagatedResponseTypeExcluded!!,
            sameResponseAmount = entity.sameResponseAmount!!,
            note = entity.note!!,
            wishValue = entity.wishValue!!,
            difficulty = entity.difficulty!!,
            discarded = entity.discarded!!,
            characterId = entity.characterId!!,
            awardedByCharacterId = entity.awardedByCharacterId!!,
            awardedByName = entity.awardedByName!!,
        )
    }
}
