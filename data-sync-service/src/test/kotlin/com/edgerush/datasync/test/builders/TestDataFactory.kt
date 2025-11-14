package com.edgerush.datasync.test.builders

import com.edgerush.datasync.entity.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Factory for creating test data with sensible defaults.
 *
 * This factory provides builder methods for creating test entities with:
 * - Sensible default values
 * - Optional parameter overrides
 * - Consistent test data across tests
 *
 * Usage:
 * ```kotlin
 * // Create with defaults
 * val raider = TestDataFactory.createTestRaider()
 *
 * // Create with custom values
 * val raider = TestDataFactory.createTestRaider(
 *     characterName = "CustomName"
 * )
 * ```
 *
 * Note: Add more builder methods as needed for specific test scenarios.
 * Each builder should match the actual entity structure.
 */
object TestDataFactory {
    /**
     * Creates a test Raider entity with sensible defaults.
     */
    fun createTestRaider(
        id: Long? = null,
        characterName: String = "TestRaider-${UUID.randomUUID().toString().take(8)}",
        realm: String = "TestRealm",
        region: String = "US",
        wowauditId: Long? = null,
        clazz: String = "Warrior",
        spec: String = "Protection",
        role: String = "TANK",
        rank: String? = "Raider",
        status: String? = "Active",
        note: String? = null,
        blizzardId: Long? = null,
        trackingSince: java.time.OffsetDateTime? = null,
        joinDate: java.time.OffsetDateTime? = null,
        blizzardLastModified: java.time.OffsetDateTime? = null,
        lastSync: java.time.OffsetDateTime = java.time.OffsetDateTime.now(),
    ): RaiderEntity =
        RaiderEntity(
            id = id,
            characterName = characterName,
            realm = realm,
            region = region,
            wowauditId = wowauditId,
            clazz = clazz,
            spec = spec,
            role = role,
            rank = rank,
            status = status,
            note = note,
            blizzardId = blizzardId,
            trackingSince = trackingSince,
            joinDate = joinDate,
            blizzardLastModified = blizzardLastModified,
            lastSync = lastSync,
        )

    /**
     * Creates a test Raid entity with sensible defaults.
     */
    fun createTestRaid(
        raidId: Long = 1L,
        date: java.time.LocalDate? = java.time.LocalDate.now().plusDays(1),
        startTime: java.time.LocalTime? = java.time.LocalTime.of(19, 0),
        endTime: java.time.LocalTime? = java.time.LocalTime.of(22, 0),
        instance: String? = "Nerub-ar Palace",
        difficulty: String? = "Mythic",
        optional: Boolean? = false,
        status: String? = "SCHEDULED",
        presentSize: Int? = 20,
        totalSize: Int? = 20,
        notes: String? = null,
        selectionsImage: String? = null,
        teamId: Long? = 1L,
        seasonId: Long? = 1L,
        periodId: Long? = 1L,
        createdAt: java.time.OffsetDateTime? = java.time.OffsetDateTime.now(),
        updatedAt: java.time.OffsetDateTime? = java.time.OffsetDateTime.now(),
        syncedAt: java.time.OffsetDateTime = java.time.OffsetDateTime.now(),
    ): RaidEntity =
        RaidEntity(
            raidId = raidId,
            date = date,
            startTime = startTime,
            endTime = endTime,
            instance = instance,
            difficulty = difficulty,
            optional = optional,
            status = status,
            presentSize = presentSize,
            totalSize = totalSize,
            notes = notes,
            selectionsImage = selectionsImage,
            teamId = teamId,
            seasonId = seasonId,
            periodId = periodId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncedAt = syncedAt,
        )

    /**
     * Creates a test BehavioralAction entity with sensible defaults.
     */
    fun createTestBehavioralAction(
        id: Long? = null,
        guildId: String = "test-guild",
        characterName: String = "TestCharacter",
        actionType: String = "DEDUCTION",
        deductionAmount: Double = 0.1,
        reason: String = "Test reason",
        appliedBy: String = "GuildLeader",
        appliedAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = LocalDateTime.now().plusDays(7),
    ): BehavioralActionEntity =
        BehavioralActionEntity(
            id = id,
            guildId = guildId,
            characterName = characterName,
            actionType = actionType,
            deductionAmount = deductionAmount,
            reason = reason,
            appliedBy = appliedBy,
            appliedAt = appliedAt,
            expiresAt = expiresAt,
        )

    /**
     * Creates a test LootBan entity with sensible defaults.
     */
    fun createTestLootBan(
        id: Long? = null,
        guildId: String = "test-guild",
        characterName: String = "TestCharacter",
        reason: String = "Test ban reason",
        bannedBy: String = "GuildLeader",
        bannedAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
    ): LootBanEntity =
        LootBanEntity(
            id = id,
            guildId = guildId,
            characterName = characterName,
            reason = reason,
            bannedBy = bannedBy,
            bannedAt = bannedAt,
            expiresAt = expiresAt,
        )
}
