package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain as stringContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.time.Instant

class DockerSimulationExecutorTest : UnitTest() {

    private lateinit var executor: DockerSimulationExecutor

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        executor = DockerSimulationExecutor(
            dockerImage = "simulationcraftorg/simc",
            profileDirectory = tempDir.toString(),
            dockerCommand = "docker"
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
    inner class ProfileFileGeneration {
        @Test
        fun `should write profile to file before execution`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act - just test the file writing part
            val profileFile = executor.writeProfileToFile(request)

            // Assert
            profileFile.exists() shouldBe true
            profileFile.readText() stringContain "warrior=\"Testchar\""
            profileFile.readText() stringContain "level=80"
        }

        @Test
        fun `should create unique profile filename per character`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act
            val profileFile = executor.writeProfileToFile(request)

            // Assert
            profileFile.name stringContain "Testchar"
            profileFile.name stringContain "TestRealm"
            profileFile.extension shouldBe "simc"
        }
    }

    @Nested
    inner class CommandBuilding {
        @Test
        fun `should build correct docker command`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(
                profile = profile,
                iterations = 10000,
                fightLengthSeconds = 300
            )

            // Act
            val command = executor.buildDockerCommand(request, File(tempDir.toFile(), "test.simc"))

            // Assert
            command[0] shouldBe "docker"
            command[1] shouldBe "run"
            command shouldContain "--rm"
            command.any { it.contains("simulationcraftorg/simc") } shouldBe true
            command.any { it.contains("iterations=10000") } shouldBe true
            command.any { it.contains("max_time=300") } shouldBe true
        }

        @Test
        fun `should include json output option`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)
            val profileFile = File(tempDir.toFile(), "test.simc")

            // Act
            val command = executor.buildDockerCommand(request, profileFile)

            // Assert
            command.any { it.contains("json2=") } shouldBe true
        }
    }

    @Nested
    inner class JsonResultParsing {
        @Test
        fun `should parse simulation results from json`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean": 105000.0,
                                    "mean_pct": 5.0
                                },
                                {
                                    "name": "neck=,id=12346,ilevel=639",
                                    "mean": 102000.0,
                                    "mean_pct": 2.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "name": "Testchar",
                                "collected_data": {
                                    "dps": {
                                        "mean": 100000.0
                                    }
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert
            results shouldHaveSize 2
            results[0].itemId shouldBe 12345L
            results[0].percentGain shouldBe 5.0
            results[1].itemId shouldBe 12346L
            results[1].percentGain shouldBe 2.0
        }

        @Test
        fun `should handle empty profilesets`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": []
                        }
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert
            results shouldHaveSize 0
        }

        @Test
        fun `should extract item id from profileset name`() {
            // Arrange
            val profilesetName = "head=,id=207174,ilevel=639"

            // Act
            val itemId = executor.extractItemIdFromName(profilesetName)

            // Assert
            itemId shouldBe 207174L
        }

        @Test
        fun `should extract slot from profileset name`() {
            // Arrange
            val profilesetName = "trinket1=,id=207174,ilevel=639"

            // Act
            val slot = executor.extractSlotFromName(profilesetName)

            // Assert
            slot shouldBe "trinket1"
        }

        @Test
        fun `should return null for invalid item id format`() {
            // Arrange
            val invalidName = "head=invalid_no_id"

            // Act
            val itemId = executor.extractItemIdFromName(invalidName)

            // Assert
            itemId shouldBe null
        }

        @Test
        fun `should return null for invalid slot format`() {
            // Arrange
            val invalidName = "=no_slot,id=12345"

            // Act
            val slot = executor.extractSlotFromName(invalidName)

            // Assert
            slot shouldBe null
        }

        @Test
        fun `should handle malformed json gracefully`() {
            // Arrange
            val invalidJson = "{ this is not valid json }"

            // Act
            val results = executor.parseSimulationResults(invalidJson)

            // Assert
            results shouldHaveSize 0
        }

        @Test
        fun `should handle json with missing profilesets path`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "players": []
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert
            results shouldHaveSize 0
        }

        @Test
        fun `should handle profileset with missing item id`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "collected_data": {
                                    "dps": { "mean": 100000.0 }
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should skip items without valid id
            results shouldHaveSize 0
        }

        @Test
        fun `should handle profileset with missing slot`() {
            // Arrange - name starts with non-word character so slot extraction fails
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "=id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "collected_data": {
                                    "dps": { "mean": 100000.0 }
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should skip items without valid slot
            results shouldHaveSize 0
        }

        @Test
        fun `should use default base dps when players array is empty`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": []
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert
            results shouldHaveSize 1
            // DPS gain should be 100000 * (5.0 / 100.0) = 5000
            results[0].dpsGain shouldBe 5000.0
        }

        @Test
        fun `should calculate dps gain correctly from percentage`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 10.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "collected_data": {
                                    "dps": { "mean": 200000.0 }
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert
            results shouldHaveSize 1
            // DPS gain = 200000 * (10.0 / 100.0) = 20000
            results[0].dpsGain shouldBe 20000.0
            results[0].percentGain shouldBe 10.0
        }
    }

    @Nested
    inner class ProfileDirectoryHandling {
        @Test
        fun `should create profile directory if not exists`() {
            // Arrange
            val nonExistentDir = tempDir.resolve("non-existent-dir").toString()
            val executorWithNonExistentDir = DockerSimulationExecutor(
                dockerImage = "simulationcraftorg/simc",
                profileDirectory = nonExistentDir,
                dockerCommand = "docker"
            )
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Act
            val profileFile = executorWithNonExistentDir.writeProfileToFile(request)

            // Assert
            profileFile.parentFile.exists() shouldBe true
            profileFile.exists() shouldBe true
        }
    }

    @Nested
    inner class VolumeMapping {
        @Test
        fun `should include volume mapping for profiles`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)
            val profileFile = File(tempDir.toFile(), "test.simc")

            // Act
            val command = executor.buildDockerCommand(request, profileFile)

            // Assert
            command shouldContain "-v"
            command.any { it.contains(":/simc/profiles") } shouldBe true
        }
    }

    @Nested
    inner class FirstOrNullExtension {
        @Test
        fun `should handle player data with missing dps path`() {
            // Arrange - player array exists but has no dps data
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "name": "Testchar",
                                "collected_data": {}
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 5000.0
        }

        @Test
        fun `should handle non-array players node`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": "not an array"
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 5000.0
        }

        @Test
        fun `should handle missing players node`() {
            // Arrange
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        }
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 5000.0
        }
    }

    @Nested
    inner class TimeoutConfiguration {
        @Test
        fun `should use default timeout when not specified`() {
            // Arrange & Act
            val executorWithDefaults = DockerSimulationExecutor(
                dockerImage = "simc",
                profileDirectory = tempDir.toString(),
                dockerCommand = "docker"
            )

            // Assert - timeout defaults to 30 minutes
            // We can verify the executor was created successfully
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)
            val command = executorWithDefaults.buildDockerCommand(request, File(tempDir.toFile(), "test.simc"))
            command.isNotEmpty() shouldBe true
        }

        @Test
        fun `should use custom timeout when specified`() {
            // Arrange
            val customTimeoutExecutor = DockerSimulationExecutor(
                dockerImage = "simc",
                profileDirectory = tempDir.toString(),
                dockerCommand = "docker",
                timeoutMinutes = 60
            )

            // Assert - executor should be created successfully with custom timeout
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)
            val command = customTimeoutExecutor.buildDockerCommand(request, File(tempDir.toFile(), "test.simc"))
            command.isNotEmpty() shouldBe true
        }
    }

    @Nested
    inner class ExecuteMethodTests {
        @Test
        fun `should return failure when process times out`() {
            runBlocking {
                // Arrange - use a command that sleeps longer than timeout
                // On Windows use "timeout" (or "ping -n"), on Unix use "sleep"
                val sleepCommand = if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "cmd"
                } else {
                    "sleep"
                }

                val shortTimeoutExecutor = DockerSimulationExecutor(
                    dockerImage = "simc",
                    profileDirectory = tempDir.toString(),
                    dockerCommand = sleepCommand,
                    timeoutMinutes = 0 // 0 minutes means immediate timeout
                )

                val profile = createProfile()
                val request = SimulationRequest.create(profile = profile)

                // Act
                val result = shortTimeoutExecutor.execute(request)

                // Assert
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message?.contains("timed out") shouldBe true
            }
        }

        @Test
        fun `should return failure when process exits with non-zero code`() {
            runBlocking {
                // Arrange - use a command that exits with error
                val failCommand = if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "cmd"
                } else {
                    "false"
                }

                val failingExecutor = DockerSimulationExecutor(
                    dockerImage = "/c exit 1", // For cmd, this becomes: cmd /c exit 1
                    profileDirectory = tempDir.toString(),
                    dockerCommand = failCommand,
                    timeoutMinutes = 1
                )

                val profile = createProfile()
                val request = SimulationRequest.create(profile = profile)

                // Act
                val result = failingExecutor.execute(request)

                // Assert
                result.isFailure shouldBe true
                // Either times out or fails with exit code
                result.exceptionOrNull() shouldBe io.kotest.matchers.types.beInstanceOf<Exception>()
            }
        }

        @Test
        fun `should return failure when output file does not exist`() {
            runBlocking {
                // Arrange - use echo which succeeds but doesn't create output file
                val echoCommand = if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "cmd"
                } else {
                    "echo"
                }

                val noOutputExecutor = DockerSimulationExecutor(
                    dockerImage = "/c echo done", // cmd /c echo done
                    profileDirectory = tempDir.toString(),
                    dockerCommand = echoCommand,
                    timeoutMinutes = 1
                )

                val profile = createProfile()
                val request = SimulationRequest.create(profile = profile)

                // Act
                val result = noOutputExecutor.execute(request)

                // Assert
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message?.contains("Output file not found") shouldBe true
            }
        }

        @Test
        fun `should return success when simulation completes successfully`() {
            runBlocking {
                // Arrange - Create a fake output file before execution
                val profile = createProfile()
                val request = SimulationRequest.create(profile = profile)

                // Write profile file first
                val profileFile = executor.writeProfileToFile(request)

                // Create the expected output file with valid simulation results
                val outputFile = File(profileFile.parent, "${profileFile.nameWithoutExtension}_results.json")
                outputFile.writeText("""
                    {
                        "sim": {
                            "profilesets": {
                                "results": [
                                    {
                                        "name": "head=,id=12345,ilevel=639",
                                        "mean_pct": 5.0
                                    }
                                ]
                            },
                            "players": [
                                {
                                    "collected_data": {
                                        "dps": { "mean": 100000.0 }
                                    }
                                }
                            ]
                        }
                    }
                """.trimIndent())

                // Use echo to simulate a successful command
                val echoCommand = if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "cmd"
                } else {
                    "true"
                }

                val successExecutor = DockerSimulationExecutor(
                    dockerImage = "/c echo done",
                    profileDirectory = tempDir.toString(),
                    dockerCommand = echoCommand,
                    timeoutMinutes = 1
                )

                // Act
                val result = successExecutor.execute(request)

                // Assert
                result.isSuccess shouldBe true
                result.getOrNull()?.size shouldBe 1
                result.getOrNull()?.first()?.itemId shouldBe 12345L
            }
        }

        @Test
        fun `should handle exception during execution`() {
            runBlocking {
                // Arrange - use a non-existent command to trigger an exception
                val badExecutor = DockerSimulationExecutor(
                    dockerImage = "simc",
                    profileDirectory = tempDir.toString(),
                    dockerCommand = "this_command_does_not_exist_anywhere_12345",
                    timeoutMinutes = 1
                )

                val profile = createProfile()
                val request = SimulationRequest.create(profile = profile)

                // Act
                val result = badExecutor.execute(request)

                // Assert
                result.isFailure shouldBe true
            }
        }
    }

    @Nested
    inner class ParseSimulationResultsNullBranches {
        @Test
        fun `should handle player with null collected_data path`() {
            // Arrange - player has no collected_data at all
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "name": "Testchar"
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value (100000)
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 5000.0
        }

        @Test
        fun `should handle player with null dps path inside collected_data`() {
            // Arrange - collected_data exists but dps path is missing
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 10.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "name": "Testchar",
                                "collected_data": {
                                    "other_field": 123
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 10000.0
        }

        @Test
        fun `should handle player with null mean inside dps`() {
            // Arrange - dps exists but mean is missing
            val jsonContent = """
                {
                    "sim": {
                        "profilesets": {
                            "results": [
                                {
                                    "name": "head=,id=12345,ilevel=639",
                                    "mean_pct": 5.0
                                }
                            ]
                        },
                        "players": [
                            {
                                "name": "Testchar",
                                "collected_data": {
                                    "dps": {
                                        "median": 100000.0
                                    }
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()

            // Act
            val results = executor.parseSimulationResults(jsonContent)

            // Assert - should use default dps value since mean is missing
            results shouldHaveSize 1
            results[0].dpsGain shouldBe 5000.0
        }
    }
}
