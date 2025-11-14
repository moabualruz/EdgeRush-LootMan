package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId

import org.springframework.stereotype.Service

/**
 * Use case for managing loot bans.
 *
 * Handles creation of loot bans and querying active bans for raiders.
 */
@Service
class ManageLootBansUseCase(
    private val lootBanRepository: LootBanRepository,
) {
    /**
     * Creates a new loot ban for a raider.
     */
    fun createBan(command: CreateLootBanCommand): Result<LootBan> =
        runCatching {
            val expiresAt =
                if (command.durationDays > 0) {
                    java.time.Instant.now().plusSeconds(command.durationDays.toLong() * 24 * 60 * 60)
                } else {
                    null
                }

            val ban =
                LootBan.create(
                    raiderId = command.raiderId,
                    guildId = command.guildId,
                    reason = command.reason,
                    expiresAt = expiresAt,
                )

            lootBanRepository.save(ban)
        }

    /**
     * Gets all active bans for a raider in a guild.
     */
    fun getActiveBans(
        raiderId: RaiderId,
        guildId: GuildId,
    ): Result<List<LootBan>> =
        runCatching {
            lootBanRepository.findActiveByRaiderId(raiderId, guildId)
        }
}

data class CreateLootBanCommand(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val reason: String,
    val durationDays: Int,
)

