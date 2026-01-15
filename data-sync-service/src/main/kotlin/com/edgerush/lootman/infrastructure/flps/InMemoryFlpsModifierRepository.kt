package com.edgerush.lootman.infrastructure.flps

import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.repository.FlpsThresholds
import com.edgerush.lootman.domain.flps.repository.IpiWeights
import com.edgerush.lootman.domain.flps.repository.RmsWeights
import com.edgerush.lootman.domain.flps.repository.RoleMultipliers
import com.edgerush.lootman.domain.shared.GuildId
/**
 * In-memory implementation of FlpsModifierRepository.
 *
 * This implementation returns default modifiers for all guilds.
 * Used for testing without database dependency.
 */
class InMemoryFlpsModifierRepository : FlpsModifierRepository {
    private val defaultModifiers =
        FlpsModifiers(
            guildId = GuildId("default"),
            rmsWeights = RmsWeights(),
            ipiWeights = IpiWeights(),
            roleMultipliers = RoleMultipliers(),
            thresholds = FlpsThresholds(),
        )

    override fun findByGuildId(guildId: GuildId): FlpsModifiers {
        // Return default modifiers for all guilds.
        // For database-backed storage, use JdbcFlpsModifierRepository instead.
        return defaultModifiers.copy(guildId = guildId)
    }
}
