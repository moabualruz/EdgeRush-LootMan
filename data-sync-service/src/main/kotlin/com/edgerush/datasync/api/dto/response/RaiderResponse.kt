package com.edgerush.datasync.api.dto.response


data class RaiderResponse(
    val id: Long?,
    val characterName: String,
    val realm: String,
    val region: String
)
