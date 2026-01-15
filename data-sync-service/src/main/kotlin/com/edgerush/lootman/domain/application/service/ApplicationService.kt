package com.edgerush.lootman.domain.application.service

import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.application.repository.EnhancedApplicationRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Service

/**
 * Domain service for managing guild applications.
 *
 * Handles the application lifecycle including submission, review, approval/rejection,
 * and withdrawal. Enforces business rules around duplicate applications.
 */
@Service
class ApplicationService(
    private val repository: EnhancedApplicationRepository,
) {
    /**
     * Submits a new application to a guild.
     *
     * @throws IllegalStateException if an application already exists for the Discord or Battle.net account
     */
    fun submitApplication(
        guildId: GuildId,
        battleNetId: String,
        discordId: String,
        email: String,
        characterName: String,
        characterRealm: String,
        characterClass: String,
        specialization: String,
        itemLevel: Double,
        raiderIOScore: Double?,
        bestParseAverage: Double?,
        age: Int,
        location: String,
        timezone: String,
        raidDaysAvailable: List<String>,
        previousGuilds: String,
        reasonForLeaving: String,
        whyThisGuild: String,
    ): Application {
        // Check for existing applications
        val existingByDiscord = repository.findByGuildIdAndDiscordId(guildId, discordId)
        if (existingByDiscord != null && existingByDiscord.status in listOf(ApplicationStatus.PENDING, ApplicationStatus.UNDER_REVIEW)) {
            throw IllegalStateException("An application already exists for this Discord account")
        }

        val existingByBattleNet = repository.findByGuildIdAndBattleNetId(guildId, battleNetId)
        if (existingByBattleNet != null && existingByBattleNet.status in listOf(ApplicationStatus.PENDING, ApplicationStatus.UNDER_REVIEW)) {
            throw IllegalStateException("An application already exists for this Battle.net account")
        }

        val application =
            Application.create(
                guildId = guildId,
                battleNetId = battleNetId,
                discordId = discordId,
                email = email,
                characterName = characterName,
                characterRealm = characterRealm,
                characterClass = characterClass,
                specialization = specialization,
                itemLevel = itemLevel,
                raiderIOScore = raiderIOScore,
                bestParseAverage = bestParseAverage,
                age = age,
                location = location,
                timezone = timezone,
                raidDaysAvailable = raidDaysAvailable,
                previousGuilds = previousGuilds,
                reasonForLeaving = reasonForLeaving,
                whyThisGuild = whyThisGuild,
            )

        return repository.save(application)
    }

    /**
     * Starts the review process for an application.
     */
    fun startReview(
        applicationId: ApplicationId,
        reviewerId: String,
    ): Application {
        val application = findApplicationOrThrow(applicationId)
        val updated = application.startReview(reviewerId)
        return repository.save(updated)
    }

    /**
     * Approves an application.
     */
    fun approveApplication(
        applicationId: ApplicationId,
        reviewerId: String,
    ): Application {
        val application = findApplicationOrThrow(applicationId)
        val approved = application.approve(reviewerId)
        return repository.save(approved)
    }

    /**
     * Rejects an application.
     */
    fun rejectApplication(
        applicationId: ApplicationId,
        reviewerId: String,
    ): Application {
        val application = findApplicationOrThrow(applicationId)
        val rejected = application.reject(reviewerId)
        return repository.save(rejected)
    }

    /**
     * Withdraws an application.
     */
    fun withdrawApplication(applicationId: ApplicationId): Application {
        val application = findApplicationOrThrow(applicationId)
        val withdrawn = application.withdraw()
        return repository.save(withdrawn)
    }

    /**
     * Gets an application by ID.
     */
    fun getApplicationById(applicationId: ApplicationId): Application? {
        return repository.findById(applicationId)
    }

    /**
     * Gets all applications for a guild.
     */
    fun getApplicationsByGuild(
        guildId: GuildId,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Application> {
        return repository.findByGuildId(guildId, offset, limit)
    }

    /**
     * Gets pending applications for a guild.
     */
    fun getPendingApplications(
        guildId: GuildId,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Application> {
        return repository.findByGuildIdAndStatus(guildId, ApplicationStatus.PENDING, offset, limit)
    }

    /**
     * Gets applications by status for a guild.
     */
    fun getApplicationsByStatus(
        guildId: GuildId,
        status: ApplicationStatus,
        offset: Long = 0,
        limit: Int = 50,
    ): List<Application> {
        return repository.findByGuildIdAndStatus(guildId, status, offset, limit)
    }

    /**
     * Counts applications for a guild.
     */
    fun countApplicationsByGuild(guildId: GuildId): Long {
        return repository.countByGuildId(guildId)
    }

    /**
     * Counts applications by status for a guild.
     */
    fun countApplicationsByStatus(
        guildId: GuildId,
        status: ApplicationStatus,
    ): Long {
        return repository.countByGuildIdAndStatus(guildId, status)
    }

    private fun findApplicationOrThrow(applicationId: ApplicationId): Application {
        return repository.findById(applicationId)
            ?: throw IllegalArgumentException("Application not found: ${applicationId.value}")
    }
}
