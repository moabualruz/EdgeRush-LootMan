package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.lootman.application.simulation.SimulationExecutor
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Executes SimulationCraft simulations via Docker.
 *
 * Uses the official SimulationCraft Docker image to run gear upgrade simulations
 * locally without requiring external API access.
 */
@Component
class DockerSimulationExecutor(
    @Value("\${simulation.docker.image:simulationcraftorg/simc}")
    private val dockerImage: String,
    @Value("\${simulation.docker.profile-directory:./simc-profiles}")
    private val profileDirectory: String,
    @Value("\${simulation.docker.command:docker}")
    private val dockerCommand: String,
    @Value("\${simulation.docker.timeout-minutes:30}")
    private val timeoutMinutes: Long = 30,
) : SimulationExecutor {
    private val logger = LoggerFactory.getLogger(DockerSimulationExecutor::class.java)
    private val objectMapper = ObjectMapper()

    override suspend fun execute(request: SimulationRequest): Result<List<SimulationResult>> {
        return try {
            logger.info("Starting simulation for ${request.profile.characterIdentifier}")

            // Write profile to file
            val profileFile = writeProfileToFile(request)
            val outputFile = File(profileFile.parent, "${profileFile.nameWithoutExtension}_results.json")

            // Build and execute Docker command
            val command = buildDockerCommand(request, profileFile)
            logger.debug("Executing command: ${command.joinToString(" ")}")

            val process =
                ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()

            val completed = process.waitFor(timeoutMinutes, TimeUnit.MINUTES)

            if (!completed) {
                process.destroyForcibly()
                return Result.failure(RuntimeException("Simulation timed out after $timeoutMinutes minutes"))
            }

            val exitCode = process.exitValue()
            if (exitCode != 0) {
                val output = process.inputStream.bufferedReader().readText()
                logger.error("SimulationCraft exited with code $exitCode: $output")
                return Result.failure(RuntimeException("SimulationCraft exited with code $exitCode"))
            }

            // Parse results
            if (!outputFile.exists()) {
                return Result.failure(RuntimeException("Output file not found: ${outputFile.absolutePath}"))
            }

            val results = parseSimulationResults(outputFile.readText())
            logger.info("Simulation completed with ${results.size} results")

            // Cleanup
            profileFile.delete()
            outputFile.delete()

            Result.success(results)
        } catch (e: Exception) {
            logger.error("Simulation failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Writes the simulation profile to a file.
     */
    fun writeProfileToFile(request: SimulationRequest): File {
        val profileDir = File(profileDirectory)
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }

        val fileName = "${request.profile.characterName}-${request.profile.characterRealm}.simc"
        val profileFile = File(profileDir, fileName)
        profileFile.writeText(request.profile.profileContent)

        logger.debug("Wrote profile to ${profileFile.absolutePath}")
        return profileFile
    }

    /**
     * Builds the Docker command for executing SimulationCraft.
     */
    fun buildDockerCommand(
        request: SimulationRequest,
        profileFile: File,
    ): List<String> {
        val outputFile = File(profileFile.parent, "${profileFile.nameWithoutExtension}_results.json")
        val profileDir = profileFile.parentFile.absolutePath

        return listOf(
            dockerCommand,
            "run",
            "--rm",
            "-v", "$profileDir:/simc/profiles",
            dockerImage,
            "/simc/profiles/${profileFile.name}",
            "iterations=${request.iterations}",
            "max_time=${request.fightLengthSeconds}",
            "json2=/simc/profiles/${outputFile.name}",
        )
    }

    /**
     * Parses simulation results from SimC JSON output.
     */
    fun parseSimulationResults(jsonContent: String): List<SimulationResult> {
        val results = mutableListOf<SimulationResult>()

        try {
            val root = objectMapper.readTree(jsonContent)
            val profilesets = root.path("sim").path("profilesets").path("results")

            if (profilesets.isArray) {
                for (profileset in profilesets) {
                    val name = profileset.path("name").asText("")
                    val meanPct = profileset.path("mean_pct").asDouble(0.0)

                    val itemId = extractItemIdFromName(name)
                    val slot = extractSlotFromName(name)

                    if (itemId != null && slot != null) {
                        // Calculate DPS gain from percentage (baseline from first player)
                        val baseDps =
                            root.path("sim").path("players").firstOrNull()
                                ?.path("collected_data")?.path("dps")?.path("mean")?.asDouble(100000.0)
                                ?: 100000.0
                        val dpsGain = baseDps * (meanPct / 100.0)

                        results.add(
                            SimulationResult.create(
                                itemId = itemId,
                                itemName = name,
                                slot = slot,
                                dpsGain = dpsGain,
                                percentGain = meanPct,
                                simulatedAt = Instant.now(),
                            ),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse simulation results: ${e.message}", e)
        }

        return results
    }

    /**
     * Extracts the WoW item ID from a profileset name.
     * Format: "slot=,id=12345,ilevel=639"
     */
    fun extractItemIdFromName(name: String): Long? {
        val idMatch = Regex("id=(\\d+)").find(name)
        return idMatch?.groupValues?.get(1)?.toLongOrNull()
    }

    /**
     * Extracts the equipment slot from a profileset name.
     * Format: "slot=,id=12345,ilevel=639"
     */
    fun extractSlotFromName(name: String): String? {
        val slotMatch = Regex("^(\\w+)=").find(name)
        return slotMatch?.groupValues?.get(1)
    }

    private fun JsonNode.firstOrNull(): JsonNode? {
        return if (this.isArray && this.size() > 0) this[0] else null
    }
}
