package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("raiders")
data class RaiderEntity(
    @Id
    val id: Long? = null,
    val characterName: String,
    val realm: String,
    val region: String,
    val clazz: String,
    val spec: String,
    val role: String,
    val lastSync: java.time.OffsetDateTime
)
