package com.edgerush.datasync.domain.raids.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class RaidEncounterTest : UnitTest() {
    
    @Test
    fun `should create encounter with valid data`() {
        // Arrange & Act
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros",
            enabled = true,
            extra = false,
            notes = "First boss"
        )
        
        // Assert
        encounter.encounterId shouldBe 1001L
        encounter.name shouldBe "Terros"
        encounter.enabled shouldBe true
        encounter.extra shouldBe false
        encounter.notes shouldBe "First boss"
        encounter.id shouldBe null
    }
    
    @Test
    fun `should create encounter with default values`() {
        // Arrange & Act
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros"
        )
        
        // Assert
        encounter.enabled shouldBe true
        encounter.extra shouldBe false
        encounter.notes shouldBe null
    }
    
    @Test
    fun `should throw exception when name is blank`() {
        // Arrange & Act & Assert
        shouldThrow<IllegalArgumentException> {
            RaidEncounter.create(
                encounterId = 1001L,
                name = ""
            )
        }
    }
    
    @Test
    fun `should disable encounter`() {
        // Arrange
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros",
            enabled = true
        )
        
        // Act
        val disabled = encounter.disable()
        
        // Assert
        disabled.enabled shouldBe false
        encounter.enabled shouldBe true // Original unchanged
    }
    
    @Test
    fun `should enable encounter`() {
        // Arrange
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros",
            enabled = false
        )
        
        // Act
        val enabled = encounter.enable()
        
        // Assert
        enabled.enabled shouldBe true
        encounter.enabled shouldBe false // Original unchanged
    }
    
    @Test
    fun `should update notes`() {
        // Arrange
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Terros",
            notes = "Old notes"
        )
        
        // Act
        val updated = encounter.updateNotes("New notes")
        
        // Assert
        updated.notes shouldBe "New notes"
        encounter.notes shouldBe "Old notes" // Original unchanged
    }
    
    @Test
    fun `should reconstitute encounter with id`() {
        // Arrange & Act
        val encounter = RaidEncounter.reconstitute(
            id = 123L,
            encounterId = 1001L,
            name = "Terros",
            enabled = true,
            extra = false,
            notes = "Test"
        )
        
        // Assert
        encounter.id shouldBe 123L
        encounter.encounterId shouldBe 1001L
        encounter.name shouldBe "Terros"
    }
}
