package com.edgerush.lootman.domain.application.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * Entity representing a guild application from a prospective raider.
 *
 * Contains applicant information obtained through OAuth (Battle.net, Discord)
 * and auto-fetched from external APIs (Raider.IO, Warcraft Logs), along with
 * user-provided information about their raiding background and availability.
 */
@ConsistentCopyVisibility
data class Application private constructor(
    val id: ApplicationId,
    val guildId: GuildId,
    // OAuth data
    val battleNetId: String,
    val discordId: String,
    val email: String,
    // Character data (auto-fetched)
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Double,
    val raiderIOScore: Double?,
    val bestParseAverage: Double?,
    // User input
    val age: Int,
    val location: String,
    val timezone: String,
    val raidDaysAvailable: List<String>,
    val previousGuilds: String,
    val reasonForLeaving: String,
    val whyThisGuild: String,
    // Status
    val status: ApplicationStatus,
    val reviewedBy: String?,
    val reviewedAt: Instant?,
    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        /**
         * Reconstructs an Application from persisted data.
         *
         * This is used by repositories to load Application entities from the database.
         * Unlike [create], this does not generate a new ID or set timestamps.
         */
        fun reconstruct(
            id: ApplicationId,
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
            status: ApplicationStatus,
            reviewedBy: String?,
            reviewedAt: Instant?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Application = Application(
            id = id,
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
            status = status,
            reviewedBy = reviewedBy,
            reviewedAt = reviewedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        /**
         * Creates a new Application with PENDING status.
         */
        fun create(
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
            require(characterName.isNotBlank()) {
                "Character name cannot be blank"
            }
            require(characterRealm.isNotBlank()) {
                "Character realm cannot be blank"
            }
            require(EMAIL_REGEX.matches(email)) {
                "Invalid email format"
            }
            require(itemLevel >= 0) {
                "Item level cannot be negative"
            }
            require(raiderIOScore == null || raiderIOScore >= 0) {
                "Raider.IO score cannot be negative"
            }
            require(bestParseAverage == null || bestParseAverage in 0.0..100.0) {
                "Best parse average must be between 0 and 100"
            }
            require(age >= 18) {
                "Applicant must be at least 18 years old"
            }
            require(raidDaysAvailable.isNotEmpty()) {
                "At least one raid day must be available"
            }

            val now = Instant.now()
            return Application(
                id = ApplicationId.generate(),
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
                status = ApplicationStatus.PENDING,
                reviewedBy = null,
                reviewedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    /**
     * Marks the application as under review by an officer.
     */
    fun startReview(reviewerId: String): Application {
        check(status == ApplicationStatus.PENDING) {
            "Can only start review on PENDING applications"
        }
        return copy(
            status = ApplicationStatus.UNDER_REVIEW,
            reviewedBy = reviewerId,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Approves the application.
     */
    fun approve(reviewerId: String): Application {
        check(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Can only approve PENDING or UNDER_REVIEW applications"
        }
        val now = Instant.now()
        return copy(
            status = ApplicationStatus.APPROVED,
            reviewedBy = reviewerId,
            reviewedAt = now,
            updatedAt = now,
        )
    }

    /**
     * Rejects the application.
     */
    fun reject(reviewerId: String): Application {
        check(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Can only reject PENDING or UNDER_REVIEW applications"
        }
        val now = Instant.now()
        return copy(
            status = ApplicationStatus.REJECTED,
            reviewedBy = reviewerId,
            reviewedAt = now,
            updatedAt = now,
        )
    }

    /**
     * Withdraws the application by the applicant.
     */
    fun withdraw(): Application {
        check(status == ApplicationStatus.PENDING || status == ApplicationStatus.UNDER_REVIEW) {
            "Cannot withdraw an already reviewed application"
        }
        return copy(
            status = ApplicationStatus.WITHDRAWN,
            updatedAt = Instant.now(),
        )
    }
}
