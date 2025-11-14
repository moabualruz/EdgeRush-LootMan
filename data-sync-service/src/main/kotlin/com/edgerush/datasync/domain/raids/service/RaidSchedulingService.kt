package com.edgerush.datasync.domain.raids.service

import com.edgerush.datasync.domain.raids.model.*
import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain service for raid scheduling logic.
 */
class RaidSchedulingService {
    
    /**
     * Validates if a raid can be scheduled on the given date.
     */
    fun canScheduleRaid(
        scheduledDate: LocalDate,
        existingRaids: List<Raid>
    ): Boolean {
        val now = LocalDate.now()
        
        // Cannot schedule raids in the past
        if (scheduledDate.isBefore(now)) {
            return false
        }
        
        // Check for conflicts with existing raids on the same date
        val conflictingRaids = existingRaids.filter { raid ->
            raid.scheduledDate == scheduledDate && 
            (raid.isScheduled() || raid.isInProgress())
        }
        
        return conflictingRaids.isEmpty()
    }
    
    /**
     * Validates if a raid has minimum required signups to start.
     */
    fun hasMinimumSignups(raid: Raid, minimumRequired: Int = 10): Boolean {
        return raid.getConfirmedSignupCount() >= minimumRequired
    }
    
    /**
     * Validates if a raid can be started.
     */
    fun canStartRaid(raid: Raid, minimumRequired: Int = 10): Boolean {
        return raid.isScheduled() && hasMinimumSignups(raid, minimumRequired)
    }
    
    /**
     * Calculates the optimal raid composition based on signups.
     */
    fun calculateOptimalComposition(signups: List<RaidSignup>): RaidComposition {
        val confirmedSignups = signups.filter { it.isConfirmed() }
        
        val tanks = confirmedSignups.count { it.role == RaidRole.TANK }
        val healers = confirmedSignups.count { it.role == RaidRole.HEALER }
        val dps = confirmedSignups.count { it.role == RaidRole.DPS }
        
        return RaidComposition(
            tanks = tanks,
            healers = healers,
            dps = dps,
            total = confirmedSignups.size
        )
    }
    
    /**
     * Validates if the raid composition is viable.
     */
    fun isViableComposition(composition: RaidComposition): Boolean {
        // Standard raid composition: 2 tanks, 4-5 healers, rest DPS
        // Minimum viable: 2 tanks, 3 healers, 5 DPS (10 total)
        return composition.tanks >= 2 &&
               composition.healers >= 3 &&
               composition.dps >= 5 &&
               composition.total >= 10
    }
    
    /**
     * Suggests missing roles for a raid composition.
     */
    fun suggestMissingRoles(composition: RaidComposition): List<RaidRole> {
        val missing = mutableListOf<RaidRole>()
        
        if (composition.tanks < 2) {
            repeat(2 - composition.tanks) { missing.add(RaidRole.TANK) }
        }
        
        if (composition.healers < 3) {
            repeat(3 - composition.healers) { missing.add(RaidRole.HEALER) }
        }
        
        if (composition.dps < 5) {
            repeat(5 - composition.dps) { missing.add(RaidRole.DPS) }
        }
        
        return missing
    }
}

/**
 * Value object representing raid composition.
 */
data class RaidComposition(
    val tanks: Int,
    val healers: Int,
    val dps: Int,
    val total: Int
) {
    init {
        require(tanks >= 0) { "Tanks cannot be negative" }
        require(healers >= 0) { "Healers cannot be negative" }
        require(dps >= 0) { "DPS cannot be negative" }
        require(total == tanks + healers + dps) { "Total must equal sum of roles" }
    }
}
