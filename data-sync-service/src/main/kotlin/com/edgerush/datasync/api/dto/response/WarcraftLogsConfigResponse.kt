package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class WarcraftLogsConfigResponse(
    val guildId: String,
    val enabled: Boolean,
    val guildName: String,
    val realm: String,
    val region: String,
    val encryptedClientId: String?,
    val encryptedClientSecret: String?,
    val configJson: String,
    val updatedAt: Instant,
    val updatedBy: String?,
)
