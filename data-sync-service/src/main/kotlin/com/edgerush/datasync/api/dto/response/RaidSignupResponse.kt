package com.edgerush.datasync.api.dto.response


data class RaidSignupResponse(
    val id: Long?,
    val raidId: Long,
    val characterId: Long?,
    val characterName: String?,
    val characterRealm: String?,
    val characterRegion: String?,
    val characterClass: String?,
    val characterRole: String?,
    val characterGuest: Boolean?,
    val status: String?,
    val comment: String?,
    val selected: Boolean?
)
