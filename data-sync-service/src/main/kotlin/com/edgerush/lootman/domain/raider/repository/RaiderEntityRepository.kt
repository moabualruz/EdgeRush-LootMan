package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderEntity

interface RaiderEntityRepository {
    fun findById(id: Long): RaiderEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderEntity>

    fun count(): Long

    fun findByRealm(
        realm: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity>

    fun countByRealm(realm: String): Long

    fun findByRegion(
        region: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity>

    fun countByRegion(region: String): Long

    fun save(entity: RaiderEntity): RaiderEntity

    fun delete(id: Long)

    fun findByCharacterNameAndRealm(
        characterName: String,
        realm: String,
    ): RaiderEntity?

    /**
     * Case-insensitive lookup that handles realm name variations.
     * Matches both slug format (twisting-nether) and display format (Twisting Nether).
     */
    fun findByCharacterNameAndRealmNormalized(
        characterName: String,
        realm: String,
    ): RaiderEntity?

    fun findByBlizzardId(blizzardId: Long): RaiderEntity?

    fun findByWowauditId(wowauditId: Long): RaiderEntity?

    fun findByWowauditIds(wowauditIds: List<Long>): List<RaiderEntity>

    fun findByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<RaiderEntity>

    fun countByGuildId(guildId: String): Long
}
