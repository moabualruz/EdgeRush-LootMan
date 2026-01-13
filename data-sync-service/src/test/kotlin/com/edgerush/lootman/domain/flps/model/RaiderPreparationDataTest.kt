package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for RaiderPreparationData value object.
 */
class RaiderPreparationDataTest : UnitTest() {

    @Nested
    inner class ConstructorValidationTests {
        @Test
        fun `should create with valid vault slot counts`() {
            // When
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 3,
                mythicPlusVaultSlots = 2,
                pvpVaultSlots = 1,
                mythicPlusRating = 2500,
                crestsUsed = 10,
                hasHeroicClear = true,
                hasNormalClear = true
            )

            // Then
            data.raidVaultSlots shouldBe 3
            data.mythicPlusVaultSlots shouldBe 2
            data.pvpVaultSlots shouldBe 1
            data.mythicPlusRating shouldBe 2500
            data.crestsUsed shouldBe 10
            data.hasHeroicClear shouldBe true
            data.hasNormalClear shouldBe true
        }

        @Test
        fun `should fail when raid vault slots exceeds 3`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = 4,
                    mythicPlusVaultSlots = 0,
                    pvpVaultSlots = 0,
                    mythicPlusRating = 0,
                    crestsUsed = 0,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "Raid vault slots must be between 0 and 3"
        }

        @Test
        fun `should fail when raid vault slots is negative`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = -1,
                    mythicPlusVaultSlots = 0,
                    pvpVaultSlots = 0,
                    mythicPlusRating = 0,
                    crestsUsed = 0,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "Raid vault slots must be between 0 and 3"
        }

        @Test
        fun `should fail when mythic plus vault slots exceeds 3`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = 0,
                    mythicPlusVaultSlots = 5,
                    pvpVaultSlots = 0,
                    mythicPlusRating = 0,
                    crestsUsed = 0,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "M+ vault slots must be between 0 and 3"
        }

        @Test
        fun `should fail when pvp vault slots exceeds 3`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = 0,
                    mythicPlusVaultSlots = 0,
                    pvpVaultSlots = 10,
                    mythicPlusRating = 0,
                    crestsUsed = 0,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "PvP vault slots must be between 0 and 3"
        }

        @Test
        fun `should fail when mythic plus rating is negative`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = 0,
                    mythicPlusVaultSlots = 0,
                    pvpVaultSlots = 0,
                    mythicPlusRating = -100,
                    crestsUsed = 0,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "M+ rating cannot be negative"
        }

        @Test
        fun `should fail when crests used is negative`() {
            // When/Then
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderPreparationData(
                    raiderId = RaiderId(1L),
                    raidVaultSlots = 0,
                    mythicPlusVaultSlots = 0,
                    pvpVaultSlots = 0,
                    mythicPlusRating = 0,
                    crestsUsed = -5,
                    hasHeroicClear = false,
                    hasNormalClear = false
                )
            }
            exception.message shouldBe "Crests used cannot be negative"
        }
    }

    @Nested
    inner class ComputedPropertiesTests {
        @Test
        fun `totalVaultSlots should sum all vault types`() {
            // Given
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 3,
                mythicPlusVaultSlots = 2,
                pvpVaultSlots = 1,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false
            )

            // Then
            data.totalVaultSlots shouldBe 6
        }

        @Test
        fun `hasAnyVaultSlot should return true when at least one slot unlocked`() {
            // Given
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 0,
                mythicPlusVaultSlots = 1,
                pvpVaultSlots = 0,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false
            )

            // Then
            data.hasAnyVaultSlot shouldBe true
        }

        @Test
        fun `hasAnyVaultSlot should return false when no slots unlocked`() {
            // Given
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 0,
                mythicPlusVaultSlots = 0,
                pvpVaultSlots = 0,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false
            )

            // Then
            data.hasAnyVaultSlot shouldBe false
        }

        @Test
        fun `hasFullRaidVault should return true when all 3 raid slots unlocked`() {
            // Given
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 3,
                mythicPlusVaultSlots = 0,
                pvpVaultSlots = 0,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false
            )

            // Then
            data.hasFullRaidVault shouldBe true
        }

        @Test
        fun `hasFullRaidVault should return false when fewer than 3 raid slots unlocked`() {
            // Given
            val data = RaiderPreparationData(
                raiderId = RaiderId(1L),
                raidVaultSlots = 2,
                mythicPlusVaultSlots = 3,
                pvpVaultSlots = 3,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false
            )

            // Then
            data.hasFullRaidVault shouldBe false
        }
    }

    @Nested
    inner class FactoryMethodTests {
        @Test
        fun `empty should create data with all zeros and false flags`() {
            // When
            val data = RaiderPreparationData.empty(RaiderId(1L))

            // Then
            data.raiderId.value shouldBe 1L
            data.raidVaultSlots shouldBe 0
            data.mythicPlusVaultSlots shouldBe 0
            data.pvpVaultSlots shouldBe 0
            data.mythicPlusRating shouldBe 0
            data.crestsUsed shouldBe 0
            data.hasHeroicClear shouldBe false
            data.hasNormalClear shouldBe false
            data.totalVaultSlots shouldBe 0
            data.hasAnyVaultSlot shouldBe false
        }

        @Test
        fun `create should use default values when not specified`() {
            // When
            val data = RaiderPreparationData.create(
                raiderId = RaiderId(1L),
                raidVaultSlots = 2,
                hasHeroicClear = true
            )

            // Then
            data.raidVaultSlots shouldBe 2
            data.mythicPlusVaultSlots shouldBe 0
            data.pvpVaultSlots shouldBe 0
            data.mythicPlusRating shouldBe 0
            data.crestsUsed shouldBe 0
            data.hasHeroicClear shouldBe true
            data.hasNormalClear shouldBe false
        }

        @Test
        fun `create should allow all parameters to be specified`() {
            // When
            val data = RaiderPreparationData.create(
                raiderId = RaiderId(1L),
                raidVaultSlots = 3,
                mythicPlusVaultSlots = 3,
                pvpVaultSlots = 1,
                mythicPlusRating = 3000,
                crestsUsed = 15,
                hasHeroicClear = true,
                hasNormalClear = true
            )

            // Then
            data.raidVaultSlots shouldBe 3
            data.mythicPlusVaultSlots shouldBe 3
            data.pvpVaultSlots shouldBe 1
            data.mythicPlusRating shouldBe 3000
            data.crestsUsed shouldBe 15
            data.hasHeroicClear shouldBe true
            data.hasNormalClear shouldBe true
            data.totalVaultSlots shouldBe 7
        }
    }
}
