package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

/**
 * Health indicator for the SimulationCraft integration.
 *
 * Reports health status based on:
 * - Docker availability (can execute simulations)
 * - Pending simulation queue size
 * - Recent failure rate
 */
@Component
class SimulationHealthIndicator(
    private val simulationRepository: SimulationRepository,
    private val dockerSimulationExecutor: DockerSimulationExecutor,
) : HealthIndicator {
    private val successCount = AtomicLong(0)
    private val failureCount = AtomicLong(0)
    private val lastExecutionTimeMs = AtomicLong(0)

    override fun health(): Health {
        val pendingCount = simulationRepository.findPendingRequests().size
        val dockerAvailable = checkDockerAvailable()
        val totalExecutions = successCount.get() + failureCount.get()
        val successRate =
            if (totalExecutions > 0) {
                successCount.get().toDouble() / totalExecutions
            } else {
                1.0
            }

        val builder =
            Health.Builder()
                .withDetail("pendingSimulations", pendingCount)
                .withDetail("dockerAvailable", dockerAvailable)
                .withDetail("successCount", successCount.get())
                .withDetail("failureCount", failureCount.get())
                .withDetail("successRate", String.format("%.2f%%", successRate * 100))
                .withDetail("lastExecutionTimeMs", lastExecutionTimeMs.get())

        return when {
            !dockerAvailable ->
                builder.down()
                    .withDetail("reason", "Docker is not available")
                    .build()
            pendingCount > MAX_HEALTHY_PENDING ->
                builder.outOfService()
                    .withDetail("reason", "Too many pending simulations")
                    .build()
            successRate < MIN_SUCCESS_RATE && totalExecutions >= MIN_EXECUTIONS_FOR_RATE ->
                builder.outOfService()
                    .withDetail("reason", "Success rate below threshold")
                    .build()
            else -> builder.up().build()
        }
    }

    /**
     * Records a successful simulation execution.
     */
    fun recordSuccess(executionTimeMs: Long) {
        successCount.incrementAndGet()
        lastExecutionTimeMs.set(executionTimeMs)
    }

    /**
     * Records a failed simulation execution.
     */
    fun recordFailure() {
        failureCount.incrementAndGet()
    }

    private fun checkDockerAvailable(): Boolean {
        return try {
            val process =
                ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val MAX_HEALTHY_PENDING = 50
        private const val MIN_SUCCESS_RATE = 0.8
        private const val MIN_EXECUTIONS_FOR_RATE = 5
    }
}
