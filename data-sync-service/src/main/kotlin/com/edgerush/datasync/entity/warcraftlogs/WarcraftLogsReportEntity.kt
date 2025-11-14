package com.edgerush.datasync.entity.warcraftlogs

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("warcraft_logs_reports")
data class WarcraftLogsReportEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val reportCode: String,
    val title: String?,
    val startTime: Instant,
    val endTime: Instant,
    val owner: String?,
    val zone: Int?,
    val syncedAt: Instant,
    val rawMetadata: String?
)
