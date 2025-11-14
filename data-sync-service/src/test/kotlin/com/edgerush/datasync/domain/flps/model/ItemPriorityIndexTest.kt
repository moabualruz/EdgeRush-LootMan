package com.edgerush.datasync.domain.flps.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ItemPriorityIndexTest {

    @Test
    fun `should create valid item priority index within range`() {
        // Given
        val value = 0.75

        // When
        val index = ItemPriorityIndex.of(value)

        // Then
        index.value shouldBe 0.75
    }

    @Test
    fun `should create zero item priority index`() {
        // Given/When
        val index = ItemPriorityIndex.zero()

        // Then
        index.value shouldBe 0.0
    }

    @Test
    fun `should create max item priority index`() {
        // Given/When
        val index = ItemPriorityIndex.max()

        // Then
        index.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when index is negative`() {
        // Given
        val invalidValue = -0.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            ItemPriorityIndex.of(invalidValue)
        }
    }

    @Test
    fun `should throw exception when index exceeds 1_0`() {
        // Given
        val invalidValue = 1.2

        // When/Then
        shouldThrow<IllegalArgumentException> {
            ItemPriorityIndex.of(invalidValue)
        }
    }

    @Test
    fun `should calculate from component scores with weights`() {
        // Given
        val upgradeValue = 0.8
        val tierBonus = 1.2
        val roleMultiplier = 1.0
        val weights = IpiWeights(upgradeValue = 0.45, tierBonus = 0.35, roleMultiplier = 0.20)

        // When
        val index = ItemPriorityIndex.fromComponents(upgradeValue, tierBonus, roleMultiplier, weights)

        // Then
        // Expected: (0.8 * 0.45) + (1.2 * 0.35) + (1.0 * 0.20) = 0.36 + 0.42 + 0.20 = 0.98
        index.value shouldBe 0.98
    }

    @Test
    fun `should cap calculated index at 1_0`() {
        // Given
        val upgradeValue = 1.0
        val tierBonus = 1.5
        val roleMultiplier = 1.2
        val weights = IpiWeights(upgradeValue = 0.45, tierBonus = 0.35, roleMultiplier = 0.20)

        // When
        val index = ItemPriorityIndex.fromComponents(upgradeValue, tierBonus, roleMultiplier, weights)

        // Then
        index.value shouldBe 1.0
    }

    @Test
    fun `should be immutable value object`() {
        // Given
        val index1 = ItemPriorityIndex.of(0.65)
        val index2 = ItemPriorityIndex.of(0.65)

        // Then
        (index1 == index2) shouldBe true
        (index1.hashCode() == index2.hashCode()) shouldBe true
    }

    @Test
    fun `should compare item priority indices correctly`() {
        // Given
        val lower = ItemPriorityIndex.of(0.5)
        val higher = ItemPriorityIndex.of(0.8)

        // Then
        (lower < higher) shouldBe true
        (higher > lower) shouldBe true
    }
}
