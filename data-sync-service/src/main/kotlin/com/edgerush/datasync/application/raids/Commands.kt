package com.edgerush.datasync.application.raids

import java.time.LocalDate
import java.time.LocalTime

/**
 * Command to schedule a new raid.
 */
data class ScheduleRaidCommand(
    val guildId: String,
    val scheduledDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val instance: String?,
    val difficulty: String?,
    val optional: Boolean,
    val notes: String?
)

/**
 * Command to add a signup to a raid.
 */
data class AddSignupCommand(
    val raidId: Long,
    val raiderId: Long,
    val role: String,
    val comment: String?
)

/**
 * Command to remove a signup from a raid.
 */
data class RemoveSignupCommand(
    val raidId: Long,
    val raiderId: Long
)

/**
 * Command to update a signup status.
 */
data class UpdateSignupStatusCommand(
    val raidId: Long,
    val raiderId: Long,
    val status: String
)

/**
 * Command to select a signup for the raid roster.
 */
data class SelectSignupCommand(
    val raidId: Long,
    val raiderId: Long
)

/**
 * Command to start a raid.
 */
data class StartRaidCommand(
    val raidId: Long
)

/**
 * Command to complete a raid.
 */
data class CompleteRaidCommand(
    val raidId: Long
)

/**
 * Command to cancel a raid.
 */
data class CancelRaidCommand(
    val raidId: Long
)
