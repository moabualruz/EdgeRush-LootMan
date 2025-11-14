package com.edgerush.datasync.application.raids

import com.edgerush.datasync.domain.raids.model.GuildId
import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidDifficulty
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.domain.raids.service.RaidSchedulingService
import org.springframework.stereotype.Service

/**
 * Use case for scheduling a new raid.
 * 
 * This use case orchestrates the process of:
 * 1. Validating the schedule with the scheduling service
 * 2. Creating a new raid aggregate
 * 3. Persisting the raid
 */
@Service
class ScheduleRaidUseCase(
    private val raidRepository: RaidRepository,
    private val schedulingService: RaidSchedulingService
) {
    
    /**
     * Execute the schedule raid use case.
     * 
     * @param command The command containing raid scheduling details
     * @return Result containing the scheduled Raid or an exception
     */
    fun execute(command: ScheduleRaidCommand): Result<Raid> = runCatching {
        val guildId = GuildId(command.guildId)
        
        // Validate the schedule by checking existing raids
        val existingRaids = raidRepository.findByGuildIdAndDate(guildId, command.scheduledDate)
        if (!schedulingService.canScheduleRaid(command.scheduledDate, existingRaids)) {
            throw IllegalArgumentException("Cannot schedule raid on ${command.scheduledDate}: date is in the past or conflicts with existing raid")
        }
        
        // Parse difficulty if provided
        val difficulty = command.difficulty?.let { 
            RaidDifficulty.fromString(it)
        }
        
        // Create the raid aggregate
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = command.scheduledDate,
            startTime = command.startTime,
            endTime = command.endTime,
            instance = command.instance,
            difficulty = difficulty,
            optional = command.optional,
            notes = command.notes
        )
        
        // Persist the raid
        raidRepository.save(raid)
    }
}
