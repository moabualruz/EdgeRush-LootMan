package com.edgerush.lootman.domain.raidplan.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant
import java.util.UUID

/**
 * Aggregate root for raid plan domain.
 * A raid plan contains multiple steps with positioning markers and shapes
 * for organizing raid strategies.
 */
@ConsistentCopyVisibility
data class RaidPlan private constructor(
    val id: String,
    val guildId: GuildId,
    val encounterId: Int,
    val encounterName: String,
    val name: String,
    val steps: List<PlanStep>,
    val visibility: PlanVisibility,
    val shareToken: String?,
    val createdBy: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(name.isNotBlank()) { "Plan name cannot be blank" }
        require(encounterName.isNotBlank()) { "Encounter name cannot be blank" }
        require(encounterId > 0) { "Encounter ID must be positive" }
        require(createdBy > 0) { "Created by user ID must be positive" }
    }

    /**
     * Adds a new step to the plan.
     */
    fun addStep(notes: String? = null): RaidPlan {
        val newStep =
            PlanStep.create(
                order = steps.size,
                notes = notes,
            )
        return copy(
            steps = steps + newStep,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Removes a step by its order and re-orders remaining steps.
     */
    fun removeStep(order: Int): RaidPlan {
        val step =
            steps.find { it.order == order }
                ?: throw IllegalArgumentException("Step with order $order does not exist")

        val remainingSteps = steps.filter { it.order != order }
        val reorderedSteps =
            remainingSteps.mapIndexed { index, s ->
                s.withOrder(index)
            }

        return copy(
            steps = reorderedSteps,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Updates a step's notes by its order.
     */
    fun updateStep(
        order: Int,
        notes: String?,
    ): RaidPlan {
        val stepIndex = steps.indexOfFirst { it.order == order }
        if (stepIndex == -1) {
            throw IllegalArgumentException("Step with order $order does not exist")
        }

        val updatedSteps = steps.toMutableList()
        updatedSteps[stepIndex] = updatedSteps[stepIndex].withNotes(notes)

        return copy(
            steps = updatedSteps,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Generates a new share token for this plan.
     */
    fun generateShareToken(): RaidPlan =
        copy(
            shareToken = UUID.randomUUID().toString().replace("-", ""),
            updatedAt = Instant.now(),
        )

    /**
     * Revokes the share token.
     */
    fun revokeShareToken(): RaidPlan =
        copy(
            shareToken = null,
            updatedAt = Instant.now(),
        )

    /**
     * Changes the visibility of this plan.
     */
    fun changeVisibility(visibility: PlanVisibility): RaidPlan =
        copy(
            visibility = visibility,
            updatedAt = Instant.now(),
        )

    companion object {
        /**
         * Creates a new raid plan.
         */
        fun create(
            guildId: GuildId,
            encounterId: Int,
            encounterName: String,
            name: String,
            createdBy: Long,
            visibility: PlanVisibility = PlanVisibility.GUILD,
        ): RaidPlan {
            val now = Instant.now()
            return RaidPlan(
                id = UUID.randomUUID().toString(),
                guildId = guildId,
                encounterId = encounterId,
                encounterName = encounterName,
                name = name,
                steps = emptyList(),
                visibility = visibility,
                shareToken = null,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now,
            )
        }

        /**
         * Reconstitutes a raid plan from persistence.
         */
        fun reconstitute(
            id: String,
            guildId: GuildId,
            encounterId: Int,
            encounterName: String,
            name: String,
            steps: List<PlanStep>,
            visibility: PlanVisibility,
            shareToken: String?,
            createdBy: Long,
            createdAt: Instant,
            updatedAt: Instant,
        ): RaidPlan =
            RaidPlan(
                id = id,
                guildId = guildId,
                encounterId = encounterId,
                encounterName = encounterName,
                name = name,
                steps = steps,
                visibility = visibility,
                shareToken = shareToken,
                createdBy = createdBy,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }
}
