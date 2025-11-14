package com.edgerush.datasync.api.dto.response

data class RaidEncounterResponse(
    val id: Long?,
    val raidId: Long,
    val encounterId: Long?,
    val name: String?,
    val enabled: Boolean?,
    val extra: Boolean?,
    val notes: String?,
)
