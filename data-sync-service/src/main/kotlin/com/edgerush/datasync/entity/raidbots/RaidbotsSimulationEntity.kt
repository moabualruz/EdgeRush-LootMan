package com.edgerush.datasync.entity.raidbots

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("raidbots_simulations")
data class RaidbotsSimulationEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val simId: String,
    val status: String,
    val submittedAt: Instant,
    val completedAt: Instant?,
    val profile: String,
    val simOptions: String,
)
