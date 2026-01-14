package com.edgerush.lootman.api.performance

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.datasync.entity.RaiderEntity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

/**
 * Unit tests for PerformanceController.
 *
 * Tests controller methods directly without Spring context,
 * mocking services and repositories as dependencies.
 */
class PerformanceControllerTest : UnitTest() {
    private lateinit var currentUserService: CurrentUserService
    private lateinit var raiderPerformanceRepository: RaiderPerformanceRepository
    private lateinit var raiderEntityRepository: RaiderEntityRepository
    private lateinit var controller: PerformanceController

    @BeforeEach
    fun setup() {
        currentUserService = mockk()
        raiderPerformanceRepository = mockk()
        raiderEntityRepository = mockk()
        controller = PerformanceController(
            currentUserService,
            raiderPerformanceRepository,
            raiderEntityRepository,
        )
    }

    @Test
    fun `getMyPerformance should return performance metrics for current user`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(123L)
        val authenticatedUser = mockk<AuthenticatedUser>()
        val raider = mockk<RaiderEntity>()

        every { authenticatedUser.hasGuildAccess(guildId) } returns true
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) } returns Unit
        every { raiderEntityRepository.findById(123L) } returns raider
        every { raider.characterName } returns "TestRaider"

        val performanceData = RaiderPerformanceData(
            raiderId = raiderId,
            characterName = "TestRaider",
            characterRealm = "TestRealm",
            totalDeaths = 5,
            totalFights = 10,
            deathsPerAttempt = 0.5,
            avoidableDamagePercentage = 15.0,
            periodStart = Instant.now().minusSeconds(604800),
            periodEnd = Instant.now(),
        )

        every { raiderPerformanceRepository.findByRaiderAndPeriod(raiderId, GuildId(guildId), any(), any()) } returns performanceData

        // When
        val response = controller.getMyPerformance(guildId, authenticatedUser)

        // Then
        response.raiderId shouldBe 123L
        response.characterName shouldBe "TestRaider"
        response.dpa shouldBe 0.5
        response.adt shouldBe 15.0

        verify(exactly = 1) { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) }
        verify(exactly = 1) { raiderPerformanceRepository.findByRaiderAndPeriod(raiderId, GuildId(guildId), any(), any()) }
    }

    @Test
    fun `getMyPerformance should return default values when no performance data exists`() {
        // Given
        val guildId = "test-guild"
        val raiderId = RaiderId(456L)
        val authenticatedUser = mockk<AuthenticatedUser>()
        val raider = mockk<RaiderEntity>()

        every { authenticatedUser.hasGuildAccess(guildId) } returns true
        every { currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser) } returns raiderId
        every { currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId)) } returns Unit
        every { raiderEntityRepository.findById(456L) } returns raider
        every { raider.characterName } returns "NoDataRaider"
        every { raiderPerformanceRepository.findByRaiderAndPeriod(raiderId, GuildId(guildId), any(), any()) } returns null

        // When
        val response = controller.getMyPerformance(guildId, authenticatedUser)

        // Then
        response.raiderId shouldBe 456L
        response.characterName shouldBe "NoDataRaider"
        response.dpa shouldBe 0.0
        response.adt shouldBe 0.0
        response.specAverage shouldBe 0.0
    }

    @Test
    fun `getPerformance should return performance metrics for specified raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 789L
        val raider = mockk<RaiderEntity>()

        every { raiderEntityRepository.findById(raiderId) } returns raider
        every { raider.characterName } returns "SpecificRaider"

        val performanceData = RaiderPerformanceData(
            raiderId = RaiderId(raiderId),
            characterName = "SpecificRaider",
            characterRealm = "TestRealm",
            totalDeaths = 3,
            totalFights = 15,
            deathsPerAttempt = 0.2,
            avoidableDamagePercentage = 8.0,
            periodStart = Instant.now().minusSeconds(604800),
            periodEnd = Instant.now(),
        )

        every { raiderPerformanceRepository.findByRaiderAndPeriod(RaiderId(raiderId), GuildId(guildId), any(), any()) } returns performanceData

        // When
        val response = controller.getPerformance(guildId, raiderId)

        // Then
        response.raiderId shouldBe 789L
        response.characterName shouldBe "SpecificRaider"
        response.dpa shouldBe 0.2
        response.adt shouldBe 8.0

        verify(exactly = 1) { raiderEntityRepository.findById(raiderId) }
        verify(exactly = 1) { raiderPerformanceRepository.findByRaiderAndPeriod(RaiderId(raiderId), GuildId(guildId), any(), any()) }
    }

    @Test
    fun `getPerformance should throw exception when raider not found`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 999L

        every { raiderEntityRepository.findById(raiderId) } returns null

        // When / Then
        val exception = assertThrows<IllegalArgumentException> {
            controller.getPerformance(guildId, raiderId)
        }

        exception.message shouldBe "Raider not found: $raiderId"
    }

    @Test
    fun `getPerformance should return default values when no performance data for raider`() {
        // Given
        val guildId = "test-guild"
        val raiderId = 123L
        val raider = mockk<RaiderEntity>()

        every { raiderEntityRepository.findById(raiderId) } returns raider
        every { raider.characterName } returns "RaiderWithoutData"
        every { raiderPerformanceRepository.findByRaiderAndPeriod(RaiderId(raiderId), GuildId(guildId), any(), any()) } returns null

        // When
        val response = controller.getPerformance(guildId, raiderId)

        // Then
        response.raiderId shouldBe 123L
        response.characterName shouldBe "RaiderWithoutData"
        response.dpa shouldBe 0.0
        response.adt shouldBe 0.0
    }
}
