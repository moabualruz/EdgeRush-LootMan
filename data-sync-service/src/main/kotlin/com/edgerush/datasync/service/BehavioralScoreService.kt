package com.edgerush.datasync.service

import com.edgerush.datasync.repository.BehavioralActionRepository
import com.edgerush.datasync.repository.LootBanRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BehavioralScoreService(
    private val behavioralActionRepository: BehavioralActionRepository,
    private val lootBanRepository: LootBanRepository,
) {
    private val logger = LoggerFactory.getLogger(BehavioralScoreService::class.java)

    /**
     * Calculate behavioral score for a character.
     * Default is 1.0 (perfect behavior) minus any active deductions.
     */
    fun calculateBehavioralScore(
        guildId: String,
        characterName: String,
    ): Double {
        val currentTime = LocalDateTime.now()
        val activeActions =
            behavioralActionRepository.findActiveActionsForCharacter(
                guildId,
                characterName,
                currentTime,
            )

        var behavioralScore = 1.0 // Start with perfect behavior

        activeActions.forEach { action ->
            when (action.actionType) {
                "DEDUCTION" -> {
                    behavioralScore -= action.deductionAmount
                    logger.debug("Applied behavioral deduction of ${action.deductionAmount} to $characterName: ${action.reason}")
                }
                "RESTORATION" -> {
                    behavioralScore += action.deductionAmount
                    logger.debug("Applied behavioral restoration of ${action.deductionAmount} to $characterName: ${action.reason}")
                }
            }
        }

        // Ensure score stays within bounds
        return behavioralScore.coerceIn(0.0, 1.0)
    }

    /**
     * Check if a character is currently banned from loot.
     */
    fun isCharacterBannedFromLoot(
        guildId: String,
        characterName: String,
    ): LootBanInfo {
        val currentTime = LocalDateTime.now()
        val activeBan =
            lootBanRepository.findActiveBanForCharacter(
                guildId,
                characterName,
                currentTime,
            )

        return if (activeBan != null) {
            LootBanInfo(
                isBanned = true,
                reason = activeBan.reason,
                bannedBy = activeBan.bannedBy,
                bannedAt = activeBan.bannedAt,
                expiresAt = activeBan.expiresAt,
            )
        } else {
            LootBanInfo(isBanned = false)
        }
    }

    /**
     * Get behavioral breakdown for a character including actions and ban status.
     */
    fun getBehavioralBreakdown(
        guildId: String,
        characterName: String,
    ): BehavioralBreakdown {
        val currentTime = LocalDateTime.now()
        val behavioralScore = calculateBehavioralScore(guildId, characterName)
        val lootBanInfo = isCharacterBannedFromLoot(guildId, characterName)
        val activeActions =
            behavioralActionRepository.findActiveActionsForCharacter(
                guildId,
                characterName,
                currentTime,
            )

        return BehavioralBreakdown(
            behavioralScore = behavioralScore,
            lootBanInfo = lootBanInfo,
            activeActions =
                activeActions.map { action ->
                    BehavioralActionInfo(
                        actionType = action.actionType,
                        deductionAmount = action.deductionAmount,
                        reason = action.reason,
                        appliedBy = action.appliedBy,
                        appliedAt = action.appliedAt,
                        expiresAt = action.expiresAt,
                    )
                },
        )
    }
}

data class LootBanInfo(
    val isBanned: Boolean,
    val reason: String? = null,
    val bannedBy: String? = null,
    val bannedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
)

data class BehavioralActionInfo(
    val actionType: String,
    val deductionAmount: Double,
    val reason: String,
    val appliedBy: String,
    val appliedAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
)

data class BehavioralBreakdown(
    val behavioralScore: Double,
    val lootBanInfo: LootBanInfo,
    val activeActions: List<BehavioralActionInfo>,
)
