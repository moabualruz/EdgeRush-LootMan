package com.edgerush.lootman.application.raidplan

import com.edgerush.lootman.domain.raidplan.model.PlanVisibility
import com.edgerush.lootman.domain.raidplan.model.RaidPlan
import com.edgerush.lootman.domain.raidplan.repository.RaidPlanRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Service

/**
 * Application service for raid plan management.
 */
@Service
class RaidPlanService(
    private val raidPlanRepository: RaidPlanRepository,
) {
    /**
     * Creates a new raid plan.
     */
    fun createPlan(request: CreateRaidPlanRequest): RaidPlan {
        val plan =
            RaidPlan.create(
                guildId = GuildId(request.guildId),
                encounterId = request.encounterId,
                encounterName = request.encounterName,
                name = request.name,
                createdBy = request.createdBy,
                visibility = request.visibility,
            )
        return raidPlanRepository.save(plan)
    }

    /**
     * Gets a raid plan by ID.
     * @throws RaidPlanNotFoundException if not found
     */
    fun getPlan(id: String): RaidPlan {
        return raidPlanRepository.findById(id)
            ?: throw RaidPlanNotFoundException(id)
    }

    /**
     * Gets a raid plan by share token.
     * @throws RaidPlanNotFoundException if not found
     */
    fun getPlanByShareToken(shareToken: String): RaidPlan {
        return raidPlanRepository.findByShareToken(shareToken)
            ?: throw RaidPlanNotFoundException(shareToken)
    }

    /**
     * Gets all raid plans for a guild.
     */
    fun getPlansByGuild(guildId: String): List<RaidPlan> {
        return raidPlanRepository.findByGuildId(GuildId(guildId))
    }

    /**
     * Gets paginated raid plans for a guild.
     */
    fun getPlansByGuildPaginated(
        guildId: String,
        page: Int,
        size: Int,
    ): PagedRaidPlans {
        val guildIdObj = GuildId(guildId)
        val offset = page.toLong() * size
        val plans = raidPlanRepository.findByGuildId(guildIdObj, offset, size)
        val total = raidPlanRepository.countByGuildId(guildIdObj)
        return PagedRaidPlans(
            content = plans,
            page = page,
            size = size,
            totalElements = total,
            totalPages = ((total + size - 1) / size).toInt(),
        )
    }

    /**
     * Gets raid plans for a specific encounter.
     */
    fun getPlansByEncounter(
        guildId: String,
        encounterId: Int,
    ): List<RaidPlan> {
        return raidPlanRepository.findByGuildIdAndEncounterId(GuildId(guildId), encounterId)
    }

    /**
     * Updates a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun updatePlan(
        id: String,
        request: UpdateRaidPlanRequest,
    ): RaidPlan {
        var plan = getPlan(id)

        request.name?.let { newName ->
            plan =
                RaidPlan.reconstitute(
                    id = plan.id,
                    guildId = plan.guildId,
                    encounterId = plan.encounterId,
                    encounterName = plan.encounterName,
                    name = newName,
                    steps = plan.steps,
                    visibility = plan.visibility,
                    shareToken = plan.shareToken,
                    createdBy = plan.createdBy,
                    createdAt = plan.createdAt,
                    updatedAt = java.time.Instant.now(),
                )
        }

        request.visibility?.let { newVisibility ->
            plan = plan.changeVisibility(newVisibility)
        }

        request.steps?.let { newSteps ->
            // Reconstitute with new steps
            plan =
                RaidPlan.reconstitute(
                    id = plan.id,
                    guildId = plan.guildId,
                    encounterId = plan.encounterId,
                    encounterName = plan.encounterName,
                    name = plan.name,
                    steps = newSteps,
                    visibility = plan.visibility,
                    shareToken = plan.shareToken,
                    createdBy = plan.createdBy,
                    createdAt = plan.createdAt,
                    updatedAt = java.time.Instant.now(),
                )
        }

        return raidPlanRepository.save(plan)
    }

    /**
     * Adds a step to a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun addStep(
        planId: String,
        notes: String? = null,
    ): RaidPlan {
        val plan = getPlan(planId)
        val updatedPlan = plan.addStep(notes)
        return raidPlanRepository.save(updatedPlan)
    }

    /**
     * Removes a step from a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun removeStep(
        planId: String,
        stepOrder: Int,
    ): RaidPlan {
        val plan = getPlan(planId)
        val updatedPlan = plan.removeStep(stepOrder)
        return raidPlanRepository.save(updatedPlan)
    }

    /**
     * Updates a step's notes.
     * @throws RaidPlanNotFoundException if not found
     */
    fun updateStep(
        planId: String,
        stepOrder: Int,
        notes: String?,
    ): RaidPlan {
        val plan = getPlan(planId)
        val updatedPlan = plan.updateStep(stepOrder, notes)
        return raidPlanRepository.save(updatedPlan)
    }

    /**
     * Generates a share token for a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun generateShareToken(planId: String): RaidPlan {
        val plan = getPlan(planId)
        val updatedPlan = plan.generateShareToken()
        return raidPlanRepository.save(updatedPlan)
    }

    /**
     * Revokes the share token for a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun revokeShareToken(planId: String): RaidPlan {
        val plan = getPlan(planId)
        val updatedPlan = plan.revokeShareToken()
        return raidPlanRepository.save(updatedPlan)
    }

    /**
     * Deletes a raid plan.
     * @throws RaidPlanNotFoundException if not found
     */
    fun deletePlan(planId: String) {
        // Verify the plan exists
        getPlan(planId)
        raidPlanRepository.delete(planId)
    }
}

/**
 * Request to create a new raid plan.
 */
data class CreateRaidPlanRequest(
    val guildId: String,
    val encounterId: Int,
    val encounterName: String,
    val name: String,
    val createdBy: Long,
    val visibility: PlanVisibility = PlanVisibility.GUILD,
)

/**
 * Request to update an existing raid plan.
 */
data class UpdateRaidPlanRequest(
    val name: String? = null,
    val visibility: PlanVisibility? = null,
    val steps: List<com.edgerush.lootman.domain.raidplan.model.PlanStep>? = null,
)

/**
 * Paginated response for raid plans.
 */
data class PagedRaidPlans(
    val content: List<RaidPlan>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

/**
 * Exception thrown when a raid plan is not found.
 */
class RaidPlanNotFoundException(val planId: String) : RuntimeException("Raid plan not found: $planId")
