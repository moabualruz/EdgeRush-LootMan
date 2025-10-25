package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_roster_members_raw")
data class WoWAuditRosterMemberRawEntity(
    @Id
    val characterId: Long,
    val payload: String,
    val syncedAt: OffsetDateTime
)

