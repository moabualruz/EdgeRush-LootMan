package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("application_alts")
data class ApplicationAltEntity(
    @Id
    val id: Long? = null,
    @Column("application_id")
    val applicationId: Long,
    @Column("name")
    val name: String?,
    @Column("realm")
    val realm: String?,
    @Column("region")
    val region: String?,
    @Column("class")
    val clazz: String?,
    @Column("role")
    val role: String?,
    @Column("level")
    val level: Int?,
    @Column("faction")
    val faction: String?,
    @Column("race")
    val race: String?,
)
