package com.edgerush.lootman.infrastructure.recruitment

import com.edgerush.datasync.entity.RecruitmentApplicationEntity
import com.edgerush.datasync.entity.RecruitmentCommentEntity
import com.edgerush.lootman.domain.recruitment.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.ZoneOffset

@Repository
class JdbcRecruitmentRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : RecruitmentRepository {
    override fun findById(id: String): RecruitmentApplication? {
        val sql = "SELECT * FROM enhanced_applications WHERE enhanced_application_id = ?"
        return jdbcTemplate.query(sql, applicationRowMapper, id).firstOrNull()?.toDomain()
    }

    override fun findByGuildId(
        guildId: String,
        status: RecruitmentStatus?,
    ): List<RecruitmentApplication> {
        val sql =
            if (status != null) {
                "SELECT * FROM enhanced_applications WHERE guild_id = ? AND status = ? ORDER BY created_at DESC"
            } else {
                "SELECT * FROM enhanced_applications WHERE guild_id = ? ORDER BY created_at DESC"
            }

        val args = if (status != null) arrayOf(guildId, status.name) else arrayOf(guildId)

        return jdbcTemplate.query(sql, applicationRowMapper, *args).map { it.toDomain() }
    }

    override fun save(application: RecruitmentApplication): RecruitmentApplication {
        // Upsert logic
        // Check if exists logic omitted for brevity, assuming Save = Insert OR Update based on existence
        // For simplicity, let's just do an UPSERT (INSERT ON CONFLICT) implementation pattern or check exists.

        val exists =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM enhanced_applications WHERE enhanced_application_id = ?",
                Int::class.java,
                application.id,
            ) ?: 0 > 0

        val entity = application.toEntity()

