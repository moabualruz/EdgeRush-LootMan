package com.edgerush.lootman.domain.simulation.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SimulationProfileTest : UnitTest() {
    @Nested
    inner class Creation {
        @Test
        fun `should create SimulationProfile with valid data`() {
            // Arrange
            val guildId = "test-guild-123"
            val characterName = "Testchar"
            val characterRealm = "TestRealm"
            val profileContent =
                """
                warrior="Testchar"
                level=80
                race=human
                spec=fury
                """.trimIndent()
            val createdAt = Instant.now()

            // Act
            val profile =
                SimulationProfile.create(
                    guildId = guildId,
                    characterName = characterName,
                    characterRealm = characterRealm,
                    profileContent = profileContent,
                    createdAt = createdAt,
                )

            // Assert
            profile.guildId shouldBe guildId
            profile.characterName shouldBe characterName
            profile.characterRealm shouldBe characterRealm
            profile.profileContent shouldBe profileContent
            profile.createdAt shouldBe createdAt
        }

        @Test
        fun `should throw exception when guildId is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationProfile.create(
                        guildId = "",
                        characterName = "Testchar",
                        characterRealm = "TestRealm",
                        profileContent = "warrior=\"Testchar\"",
                        createdAt = Instant.now(),
                    )
                }
            exception.message shouldContain "guildId"
        }

        @Test
        fun `should throw exception when characterName is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationProfile.create(
                        guildId = "guild-123",
                        characterName = "  ",
                        characterRealm = "TestRealm",
                        profileContent = "warrior=\"Testchar\"",
                        createdAt = Instant.now(),
                    )
                }
            exception.message shouldContain "characterName"
        }

        @Test
        fun `should throw exception when characterRealm is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationProfile.create(
                        guildId = "guild-123",
                        characterName = "Testchar",
                        characterRealm = "",
                        profileContent = "warrior=\"Testchar\"",
                        createdAt = Instant.now(),
                    )
                }
            exception.message shouldContain "characterRealm"
        }

        @Test
        fun `should throw exception when profileContent is blank`() {
            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    SimulationProfile.create(
                        guildId = "guild-123",
                        characterName = "Testchar",
                        characterRealm = "TestRealm",
                        profileContent = "",
                        createdAt = Instant.now(),
                    )
                }
            exception.message shouldContain "profileContent"
        }
    }

    @Nested
    inner class CharacterIdentifier {
        @Test
        fun `should return correct character identifier`() {
            // Arrange
            val profile =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    profileContent = "warrior=\"Testchar\"",
                    createdAt = Instant.now(),
                )

            // Act
            val identifier = profile.characterIdentifier

            // Assert
            identifier shouldBe "Testchar-TestRealm"
        }
    }

    @Nested
    inner class Equality {
        @Test
        fun `profiles with same guildId, characterName, and characterRealm should be equal`() {
            // Arrange
            val now = Instant.now()
            val profile1 =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    profileContent = "warrior=\"Testchar\"",
                    createdAt = now,
                )
            val profile2 =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    profileContent = "warrior=\"Testchar\"",
                    createdAt = now,
                )

            // Assert
            profile1 shouldBe profile2
        }

        @Test
        fun `profiles with different characterName should not be equal`() {
            // Arrange
            val now = Instant.now()
            val profile1 =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar1",
                    characterRealm = "TestRealm",
                    profileContent = "warrior=\"Testchar\"",
                    createdAt = now,
                )
            val profile2 =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar2",
                    characterRealm = "TestRealm",
                    profileContent = "warrior=\"Testchar\"",
                    createdAt = now,
                )

            // Assert
            profile1 shouldNotBe profile2
        }
    }

    @Nested
    inner class ProfileContentValidation {
        @Test
        fun `should preserve multiline profile content`() {
            // Arrange
            val multilineContent =
                """
                |warrior="Testchar"
                |level=80
                |race=human
                |spec=fury
                |talents=xxx
                |
                |# Gear
                |head=,id=12345
                |neck=,id=12346
                """.trimMargin()

            // Act
            val profile =
                SimulationProfile.create(
                    guildId = "guild-123",
                    characterName = "Testchar",
                    characterRealm = "TestRealm",
                    profileContent = multilineContent,
                    createdAt = Instant.now(),
                )

            // Assert
            profile.profileContent shouldBe multilineContent
            profile.profileContent shouldContain "warrior=\"Testchar\""
            profile.profileContent shouldContain "head=,id=12345"
        }
    }
}
