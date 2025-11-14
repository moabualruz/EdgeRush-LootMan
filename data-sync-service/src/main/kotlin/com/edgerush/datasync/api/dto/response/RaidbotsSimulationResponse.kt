package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class RaidbotsSimulationResponse(
    val id: Long?,
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val simId: String,
    val status: String,
    val submittedAt: Instant,
    val completedAt: Instant?,
    val profile: String,
    val simOptions: String,
)
