package com.edgerush.datasync.model

data class FlpsBreakdown(
    val name: String,
    val role: Role,
    val acs: Double,
    val mas: Double,
    val eps: Double,
    val rms: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
    val ipi: Double,
    val rdf: Double,
    val flps: Double,
    val eligible: Boolean,
)
