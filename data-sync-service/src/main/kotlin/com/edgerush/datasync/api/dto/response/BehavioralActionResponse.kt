package com.edgerush.datasync.api.dto.response


data class BehavioralActionResponse(
    val id: Long?,
    val guildId: String,
    val characterName: String,
    val actionType: String,
    val deductionAmount: Double
)
