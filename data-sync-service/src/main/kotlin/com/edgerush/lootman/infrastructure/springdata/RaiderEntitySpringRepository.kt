package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RaiderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RaiderEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RaiderEntitySpringRepository :
    CrudRepository<RaiderEntity, Long>,
    PagingAndSortingRepository<RaiderEntity, Long> {

    fun findByRealm(realm: String, pageable: Pageable): Page<RaiderEntity>

    fun countByRealm(realm: String): Long

    fun findByRegion(region: String, pageable: Pageable): Page<RaiderEntity>

    fun countByRegion(region: String): Long

    fun findByCharacterNameAndRealm(characterName: String, realm: String): RaiderEntity?

    /**
     * Case-insensitive lookup by character name and normalized realm.
     * Handles both slug format (twisting-nether) and display format (Twisting Nether).
     */
    @Query("""
        SELECT * FROM raiders
        WHERE LOWER(character_name) = LOWER(:characterName)
        AND (
            LOWER(realm) = LOWER(:realm)
            OR LOWER(REPLACE(realm, ' ', '-')) = LOWER(:realm)
            OR LOWER(realm) = LOWER(REPLACE(:realm, ' ', '-'))
        )
        LIMIT 1
    """)
    fun findByCharacterNameAndRealmNormalized(characterName: String, realm: String): RaiderEntity?

    fun findByBlizzardId(blizzardId: Long): RaiderEntity?

    fun findByWowauditId(wowauditId: Long): RaiderEntity?

    fun findByWowauditIdIn(wowauditIds: List<Long>): List<RaiderEntity>

    fun findByGuildId(guildId: String, pageable: Pageable): Page<RaiderEntity>

    fun countByGuildId(guildId: String): Long

    @Query("SELECT * FROM raiders WHERE guild_id = :guildId AND status = 'ACTIVE'")
    fun findActiveByGuildId(guildId: String): List<RaiderEntity>
}
