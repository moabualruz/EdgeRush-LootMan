package com.edgerush.datasync.repository.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsFightEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface WarcraftLogsFightRepository : CrudRepository<WarcraftLogsFightEntity, Long> {
    fun findByReportId(reportId: Long): List<WarcraftLogsFightEntity>

    @Query(
        """
        SELECT * FROM warcraft_logs_fights 
        WHERE report_id = :reportId 
        AND fight_id = :fightId
    """,
    )
    fun findByReportIdAndFightId(
        @Param("reportId") reportId: Long,
        @Param("fightId") fightId: Int,
    ): WarcraftLogsFightEntity?

    @Query(
        """
        SELECT * FROM warcraft_logs_fights 
        WHERE report_id = :reportId 
        AND encounter_id = :encounterId
    """,
    )
    fun findByReportIdAndEncounterId(
        @Param("reportId") reportId: Long,
        @Param("encounterId") encounterId: Int,
    ): List<WarcraftLogsFightEntity>

    @Query(
        """
        SELECT * FROM warcraft_logs_fights 
        WHERE encounter_id = :encounterId 
        AND difficulty = :difficulty 
        AND kill = true
    """,
    )
    fun findKillsByEncounterAndDifficulty(
        @Param("encounterId") encounterId: Int,
        @Param("difficulty") difficulty: String,
    ): List<WarcraftLogsFightEntity>
}
