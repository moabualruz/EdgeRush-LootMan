package com.edgerush.datasync.domain.applications.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CharacterInfoTest {

    @Test
    fun `should create valid character info`() {
        // Given/When
        val characterInfo = CharacterInfo(
            name = "TestChar",
            realm = "TestRealm",
            region = "US",
            characterClass = "Warrior",
            role = "Tank",
            race = "Human",
            faction = "Alliance",
            level = 80
        )

        // Then
        characterInfo.name shouldBe "TestChar"
        characterInfo.fullName() shouldBe "TestChar-TestRealm"
    }

    @Test
    fun `should throw exception when name is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            CharacterInfo("", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)
        }
    }

    @Test
    fun `should throw exception when level is invalid`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 100)
        }
    }

    @Test
    fun `should allow null optional fields`() {
        // Given/When
        val characterInfo = CharacterInfo(
            name = "TestChar",
            realm = "TestRealm",
            region = "US",
            characterClass = "Warrior",
            role = "Tank",
            race = null,
            faction = null,
            level = null
        )

        // Then
        characterInfo.race shouldBe null
        characterInfo.faction shouldBe null
        characterInfo.level shouldBe null
    }
}
