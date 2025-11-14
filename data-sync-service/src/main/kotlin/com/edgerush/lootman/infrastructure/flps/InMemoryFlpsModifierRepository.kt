package com.edgerush.lootman.infrastructure.flps

import com.edgerush.lootman.domain.flps.repository.*
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Repository

/**
 * In-memory implementation of FlpsModifierRepository.
 *
 * This implementation returns default modifiers for all guilds.
 * In a future iteration, this will be replaced with a database-backed implementation.
 */
@Repository
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
        // For now, return default modifiers for all guilds
        // TODO: Implement database-backed storage
        return defaultModifiers.copy(guildId = guildId)
    }
}
