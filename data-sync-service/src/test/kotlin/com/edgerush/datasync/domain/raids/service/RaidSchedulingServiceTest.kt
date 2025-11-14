package com.edgerush.datasync.domain.raids.service

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RaidSchedulingServiceTest : UnitTest() {
    
    private lateinit var service: RaidSchedulingService
    
    @BeforeEach
    fun setup() {
        service = RaidSchedulingService()
    }
    
    @Test
    fun `should allow scheduling raid in the future with no conflicts`() {
        // Arrange
        val futureDate = LocalDate.now().plusDays(7)
        val existingRaids = emptyList<Raid>()
        
        // Act
        val canSchedule = service.canScheduleRaid(futureDate, existingRaids)
        
        // Assert
        canSchedule shouldBe true
    }
    
    @Test
    fun `should not allow scheduling raid in the past`() {
        // Arrange
        val pastDate = LocalDate.now().minusDays(1)
        val existingRaids = emptyList<Raid>()
        
        // Act
        val canSchedule = service.canScheduleRaid(pastDate, existingRaids)
        
        // Assert
        canSchedule shouldBe false
    }
    
    @Test
    fun `should not allow scheduling raid on date with existing scheduled raid`() {
        // Arrange
        val date = LocalDate.now().plusDays(7)
        val existingRaid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = date
        )
        val existingRaids = listOf(existingRaid)
        
        // Act
        val canSchedule = service.canScheduleRaid(date, existingRaids)
        
        // Assert
        canSchedule shouldBe false
    }
    
    @Test
    fun `should allow scheduling raid on date with completed raid`() {
        // Arrange
        val date = LocalDate.now().plusDays(7)
        val scheduledRaid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = date
        )
        val withSignup = scheduledRaid.addSignup(RaiderId(1L), RaidRole.TANK)
        val completed = withSignup.start().complete()
        val existingRaids = listOf(completed)
        
        // Act
        val canSchedule = service.canScheduleRaid(date, existingRaids)
        
        // Assert
        canSchedule shouldBe true
    }
    
    @Test
    fun `should check if raid has minimum signups`() {
        // Arrange
        var raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        // Add 10 confirmed signups
        repeat(10) { i ->
            raid = raid.addSignup(RaiderId(i.toLong()), RaidRole.DPS)
        }
        
        // Act
        val hasMinimum = service.hasMinimumSignups(raid, 10)
        
        // Assert
        hasMinimum shouldBe true
    }
    
    @Test
    fun `should not count tentative signups for minimum`() {
        // Arrange
        var raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        // Add 9 confirmed and 1 tentative
        repeat(9) { i ->
            raid = raid.addSignup(RaiderId(i.toLong()), RaidRole.DPS)
        }
        raid = raid.addSignup(RaiderId(9L), RaidRole.DPS)
        raid = raid.updateSignupStatus(RaiderId(9L), RaidSignup.SignupStatus.TENTATIVE)
        
        // Act
        val hasMinimum = service.hasMinimumSignups(raid, 10)
        
        // Assert
        hasMinimum shouldBe false
    }
    
    @Test
    fun `should check if raid can be started`() {
        // Arrange
        var raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        repeat(10) { i ->
            raid = raid.addSignup(RaiderId(i.toLong()), RaidRole.DPS)
        }
        
        // Act
        val canStart = service.canStartRaid(raid, 10)
        
        // Assert
        canStart shouldBe true
    }
    
    @Test
    fun `should not allow starting in-progress raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        // Act
        val canStart = service.canStartRaid(started, 1)
        
        // Assert
        canStart shouldBe false
    }
    
    @Test
    fun `should calculate raid composition`() {
        // Arrange
        val signups = listOf(
            RaidSignup.create(RaiderId(1L), RaidRole.TANK),
            RaidSignup.create(RaiderId(2L), RaidRole.TANK),
            RaidSignup.create(RaiderId(3L), RaidRole.HEALER),
            RaidSignup.create(RaiderId(4L), RaidRole.HEALER),
            RaidSignup.create(RaiderId(5L), RaidRole.HEALER),
            RaidSignup.create(RaiderId(6L), RaidRole.HEALER),
            RaidSignup.create(RaiderId(7L), RaidRole.DPS),
            RaidSignup.create(RaiderId(8L), RaidRole.DPS),
            RaidSignup.create(RaiderId(9L), RaidRole.DPS),
            RaidSignup.create(RaiderId(10L), RaidRole.DPS),
            RaidSignup.create(RaiderId(11L), RaidRole.DPS),
            RaidSignup.create(RaiderId(12L), RaidRole.DPS),
            RaidSignup.create(RaiderId(13L), RaidRole.DPS),
            RaidSignup.create(RaiderId(14L), RaidRole.DPS)
        )
        
        // Act
        val composition = service.calculateOptimalComposition(signups)
        
        // Assert
        composition.tanks shouldBe 2
        composition.healers shouldBe 4
        composition.dps shouldBe 8
        composition.total shouldBe 14
    }
    
    @Test
    fun `should only count confirmed signups in composition`() {
        // Arrange
        val signups = listOf(
            RaidSignup.create(RaiderId(1L), RaidRole.TANK, RaidSignup.SignupStatus.CONFIRMED),
            RaidSignup.create(RaiderId(2L), RaidRole.TANK, RaidSignup.SignupStatus.TENTATIVE),
            RaidSignup.create(RaiderId(3L), RaidRole.HEALER, RaidSignup.SignupStatus.CONFIRMED)
        )
        
        // Act
        val composition = service.calculateOptimalComposition(signups)
        
        // Assert
        composition.tanks shouldBe 1
        composition.healers shouldBe 1
        composition.total shouldBe 2
    }
    
    @Test
    fun `should validate viable composition`() {
        // Arrange
        val viableComposition = RaidComposition(
            tanks = 2,
            healers = 4,
            dps = 14,
            total = 20
        )
        
        val minimalComposition = RaidComposition(
            tanks = 2,
            healers = 3,
            dps = 5,
            total = 10
        )
        
        val notEnoughTanks = RaidComposition(
            tanks = 1,
            healers = 4,
            dps = 15,
            total = 20
        )
        
        val notEnoughHealers = RaidComposition(
            tanks = 2,
            healers = 2,
            dps = 16,
            total = 20
        )
        
        val notEnoughDps = RaidComposition(
            tanks = 2,
            healers = 4,
            dps = 3,
            total = 9
        )
        
        // Act & Assert
        service.isViableComposition(viableComposition) shouldBe true
        service.isViableComposition(minimalComposition) shouldBe true
        service.isViableComposition(notEnoughTanks) shouldBe false
        service.isViableComposition(notEnoughHealers) shouldBe false
        service.isViableComposition(notEnoughDps) shouldBe false
    }
    
    @Test
    fun `should suggest missing roles`() {
        // Arrange
        val composition = RaidComposition(
            tanks = 1,
            healers = 2,
            dps = 3,
            total = 6
        )
        
        // Act
        val missing = service.suggestMissingRoles(composition)
        
        // Assert
        missing shouldHaveSize 4
        missing.count { it == RaidRole.TANK } shouldBe 1
        missing.count { it == RaidRole.HEALER } shouldBe 1
        missing.count { it == RaidRole.DPS } shouldBe 2
    }
    
    @Test
    fun `should not suggest roles when composition is complete`() {
        // Arrange
        val composition = RaidComposition(
            tanks = 2,
            healers = 4,
            dps = 14,
            total = 20
        )
        
        // Act
        val missing = service.suggestMissingRoles(composition)
        
        // Assert
        missing shouldHaveSize 0
    }
}
