package com.edgerush.lootman.domain.recruitment

import java.time.Instant

data class RecruitmentApplication(
    val id: String,
    val guildId: String,
    val applicant: RecruitmentApplicant,
    val details: RecruitmentDetails,
    val status: RecruitmentStatus,
    val review: RecruitmentReview?,
    val timestamps: RecruitmentTimestamps
)

data class RecruitmentApplicant(
    val battleNetId: String,
    val discordId: String,
    val email: String,
    val character: RecruitmentCharacter
)

data class RecruitmentCharacter(
    val name: String,
    val realm: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Double,
    val scores: RecruitmentScores
)

data class RecruitmentScores(
    val raiderIoScore: Double?,
    val bestParseAverage: Double?
)

data class RecruitmentDetails(
    val age: Int,
    val location: String,
    val timezone: String,
    val raidDaysAvailable: List<String>,
    val previousGuilds: String,
    val reasonForLeaving: String,
    val whyThisGuild: String
)

data class RecruitmentReview(
    val reviewedBy: String?,
    val reviewedAt: Instant?
)

data class RecruitmentTimestamps(
    val createdAt: Instant,
    val updatedAt: Instant
)
