package com.edgerush.lootman.domain.loot.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for LootTier enum.
 *
 * Tests verify:
 * - All expected tier values exist
 * - Tier ordering and comparison
 */
class LootTierTest : UnitTest() {
    @Nested
    inner class TierValuesTests {
        @Test
        fun `should have MYTHIC tier`() {
            LootTier.valueOf("MYTHIC") shouldBe LootTier.MYTHIC
        }

        @Test
        fun `should have HEROIC tier`() {
            LootTier.valueOf("HEROIC") shouldBe LootTier.HEROIC
        }

        @Test
        fun `should have NORMAL tier`() {
            LootTier.valueOf("NORMAL") shouldBe LootTier.NORMAL
        }

        @Test
        fun `should have LFR tier`() {
            LootTier.valueOf("LFR") shouldBe LootTier.LFR
        }

        @Test
        fun `should have exactly four tiers`() {
            LootTier.entries.size shouldBe 4
        }

        @Test
        fun `should contain all expected tiers`() {
            LootTier.entries shouldContainExactly
                listOf(
                    LootTier.MYTHIC,
                    LootTier.HEROIC,
                    LootTier.NORMAL,
                    LootTier.LFR,
                )
        }
    }

    @Nested
    inner class TierOrderingTests {
        @Test
        fun `should have correct ordinal order from highest to lowest`() {
            // MYTHIC should be highest (ordinal 0)
            LootTier.MYTHIC.ordinal shouldBe 0
            // HEROIC should be second (ordinal 1)
            LootTier.HEROIC.ordinal shouldBe 1
            // NORMAL should be third (ordinal 2)
            LootTier.NORMAL.ordinal shouldBe 2
            // LFR should be lowest (ordinal 3)
            LootTier.LFR.ordinal shouldBe 3
        }

        @Test
        fun `should compare tiers correctly`() {
            // MYTHIC > HEROIC > NORMAL > LFR based on ordinal (lower ordinal = higher tier)
            (LootTier.MYTHIC.ordinal < LootTier.HEROIC.ordinal) shouldBe true
            (LootTier.HEROIC.ordinal < LootTier.NORMAL.ordinal) shouldBe true
            (LootTier.NORMAL.ordinal < LootTier.LFR.ordinal) shouldBe true
        }

        @Test
        fun `should sort tiers from highest to lowest`() {
            val shuffled = listOf(LootTier.LFR, LootTier.MYTHIC, LootTier.NORMAL, LootTier.HEROIC)
            val sorted = shuffled.sortedBy { it.ordinal }

            sorted shouldContainExactly
                listOf(
                    LootTier.MYTHIC,
                    LootTier.HEROIC,
                    LootTier.NORMAL,
                    LootTier.LFR,
                )
        }
    }

    @Nested
    inner class TierNameTests {
        @Test
        fun `should have correct name for MYTHIC`() {
            LootTier.MYTHIC.name shouldBe "MYTHIC"
        }

        @Test
        fun `should have correct name for HEROIC`() {
            LootTier.HEROIC.name shouldBe "HEROIC"
        }

        @Test
        fun `should have correct name for NORMAL`() {
            LootTier.NORMAL.name shouldBe "NORMAL"
        }

        @Test
        fun `should have correct name for LFR`() {
            LootTier.LFR.name shouldBe "LFR"
        }
    }
}
