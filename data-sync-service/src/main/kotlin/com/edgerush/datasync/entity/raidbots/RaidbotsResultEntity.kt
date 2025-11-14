package com.edgerush.datasync.entity.raidbots

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("raidbots_results")
data class RaidbotsResultEntity(
    @Id val id: Long? = null,
    val simulationId: Long,
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val calculatedAt: Instant
)
