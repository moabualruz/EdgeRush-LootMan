package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * GraphQL Subscription resolver for Loot events.
 *
 * Provides real-time updates for loot-related events via WebSocket subscriptions.
 * Clients can subscribe to loot awarded and revoked events for specific guilds.
 */
@Component
class LootSubscriptionResolver(
    private val lootEventPublisher: LootEventPublisher,
) : Subscription {
    /**
     * Subscribe to loot awarded events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of loot awarded events
     */
    fun lootAwarded(guildId: String): Flow<LootAwardedEvent> {
        return lootEventPublisher.lootAwardedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to loot revoked events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of loot revoked events
     */
    fun lootRevoked(guildId: String): Flow<LootRevokedEvent> {
        return lootEventPublisher.lootRevokedEvents
            .filter { it.guildId == guildId }
    }
}

/**
 * Publisher for loot-related events.
 *
 * This component manages the event streams for loot subscriptions.
 * Other services can publish events through this component, and
 * subscribers will receive them via GraphQL subscriptions.
 */
@Component
class LootEventPublisher {
    private val _lootAwardedEvents = MutableSharedFlow<LootAwardedEvent>(replay = 0)
    private val _lootRevokedEvents = MutableSharedFlow<LootRevokedEvent>(replay = 0)

    val lootAwardedEvents: Flow<LootAwardedEvent> = _lootAwardedEvents
    val lootRevokedEvents: Flow<LootRevokedEvent> = _lootRevokedEvents

    /**
     * Publish a loot awarded event.
     *
     * @param award The loot award that was created
     */
    suspend fun publishLootAwarded(award: LootAward) {
        _lootAwardedEvents.emit(award.toLootAwardedEvent())
    }

    /**
     * Publish a loot revoked event.
     *
     * @param guildId The guild ID where the revocation occurred
     * @param awardId The ID of the revoked award
     */
    suspend fun publishLootRevoked(
        guildId: String,
        awardId: String,
    ) {
        _lootRevokedEvents.emit(
            LootRevokedEvent(
                guildId = guildId,
                awardId = awardId,
                revokedAt = Instant.now(),
            ),
        )
    }
}

/**
 * Event type for loot awarded notifications.
 */
data class LootAwardedEvent(
    val id: String,
    val guildId: String,
    val raiderId: String,
    val itemId: String,
    val tier: LootTier,
    val flpsScore: Double,
    val awardedAt: Instant,
)

/**
 * Event type for loot revoked notifications.
 */
data class LootRevokedEvent(
    val guildId: String,
    val awardId: String,
    val revokedAt: Instant,
)

/**
 * Extension function to convert LootAward to LootAwardedEvent.
 */
private fun LootAward.toLootAwardedEvent(): LootAwardedEvent =
    LootAwardedEvent(
        id = this.id.value,
        guildId = this.guildId.value,
        raiderId = this.raiderId.value.toString(),
        itemId = this.itemId.value.toString(),
        tier = this.tier,
        flpsScore = this.flpsScore.value,
        awardedAt = this.awardedAt,
    )
