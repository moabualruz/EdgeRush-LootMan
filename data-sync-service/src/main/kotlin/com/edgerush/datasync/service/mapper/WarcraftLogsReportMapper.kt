package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsReportResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsReportEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WarcraftLogsReportMapper {
    fun toEntity(request: CreateWarcraftLogsReportRequest): WarcraftLogsReportEntity {
        return WarcraftLogsReportEntity(
            id = null,
            guildId = request.guildId ?: "",
            reportCode = request.reportCode ?: "",
            title = request.title,
            startTime = request.startTime ?: Instant.now(),
            endTime = request.endTime ?: Instant.now(),
            owner = request.owner,
            zone = request.zone,
            syncedAt = request.syncedAt ?: Instant.now(),
            rawMetadata = request.rawMetadata,
        )
    }

    fun updateEntity(
        entity: WarcraftLogsReportEntity,
        request: UpdateWarcraftLogsReportRequest,
    ): WarcraftLogsReportEntity {
        return entity.copy(
            guildId = request.guildId ?: entity.guildId,
            reportCode = request.reportCode ?: entity.reportCode,
            title = request.title ?: entity.title,
            startTime = request.startTime ?: entity.startTime,
            endTime = request.endTime ?: entity.endTime,
            owner = request.owner ?: entity.owner,
            zone = request.zone ?: entity.zone,
            syncedAt = request.syncedAt ?: entity.syncedAt,
            rawMetadata = request.rawMetadata ?: entity.rawMetadata,
        )
    }

    fun toResponse(entity: WarcraftLogsReportEntity): WarcraftLogsReportResponse {
        return WarcraftLogsReportResponse(
            id = entity.id!!,
            guildId = entity.guildId,
            reportCode = entity.reportCode,
            title = entity.title!!,
            startTime = entity.startTime,
            endTime = entity.endTime,
            owner = entity.owner!!,
            zone = entity.zone!!,
            syncedAt = entity.syncedAt,
            rawMetadata = entity.rawMetadata!!,
        )
    }
}
