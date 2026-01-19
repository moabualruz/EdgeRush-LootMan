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
    @Column("name")
    val name: String,
    @Column("realm")
    val realm: String?,
    @Column("class")
    val clazz: String?,
    @Column("role")
    val role: String?,
    @Column("blizzard_id")
    val blizzardId: Long?,
    @Column("tracking_since")
    val trackingSince: OffsetDateTime?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime,
)
