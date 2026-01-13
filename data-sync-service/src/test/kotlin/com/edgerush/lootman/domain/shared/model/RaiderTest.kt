package com.edgerush.lootman.domain.shared.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for Raider, CharacterClass, Role, and RaiderStatus.
 */
class RaiderTest : UnitTest() {

    // region Test Fixtures

    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("guild-123"),
        characterName: String = "Arthas",
        realm: String = "Icecrown",
        characterClass: CharacterClass = CharacterClass.DEATH_KNIGHT,
        role: Role = Role.TANK,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.of(2024, 1, 15, 10, 30),
        wowauditId: Long? = 12345L
    ) = Raider(
        id = id,
        guildId = guildId,
        characterName = characterName,
        realm = realm,
        characterClass = characterClass,
        role = role,
        rank = rank,
        status = status,
        joinDate = joinDate,
        wowauditId = wowauditId
    )

    // endregion

    @Nested
    inner class RaiderCreationTests {

        @Test
        fun `should create valid raider with all fields`() {
            // Arrange
            val joinDate = LocalDateTime.of(2024, 3, 1, 20, 0)

            // Act
            val raider = createRaider(
                id = RaiderId(42L),
                guildId = GuildId("edge-rush"),
                characterName = "Thrall",
                realm = "Draenor",
                characterClass = CharacterClass.SHAMAN,
                role = Role.DPS,
                rank = "Officer",
                status = RaiderStatus.ACTIVE,
                joinDate = joinDate,
                wowauditId = 98765L
            )

            // Assert
            raider.id shouldBe RaiderId(42L)
            raider.guildId shouldBe GuildId("edge-rush")
            raider.characterName shouldBe "Thrall"
            raider.realm shouldBe "Draenor"
            raider.characterClass shouldBe CharacterClass.SHAMAN
            raider.role shouldBe Role.DPS
            raider.rank shouldBe "Officer"
            raider.status shouldBe RaiderStatus.ACTIVE
            raider.joinDate shouldBe joinDate
            raider.wowauditId shouldBe 98765L
        }

        @Test
        fun `should create raider with nullable fields as null`() {
            // Arrange & Act
            val raider = createRaider(
                rank = null,
                joinDate = null,
                wowauditId = null
            )

            // Assert
            raider.rank.shouldBeNull()
            raider.joinDate.shouldBeNull()
            raider.wowauditId.shouldBeNull()
        }

        @Test
        fun `should throw exception when character name is blank`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createRaider(characterName = "")
            }
            exception.message shouldBe "Character name cannot be blank"
        }

        @Test
        fun `should throw exception when character name contains only whitespace`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createRaider(characterName = "   ")
            }
            exception.message shouldBe "Character name cannot be blank"
        }

        @Test
        fun `should throw exception when realm is blank`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createRaider(realm = "")
            }
            exception.message shouldBe "Realm cannot be blank"
        }

        @Test
        fun `should throw exception when realm contains only whitespace`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createRaider(realm = "   \t\n")
            }
            exception.message shouldBe "Realm cannot be blank"
        }

        @Test
        fun `should be immutable data class`() {
            // Arrange
            val raider1 = createRaider(id = RaiderId(1L), characterName = "Test")
            val raider2 = createRaider(id = RaiderId(1L), characterName = "Test")

            // Assert
            raider1 shouldBe raider2
            raider1.hashCode() shouldBe raider2.hashCode()
        }
    }

    @Nested
    inner class IsEligibleForLootTests {

        @Test
        fun `should be eligible for loot when status is ACTIVE`() {
            // Arrange
            val raider = createRaider(status = RaiderStatus.ACTIVE)

            // Act & Assert
            raider.isEligibleForLoot() shouldBe true
        }

        @Test
        fun `should not be eligible for loot when status is INACTIVE`() {
            // Arrange
            val raider = createRaider(status = RaiderStatus.INACTIVE)

            // Act & Assert
            raider.isEligibleForLoot() shouldBe false
        }

        @Test
        fun `should not be eligible for loot when status is BENCHED`() {
            // Arrange
            val raider = createRaider(status = RaiderStatus.BENCHED)

            // Act & Assert
            raider.isEligibleForLoot() shouldBe false
        }

        @Test
        fun `should not be eligible for loot when status is TRIAL`() {
            // Arrange
            val raider = createRaider(status = RaiderStatus.TRIAL)

            // Act & Assert
            raider.isEligibleForLoot() shouldBe false
        }

        @Test
        fun `should not be eligible for loot when status is ALUMNI`() {
            // Arrange
            val raider = createRaider(status = RaiderStatus.ALUMNI)

            // Act & Assert
            raider.isEligibleForLoot() shouldBe false
        }
    }

    @Nested
    inner class GetFullNameTests {

        @Test
        fun `should return name-realm format`() {
            // Arrange
            val raider = createRaider(
                characterName = "Illidan",
                realm = "Black Temple"
            )

            // Act
            val fullName = raider.getFullName()

            // Assert
            fullName shouldBe "Illidan-Black Temple"
        }

        @Test
        fun `should handle single word names and realms`() {
            // Arrange
            val raider = createRaider(
                characterName = "Sylvanas",
                realm = "Silvermoon"
            )

            // Act
            val fullName = raider.getFullName()

            // Assert
            fullName shouldBe "Sylvanas-Silvermoon"
        }

        @Test
        fun `should preserve special characters in name`() {
            // Arrange
            val raider = createRaider(
                characterName = "Kael'thas",
                realm = "Quel'Thalas"
            )

            // Act
            val fullName = raider.getFullName()

            // Assert
            fullName shouldBe "Kael'thas-Quel'Thalas"
        }
    }

    @Nested
    inner class CharacterClassTests {

        @Test
        fun `should have all 13 character classes`() {
            // Act
            val classes = CharacterClass.entries

            // Assert
            classes.size shouldBe 13
        }

        @Test
        fun `should parse character class from string with spaces`() {
            // Act & Assert
            CharacterClass.fromString("Death Knight") shouldBe CharacterClass.DEATH_KNIGHT
            CharacterClass.fromString("Demon Hunter") shouldBe CharacterClass.DEMON_HUNTER
        }

        @Test
        fun `should parse character class case insensitively`() {
            // Act & Assert
            CharacterClass.fromString("MAGE") shouldBe CharacterClass.MAGE
            CharacterClass.fromString("mage") shouldBe CharacterClass.MAGE
            CharacterClass.fromString("Mage") shouldBe CharacterClass.MAGE
            CharacterClass.fromString("DEATH KNIGHT") shouldBe CharacterClass.DEATH_KNIGHT
            CharacterClass.fromString("death knight") shouldBe CharacterClass.DEATH_KNIGHT
        }

        @Test
        fun `should parse single word class names`() {
            // Act & Assert
            CharacterClass.fromString("Druid") shouldBe CharacterClass.DRUID
            CharacterClass.fromString("Evoker") shouldBe CharacterClass.EVOKER
            CharacterClass.fromString("Hunter") shouldBe CharacterClass.HUNTER
            CharacterClass.fromString("Mage") shouldBe CharacterClass.MAGE
            CharacterClass.fromString("Monk") shouldBe CharacterClass.MONK
            CharacterClass.fromString("Paladin") shouldBe CharacterClass.PALADIN
            CharacterClass.fromString("Priest") shouldBe CharacterClass.PRIEST
            CharacterClass.fromString("Rogue") shouldBe CharacterClass.ROGUE
            CharacterClass.fromString("Shaman") shouldBe CharacterClass.SHAMAN
            CharacterClass.fromString("Warlock") shouldBe CharacterClass.WARLOCK
            CharacterClass.fromString("Warrior") shouldBe CharacterClass.WARRIOR
        }

        @Test
        fun `should throw exception for unknown character class`() {
            // Arrange, Act & Assert
            shouldThrow<NoSuchElementException> {
                CharacterClass.fromString("Unknown")
            }
        }

        @Test
        fun `should throw exception for empty string`() {
            // Arrange, Act & Assert
            shouldThrow<NoSuchElementException> {
                CharacterClass.fromString("")
            }
        }
    }

    @Nested
    inner class RoleTests {

        @Test
        fun `should have all 3 roles`() {
            // Act
            val roles = Role.entries

            // Assert
            roles.size shouldBe 3
            roles shouldBe listOf(Role.TANK, Role.HEALER, Role.DPS)
        }

        @Test
        fun `should parse role from string case insensitively`() {
            // Act & Assert
            Role.fromString("TANK") shouldBe Role.TANK
            Role.fromString("tank") shouldBe Role.TANK
            Role.fromString("Tank") shouldBe Role.TANK
            Role.fromString("HEALER") shouldBe Role.HEALER
            Role.fromString("healer") shouldBe Role.HEALER
            Role.fromString("DPS") shouldBe Role.DPS
            Role.fromString("dps") shouldBe Role.DPS
        }

        @Test
        fun `should throw exception for unknown role`() {
            // Arrange, Act & Assert
            shouldThrow<NoSuchElementException> {
                Role.fromString("Support")
            }
        }

        @Test
        fun `should throw exception for empty string`() {
            // Arrange, Act & Assert
            shouldThrow<NoSuchElementException> {
                Role.fromString("")
            }
        }
    }

    @Nested
    inner class RaiderStatusTests {

        @Test
        fun `should have all 5 raider statuses`() {
            // Act
            val statuses = RaiderStatus.entries

            // Assert
            statuses.size shouldBe 5
            statuses shouldBe listOf(
                RaiderStatus.ACTIVE,
                RaiderStatus.INACTIVE,
                RaiderStatus.BENCHED,
                RaiderStatus.TRIAL,
                RaiderStatus.ALUMNI
            )
        }

        @Test
        fun `should parse status from string case insensitively`() {
            // Act & Assert
            RaiderStatus.fromString("ACTIVE") shouldBe RaiderStatus.ACTIVE
            RaiderStatus.fromString("active") shouldBe RaiderStatus.ACTIVE
            RaiderStatus.fromString("Active") shouldBe RaiderStatus.ACTIVE
            RaiderStatus.fromString("INACTIVE") shouldBe RaiderStatus.INACTIVE
            RaiderStatus.fromString("inactive") shouldBe RaiderStatus.INACTIVE
            RaiderStatus.fromString("BENCHED") shouldBe RaiderStatus.BENCHED
            RaiderStatus.fromString("TRIAL") shouldBe RaiderStatus.TRIAL
            RaiderStatus.fromString("ALUMNI") shouldBe RaiderStatus.ALUMNI
        }

        @Test
        fun `should return null for unknown status`() {
            // Act & Assert
            RaiderStatus.fromString("Unknown").shouldBeNull()
            RaiderStatus.fromString("RETIRED").shouldBeNull()
            RaiderStatus.fromString("").shouldBeNull()
        }
    }

    @Nested
    inner class RaiderCopyTests {

        @Test
        fun `should allow status changes via copy`() {
            // Arrange
            val activeRaider = createRaider(status = RaiderStatus.ACTIVE)

            // Act
            val benchedRaider = activeRaider.copy(status = RaiderStatus.BENCHED)

            // Assert
            activeRaider.status shouldBe RaiderStatus.ACTIVE
            benchedRaider.status shouldBe RaiderStatus.BENCHED
            benchedRaider.characterName shouldBe activeRaider.characterName
        }

        @Test
        fun `should allow role changes via copy`() {
            // Arrange
            val tankRaider = createRaider(
                characterClass = CharacterClass.PALADIN,
                role = Role.TANK
            )

            // Act
            val healerRaider = tankRaider.copy(role = Role.HEALER)

            // Assert
            tankRaider.role shouldBe Role.TANK
            healerRaider.role shouldBe Role.HEALER
        }
    }
}
