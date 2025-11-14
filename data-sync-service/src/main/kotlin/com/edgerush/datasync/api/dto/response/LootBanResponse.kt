package com.edgerush.datasync.api.dto.response

import java.time.LocalDateTime

data class LootBanResponse(
    val id: Long?,
    val guildId: String,
    val characterName: String,
    val reason: String,
    val bannedBy: String,
    val bannedAt: LocalDateTime,
    val expiresAt: LocalDateTime?
)
