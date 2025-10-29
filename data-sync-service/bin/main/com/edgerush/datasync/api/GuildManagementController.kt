package com.edgerush.datasync.api

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.repository.BehavioralActionRepository
import com.edgerush.datasync.repository.LootBanRepository
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/guild/{guildId}/management")
class GuildManagementController(
    private val behavioralActionRepository: BehavioralActionRepository,
    private val lootBanRepository: LootBanRepository
) {

    /**
     * Apply behavioral deduction to a character
     */
    @PostMapping("/behavioral/deduction")
    fun applyBehavioralDeduction(
        @PathVariable guildId: String,
        @RequestBody request: BehavioralActionRequest
    ): BehavioralActionEntity {
        val action = BehavioralActionEntity(
            guildId = guildId,
            characterName = request.characterName,
            actionType = "DEDUCTION",
            deductionAmount = request.deductionAmount,
            reason = request.reason,
            appliedBy = request.appliedBy,
            appliedAt = LocalDateTime.now(),
            expiresAt = request.expiresAt
        )
        return behavioralActionRepository.save(action)
    }

    /**
     * Apply behavioral restoration to a character
     */
    @PostMapping("/behavioral/restoration")
    fun applyBehavioralRestoration(
        @PathVariable guildId: String,
        @RequestBody request: BehavioralActionRequest
    ): BehavioralActionEntity {
        val action = BehavioralActionEntity(
            guildId = guildId,
            characterName = request.characterName,
            actionType = "RESTORATION",
            deductionAmount = request.deductionAmount,
            reason = request.reason,
            appliedBy = request.appliedBy,
            appliedAt = LocalDateTime.now(),
            expiresAt = request.expiresAt
        )
        return behavioralActionRepository.save(action)
    }

    /**
     * Ban a character from loot
     */
    @PostMapping("/loot-ban")
    fun banCharacterFromLoot(
        @PathVariable guildId: String,
        @RequestBody request: LootBanRequest
    ): LootBanEntity {
        val ban = LootBanEntity(
            guildId = guildId,
            characterName = request.characterName,
            reason = request.reason,
            bannedBy = request.bannedBy,
            bannedAt = LocalDateTime.now(),
            expiresAt = request.expiresAt
        )
        return lootBanRepository.save(ban)
    }

    /**
     * Lift loot ban from a character
     */
    @DeleteMapping("/loot-ban/{characterName}")
    fun liftLootBan(
        @PathVariable guildId: String,
        @PathVariable characterName: String
    ): Map<String, String> {
        val currentTime = LocalDateTime.now()
        val activeBan = lootBanRepository.findActiveBanForCharacter(guildId, characterName, currentTime)
        
        return if (activeBan != null) {
            // Deactivate the ban by setting it to inactive
            val updatedBan = activeBan.copy(isActive = false)
            lootBanRepository.save(updatedBan)
            mapOf("status" to "Ban lifted for $characterName")
        } else {
            mapOf("status" to "No active ban found for $characterName")
        }
    }

    /**
     * Get all active behavioral actions for the guild
     */
    @GetMapping("/behavioral/active")
    fun getActiveBehavioralActions(
        @PathVariable guildId: String
    ): List<BehavioralActionEntity> {
        return behavioralActionRepository.findActiveActionsForGuild(guildId, LocalDateTime.now())
    }

    /**
     * Get all active loot bans for the guild
     */
    @GetMapping("/loot-bans/active")
    fun getActiveLootBans(
        @PathVariable guildId: String
    ): List<LootBanEntity> {
        return lootBanRepository.findActiveBansForGuild(guildId, LocalDateTime.now())
    }
}

data class BehavioralActionRequest(
    val characterName: String,
    val deductionAmount: Double, // 0.0 to 1.0
    val reason: String,
    val appliedBy: String,
    val expiresAt: LocalDateTime? = null // null = permanent
)

data class LootBanRequest(
    val characterName: String,
    val reason: String,
    val bannedBy: String,
    val expiresAt: LocalDateTime? = null // null = permanent
)