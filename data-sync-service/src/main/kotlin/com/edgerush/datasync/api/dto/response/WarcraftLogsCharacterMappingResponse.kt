package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class WarcraftLogsCharacterMappingResponse(
    val id: Long?,
    val guildId: String,
    val wowauditName: String,
    val wowauditRealm: String,
    val warcraftLogsName: String,
    val warcraftLogsRealm: String,
    val createdAt: Instant,
    val createdBy: String?
)
