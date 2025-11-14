package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateRaidEncounterRequest(
    @field:Min(value = 0, message = "Raid id must be positive")
    val raidId: Long? = null,

    val encounterId: Long? = null,

    val name: String? = null,

    val enabled: Boolean? = null,

    val extra: Boolean? = null,

    val notes: String? = null
)

data class UpdateRaidEncounterRequest(
    @field:Min(value = 0, message = "Raid id must be positive")
    val raidId: Long? = null,

    val encounterId: Long? = null,

    val name: String? = null,

    val enabled: Boolean? = null,

    val extra: Boolean? = null,

    val notes: String? = null
)
