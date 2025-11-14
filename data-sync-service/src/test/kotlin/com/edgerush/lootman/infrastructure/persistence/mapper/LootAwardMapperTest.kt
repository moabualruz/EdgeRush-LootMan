package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootAwardStatus
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class LootAwardMapperTest {
    private val mapper = LootAwardMapper()

    @Test
    fun `should map entity to domain model correctly`() {
        // Given
        val entity =
            LootAwardEntity(
                id = 123L,
                raiderId = 456L,
                itemId = 789L,
                itemName = "Test Item",
                tier = "MYTHIC",
                flps = 0.85,
                rdf = 0.95,
                awardedAt = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                rclootcouncilId = null,
                icon = null,
                slot = null,
                quality = null,
                responseTypeId = null,
                responseTypeName = null,
                responseTypeRgba = null,
                responseTypeExcluded = null,
                propagatedResponseTypeId = null,
                propagatedResponseTypeName = null,
                propagatedResponseTypeRgba = null,
                propagatedResponseTypeExcluded = null,
                sameResponseAmount = null,
                note = null,
                wishValue = null,
                difficulty = null,
                discarded = null,
                characterId = null,
                awardedByCharacterId = null,
                awardedByName = null,
            )

        // When
        val domain = mapper.toDomain(entity, GuildId("test-guild"))

        // Then
        domain.id.value shouldBe "123"
        domain.itemId.value shouldBe 789L
        domain.raiderId.value shouldBe "456"
        domain.guildId.value shouldBe "test-guild"
        domain.flpsScore.value shouldBe 0.85
        domain.tier shouldBe LootTier.MYTHIC
        domain.isActive() shouldBe true
    }

    @Test
    fun `should map domain model to entity correctly`() {
        // Given
        val domain =
            LootAward(
                id = LootAwardId("123"),
                itemId = ItemId(789L),
                raiderId = RaiderId("456"),
                guildId = GuildId("test-guild"),
                awardedAt = Instant.parse("2024-01-15T10:30:00Z"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.HEROIC,
                status = LootAwardStatus.ACTIVE,
            )

        // When
        val entity = mapper.toEntity(domain)

        // Then
        entity.id shouldBe 123L
        entity.itemId shouldBe 789L
        entity.raiderId shouldBe 456L
        entity.tier shouldBe "HEROIC"
        entity.flps shouldBe 0.85
    }

    @Test
    fun `should handle all loot tiers correctly`() {
        // Given
        val tiers = listOf(LootTier.MYTHIC, LootTier.HEROIC, LootTier.NORMAL, LootTier.LFR)

        tiers.forEach { tier ->
            // When
            val domain =
                LootAward.create(
                    itemId = ItemId(100L),
                    raiderId = RaiderId("1"),
                    guildId = GuildId("guild"),
                    flpsScore = FlpsScore.of(0.5),
                    tier = tier,
                )
            val entity = mapper.toEntity(domain)
            val roundTrip = mapper.toDomain(entity, GuildId("guild"))

            // Then
            roundTrip.tier shouldBe tier
        }
    }
}
