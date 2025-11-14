package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class LootBanMapperTest {
    private val mapper = LootBanMapper()

    @Test
    fun `should map entity to domain model correctly`() {
        // Given
        val entity =
            LootBanEntity(
                id = 123L,
                guildId = "test-guild",
                characterName = "TestCharacter",
                reason = "Repeated loot hoarding",
                bannedBy = "GuildLeader",
                bannedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
                expiresAt = LocalDateTime.of(2024, 2, 15, 10, 30, 0),
                isActive = true,
            )

        // When
        val domain = mapper.toDomain(entity)

        // Then
        domain.id.value shouldBe "123"
        domain.guildId.value shouldBe "test-guild"
        domain.raiderId.value shouldBe "TestCharacter"
        domain.reason shouldBe "Repeated loot hoarding"
        domain.bannedAt shouldBe LocalDateTime.of(2024, 1, 15, 10, 30, 0).toInstant(ZoneOffset.UTC)
        domain.expiresAt shouldBe LocalDateTime.of(2024, 2, 15, 10, 30, 0).toInstant(ZoneOffset.UTC)
    }

    @Test
    fun `should map domain model to entity correctly`() {
        // Given
        val domain =
            LootBan(
                id = LootBanId("123"),
                raiderId = RaiderId("TestCharacter"),
                guildId = GuildId("test-guild"),
                reason = "Repeated loot hoarding",
                bannedAt = Instant.parse("2024-01-15T10:30:00Z"),
                expiresAt = Instant.parse("2024-02-15T10:30:00Z"),
            )

        // When
        val entity = mapper.toEntity(domain)

        // Then
        entity.id shouldBe 123L
        entity.guildId shouldBe "test-guild"
        entity.characterName shouldBe "TestCharacter"
        entity.reason shouldBe "Repeated loot hoarding"
        entity.isActive shouldBe true
    }

    @Test
    fun `should handle permanent bans with null expiry`() {
        // Given
        val entity =
            LootBanEntity(
                id = 456L,
                guildId = "test-guild",
                characterName = "BadPlayer",
                reason = "Permanent ban",
                bannedBy = "Admin",
                bannedAt = LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                expiresAt = null,
                isActive = true,
            )

        // When
        val domain = mapper.toDomain(entity)

        // Then
        domain.expiresAt shouldBe null
        domain.isActive() shouldBe true
    }

    @Test
    fun `should round-trip conversion correctly`() {
        // Given
        val originalDomain =
            LootBan.create(
                raiderId = RaiderId("Player1"),
                guildId = GuildId("guild-123"),
                reason = "Test reason",
                expiresAt = Instant.parse("2024-12-31T23:59:59Z"),
            )

        // When
        val entity = mapper.toEntity(originalDomain)
        val roundTripDomain = mapper.toDomain(entity)

        // Then
        roundTripDomain.raiderId shouldBe originalDomain.raiderId
        roundTripDomain.guildId shouldBe originalDomain.guildId
        roundTripDomain.reason shouldBe originalDomain.reason
    }
}
