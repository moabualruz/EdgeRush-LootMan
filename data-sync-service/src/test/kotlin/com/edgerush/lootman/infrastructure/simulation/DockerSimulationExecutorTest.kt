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
    }
}
