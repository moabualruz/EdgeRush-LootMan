package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*

data class CreateRaidSignupRequest(
    @field:Min(value = 0, message = "Raid id must be positive")
    val raidId: Long? = null,
    val characterId: Long? = null,
    val characterName: String? = null,
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val characterClass: String? = null,
    val characterRole: String? = null,
    val characterGuest: Boolean? = null,
    val status: String? = null,
    val comment: String? = null,
    val selected: Boolean? = null,
)

data class UpdateRaidSignupRequest(
    @field:Min(value = 0, message = "Raid id must be positive")
    val raidId: Long? = null,
    val characterId: Long? = null,
    val characterName: String? = null,
    val characterRealm: String? = null,
    val characterRegion: String? = null,
    val characterClass: String? = null,
    val characterRole: String? = null,
    val characterGuest: Boolean? = null,
    val status: String? = null,
    val comment: String? = null,
    val selected: Boolean? = null,
)
