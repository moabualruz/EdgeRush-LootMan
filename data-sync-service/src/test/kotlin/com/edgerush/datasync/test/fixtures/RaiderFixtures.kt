package com.edgerush.datasync.test.fixtures

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import java.time.Instant
import java.time.LocalDateTime

/**
 * Test fixtures for creating Raider instances.
 *
 * Provides helper functions for creating Raiders with sensible defaults,
 * allowing tests to focus on the specific properties they care about.
 */
object RaiderFixtures {
    private var characterIdCounter = 1000L

    /**
     * Creates a test Raider with sensible defaults.
     * All parameters can be overridden as needed.
     */
    fun createRaider(
        id: RaiderId = RaiderId(1L),
        characterId: CharacterId = CharacterId(nextCharacterId()),
        guildId: GuildId = GuildId("guild-123"),
        name: String = "Arthas",
        realm: String = "Icecrown",
        region: String = "eu",
        characterClass: CharacterClass = CharacterClass.DEATH_KNIGHT,
        role: Role = Role.TANK,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.of(2024, 1, 15, 10, 30),
        wowauditId: Long? = 12345L,
        blizzardId: Long? = null,
        accountId: AccountId? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Raider {
        return Raider(
            id = id,
            characterId = characterId,
            name = name,
            realm = realm,
            region = region,
            characterClass = characterClass,
            blizzardId = blizzardId,
            accountId = accountId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            guildId = guildId,
            role = role,
            rank = rank,
            status = status,
            joinDate = joinDate,
            wowauditId = wowauditId,
        )
    }

    /**
     * Alias for [createRaider] - commonly used name in tests.
     */
    fun createTestRaider(
        id: RaiderId = RaiderId(1L),
        characterId: CharacterId = CharacterId(nextCharacterId()),
        guildId: GuildId = GuildId("guild-123"),
        name: String = "Arthas",
        realm: String = "Icecrown",
        region: String = "eu",
        characterClass: CharacterClass = CharacterClass.DEATH_KNIGHT,
        role: Role = Role.TANK,
        rank: String? = "Raider",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        joinDate: LocalDateTime? = LocalDateTime.of(2024, 1, 15, 10, 30),
        wowauditId: Long? = 12345L,
        blizzardId: Long? = null,
        accountId: AccountId? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Raider = createRaider(
        id = id,
        characterId = characterId,
        guildId = guildId,
        name = name,
        realm = realm,
        region = region,
        characterClass = characterClass,
        role = role,
        rank = rank,
        status = status,
        joinDate = joinDate,
        wowauditId = wowauditId,
        blizzardId = blizzardId,
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    /**
     * Creates a list of test raiders with unique IDs and names.
     */
    fun createRaiders(count: Int, guildId: GuildId = GuildId("guild-123")): List<Raider> {
        val names = listOf("Arthas", "Thrall", "Jaina", "Sylvanas", "Illidan", "Anduin", "Varian", "Tyrande")
        return (1..count).map { i ->
            createRaider(
                id = RaiderId(i.toLong()),
                characterId = CharacterId(nextCharacterId()),
                guildId = guildId,
                name = names.getOrElse(i - 1) { "Raider$i" },
            )
        }
    }

    private fun nextCharacterId(): Long = characterIdCounter++

    /**
     * Resets the character ID counter. Call this in test setup if needed.
     */
    fun resetCounters() {
        characterIdCounter = 1000L
    }
}
