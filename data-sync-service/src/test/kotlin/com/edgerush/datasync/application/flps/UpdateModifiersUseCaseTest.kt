package com.edgerush.datasync.application.flps

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.domain.flps.repository.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateModifiersUseCaseTest {

    private lateinit var flpsModifierRepository: FlpsModifierRepository
    private lateinit var useCase: UpdateModifiersUseCase

    @BeforeEach
    fun setup() {
        flpsModifierRepository = mockk(relaxed = true)
        useCase = UpdateModifiersUseCase(flpsModifierRepository)
    }

    @Test
    fun `should update modifiers with valid configuration`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "test-guild",
            rmsWeights = RmsWeights(
                attendance = 0.4,
                mechanical = 0.4,
                preparation = 0.2
            ),
            ipiWeights = IpiWeights(
                upgradeValue = 0.45,
                tierBonus = 0.35,
                roleMultiplier = 0.20
            ),
            thresholds = FlpsThresholds(
                eligibilityAttendance = 0.8,
                eligibilityActivity = 0.0
            ),
            roleMultipliers = RoleMultipliers(
                tank = 0.8,
                healer = 0.7,
                dps = 1.0
            ),
            recencyPenalties = RecencyPenalties(
                tierA = 0.8,
                tierB = 0.9,
                tierC = 1.0,
                recoveryRate = 0.1
            )
        )

        val configSlot = slot<FlpsConfiguration>()
        every { flpsModifierRepository.save("test-guild", capture(configSlot)) } returns Unit

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { flpsModifierRepository.save("test-guild", any()) }
        
        val savedConfig = configSlot.captured
        assertEquals(0.4, savedConfig.rmsWeights.attendance)
        assertEquals(0.4, savedConfig.rmsWeights.mechanical)
        assertEquals(0.2, savedConfig.rmsWeights.preparation)
        assertEquals(0.45, savedConfig.ipiWeights.upgradeValue)
        assertEquals(0.35, savedConfig.ipiWeights.tierBonus)
        assertEquals(0.20, savedConfig.ipiWeights.roleMultiplier)
    }

    @Test
    fun `should reject invalid RMS weights that don't sum to positive value`() {
        // Given - RmsWeights constructor will throw before we even get to the use case
        // This tests that the domain model itself validates
        val exception = assertThrows<IllegalArgumentException> {
            RmsWeights(
                attendance = 0.0,
                mechanical = 0.0,
                preparation = 0.0
            )
        }

        // Then
        assertTrue(exception.message?.contains("Sum of weights must be greater than 0") == true)
    }

    @Test
    fun `should reject negative RMS weights`() {
        // Given - RmsWeights constructor will throw before we even get to the use case
        // This tests that the domain model itself validates
        val exception = assertThrows<IllegalArgumentException> {
            RmsWeights(
                attendance = -0.5,
                mechanical = 0.5,
                preparation = 0.5
            )
        }

        // Then
        assertTrue(exception.message?.contains("All weights must be non-negative") == true)
    }

    @Test
    fun `should reject invalid IPI weights that don't sum to positive value`() {
        // Given - IpiWeights constructor will throw before we even get to the use case
        // This tests that the domain model itself validates
        val exception = assertThrows<IllegalArgumentException> {
            IpiWeights(
                upgradeValue = 0.0,
                tierBonus = 0.0,
                roleMultiplier = 0.0
            )
        }

        // Then
        assertTrue(exception.message?.contains("Sum of weights must be greater than 0") == true)
    }

    @Test
    fun `should reject negative IPI weights`() {
        // Given - IpiWeights constructor will throw before we even get to the use case
        // This tests that the domain model itself validates
        val exception = assertThrows<IllegalArgumentException> {
            IpiWeights(
                upgradeValue = -0.5,
                tierBonus = 0.5,
                roleMultiplier = 0.2
            )
        }

        // Then
        assertTrue(exception.message?.contains("All weights must be non-negative") == true)
    }

    @Test
    fun `should reject invalid attendance threshold`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "test-guild",
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds(
                eligibilityAttendance = 1.5,
                eligibilityActivity = 0.0
            ),
            roleMultipliers = RoleMultipliers.default(),
            recencyPenalties = RecencyPenalties.default()
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Attendance threshold must be between 0.0 and 1.0") == true)
        verify(exactly = 0) { flpsModifierRepository.save(any(), any()) }
    }

    @Test
    fun `should reject invalid role multipliers`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "test-guild",
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds.default(),
            roleMultipliers = RoleMultipliers(
                tank = -0.5,
                healer = 0.7,
                dps = 1.0
            ),
            recencyPenalties = RecencyPenalties.default()
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Role multipliers must be non-negative") == true)
        verify(exactly = 0) { flpsModifierRepository.save(any(), any()) }
    }

    @Test
    fun `should reject invalid recency penalties`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "test-guild",
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds.default(),
            roleMultipliers = RoleMultipliers.default(),
            recencyPenalties = RecencyPenalties(
                tierA = 1.5,
                tierB = 0.9,
                tierC = 1.0,
                recoveryRate = 0.1
            )
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Recency penalties must be between 0.0 and 1.0") == true)
        verify(exactly = 0) { flpsModifierRepository.save(any(), any()) }
    }

    @Test
    fun `should reject blank guild ID`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "",
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds.default(),
            roleMultipliers = RoleMultipliers.default(),
            recencyPenalties = RecencyPenalties.default()
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Guild ID must not be blank") == true)
        verify(exactly = 0) { flpsModifierRepository.save(any(), any()) }
    }

    @Test
    fun `should return failure when repository throws exception`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "test-guild",
            rmsWeights = RmsWeights.default(),
            ipiWeights = IpiWeights.default(),
            thresholds = FlpsThresholds.default(),
            roleMultipliers = RoleMultipliers.default(),
            recencyPenalties = RecencyPenalties.default()
        )

        every { flpsModifierRepository.save("test-guild", any()) } throws RuntimeException("Database error")

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should accept valid custom configuration`() {
        // Given
        val command = UpdateModifiersCommand(
            guildId = "custom-guild",
            rmsWeights = RmsWeights(
                attendance = 0.5,
                mechanical = 0.3,
                preparation = 0.2
            ),
            ipiWeights = IpiWeights(
                upgradeValue = 0.45,
                tierBonus = 0.35,
                roleMultiplier = 0.20
            ),
            thresholds = FlpsThresholds(
                eligibilityAttendance = 0.75,
                eligibilityActivity = 0.1
            ),
            roleMultipliers = RoleMultipliers(
                tank = 0.9,
                healer = 0.85,
                dps = 1.0
            ),
            recencyPenalties = RecencyPenalties(
                tierA = 0.7,
                tierB = 0.85,
                tierC = 0.95,
                recoveryRate = 0.15
            )
        )

        val configSlot = slot<FlpsConfiguration>()
        every { flpsModifierRepository.save("custom-guild", capture(configSlot)) } returns Unit

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { flpsModifierRepository.save("custom-guild", any()) }
        
        val savedConfig = configSlot.captured
        assertEquals(0.5, savedConfig.rmsWeights.attendance)
        assertEquals(0.75, savedConfig.thresholds.eligibilityAttendance)
        assertEquals(0.9, savedConfig.roleMultipliers.tank)
        assertEquals(0.7, savedConfig.recencyPenalties.tierA)
    }
}
