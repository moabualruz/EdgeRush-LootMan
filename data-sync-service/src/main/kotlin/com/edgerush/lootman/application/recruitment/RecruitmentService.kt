package com.edgerush.lootman.application.recruitment

import com.edgerush.lootman.domain.recruitment.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RecruitmentService(
    private val recruitmentRepository: RecruitmentRepository
) {
    fun getApplication(id: String): RecruitmentApplication? {
        // TODO: Enforce guild permission checks via AOP or caller
        return recruitmentRepository.findById(id)
    }

    fun getApplications(guildId: String, status: RecruitmentStatus? = null): List<RecruitmentApplication> {
        return recruitmentRepository.findByGuildId(guildId, status)
    }

    fun searchCandidate(name: String, realm: String, region: String): RecruitmentCharacter {
        // MOCK: In real world, call Blizzard API / Raider.io API
        // For now, return a dummy strong candidate
        return RecruitmentCharacter(
            name = name,
            realm = realm,
            characterClass = "Mage",
            specialization = "Fire",
            itemLevel = 630.0,
            scores = RecruitmentScores(
                raiderIoScore = 3200.0,
                bestParseAverage = 95.5
            )
        )
    }

    @Transactional
    fun createApplication(
        guildId: String,
        command: CreateApplicationCommand
    ): RecruitmentApplication {
        val application = RecruitmentApplication(
            id = UUID.randomUUID().toString(),
            guildId = guildId,
            applicant = RecruitmentApplicant(
                battleNetId = command.battleNetId,
                discordId = command.discordId,
                email = command.email,
                character = RecruitmentCharacter(
                    name = command.characterName,
                    realm = command.characterRealm,
                    characterClass = command.characterClass,
                    specialization = command.specialization,
                    itemLevel = command.itemLevel,
                    scores = RecruitmentScores(
                        raiderIoScore = command.raiderIoScore,
                        bestParseAverage = command.bestParseAverage
                    )
                )
            ),
            details = RecruitmentDetails(
                age = command.age,
                location = command.location,
                timezone = command.timezone,
                raidDaysAvailable = command.raidDaysAvailable,
                previousGuilds = command.previousGuilds,
                reasonForLeaving = command.reasonForLeaving,
                whyThisGuild = command.whyThisGuild
            ),
            status = RecruitmentStatus.PENDING,
            review = null,
            timestamps = RecruitmentTimestamps(
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        return recruitmentRepository.save(application)
    }

    @Transactional
    fun updateStatus(id: String, status: RecruitmentStatus, reviewer: String): RecruitmentApplication {
        val application = recruitmentRepository.findById(id)
            ?: throw IllegalArgumentException("Application not found")

        val updated = application.copy(
            status = status,
            review = RecruitmentReview(
                reviewedBy = reviewer,
                reviewedAt = Instant.now()
            ),
            timestamps = application.timestamps.copy(updatedAt = Instant.now())
        )
        return recruitmentRepository.save(updated)
    }

    @Transactional
    fun addComment(applicationId: String, authorId: Long, text: String): RecruitmentComment {
        val comment = RecruitmentComment(
            applicationId = applicationId,
            authorId = authorId,
            text = text,
            createdAt = Instant.now()
        )
        return recruitmentRepository.addComment(comment)
    }
}

data class CreateApplicationCommand(
    val battleNetId: String,
    val discordId: String,
    val email: String,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Double,
    val raiderIoScore: Double?,
    val bestParseAverage: Double?,
    val age: Int,
    val location: String,
    val timezone: String,
    val raidDaysAvailable: List<String>,
    val previousGuilds: String,
    val reasonForLeaving: String,
    val whyThisGuild: String
)
