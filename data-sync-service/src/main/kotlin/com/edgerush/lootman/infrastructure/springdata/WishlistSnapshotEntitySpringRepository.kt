package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for WishlistSnapshotEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface WishlistSnapshotEntitySpringRepository :
    CrudRepository<WishlistSnapshotEntity, Long>,
    PagingAndSortingRepository<WishlistSnapshotEntity, Long> {

    fun findByRaiderId(raiderId: Long, pageable: Pageable): Page<WishlistSnapshotEntity>

    fun countByRaiderId(raiderId: Long): Long

    fun findByRaiderId(raiderId: Long): List<WishlistSnapshotEntity>

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<WishlistSnapshotEntity>

    fun countByTeamId(teamId: Long): Long

    @Query(
        """
        SELECT * FROM wishlist_snapshots
        WHERE character_name = :characterName AND character_realm = :characterRealm
        ORDER BY synced_at DESC
        """
    )
    fun findByCharacterNameAndRealm(characterName: String, characterRealm: String): List<WishlistSnapshotEntity>

    @Query(
        """
        SELECT * FROM wishlist_snapshots
        WHERE team_id = :teamId AND season_id = :seasonId AND period_id = :periodId
        """
    )
    fun findByTeamIdAndSeasonIdAndPeriodId(
        teamId: Long,
        seasonId: Long,
        periodId: Long,
    ): List<WishlistSnapshotEntity>
}
