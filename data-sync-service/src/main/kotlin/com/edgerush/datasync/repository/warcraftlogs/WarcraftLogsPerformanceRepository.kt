package com.edgerush.datasync.repository.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsPerformanceEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface WarcraftLogsPerformanceRepository : CrudRepository<WarcraftLogsPerformanceEntity, Long> {
    fun findByFightId(fightId: Long): List<WarcraftLogsPerformanceEntity>

    @Query(
        """
        SELECT * FROM warcraft_logs_performance 
        WHERE character_name = :characterName 
        AND character_realm = :characterRealm
        ORDER BY calculated_at DESC
    """,
    )
    fun findByCharacter(
        @Param("characterName") characterName: String,
        @Param("characterRealm") characterRealm: String,
    ): List<WarcraftLogsPerformanceEntity>

    @Query(
        """
        SELECT * FROM warcraft_logs_performance 
        WHERE character_name = :characterName 
        AND character_realm = :characterRealm
        AND calculated_at >= :since
        ORDER BY calculated_at DESC
    """,
    )
    fun findByCharacterSince(
        @Param("characterName") characterName: String,
        @Param("characterRealm") characterRealm: String,
        @Param("since") since: Instant,
    ): List<WarcraftLogsPerformanceEntity>

    @Query(
        """
        SELECT * FROM warcraft_logs_performance 
        WHERE character_spec = :spec
        AND calculated_at >= :since
    """,
    )
    fun findBySpecSince(
        @Param("spec") spec: String,
        @Param("since") since: Instant,
    ): List<WarcraftLogsPerformanceEntity>

    fun findByCharacterSpec(spec: String): List<WarcraftLogsPerformanceEntity>
}
