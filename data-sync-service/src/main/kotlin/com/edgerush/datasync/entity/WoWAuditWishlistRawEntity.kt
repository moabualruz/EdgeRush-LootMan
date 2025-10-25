package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("wowaudit_wishlists_raw")
data class WoWAuditWishlistRawEntity(
    @Id
    val characterId: Long,
    val summaryJson: String?,
    val detailJson: String?,
    val syncedAt: OffsetDateTime
)

