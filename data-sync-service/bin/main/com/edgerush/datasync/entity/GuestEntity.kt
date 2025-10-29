package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("guests")
data class GuestEntity(
    @Id
    @Column("guest_id")
    val guestId: Long,
    val name: String,
    val realm: String?,
    val `class`: String?,
    val role: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val syncedAt: OffsetDateTime
)
