package com.edgerush.datasync.application.raids

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RecordRaidResultsUseCaseTest : UnitTest() {
    
    @MockK
    private lateinit var raidRepository: RaidRepository
    
    @InjectMockKs
    private lateinit var useCase: RecordRaidResultsUseCase
    
    @Test
    fun `should start raid successfully when raid is scheduled`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(RaiderId(123L), RaidRole.TANK)
        
        val command = StartRaidCommand(raidId = raidId.value)
        
        val startedRaid = raid.start()
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns startedRaid
        
        // Act
        val result = useCase.startRaid(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.status shouldBe RaidStatus.IN_PROGRESS
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to start raid when raid not found`() {
        // Arrange
        val raidId = RaidId.generate()
        val command = StartRaidCommand(raidId = raidId.value)
        
        every { raidRepository.findById(raidId) } returns null
        
        // Act
        val result = useCase.startRaid(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Raid not found: ${raidId.value}"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to start raid when raid has no signups`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        
        val command = StartRaidCommand(raidId = raidId.value)
        
        every { raidRepository.findById(raidId) } returns raid
        
        // Act
        val result = useCase.startRaid(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Need at least one signup to start raid"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should complete raid successfully when raid is in progress`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(RaiderId(123L), RaidRole.TANK)
            .start()
        
        val command = CompleteRaidCommand(raidId = raidId.value)
        
        val completedRaid = raid.complete()
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns completedRaid
        
        // Act
        val result = useCase.completeRaid(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.status shouldBe RaidStatus.COMPLETED
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to complete raid when raid is not in progress`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        
        val command = CompleteRaidCommand(raidId = raidId.value)
        
        every { raidRepository.findById(raidId) } returns raid
        
        // Act
        val result = useCase.completeRaid(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Can only complete in-progress raids"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should cancel raid successfully when raid is scheduled`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        
        val command = CancelRaidCommand(raidId = raidId.value)
        
        val cancelledRaid = raid.cancel()
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns cancelledRaid
        
        // Act
        val result = useCase.cancelRaid(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.status shouldBe RaidStatus.CANCELLED
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to cancel raid when raid is not scheduled`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(RaiderId(123L), RaidRole.TANK)
            .start()
        
        val command = CancelRaidCommand(raidId = raidId.value)
        
        every { raidRepository.findById(raidId) } returns raid
        
        // Act
        val result = useCase.cancelRaid(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Can only cancel scheduled raids"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
}
