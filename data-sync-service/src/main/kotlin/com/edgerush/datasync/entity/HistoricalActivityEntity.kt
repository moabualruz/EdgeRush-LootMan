package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("historical_activity")
data class HistoricalActivityEntity(
    @Id
    val id: Long? = null,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val period: Long?,
    val dataJson: String,
    val syncedAt: OffsetDateTime
)
