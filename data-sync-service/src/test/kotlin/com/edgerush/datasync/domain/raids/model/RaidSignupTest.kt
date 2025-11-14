package com.edgerush.datasync.domain.raids.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RaidSignupTest : UnitTest() {
    
    @Test
    fun `should create signup with valid data`() {
        val raiderId = RaiderId(1L)
        val signup = RaidSignup.create(
            raiderId = raiderId,
            role = RaidRole.TANK,
            status = RaidSignup.SignupStatus.CONFIRMED,
            comment = "Ready to go"
        )
        
        signup.raiderId shouldBe raiderId
        signup.role shouldBe RaidRole.TANK
        signup.status shouldBe RaidSignup.SignupStatus.CONFIRMED
        signup.comment shouldBe "Ready to go"
        signup.selected shouldBe false
        signup.id shouldBe null
    }
    
    @Test
    fun `should create signup with default values`() {
        val raiderId = RaiderId(1L)
        val signup = RaidSignup.create(
            raiderId = raiderId,
            role = RaidRole.HEALER
        )
        
        signup.status shouldBe RaidSignup.SignupStatus.CONFIRMED
        signup.comment shouldBe null
        signup.selected shouldBe false
    }
    
    @Test
    fun `should select confirmed signup`() {
        val signup = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.DPS,
            status = RaidSignup.SignupStatus.CONFIRMED
        )
        
        val selected = signup.select()
        
        selected.selected shouldBe true
        signup.selected shouldBe false
    }
    
    @Test
    fun `should throw exception when selecting non-confirmed signup`() {
        val signup = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.DPS,
            status = RaidSignup.SignupStatus.TENTATIVE
        )
        
        shouldThrow<IllegalArgumentException> {
            signup.select()
        }
    }
    
    @Test
    fun `should deselect signup`() {
        val signup = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.TANK,
            status = RaidSignup.SignupStatus.CONFIRMED
        ).select()
        
        val deselected = signup.deselect()
        
        deselected.selected shouldBe false
        signup.selected shouldBe true
    }
    
    @Test
    fun `should update status`() {
        val signup = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.HEALER,
            status = RaidSignup.SignupStatus.CONFIRMED
        )
        
        val updated = signup.updateStatus(RaidSignup.SignupStatus.LATE)
        
        updated.status shouldBe RaidSignup.SignupStatus.LATE
        signup.status shouldBe RaidSignup.SignupStatus.CONFIRMED
    }
    
    @Test
    fun `should update comment`() {
        val signup = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.DPS,
            comment = "Old comment"
        )
        
        val updated = signup.updateComment("New comment")
        
        updated.comment shouldBe "New comment"
        signup.comment shouldBe "Old comment"
    }
    
    @Test
    fun `should check if signup is confirmed`() {
        val confirmed = RaidSignup.create(
            raiderId = RaiderId(1L),
            role = RaidRole.TANK,
            status = RaidSignup.SignupStatus.CONFIRMED
        )
        
        val tentative = RaidSignup.create(
            raiderId = RaiderId(2L),
            role = RaidRole.HEALER,
            status = RaidSignup.SignupStatus.TENTATIVE
        )
        
        confirmed.isConfirmed() shouldBe true
        tentative.isConfirmed() shouldBe false
    }
    
    @Test
    fun `should reconstitute signup with id`() {
        val signup = RaidSignup.reconstitute(
            id = 456L,
            raiderId = RaiderId(1L),
            role = RaidRole.TANK,
            status = RaidSignup.SignupStatus.CONFIRMED,
            comment = "Test",
            selected = true
        )
        
        signup.id shouldBe 456L
        signup.raiderId shouldBe RaiderId(1L)
        signup.selected shouldBe true
    }
}
