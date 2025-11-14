package com.edgerush.datasync.client.warcraftlogs

import com.edgerush.datasync.model.warcraftlogs.CharacterPerformanceData
import com.edgerush.datasync.model.warcraftlogs.WarcraftLogsFight
import com.edgerush.datasync.model.warcraftlogs.WarcraftLogsReport
import java.time.Instant

/**
 * Interface for Warcraft Logs API v2 client
 */
interface WarcraftLogsClient {
    
    /**
     * Fetches reports for a guild within a time range
     */
    suspend fun fetchReportsForGuild(
        guildName: String,
        realm: String,
        region: String,
        startTime: Instant,
        endTime: Instant
    ): List<WarcraftLogsReport>
    
    /**
     * Fetches fight data for a specific report
     */
    suspend fun fetchFightData(reportCode: String): List<WarcraftLogsFight>
    
    /**
     * Fetches character performance data for a specific fight
     */
    suspend fun fetchCharacterPerformance(
        reportCode: String,
        fightId: Int,
        characterName: String
    ): CharacterPerformanceData?
}
