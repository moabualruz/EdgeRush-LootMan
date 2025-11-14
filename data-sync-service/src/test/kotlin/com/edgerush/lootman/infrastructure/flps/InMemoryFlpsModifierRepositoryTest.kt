package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for InMemoryFlpsModifierRepository.
 */
class InMemoryFlpsModifierRepositoryTest : UnitTest() {
    private val repository = InMemoryFlpsModifierRepository()

    @Test
    fun `should return default modifiers for any guild`() {
        // Arrange
        val guildId = GuildId("test-guild")

        // Act
        val modifiers = repository.findByGuildId(guildId)

        // Assert
        modifiers.guildId shouldBe guildId
        modifiers.rmsWeights.attendance shouldBe 0.4
        modifiers.rmsWeights.mechanical shouldBe 0.4
        modifiers.rmsWeights.preparation shouldBe 0.2
        modifiers.ipiWeights.upgradeValue shouldBe 0.45
        modifiers.ipiWeights.tierBonus shouldBe 0.35
        modifiers.ipiWeights.roleMultiplier shouldBe 0.20
    }

    @Test
    fun `should return consistent modifiers for same guild`() {
        // Arrange
        val guildId = GuildId("test-guild")

        // Act
        val modifiers1 = repository.findByGuildId(guildId)
        val modifiers2 = repository.findByGuildId(guildId)

        // Assert
        modifiers1.rmsWeights shouldBe modifiers2.rmsWeights
        modifiers1.ipiWeights shouldBe modifiers2.ipiWeights
    }
}
