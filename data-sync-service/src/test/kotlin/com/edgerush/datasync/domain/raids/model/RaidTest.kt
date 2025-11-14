package com.edgerush.datasync.domain.raids.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class RaidTest : UnitTest() {
    
    @Test
    fun `should schedule raid with valid data`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val date = LocalDate.now().plusDays(1)
        
        // Act
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = date,
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(22, 0),
            instance = "Vault of the Incarnates",
            difficulty = RaidDifficulty.HEROIC,
            optional = false,
            notes = "Weekly raid"
        )
        
        // Assert
        raid.guildId shouldBe guildId
        raid.scheduledDate shouldBe date
        raid.startTime shouldBe LocalTime.of(19, 0)
        raid.endTime shouldBe LocalTime.of(22, 0)
        raid.instance shouldBe "Vault of the Incarnates"
        raid.difficulty shouldBe RaidDifficulty.HEROIC
        raid.optional shouldBe false
        raid.status shouldBe RaidStatus.SCHEDULED
        raid.notes shouldBe "Weekly raid"
        raid.getEncounters() shouldHaveSize 0
        raid.getSignups() shouldHaveSize 0
    }
    
    @Test
    fun `should add encounter to scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros"
        )
        
        // Act
        val updated = raid.addEncounter(encounter)
        
        // Assert
        updated.getEncounters() shouldHaveSize 1
        updated.getEncounters().first().name shouldBe "Terros"
        raid.getEncounters() shouldHaveSize 0 // Original unchanged
    }
    
    @Test
    fun `should throw exception when adding encounter to non-scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros"
        )
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            started.addEncounter(encounter)
        }
    }
    
    @Test
    fun `should remove encounter from scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros"
        )
        val withEncounter = raid.addEncounter(encounter)
        
        // Act
        val updated = withEncounter.removeEncounter(1001L)
        
        // Assert
        updated.getEncounters() shouldHaveSize 0
        withEncounter.getEncounters() shouldHaveSize 1 // Original unchanged
    }
    
    @Test
    fun `should add signup to scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val raiderId = RaiderId(1L)
        
        // Act
        val updated = raid.addSignup(raiderId, RaidRole.TANK, "Ready to tank")
        
        // Assert
        updated.getSignups() shouldHaveSize 1
        updated.getSignups().first().raiderId shouldBe raiderId
        updated.getSignups().first().role shouldBe RaidRole.TANK
        updated.getSignups().first().comment shouldBe "Ready to tank"
        raid.getSignups() shouldHaveSize 0 // Original unchanged
    }
    
    @Test
    fun `should throw exception when adding duplicate signup`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val raiderId = RaiderId(1L)
        val withSignup = raid.addSignup(raiderId, RaidRole.TANK)
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            withSignup.addSignup(raiderId, RaidRole.HEALER)
        }
    }
    
    @Test
    fun `should throw exception when adding signup to non-scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            started.addSignup(RaiderId(2L), RaidRole.HEALER)
        }
    }
    
    @Test
    fun `should remove signup from scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val raiderId = RaiderId(1L)
        val withSignup = raid.addSignup(raiderId, RaidRole.TANK)
        
        // Act
        val updated = withSignup.removeSignup(raiderId)
        
        // Assert
        updated.getSignups() shouldHaveSize 0
        withSignup.getSignups() shouldHaveSize 1 // Original unchanged
    }
    
    @Test
    fun `should update signup status`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val raiderId = RaiderId(1L)
        val withSignup = raid.addSignup(raiderId, RaidRole.TANK)
        
        // Act
        val updated = withSignup.updateSignupStatus(raiderId, RaidSignup.SignupStatus.LATE)
        
        // Assert
        updated.getSignups().first().status shouldBe RaidSignup.SignupStatus.LATE
        withSignup.getSignups().first().status shouldBe RaidSignup.SignupStatus.CONFIRMED // Original unchanged
    }
    
    @Test
    fun `should select signup`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val raiderId = RaiderId(1L)
        val withSignup = raid.addSignup(raiderId, RaidRole.TANK)
        
        // Act
        val updated = withSignup.selectSignup(raiderId)
        
        // Assert
        updated.getSignups().first().selected shouldBe true
        updated.getSelectedSignups() shouldHaveSize 1
        withSignup.getSignups().first().selected shouldBe false // Original unchanged
    }
    
    @Test
    fun `should get confirmed signups`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignups = raid
            .addSignup(RaiderId(1L), RaidRole.TANK)
            .addSignup(RaiderId(2L), RaidRole.HEALER)
            .updateSignupStatus(RaiderId(2L), RaidSignup.SignupStatus.TENTATIVE)
        
        // Act
        val confirmed = withSignups.getConfirmedSignups()
        
        // Assert
        confirmed shouldHaveSize 1
        confirmed.first().raiderId shouldBe RaiderId(1L)
    }
    
    @Test
    fun `should start scheduled raid with signups`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        
        // Act
        val started = withSignup.start()
        
        // Assert
        started.status shouldBe RaidStatus.IN_PROGRESS
        withSignup.status shouldBe RaidStatus.SCHEDULED // Original unchanged
    }
    
    @Test
    fun `should throw exception when starting raid without signups`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            raid.start()
        }
    }
    
    @Test
    fun `should throw exception when starting non-scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            started.start()
        }
    }
    
    @Test
    fun `should complete in-progress raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        // Act
        val completed = started.complete()
        
        // Assert
        completed.status shouldBe RaidStatus.COMPLETED
        started.status shouldBe RaidStatus.IN_PROGRESS // Original unchanged
    }
    
    @Test
    fun `should throw exception when completing non-in-progress raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            raid.complete()
        }
    }
    
    @Test
    fun `should cancel scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        
        // Act
        val cancelled = raid.cancel()
        
        // Assert
        cancelled.status shouldBe RaidStatus.CANCELLED
        raid.status shouldBe RaidStatus.SCHEDULED // Original unchanged
    }
    
    @Test
    fun `should throw exception when cancelling non-scheduled raid`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = raid.addSignup(RaiderId(1L), RaidRole.TANK)
        val started = withSignup.start()
        
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            started.cancel()
        }
    }
    
    @Test
    fun `should check raid status`() {
        // Arrange
        val scheduled = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignup = scheduled.addSignup(RaiderId(1L), RaidRole.TANK)
        val inProgress = withSignup.start()
        val completed = inProgress.complete()
        val cancelled = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(2)
        ).cancel()
        
        // Act & Assert
        scheduled.isScheduled() shouldBe true
        scheduled.isInProgress() shouldBe false
        scheduled.isCompleted() shouldBe false
        scheduled.isCancelled() shouldBe false
        
        inProgress.isScheduled() shouldBe false
        inProgress.isInProgress() shouldBe true
        inProgress.isCompleted() shouldBe false
        inProgress.isCancelled() shouldBe false
        
        completed.isScheduled() shouldBe false
        completed.isInProgress() shouldBe false
        completed.isCompleted() shouldBe true
        completed.isCancelled() shouldBe false
        
        cancelled.isScheduled() shouldBe false
        cancelled.isInProgress() shouldBe false
        cancelled.isCompleted() shouldBe false
        cancelled.isCancelled() shouldBe true
    }
    
    @Test
    fun `should get signup counts`() {
        // Arrange
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.now().plusDays(1)
        )
        val withSignups = raid
            .addSignup(RaiderId(1L), RaidRole.TANK)
            .addSignup(RaiderId(2L), RaidRole.HEALER)
            .addSignup(RaiderId(3L), RaidRole.DPS)
            .updateSignupStatus(RaiderId(3L), RaidSignup.SignupStatus.TENTATIVE)
        
        // Act & Assert
        withSignups.getSignupCount() shouldBe 3
        withSignups.getConfirmedSignupCount() shouldBe 2
    }
}
