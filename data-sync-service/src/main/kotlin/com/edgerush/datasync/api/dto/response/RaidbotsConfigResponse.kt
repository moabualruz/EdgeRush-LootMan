package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class RaidbotsConfigResponse(
    val guildId: String,
    val enabled: Boolean,
    val encryptedApiKey: String?,
    val configJson: String,
    val updatedAt: Instant
)
