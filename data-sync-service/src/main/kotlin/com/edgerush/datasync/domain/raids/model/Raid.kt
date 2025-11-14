package com.edgerush.datasync.domain.raids.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Aggregate root representing a raid event.
 * Manages the consistency boundary for raid encounters and signups.
 */
data class Raid private constructor(
    val id: RaidId,
    val guildId: GuildId,
    val scheduledDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val instance: String?,
    val difficulty: RaidDifficulty?,
    val optional: Boolean,
    val status: RaidStatus,
    val notes: String?,
    private val encounters: MutableList<RaidEncounter>,
    private val signups: MutableList<RaidSignup>
) {
    companion object {
        fun schedule(
            guildId: GuildId,
            scheduledDate: LocalDate,
            startTime: LocalTime? = null,
            endTime: LocalTime? = null,
            instance: String? = null,
            difficulty: RaidDifficulty? = null,
            optional: Boolean = false,
            notes: String? = null
        ): Raid {
            return Raid(
                id = RaidId.generate(),
                guildId = guildId,
                scheduledDate = scheduledDate,
                startTime = startTime,
                endTime = endTime,
                instance = instance,
                difficulty = difficulty,
                optional = optional,
                status = RaidStatus.SCHEDULED,
                notes = notes,
                encounters = mutableListOf(),
                signups = mutableListOf()
            )
        }
        
        fun reconstitute(
            id: RaidId,
            guildId: GuildId,
            scheduledDate: LocalDate,
            startTime: LocalTime?,
            endTime: LocalTime?,
            instance: String?,
            difficulty: RaidDifficulty?,
            optional: Boolean,
            status: RaidStatus,
            notes: String?,
            encounters: List<RaidEncounter>,
            signups: List<RaidSignup>
        ): Raid {
            return Raid(
                id = id,
                guildId = guildId,
                scheduledDate = scheduledDate,
                startTime = startTime,
                endTime = endTime,
                instance = instance,
                difficulty = difficulty,
                optional = optional,
                status = status,
                notes = notes,
                encounters = encounters.toMutableList(),
                signups = signups.toMutableList()
            )
        }
    }
    
    // Encounter management
    fun addEncounter(encounter: RaidEncounter): Raid {
        require(status == RaidStatus.SCHEDULED) { "Cannot add encounters to non-scheduled raid" }
        val newEncounters = encounters.toMutableList()
        newEncounters.add(encounter)
        return copy(encounters = newEncounters)
    }
    
    fun removeEncounter(encounterId: Long): Raid {
        require(status == RaidStatus.SCHEDULED) { "Cannot remove encounters from non-scheduled raid" }
        val newEncounters = encounters.toMutableList()
        newEncounters.removeIf { it.encounterId == encounterId }
        return copy(encounters = newEncounters)
    }
    
    fun getEncounters(): List<RaidEncounter> = encounters.toList()
    
    // Signup management
    fun addSignup(raider: RaiderId, role: RaidRole, comment: String? = null): Raid {
        require(status == RaidStatus.SCHEDULED) { "Cannot signup for non-scheduled raid" }
        require(signups.none { it.raiderId == raider }) { "Raider already signed up" }
        
        val newSignups = signups.toMutableList()
        newSignups.add(RaidSignup.create(raider, role, comment = comment))
        return copy(signups = newSignups)
    }
    
    fun removeSignup(raiderId: RaiderId): Raid {
        require(status == RaidStatus.SCHEDULED) { "Cannot remove signups from non-scheduled raid" }
        val newSignups = signups.toMutableList()
        newSignups.removeIf { it.raiderId == raiderId }
        return copy(signups = newSignups)
    }
    
    fun updateSignupStatus(raiderId: RaiderId, newStatus: RaidSignup.SignupStatus): Raid {
        val signup = signups.find { it.raiderId == raiderId }
            ?: throw IllegalArgumentException("Signup not found for raider: ${raiderId.value}")
        
        val newSignups = signups.toMutableList()
        val index = newSignups.indexOf(signup)
        newSignups[index] = signup.updateStatus(newStatus)
        return copy(signups = newSignups)
    }
    
    fun selectSignup(raiderId: RaiderId): Raid {
        val signup = signups.find { it.raiderId == raiderId }
            ?: throw IllegalArgumentException("Signup not found for raider: ${raiderId.value}")
        
        val newSignups = signups.toMutableList()
        val index = newSignups.indexOf(signup)
        newSignups[index] = signup.select()
        return copy(signups = newSignups)
    }
    
    fun getSignups(): List<RaidSignup> = signups.toList()
    
    fun getConfirmedSignups(): List<RaidSignup> = signups.filter { it.isConfirmed() }
    
    fun getSelectedSignups(): List<RaidSignup> = signups.filter { it.selected }
    
    // State transitions
    fun start(): Raid {
        require(status == RaidStatus.SCHEDULED) { "Can only start scheduled raids" }
        require(signups.isNotEmpty()) { "Need at least one signup to start raid" }
        
        return copy(status = RaidStatus.IN_PROGRESS)
    }
    
    fun complete(): Raid {
        require(status == RaidStatus.IN_PROGRESS) { "Can only complete in-progress raids" }
        return copy(status = RaidStatus.COMPLETED)
    }
    
    fun cancel(): Raid {
        require(status == RaidStatus.SCHEDULED) { "Can only cancel scheduled raids" }
        return copy(status = RaidStatus.CANCELLED)
    }
    
    // Query methods
    fun isScheduled(): Boolean = status == RaidStatus.SCHEDULED
    
    fun isInProgress(): Boolean = status == RaidStatus.IN_PROGRESS
    
    fun isCompleted(): Boolean = status == RaidStatus.COMPLETED
    
    fun isCancelled(): Boolean = status == RaidStatus.CANCELLED
    
    fun getSignupCount(): Int = signups.size
    
    fun getConfirmedSignupCount(): Int = signups.count { it.isConfirmed() }
}
