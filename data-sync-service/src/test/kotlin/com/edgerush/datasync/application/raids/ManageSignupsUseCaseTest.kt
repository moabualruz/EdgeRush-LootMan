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

class ManageSignupsUseCaseTest : UnitTest() {
    
    @MockK
    private lateinit var raidRepository: RaidRepository
    
    @InjectMockKs
    private lateinit var useCase: ManageSignupsUseCase
    
    @Test
    fun `should add signup successfully when raid exists and is scheduled`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        
        val command = AddSignupCommand(
            raidId = raidId.value,
            raiderId = 123L,
            role = "TANK",
            comment = "Main tank"
        )
        
        val updatedRaid = raid.addSignup(
            raider = RaiderId(command.raiderId),
            role = RaidRole.TANK,
            comment = command.comment
        )
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns updatedRaid
        
        // Act
        val result = useCase.addSignup(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.getSignupCount() shouldBe 1
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to add signup when raid not found`() {
        // Arrange
        val raidId = RaidId.generate()
        val command = AddSignupCommand(
            raidId = raidId.value,
            raiderId = 123L,
            role = "TANK",
            comment = null
        )
        
        every { raidRepository.findById(raidId) } returns null
        
        // Act
        val result = useCase.addSignup(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Raid not found: ${raidId.value}"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should fail to add signup when raider already signed up`() {
        // Arrange
        val raidId = RaidId.generate()
        val raiderId = RaiderId(123L)
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(raiderId, RaidRole.TANK)
        
        val command = AddSignupCommand(
            raidId = raidId.value,
            raiderId = raiderId.value,
            role = "DPS",
            comment = null
        )
        
        every { raidRepository.findById(raidId) } returns raid
        
        // Act
        val result = useCase.addSignup(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Raider already signed up"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should remove signup successfully when signup exists`() {
        // Arrange
        val raidId = RaidId.generate()
        val raiderId = RaiderId(123L)
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(raiderId, RaidRole.TANK)
        
        val command = RemoveSignupCommand(
            raidId = raidId.value,
            raiderId = raiderId.value
        )
        
        val updatedRaid = raid.removeSignup(raiderId)
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns updatedRaid
        
        // Act
        val result = useCase.removeSignup(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.getSignupCount() shouldBe 0
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should update signup status successfully`() {
        // Arrange
        val raidId = RaidId.generate()
        val raiderId = RaiderId(123L)
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(raiderId, RaidRole.TANK)
        
        val command = UpdateSignupStatusCommand(
            raidId = raidId.value,
            raiderId = raiderId.value,
            status = "TENTATIVE"
        )
        
        val updatedRaid = raid.updateSignupStatus(raiderId, RaidSignup.SignupStatus.TENTATIVE)
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns updatedRaid
        
        // Act
        val result = useCase.updateSignupStatus(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should select signup successfully when signup is confirmed`() {
        // Arrange
        val raidId = RaidId.generate()
        val raiderId = RaiderId(123L)
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        ).addSignup(raiderId, RaidRole.TANK)
        
        val command = SelectSignupCommand(
            raidId = raidId.value,
            raiderId = raiderId.value
        )
        
        val updatedRaid = raid.selectSignup(raiderId)
        
        every { raidRepository.findById(raidId) } returns raid
        every { raidRepository.save(any()) } returns updatedRaid
        
        // Act
        val result = useCase.selectSignup(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull() shouldNotBe null
        result.getOrNull()!!.getSelectedSignups().size shouldBe 1
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 1) { raidRepository.save(any()) }
    }
    
    @Test
    fun `should handle invalid role string gracefully`() {
        // Arrange
        val raidId = RaidId.generate()
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        
        val command = AddSignupCommand(
            raidId = raidId.value,
            raiderId = 123L,
            role = "INVALID_ROLE",
            comment = null
        )
        
        every { raidRepository.findById(raidId) } returns raid
        
        // Act
        val result = useCase.addSignup(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull()!!.message shouldBe "Invalid role: INVALID_ROLE"
        
        verify(exactly = 1) { raidRepository.findById(raidId) }
        verify(exactly = 0) { raidRepository.save(any()) }
    }
}
