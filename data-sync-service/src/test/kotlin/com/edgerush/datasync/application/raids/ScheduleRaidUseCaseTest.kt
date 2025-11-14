package com.edgerush.datasync.application.raids

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import com.edgerush.datasync.domain.raids.service.RaidSchedulingService
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalTime

class ScheduleRaidUseCaseTest : UnitTest() {
    
    @MockK
    private lateinit var raidRepository: RaidRepository
    
    @MockK
    private lateinit var schedulingService: RaidSchedulingService
    
    @InjectMockKs
    private lateinit var useCase: ScheduleRaidUseCase
    
    @Test
    fun `should schedule raid successfully when all inputs are valid`() {
        // Arrange
        val command = ScheduleRaidCommand(
            guildId = "test-guild",
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(23, 0),
            instance = "Nerub-ar Palace",
            difficulty = "MYTHIC",
            optional = false,
            notes = "Progression night"
        )
        
        val scheduledRaid = Raid.schedule(
            guildId = GuildId(command.guildId),
            scheduledDate = command.scheduledDate,
            startTime = command.startTime,
            endTime = command.endTime,
            instance = command.instance,
            difficulty = RaidDifficulty.MYTHIC,
            optional = command.optional,
            notes = command.notes
        )
        
        every { raidRepository.findByGuildIdAndDate(any(), any()) } returns emptyList()
        every { schedulingService.canScheduleRaid(any(), any()) } returns true
        every { raidRepository.save(any()) } returns scheduledRaid
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.guildId shouldBe GuildId(command.guildId)
        result.getOrNull()!!.scheduledDate shouldBe command.scheduledDate
        result.getOrNull()!!.status shouldBe RaidStatus.SCHEDULED
        
        verify(exactly = 1) { raidRepository.findByGuildIdAndDate(any(), any()) }
        verify(exactly = 1) { schedulingService.canScheduleRaid(any(), any()) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail when scheduling service validation fails`() {
        // Arrange
        val command = ScheduleRaidCommand(
            guildId = "test-guild",
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(23, 0),
            instance = "Nerub-ar Palace",
            difficulty = "MYTHIC",
            optional = false,
            notes = null
        )
        
        every { raidRepository.findByGuildIdAndDate(any(), any()) } returns emptyList()
        every { schedulingService.canScheduleRaid(any(), any()) } returns false
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull() shouldNotBe null
        
        verify(exactly = 1) { raidRepository.findByGuildIdAndDate(any(), any()) }
        verify(exactly = 1) { schedulingService.canScheduleRaid(any(), any()) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should schedule raid with minimal required fields`() {
        // Arrange
        val command = ScheduleRaidCommand(
            guildId = "test-guild",
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = null,
            endTime = null,
            instance = null,
            difficulty = null,
            optional = false,
            notes = null
        )
        
        val scheduledRaid = Raid.schedule(
            guildId = GuildId(command.guildId),
            scheduledDate = command.scheduledDate
        )
        
        every { raidRepository.findByGuildIdAndDate(any(), any()) } returns emptyList()
        every { schedulingService.canScheduleRaid(any(), any()) } returns true
        every { raidRepository.save(any()) } returns scheduledRaid
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.instance shouldBe null
        result.getOrNull()!!.difficulty shouldBe null
        
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should handle invalid difficulty string gracefully`() {
        // Arrange
        val command = ScheduleRaidCommand(
            guildId = "test-guild",
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(23, 0),
            instance = "Nerub-ar Palace",
            difficulty = "INVALID",
            optional = false,
            notes = null
        )
        
        val scheduledRaid = Raid.schedule(
            guildId = GuildId(command.guildId),
            scheduledDate = command.scheduledDate,
            startTime = command.startTime,
            endTime = command.endTime,
            instance = command.instance,
            difficulty = null,
            optional = command.optional
        )
        
        every { raidRepository.findByGuildIdAndDate(any(), any()) } returns emptyList()
        every { schedulingService.canScheduleRaid(any(), any()) } returns true
        every { raidRepository.save(any()) } returns scheduledRaid
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull()!!.difficulty shouldBe null
        
        verify(exactly = 1) { raidRepository.save(any()) }
    }
}
