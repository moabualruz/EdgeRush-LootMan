package com.edgerush.lootman.api.loot

import com.edgerush.lootman.application.loot.GetLootHistoryUseCase
import com.edgerush.lootman.application.loot.ManageLootBansUseCase
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Loot operations.
 *
 * Provides endpoints for:
 * - Querying loot history
 * - Managing loot bans
 * - Viewing loot awards
 */
@RestController
@RequestMapping("/api/v1/loot")
class LootController(
    private val getLootHistoryUseCase: GetLootHistoryUseCase,
    private val manageLootBansUseCase: ManageLootBansUseCase,
) {
    /**
     * Get loot history for a guild.
     */
    @GetMapping("/guilds/{guildId}/history")
    fun getGuildLootHistory(
        @PathVariable guildId: String,
    ): LootHistoryResponse {
        val result = getLootHistoryUseCase.getGuildHistory(GuildId(guildId))
        return result
            .map { awards -> LootHistoryResponse.from(awards) }
            .getOrThrow()
    }

    /**
     * Get loot history for a raider.
     */
    @GetMapping("/raiders/{raiderId}/history")
    fun getRaiderLootHistory(
        @PathVariable raiderId: String,
    ): LootHistoryResponse {
        val result = getLootHistoryUseCase.getRaiderHistory(RaiderId(raiderId))
        return result
            .map { awards -> LootHistoryResponse.from(awards) }
            .getOrThrow()
    }

    /**
     * Get active loot bans for a raider.
     */
    @GetMapping("/raiders/{raiderId}/bans")
    fun getActiveBans(
        @PathVariable raiderId: String,
        @RequestParam guildId: String,
    ): LootBansResponse {
        val result = manageLootBansUseCase.getActiveBans(RaiderId(raiderId), GuildId(guildId))
        return result
            .map { bans -> LootBansResponse.from(bans) }
            .getOrThrow()
    }
}

