package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("application_alts")
data class ApplicationAltEntity(
    @Id
    val id: Long? = null,
    val applicationId: Long,
    val name: String?,
    val realm: String?,
    val `class`: String?,
    val role: String?,
    val level: Int?
)

