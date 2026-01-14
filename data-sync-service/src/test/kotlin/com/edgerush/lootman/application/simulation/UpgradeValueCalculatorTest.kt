package com.edgerush.lootman.application.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class UpgradeValueCalculatorTest : UnitTest() {

    private lateinit var simulationRepository: SimulationRepository
    private lateinit var calculator: UpgradeValueCalculator

    @BeforeEach
    fun setUp() {
        simulationRepository = mockk()
        calculator = UpgradeValueCalculator(simulationRepository)
    }

    private fun createProfile(
        guildId: String = "guild-123",
        characterName: String = "Testchar",
        characterRealm: String = "TestRealm"
    ): SimulationProfile {
        return SimulationProfile.create(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
            profileContent = """warrior="$characterName"""",
            createdAt = Instant.now()
        )
    }

    private fun createResult(
        itemId: Long = 12345L,
        dpsGain: Double = 5000.0,
        percentGain: Double = 5.0
    ): SimulationResult {
        return SimulationResult.create(
            itemId = itemId,
            itemName = "Test Item",
            slot = "head",
            dpsGain = dpsGain,
            percentGain = percentGain,
            simulatedAt = Instant.now()
        )
    }

    @Nested
    inner class CalculateFromSimulation {
        @Test
        fun `should return upgrade value from simulation result when available`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val result = createResult(itemId = 12345L, percentGain = 5.0)

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findLatestResultForItem(42L, 12345L) } returns result

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null
            )

            // Assert
            uv.value shouldBe 0.5 // 5% / 10% max = 0.5
        }

        @Test
        fun `should clamp high percent gains to 1-0`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val result = createResult(itemId = 12345L, percentGain = 15.0)

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findLatestResultForItem(42L, 12345L) } returns result

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null
            )

            // Assert
            uv.value shouldBe 1.0
        }

        @Test
        fun `should return zero for negative percent gains`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val result = createResult(itemId = 12345L, percentGain = -2.0)

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findLatestResultForItem(42L, 12345L) } returns result

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null
            )

            // Assert
            uv.value shouldBe 0.0
        }
    }

    @Nested
    inner class FallbackToWishlist {
        @Test
        fun `should fall back to wishlist when no profile exists`() {
            // Arrange
            val itemId = ItemId(12345L)
            val wishlist = mockk<Wishlist>()

            every { simulationRepository.findProfileByCharacter(any(), any(), any()) } returns null
            every { wishlist.getUpgradePercentage(itemId) } returns 50.0

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = wishlist
            )

            // Assert
            uv.value shouldBe 0.5 // 50% / 100% = 0.5
        }

        @Test
        fun `should fall back to wishlist when no simulation result exists`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val wishlist = mockk<Wishlist>()

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findLatestResultForItem(42L, 12345L) } returns null
            every { wishlist.getUpgradePercentage(itemId) } returns 80.0

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = wishlist
            )

            // Assert
            uv.value shouldBe 0.8
        }

        @Test
        fun `should return zero when no simulation and no wishlist`() {
            // Arrange
            val itemId = ItemId(12345L)

            every { simulationRepository.findProfileByCharacter(any(), any(), any()) } returns null

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null
            )

            // Assert
            uv.value shouldBe 0.0
        }
    }

    @Nested
    inner class CustomMaxPercentGain {
        @Test
        fun `should use custom max percent gain for normalization`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val result = createResult(itemId = 12345L, percentGain = 5.0)

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findLatestResultForItem(42L, 12345L) } returns result

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null,
                maxPercentGain = 5.0 // Custom max
            )

            // Assert
            uv.value shouldBe 1.0 // 5% / 5% max = 1.0
        }
    }

    @Nested
    inner class HasSimulationData {
        @Test
        fun `should return true when simulation data exists`() {
            // Arrange
            val profile = createProfile()

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findResultsByProfile(42L) } returns listOf(createResult())

            // Act
            val hasData = calculator.hasSimulationData(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm"
            )

            // Assert
            hasData shouldBe true
        }

        @Test
        fun `should return false when no profile exists`() {
            // Arrange
            every { simulationRepository.findProfileByCharacter(any(), any(), any()) } returns null

            // Act
            val hasData = calculator.hasSimulationData(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm"
            )

            // Assert
            hasData shouldBe false
        }

        @Test
        fun `should return false when profile exists but no results`() {
            // Arrange
            val profile = createProfile()

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns 42L
            every { simulationRepository.findResultsByProfile(42L) } returns emptyList()

            // Act
            val hasData = calculator.hasSimulationData(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm"
            )

            // Assert
            hasData shouldBe false
        }

        @Test
        fun `should return false when profile exists but profileId is null`() {
            // Arrange
            val profile = createProfile()

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns null

            // Act
            val hasData = calculator.hasSimulationData(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm"
            )

            // Assert
            hasData shouldBe false
            verify(exactly = 0) { simulationRepository.findResultsByProfile(any()) }
        }
    }

    @Nested
    inner class ProfileIdNullHandling {
        @Test
        fun `should fall back to wishlist when profile exists but profileId is null`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)
            val wishlist = mockk<Wishlist>()

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns null
            every { wishlist.getUpgradePercentage(itemId) } returns 60.0

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = wishlist
            )

            // Assert
            uv.value shouldBe 0.6 // Falls back to wishlist
            verify(exactly = 0) { simulationRepository.findLatestResultForItem(any(), any()) }
        }

        @Test
        fun `should return zero when profile exists but profileId is null and no wishlist`() {
            // Arrange
            val profile = createProfile()
            val itemId = ItemId(12345L)

            every { simulationRepository.findProfileByCharacter("guild-123", "Testchar", "TestRealm") } returns profile
            every { simulationRepository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm") } returns null

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = null
            )

            // Assert
            uv.value shouldBe 0.0
        }
    }

    @Nested
    inner class WishlistNullPercentage {
        @Test
        fun `should return zero upgrade value when wishlist returns null percentage`() {
            // Arrange
            val itemId = ItemId(99999L)
            val wishlist = mockk<Wishlist>()

            every { simulationRepository.findProfileByCharacter(any(), any(), any()) } returns null
            every { wishlist.getUpgradePercentage(itemId) } returns null

            // Act
            val uv = calculator.calculateUpgradeValue(
                guildId = "guild-123",
                characterName = "Testchar",
                characterRealm = "TestRealm",
                itemId = itemId,
                wishlistFallback = wishlist
            )

            // Assert
            uv.value shouldBe 0.0
        }
    }
}
