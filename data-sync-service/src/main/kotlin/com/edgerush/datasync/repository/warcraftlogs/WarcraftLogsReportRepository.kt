package com.edgerush.datasync.repository.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsReportEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface WarcraftLogsReportRepository : CrudRepository<WarcraftLogsReportEntity, Long> {
    
    fun findByReportCode(reportCode: String): WarcraftLogsReportEntity?
    
    @Query("""
        SELECT * FROM warcraft_logs_reports 
        WHERE guild_id = :guildId 
        AND start_time >= :startTime 
        AND start_time <= :endTime
        ORDER BY start_time DESC
    """)
    fun findByGuildIdAndTimeRange(
        @Param("guildId") guildId: String,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<WarcraftLogsReportEntity>
    
    @Query("SELECT * FROM warcraft_logs_reports WHERE guild_id = :guildId ORDER BY start_time DESC")
    fun findByGuildIdOrderByStartTimeDesc(@Param("guildId") guildId: String): List<WarcraftLogsReportEntity>
    
    @Query("SELECT * FROM warcraft_logs_reports WHERE guild_id = :guildId ORDER BY synced_at DESC")
    fun findByGuildIdOrderBySyncedAtDesc(@Param("guildId") guildId: String): List<WarcraftLogsReportEntity>
}