        if (exists) {
            val sql = """
                UPDATE enhanced_applications SET
                    status = ?, reviewed_by = ?, reviewed_at = ?, updated_at = ?
                WHERE enhanced_application_id = ?
            """
            jdbcTemplate.update(
                sql,
                entity.status,
                entity.reviewedBy,
                entity.reviewedAt?.let { Timestamp.from(it.toInstant()) },
                Timestamp.from(entity.updatedAt.toInstant()),
                entity.id,
            )
        } else {
            val sql = """
                INSERT INTO enhanced_applications (
                    enhanced_application_id, guild_id, battle_net_id, discord_id, email,
                    character_name, character_realm, character_class, specialization,
                    item_level, raider_io_score, best_parse_average,
                    age, location, timezone, raid_days_available,
                    previous_guilds, reason_for_leaving, why_this_guild,
                    status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            jdbcTemplate.update(
                sql,
                entity.id, entity.guildId, entity.battleNetId, entity.discordId, entity.email,
                entity.characterName, entity.characterRealm, entity.characterClass, entity.specialization,
                entity.itemLevel, entity.raiderIoScore, entity.bestParseAverage,
                entity.age, entity.location, entity.timezone, entity.raidDaysAvailable,
                entity.previousGuilds, entity.reasonForLeaving, entity.whyThisGuild,
                entity.status,
                Timestamp.from(entity.createdAt.toInstant()),
                Timestamp.from(entity.updatedAt.toInstant()),
            )
        }
        return application
    }

    override fun addComment(comment: RecruitmentComment): RecruitmentComment {
        val sql = """
            INSERT INTO recruitment_comments (application_id, author_id, text, created_at)
            VALUES (?, ?, ?, ?) RETURNING id
        """
        val id =
            jdbcTemplate.queryForObject(
                sql,
                Long::class.java,
                comment.applicationId,
                comment.authorId,
                comment.text,
                Timestamp.from(comment.createdAt),
            )
        return comment.copy(id = id)
    }

    override fun getComments(applicationId: String): List<RecruitmentComment> {
        val sql = "SELECT * FROM recruitment_comments WHERE application_id = ? ORDER BY created_at ASC"
        return jdbcTemplate.query(sql, commentRowMapper, applicationId).map { it.toDomain() }
    }

    private val applicationRowMapper =
        RowMapper { rs, _ ->
            RecruitmentApplicationEntity(
                id = rs.getString("enhanced_application_id"),
                guildId = rs.getString("guild_id"),
                battleNetId = rs.getString("battle_net_id"),
                discordId = rs.getString("discord_id"),
                email = rs.getString("email"),
                characterName = rs.getString("character_name"),
                characterRealm = rs.getString("character_realm"),
                characterClass = rs.getString("character_class"),
                specialization = rs.getString("specialization"),
                itemLevel = rs.getDouble("item_level"),
                raiderIoScore = rs.getObject("raider_io_score") as? Double,
                bestParseAverage = rs.getObject("best_parse_average") as? Double,
                age = rs.getInt("age"),
                location = rs.getString("location"),
                timezone = rs.getString("timezone"),
                raidDaysAvailable = rs.getString("raid_days_available"),
                previousGuilds = rs.getString("previous_guilds"),
                reasonForLeaving = rs.getString("reason_for_leaving"),
                whyThisGuild = rs.getString("why_this_guild"),
                status = rs.getString("status"),
                reviewedBy = rs.getString("reviewed_by"),
                reviewedAt = rs.getTimestamp("reviewed_at")?.toInstant()?.atOffset(ZoneOffset.UTC),
                createdAt = rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
                updatedAt = rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC),
            )
        }

    private val commentRowMapper =
        RowMapper { rs, _ ->
            RecruitmentCommentEntity(
                id = rs.getLong("id"),
                applicationId = rs.getString("application_id"),
                authorId = rs.getLong("author_id"),
                text = rs.getString("text"),
                createdAt = rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
            )
        }

    private fun RecruitmentApplicationEntity.toDomain(): RecruitmentApplication {
        val daysList: List<String> =
            try {
                objectMapper.readValue(this.raidDaysAvailable, object : TypeReference<List<String>>() {})
            } catch (e: Exception) {
                emptyList()
            }

        return RecruitmentApplication(
            id = this.id,
            guildId = this.guildId,
            applicant =
                RecruitmentApplicant(
                    battleNetId = this.battleNetId,
                    discordId = this.discordId,
                    email = this.email,
                    character =
                        RecruitmentCharacter(
                            name = this.characterName,
                            realm = this.characterRealm,
                            characterClass = this.characterClass,
                            specialization = this.specialization,
                            itemLevel = this.itemLevel,
                            scores =
                                RecruitmentScores(
                                    raiderIoScore = this.raiderIoScore,
                                    bestParseAverage = this.bestParseAverage,
                                ),
                        ),
                ),
            details =
                RecruitmentDetails(
                    age = this.age,
                    location = this.location,
                    timezone = this.timezone,
                    raidDaysAvailable = daysList,
                    previousGuilds = this.previousGuilds,
                    reasonForLeaving = this.reasonForLeaving,
                    whyThisGuild = this.whyThisGuild,
                ),
            status =
                try {
                    RecruitmentStatus.valueOf(this.status)
                } catch (e: Exception) {
                    RecruitmentStatus.PENDING
                },
            review =
                RecruitmentReview(
                    reviewedBy = this.reviewedBy,
                    reviewedAt = this.reviewedAt?.toInstant(),
                ),
            timestamps =
                RecruitmentTimestamps(
                    createdAt = this.createdAt.toInstant(),
                    updatedAt = this.updatedAt.toInstant(),
                ),
        )
    }

    private fun RecruitmentApplication.toEntity(): RecruitmentApplicationEntity {
        return RecruitmentApplicationEntity(
            id = this.id,
            guildId = this.guildId,
            battleNetId = this.applicant.battleNetId,
            discordId = this.applicant.discordId,
            email = this.applicant.email,
            characterName = this.applicant.character.name,
            characterRealm = this.applicant.character.realm,
            characterClass = this.applicant.character.characterClass,
            specialization = this.applicant.character.specialization,
            itemLevel = this.applicant.character.itemLevel,
            raiderIoScore = this.applicant.character.scores.raiderIoScore,
            bestParseAverage = this.applicant.character.scores.bestParseAverage,
            age = this.details.age,
            location = this.details.location,
            timezone = this.details.timezone,
            raidDaysAvailable = objectMapper.writeValueAsString(this.details.raidDaysAvailable),
            previousGuilds = this.details.previousGuilds,
            reasonForLeaving = this.details.reasonForLeaving,
            whyThisGuild = this.details.whyThisGuild,
            status = this.status.name,
            reviewedBy = this.review?.reviewedBy,
            reviewedAt = this.review?.reviewedAt?.atOffset(ZoneOffset.UTC),
            createdAt = this.timestamps.createdAt.atOffset(ZoneOffset.UTC),
            updatedAt = this.timestamps.updatedAt.atOffset(ZoneOffset.UTC),
        )
    }

    private fun RecruitmentCommentEntity.toDomain(): RecruitmentComment {
        return RecruitmentComment(
            id = this.id,
            applicationId = this.applicationId,
            authorId = this.authorId,
            text = this.text,
            createdAt = this.createdAt.toInstant(),
        )
    }
}
