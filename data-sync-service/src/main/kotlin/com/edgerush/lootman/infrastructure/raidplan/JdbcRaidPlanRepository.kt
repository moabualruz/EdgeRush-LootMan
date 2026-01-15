package com.edgerush.lootman.infrastructure.raidplan

import com.edgerush.lootman.domain.raidplan.model.*
import com.edgerush.lootman.domain.raidplan.repository.RaidPlanRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

/**
 * JDBC implementation of RaidPlanRepository.
 *
 * Handles persistence for raid plans with their steps, markers, and shapes.
 * Uses manual SQL for complex aggregate loading.
 */
@Repository
class JdbcRaidPlanRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaidPlanRepository {
    override fun findById(id: String): RaidPlan? {
        val plans =
            jdbcTemplate.query(
                "SELECT * FROM raid_plans WHERE id = ?",
                planRowMapper,
                id,
            )
        return plans.firstOrNull()?.let { loadSteps(it) }
    }

    override fun findByShareToken(shareToken: String): RaidPlan? {
        val plans =
            jdbcTemplate.query(
                "SELECT * FROM raid_plans WHERE share_token = ?",
                planRowMapper,
                shareToken,
            )
        return plans.firstOrNull()?.let { loadSteps(it) }
    }

    override fun findByGuildId(guildId: GuildId): List<RaidPlan> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plans WHERE guild_id = ? ORDER BY updated_at DESC",
            planRowMapper,
            guildId.value,
        ).map { loadSteps(it) }
    }

    override fun findByGuildId(
        guildId: GuildId,
        offset: Long,
        limit: Int,
    ): List<RaidPlan> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plans WHERE guild_id = ? ORDER BY updated_at DESC LIMIT ? OFFSET ?",
            planRowMapper,
            guildId.value,
            limit,
            offset,
        ).map { loadSteps(it) }
    }

    override fun countByGuildId(guildId: GuildId): Long {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM raid_plans WHERE guild_id = ?",
            Long::class.java,
            guildId.value,
        ) ?: 0
    }

    override fun findByGuildIdAndEncounterId(
        guildId: GuildId,
        encounterId: Int,
    ): List<RaidPlan> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plans WHERE guild_id = ? AND encounter_id = ? ORDER BY updated_at DESC",
            planRowMapper,
            guildId.value,
            encounterId,
        ).map { loadSteps(it) }
    }

    override fun findByCreatedBy(userId: Long): List<RaidPlan> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plans WHERE created_by = ? ORDER BY updated_at DESC",
            planRowMapper,
            userId,
        ).map { loadSteps(it) }
    }

    override fun save(raidPlan: RaidPlan): RaidPlan {
        if (existsById(raidPlan.id)) {
            updatePlan(raidPlan)
        } else {
            insertPlan(raidPlan)
        }
        return raidPlan
    }

    override fun delete(id: String) {
        // Steps, markers, shapes are deleted via CASCADE
        jdbcTemplate.update("DELETE FROM raid_plans WHERE id = ?", id)
    }

    override fun existsById(id: String): Boolean {
        val count =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM raid_plans WHERE id = ?",
                Int::class.java,
                id,
            ) ?: 0
        return count > 0
    }

    private fun insertPlan(plan: RaidPlan) {
        jdbcTemplate.update(
            """
            INSERT INTO raid_plans (id, guild_id, encounter_id, encounter_name, name, visibility, share_token, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            plan.id,
            plan.guildId.value,
            plan.encounterId,
            plan.encounterName,
            plan.name,
            plan.visibility.name,
            plan.shareToken,
            plan.createdBy,
            Timestamp.from(plan.createdAt),
            Timestamp.from(plan.updatedAt),
        )
        saveSteps(plan)
    }

    private fun updatePlan(plan: RaidPlan) {
        jdbcTemplate.update(
            """
            UPDATE raid_plans
            SET guild_id = ?, encounter_id = ?, encounter_name = ?, name = ?, visibility = ?, share_token = ?, updated_at = ?
            WHERE id = ?
            """.trimIndent(),
            plan.guildId.value,
            plan.encounterId,
            plan.encounterName,
            plan.name,
            plan.visibility.name,
            plan.shareToken,
            Timestamp.from(plan.updatedAt),
            plan.id,
        )

        // Delete existing steps and re-insert (simpler than diffing)
        jdbcTemplate.update("DELETE FROM raid_plan_steps WHERE plan_id = ?", plan.id)
        saveSteps(plan)
    }

    private fun saveSteps(plan: RaidPlan) {
        for (step in plan.steps) {
            val stepId = insertStep(plan.id, step)
            saveMarkers(stepId, step.markers)
            saveShapes(stepId, step.shapes)
        }
    }

    private fun insertStep(
        planId: String,
        step: PlanStep,
    ): Long {
        jdbcTemplate.update(
            """
            INSERT INTO raid_plan_steps (plan_id, step_order, notes)
            VALUES (?, ?, ?)
            """.trimIndent(),
            planId,
            step.order,
            step.notes,
        )

        return jdbcTemplate.queryForObject(
            "SELECT id FROM raid_plan_steps WHERE plan_id = ? AND step_order = ?",
            Long::class.java,
            planId,
            step.order,
        ) ?: throw IllegalStateException("Failed to retrieve step ID after insert")
    }

    private fun saveMarkers(
        stepId: Long,
        markers: List<PlanMarker>,
    ) {
        for (marker in markers) {
            jdbcTemplate.update(
                """
                INSERT INTO raid_plan_markers (step_id, marker_type, x, y, label, color)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                stepId,
                marker.type.name,
                marker.x,
                marker.y,
                marker.label,
                marker.color,
            )
        }
    }

    private fun saveShapes(
        stepId: Long,
        shapes: List<PlanShape>,
    ) {
        for (shape in shapes) {
            jdbcTemplate.update(
                """
                INSERT INTO raid_plan_shapes (step_id, shape_type, x1, y1, x2, y2, radius, color, stroke_width)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                stepId,
                shape.shapeType.name,
                shape.x1,
                shape.y1,
                shape.x2,
                shape.y2,
                shape.radius,
                shape.color,
                shape.strokeWidth,
            )
        }
    }

    private fun loadSteps(plan: RaidPlan): RaidPlan {
        val steps =
            jdbcTemplate.query(
                "SELECT * FROM raid_plan_steps WHERE plan_id = ? ORDER BY step_order",
                stepRowMapper,
                plan.id,
            ).map { (stepId, step) ->
                val markers = loadMarkers(stepId)
                val shapes = loadShapes(stepId)
                step.withMarkers(markers).withShapes(shapes)
            }

        return RaidPlan.reconstitute(
            id = plan.id,
            guildId = plan.guildId,
            encounterId = plan.encounterId,
            encounterName = plan.encounterName,
            name = plan.name,
            steps = steps,
            visibility = plan.visibility,
            shareToken = plan.shareToken,
            createdBy = plan.createdBy,
            createdAt = plan.createdAt,
            updatedAt = plan.updatedAt,
        )
    }

    private fun loadMarkers(stepId: Long): List<PlanMarker> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plan_markers WHERE step_id = ?",
            markerRowMapper,
            stepId,
        )
    }

    private fun loadShapes(stepId: Long): List<PlanShape> {
        return jdbcTemplate.query(
            "SELECT * FROM raid_plan_shapes WHERE step_id = ?",
            shapeRowMapper,
            stepId,
        )
    }

    private val planRowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            RaidPlan.reconstitute(
                id = rs.getString("id"),
                guildId = GuildId(rs.getString("guild_id")),
                encounterId = rs.getInt("encounter_id"),
                encounterName = rs.getString("encounter_name"),
                name = rs.getString("name"),
                steps = emptyList(), // Loaded separately
                visibility = PlanVisibility.valueOf(rs.getString("visibility")),
                shareToken = rs.getString("share_token"),
                createdBy = rs.getLong("created_by"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                updatedAt = rs.getTimestamp("updated_at").toInstant(),
            )
        }

    private val stepRowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            val stepId = rs.getLong("id")
            val step =
                PlanStep.create(
                    order = rs.getInt("step_order"),
                    notes = rs.getString("notes"),
                )
            stepId to step
        }

    private val markerRowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            PlanMarker(
                type = MarkerType.valueOf(rs.getString("marker_type")),
                x = rs.getDouble("x"),
                y = rs.getDouble("y"),
                label = rs.getString("label"),
                color = rs.getString("color"),
            )
        }

    private val shapeRowMapper =
        RowMapper { rs: ResultSet, _: Int ->
            PlanShape(
                shapeType = ShapeType.valueOf(rs.getString("shape_type")),
                x1 = rs.getDouble("x1"),
                y1 = rs.getDouble("y1"),
                x2 = rs.getDouble("x2").takeIf { !rs.wasNull() },
                y2 = rs.getDouble("y2").takeIf { !rs.wasNull() },
                radius = rs.getDouble("radius").takeIf { !rs.wasNull() },
                color = rs.getString("color"),
                strokeWidth = rs.getInt("stroke_width"),
            )
        }
}
