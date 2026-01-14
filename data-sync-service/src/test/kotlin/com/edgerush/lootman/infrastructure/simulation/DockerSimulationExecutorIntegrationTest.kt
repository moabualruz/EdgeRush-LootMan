package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.EnabledIfDockerAvailable
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant

/**
 * Docker-dependent integration tests for DockerSimulationExecutor.
 *
 * These tests actually execute Docker commands and require Docker to be available.
 * They are skipped when Docker is not available.
 */
@EnabledIfDockerAvailable
class DockerSimulationExecutorIntegrationTest : UnitTest() {

    private lateinit var executor: DockerSimulationExecutor

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        executor = DockerSimulationExecutor(
            dockerImage = "simulationcraftorg/simc",
            profileDirectory = tempDir.toString(),
            dockerCommand = "docker",
            timeoutMinutes = 5
        )
    }

    private fun createProfile(): SimulationProfile {
        return SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = """
                warrior="Testchar"
                level=80
                race=human
                spec=fury
            """.trimIndent(),
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class ExecuteWithDocker {
        @Test
        fun `should handle docker execution timeout gracefully`() = runBlocking {
            // Arrange - Use a very short timeout to trigger timeout behavior
            val shortTimeoutExecutor = DockerSimulationExecutor(
                dockerImage = "simulationcraftorg/simc",
                profileDirectory = tempDir.toString(),
                dockerCommand = "docker",
                timeoutMinutes = 0 // Will timeout immediately
            )
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act
            val result = shortTimeoutExecutor.execute(request)

            // Assert
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldNotBe null
        }

        @Test
        fun `should handle non-existent docker image`() = runBlocking {
            // Arrange - Use a non-existent image
            val badImageExecutor = DockerSimulationExecutor(
                dockerImage = "nonexistent/fake-image:99999",
                profileDirectory = tempDir.toString(),
                dockerCommand = "docker",
                timeoutMinutes = 1
            )
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act
            val result = badImageExecutor.execute(request)

            // Assert
            result.isFailure shouldBe true
        }

        @Test
        fun `should handle invalid docker command`() = runBlocking {
            // Arrange - Use a non-existent docker command
            val badCommandExecutor = DockerSimulationExecutor(
                dockerImage = "simulationcraftorg/simc",
                profileDirectory = tempDir.toString(),
                dockerCommand = "nonexistent-docker-command",
                timeoutMinutes = 1
            )
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act
            val result = badCommandExecutor.execute(request)

            // Assert
            result.isFailure shouldBe true
        }

        @Test
        fun `should create profile file before execution`() = runBlocking {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act - Just write the profile, don't run Docker (faster test)
            val profileFile = executor.writeProfileToFile(request)

            // Assert
            profileFile.exists() shouldBe true
            profileFile.readText().contains("warrior=\"Testchar\"") shouldBe true

            // Cleanup
            profileFile.delete()
        }
    }

    @Nested
    inner class DockerAvailabilityCheck {
        @Test
        fun `docker info command should succeed when Docker is available`() {
            // Arrange
            val processBuilder = ProcessBuilder("docker", "info")
                .redirectErrorStream(true)

            // Act
            val process = processBuilder.start()
            val exitCode = process.waitFor()

            // Assert
            exitCode shouldBe 0
        }

        @Test
        fun `docker version command should succeed when Docker is available`() {
            // Arrange
            val processBuilder = ProcessBuilder("docker", "version")
                .redirectErrorStream(true)

            // Act
            val process = processBuilder.start()
            val exitCode = process.waitFor()

            // Assert
            exitCode shouldBe 0
        }
    }
}
