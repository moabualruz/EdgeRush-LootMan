package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for managing loot bans.
 *
 * This handles:
 * - Creating temporary or permanent loot bans
 * - Removing loot bans
 * - Querying active bans for a raider
 */
@Service
class ManageLootBansUseCase(
    private val lootBanRepository: LootBanRepository,
) {
    /**
     * Creates a new loot ban.
     *
     * @param command The ban creation parameters
     * @return Result containing LootBan or error
     */
    fun createBan(command: CreateLootBanCommand): Result<LootBan> =
        runCatching {
            // Validate reason
            require(command.reason.isNotBlank()) {
                "Ban reason cannot be blank"
            }

            // Validate expiration is in the future if provided
            command.expiresAt?.let { expiresAt ->
                require(expiresAt.isAfter(Instant.now())) {
                    "Ban expiration must be in the future"
                }
            }

            // Create ban
            val ban =
                LootBan.create(
                    raiderId = command.raiderId,
                    guildId = command.guildId,
                    reason = command.reason,
                    expiresAt = command.expiresAt,
                )

            // Persist
            lootBanRepository.save(ban)
        }

    /**
     * Removes a loot ban.
     *
     * @param command The ban removal parameters
     * @return Result indicating success or error
     */
    fun removeBan(command: RemoveLootBanCommand): Result<Unit> =
        runCatching {
            lootBanRepository.delete(command.banId)
        }

    /**
     * Gets all active bans for a raider.
     *
     * @param query The query parameters
     * @return Result containing list of active bans or error
     */
    fun getActiveBans(query: GetActiveBansQuery): Result<List<LootBan>> =
        runCatching {
            lootBanRepository.findActiveByRaiderId(query.raiderId, query.guildId)
        }
}

/**
 * Command for creating a loot ban.
 */
data class CreateLootBanCommand(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val reason: String,
    val expiresAt: Instant?, // null = permanent ban
)

/**
 * Command for removing a loot ban.
 */
data class RemoveLootBanCommand(
    val banId: LootBanId,
)

/**
 * Query for getting active bans.
 */
data class GetActiveBansQuery(
    val raiderId: RaiderId,
    val guildId: GuildId,
)
