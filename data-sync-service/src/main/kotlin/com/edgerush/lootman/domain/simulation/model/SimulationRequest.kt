package com.edgerush.lootman.domain.simulation.model

import java.time.Instant

/**
 * Entity representing a simulation request and its lifecycle.
 *
 * Tracks the state of a simulation from submission through completion or failure.
 */
@ConsistentCopyVisibility
data class SimulationRequest private constructor(
    val id: Long? = null,
    val profile: SimulationProfile,
    val iterations: Int,
    val fightLengthSeconds: Int,
    val status: SimulationStatus,
    val submittedAt: Instant,
    val completedAt: Instant?,
    val results: List<SimulationResult>,
    val errorMessage: String?
) {
    val isPending: Boolean get() = status == SimulationStatus.PENDING
    val isRunning: Boolean get() = status == SimulationStatus.RUNNING
    val isCompleted: Boolean get() = status == SimulationStatus.COMPLETED
    val isFailed: Boolean get() = status == SimulationStatus.FAILED

    /**
     * Returns a copy with the given ID assigned.
     * Used by repositories after persistence.
     */
    fun withId(id: Long): SimulationRequest = copy(id = id)

    /**
     * Marks the request as running.
     *
     * @throws IllegalStateException if not in PENDING state
     */
    fun markRunning(): SimulationRequest {
        check(status == SimulationStatus.PENDING) {
            "Cannot transition to RUNNING from $status state"
        }
        return copy(status = SimulationStatus.RUNNING)
    }

    /**
     * Marks the request as completed with results.
     *
     * @param results The simulation results
     * @throws IllegalStateException if not in RUNNING state
     */
    fun markCompleted(results: List<SimulationResult>): SimulationRequest {
        check(status == SimulationStatus.RUNNING) {
            "Cannot transition to COMPLETED from $status state"
        }
        return copy(
            status = SimulationStatus.COMPLETED,
            completedAt = Instant.now(),
            results = results
        )
    }

    /**
     * Marks the request as failed with an error message.
     *
     * @param errorMessage The error message describing the failure
     * @throws IllegalStateException if not in RUNNING state
     */
    fun markFailed(errorMessage: String): SimulationRequest {
        check(status == SimulationStatus.RUNNING) {
            "Cannot transition to FAILED from $status state"
        }
        return copy(
            status = SimulationStatus.FAILED,
            completedAt = Instant.now(),
            errorMessage = errorMessage,
            results = emptyList()
        )
    }

    companion object {
        const val DEFAULT_ITERATIONS = 10000
        const val DEFAULT_FIGHT_LENGTH_SECONDS = 300

        /**
         * Creates a new simulation request in PENDING state.
         *
         * @param profile The simulation profile to use
         * @param iterations Number of simulation iterations (default: 10000)
         * @param fightLengthSeconds Fight duration in seconds (default: 300)
         * @return A new SimulationRequest in PENDING state
         * @throws IllegalArgumentException if parameters are invalid
         */
        fun create(
            profile: SimulationProfile,
            iterations: Int = DEFAULT_ITERATIONS,
            fightLengthSeconds: Int = DEFAULT_FIGHT_LENGTH_SECONDS
        ): SimulationRequest {
            require(iterations > 0) { "iterations must be greater than 0" }
            require(fightLengthSeconds > 0) { "fightLengthSeconds must be greater than 0" }

            return SimulationRequest(
                profile = profile,
                iterations = iterations,
                fightLengthSeconds = fightLengthSeconds,
                status = SimulationStatus.PENDING,
                submittedAt = Instant.now(),
                completedAt = null,
                results = emptyList(),
                errorMessage = null
            )
        }
    }
}
