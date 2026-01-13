package com.edgerush.lootman.api.raider

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Unit tests for Raider DTO mapping.
 *
 * Tests verify:
 * - CreateRaiderRequest validation fields
 * - UpdateRaiderRequest validation fields
 * - RaiderResponse mapping from domain model
 * - RaiderListResponse mapping
 * - Computed fields (fullName, isEligibleForLoot)
 */
class RaiderDtoTest : UnitTest() {

    @Nested
    inner class CreateRaiderRequestTests {
        @Test
        fun `should create valid request with all required fields`() {
            // Given / When
            val request = CreateRaiderRequest(
                id = 1L,
                guildId = "test-guild",
                characterName = "Testchar",
                realm = "TestRealm",
                characterClass = "WARRIOR",
                role = "DPS"
            )

            // Then
            request.id shouldBe 1L
            request.guildId shouldBe "test-guild"
            request.characterName shouldBe "Testchar"
            request.realm shouldBe "TestRealm"
            request.characterClass shouldBe "WARRIOR"
            request.role shouldBe "DPS"
            request.status shouldBe "ACTIVE"  // default
            request.rank shouldBe null  // optional
        }

        @Test
        fun `should create request with optional fields`() {
            // Given
            val joinDate = LocalDateTime.of(2024, 1, 1, 0, 0)

            // When
            val request = CreateRaiderRequest(
                id = 1L,
                guildId = "test-guild",
                characterName = "Testchar",
                realm = "TestRealm",
                characterClass = "PALADIN",
                role = "TANK",
                rank = "Guild Master",
                status = "BENCHED",
                joinDate = joinDate,
                wowauditId = 12345L
            )

            // Then
            request.rank shouldBe "Guild Master"
            request.status shouldBe "BENCHED"
            request.joinDate shouldBe joinDate
            request.wowauditId shouldBe 12345L
        }
    }

    @Nested
    inner class UpdateRaiderRequestTests {
        @Test
        fun `should create update request with partial fields`() {
            // Given / When
            val request = UpdateRaiderRequest(
                status = "BENCHED"
            )

            // Then
            request.status shouldBe "BENCHED"
            request.characterName shouldBe null
            request.realm shouldBe null
            request.characterClass shouldBe null
            request.role shouldBe null
            request.rank shouldBe null
        }

        @Test
        fun `should create update request with all fields`() {
            // Given / When
            val request = UpdateRaiderRequest(
                characterName = "NewName",
                realm = "NewRealm",
                characterClass = "MAGE",
                role = "DPS",
                rank = "Officer",
                status = "ACTIVE"
            )

            // Then
            request.characterName shouldBe "NewName"
            request.realm shouldBe "NewRealm"
            request.characterClass shouldBe "MAGE"
            request.role shouldBe "DPS"
            request.rank shouldBe "Officer"
            request.status shouldBe "ACTIVE"
        }
    }

    @Nested
    inner class RaiderResponseMappingTests {
        @Test
        fun `should map Raider to RaiderResponse correctly`() {
            // Given
            val joinDate = LocalDateTime.of(2024, 1, 1, 0, 0)
            val raider = Raider(
                id = RaiderId(123L),
                guildId = GuildId("test-guild"),
                characterName = "Testchar",
                realm = "TestRealm",
                characterClass = CharacterClass.WARRIOR,
                role = Role.DPS,
                rank = "Raider",
                status = RaiderStatus.ACTIVE,
                joinDate = joinDate,
                wowauditId = 9876L
            )

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.id shouldBe 123L
            response.guildId shouldBe "test-guild"
            response.characterName shouldBe "Testchar"
            response.realm shouldBe "TestRealm"
            response.characterClass shouldBe "WARRIOR"
            response.role shouldBe "DPS"
            response.rank shouldBe "Raider"
            response.status shouldBe "ACTIVE"
            response.joinDate shouldBe joinDate
            response.wowauditId shouldBe 9876L
        }

        @Test
        fun `should format fullName correctly`() {
            // Given
            val raider = createRaider(characterName = "MyChar", realm = "MyRealm")

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.fullName shouldBe "MyChar-MyRealm"
        }

        @Test
        fun `should calculate isEligibleForLoot true for ACTIVE raider`() {
            // Given
            val raider = createRaider(status = RaiderStatus.ACTIVE)

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.isEligibleForLoot shouldBe true
        }

        @Test
        fun `should calculate isEligibleForLoot false for BENCHED raider`() {
            // Given
            val raider = createRaider(status = RaiderStatus.BENCHED)

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.isEligibleForLoot shouldBe false
        }

        @Test
        fun `should calculate isEligibleForLoot false for INACTIVE raider`() {
            // Given
            val raider = createRaider(status = RaiderStatus.INACTIVE)

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.isEligibleForLoot shouldBe false
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val raider = createRaider(rank = null, wowauditId = null, joinDate = null)

            // When
            val response = RaiderResponse.from(raider)

            // Then
            response.rank shouldBe null
            response.wowauditId shouldBe null
            response.joinDate shouldBe null
        }
    }

    @Nested
    inner class RaiderListResponseMappingTests {
        @Test
        fun `should map list of raiders to response`() {
            // Given
            val raiders = listOf(
                createRaider(id = RaiderId(1L), characterName = "Raider1"),
                createRaider(id = RaiderId(2L), characterName = "Raider2"),
                createRaider(id = RaiderId(3L), characterName = "Raider3")
            )

            // When
            val response = RaiderListResponse.from(raiders)

            // Then
            response.count shouldBe 3
            response.raiders.size shouldBe 3
            response.raiders[0].characterName shouldBe "Raider1"
            response.raiders[1].characterName shouldBe "Raider2"
            response.raiders[2].characterName shouldBe "Raider3"
        }

        @Test
        fun `should handle empty list`() {
            // Given
            val raiders = emptyList<Raider>()

            // When
            val response = RaiderListResponse.from(raiders)

            // Then
            response.count shouldBe 0
            response.raiders shouldBe emptyList()
        }
    }

    // Helper method
    private fun createRaider(
        id: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        characterClass: CharacterClass = CharacterClass.WARRIOR,
        role: Role = Role.DPS,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.now(),
        wowauditId: Long? = null
    ): Raider = Raider(
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
}
