package com.edgerush.datasync.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "flps")
data class FlpsConfigProperties(
    var rms: RmsWeights = RmsWeights(),
    var ipi: IpiWeights = IpiWeights(),
    var roleMultipliers: RoleMultipliers = RoleMultipliers(),
    var thresholds: Thresholds = Thresholds()
) {
    
    data class RmsWeights(
        var attendance: Double = 0.45,           // ACS weight in RMS calculation
        var mechanical: Double = 0.35,           // MAS weight in RMS calculation  
        var preparation: Double = 0.20           // EPS weight in RMS calculation
    )
    
    data class IpiWeights(
        var upgradeValue: Double = 0.45,         // Upgrade value weight in IPI
        var tierBonus: Double = 0.35,            // Tier bonus weight in IPI
        var roleMultiplier: Double = 0.20        // Role multiplier weight in IPI
    )
    
    data class RoleMultipliers(
        var tank: Double = 1.2,                  // Tank role multiplier (rare/critical)
        var healer: Double = 1.1,                // Healer role multiplier (important)
        var dps: Double = 1.0                    // DPS role multiplier (baseline)
    )
    
    data class Thresholds(
        var eligibilityAttendance: Double = 0.8, // Minimum ACS for eligibility
        var eligibilityActivity: Double = 0.0,   // Minimum MAS for eligibility
        var recencyDecayDays: Long = 30,         // Days for RDF calculation
        var maxAttendanceBonus: Double = 1.0,    // Maximum attendance bonus
        var minMechanicalScore: Double = 0.0,    // Minimum mechanical score
        var maxPreparationScore: Double = 1.0    // Maximum preparation score
    )
}