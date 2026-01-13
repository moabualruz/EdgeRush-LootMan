package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

/**
 * JDBC implementation of SimulationRepository.
 *
 * Provides persistence for simulation profiles, requests, and results
 * using Spring JDBC Template for database access.
 */
@Repository
class JdbcSimulationRepository(
    private val jdbcTemplate: JdbcTemplate
) : SimulationRepository {

    private val profileRowMapper = RowMapper { rs: ResultSet, _: Int ->
        SimulationProfile.create(
            guildId = rs.getString("guild_id"),
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            profileContent = rs.getString("profile_content"),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    private val resultRowMapper = RowMapper { rs: ResultSet, _: Int ->
        SimulationResult.create(
            itemId = rs.getLong("item_id"),
            itemName = rs.getString("item_name"),
            slot = rs.getString("slot"),
            dpsGain = rs.getDouble("dps_gain"),
            percentGain = rs.getDouble("percent_gain"),
            simulatedAt = rs.getTimestamp("simulated_at").toInstant()
        )
    }

    override fun saveProfile(profile: SimulationProfile): Pair<Long, SimulationProfile> {
        // Upsert profile
        jdbcTemplate.update(
            """
            INSERT INTO simulation_profiles (guild_id, character_name, character_realm, profile_content, created_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (guild_id, character_name, character_realm)
            DO UPDATE SET profile_content = EXCLUDED.profile_content, created_at = EXCLUDED.created_at
            """.trimIndent(),
            profile.guildId,
            profile.characterName,
            profile.characterRealm,
            profile.profileContent,
            Timestamp.from(profile.createdAt)
        )

        // Get the ID
        val id = jdbcTemplate.queryForObject(
            "SELECT id FROM simulation_profiles WHERE guild_id = ? AND character_name = ? AND character_realm = ?",
            Long::class.java,
            profile.guildId,
            profile.characterName,
            profile.characterRealm
        ) ?: throw IllegalStateException("Failed to retrieve profile ID after save")

        return id to profile
    }

    override fun findProfileById(id: Long): SimulationProfile? {
        val profiles = jdbcTemplate.query(
            "SELECT * FROM simulation_profiles WHERE id = ?",
            profileRowMapper,
            id
        )
        return profiles.firstOrNull()
    }

    override fun findProfileByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): SimulationProfile? {
        val profiles = jdbcTemplate.query(
            "SELECT * FROM simulation_profiles WHERE guild_id = ? AND character_name = ? AND character_realm = ?",
            profileRowMapper,
            guildId,
            characterName,
            characterRealm
        )
        return profiles.firstOrNull()
    }

    override fun saveRequest(request: SimulationRequest): SimulationRequest {
        // Get profile ID
        val profileId = jdbcTemplate.queryForObject(
            "SELECT id FROM simulation_profiles WHERE guild_id = ? AND character_name = ? AND character_realm = ?",
            Long::class.java,
            request.profile.guildId,
            request.profile.characterName,
            request.profile.characterRealm
        ) ?: throw IllegalStateException("Profile not found for request")

        if (request.id == null) {
            // Insert new request
            jdbcTemplate.update(
                """
                INSERT INTO simulation_requests
                (profile_id, iterations, fight_length_seconds, status, submitted_at, completed_at, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                profileId,
                request.iterations,
                request.fightLengthSeconds,
                request.status.name,
                Timestamp.from(request.submittedAt),
                request.completedAt?.let { Timestamp.from(it) },
                request.errorMessage
            )

            // Get generated ID
            val id = jdbcTemplate.queryForObject(
                "SELECT currval(pg_get_serial_sequence('simulation_requests', 'id'))",
                Long::class.java
            ) ?: throw IllegalStateException("Failed to retrieve request ID after insert")

            return request.withId(id)
        } else {
            // Update existing request
            jdbcTemplate.update(
                """
                UPDATE simulation_requests
                SET status = ?, completed_at = ?, error_message = ?
                WHERE id = ?
                """.trimIndent(),
                request.status.name,
                request.completedAt?.let { Timestamp.from(it) },
                request.errorMessage,
                request.id
            )
            return request
        }
    }

    override fun findRequestById(id: Long): SimulationRequest? {
        val requests = jdbcTemplate.query(
            """
            SELECT r.*, p.guild_id, p.character_name, p.character_realm, p.profile_content, p.created_at as profile_created_at
            FROM simulation_requests r
            JOIN simulation_profiles p ON r.profile_id = p.id
            WHERE r.id = ?
            """.trimIndent(),
            { rs, _ -> mapRequestRow(rs) },
            id
        )
        return requests.firstOrNull()
    }

    override fun findPendingRequests(): List<SimulationRequest> {
        return jdbcTemplate.query(
            """
            SELECT r.*, p.guild_id, p.character_name, p.character_realm, p.profile_content, p.created_at as profile_created_at
            FROM simulation_requests r
            JOIN simulation_profiles p ON r.profile_id = p.id
            WHERE r.status = ?
            ORDER BY r.submitted_at ASC
            """.trimIndent(),
            { rs, _ -> mapRequestRow(rs) },
            "PENDING"
        )
    }

    override fun saveResult(profileId: Long, result: SimulationResult) {
        jdbcTemplate.update(
            """
            INSERT INTO simulation_results
            (profile_id, item_id, item_name, slot, dps_gain, percent_gain, simulated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            profileId,
            result.itemId,
            result.itemName,
            result.slot,
            result.dpsGain,
            result.percentGain,
            Timestamp.from(result.simulatedAt)
        )
    }

    override fun findLatestResultForItem(profileId: Long, itemId: Long): SimulationResult? {
        val results = jdbcTemplate.query(
            """
            SELECT * FROM simulation_results
            WHERE profile_id = ? AND item_id = ?
            ORDER BY simulated_at DESC
            LIMIT 1
            """.trimIndent(),
            resultRowMapper,
            profileId,
            itemId
        )
        return results.firstOrNull()
    }

    override fun findResultsByProfile(profileId: Long): List<SimulationResult> {
        return jdbcTemplate.query(
            "SELECT * FROM simulation_results WHERE profile_id = ? ORDER BY simulated_at DESC",
            resultRowMapper,
            profileId
        )
    }

    private fun mapRequestRow(rs: ResultSet): SimulationRequest {
        val profile = SimulationProfile.create(
            guildId = rs.getString("guild_id"),
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            profileContent = rs.getString("profile_content"),
            createdAt = rs.getTimestamp("profile_created_at").toInstant()
        )

        return SimulationRequest.create(
            profile = profile,
            iterations = rs.getInt("iterations"),
            fightLengthSeconds = rs.getInt("fight_length_seconds")
        ).let { request ->
            // Apply state based on database values
            var result = request.withId(rs.getLong("id"))

            when (SimulationStatus.valueOf(rs.getString("status"))) {
                SimulationStatus.PENDING -> result
                SimulationStatus.RUNNING -> result.markRunning()
                SimulationStatus.COMPLETED -> result.markRunning().markCompleted(emptyList())
                SimulationStatus.FAILED -> result.markRunning().markFailed(rs.getString("error_message") ?: "Unknown error")
            }
        }
    }
}
