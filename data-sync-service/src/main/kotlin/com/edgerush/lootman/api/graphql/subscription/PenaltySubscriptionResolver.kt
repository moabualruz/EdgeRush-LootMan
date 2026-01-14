package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * GraphQL Subscription resolver for penalty events.
 *
 * Provides real-time updates for penalty-related events via WebSocket subscriptions.
 * Clients can subscribe to penalty and loot ban events for specific guilds.
 */
@Component
class PenaltySubscriptionResolver(
    private val penaltyEventPublisher: PenaltyEventPublisher,
) : Subscription {

    /**
     * Subscribe to penalty applied events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of penalty applied events
     */
    fun penaltyApplied(guildId: String): Flow<PenaltyAppliedEvent> {
        return penaltyEventPublisher.penaltyAppliedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to penalty removed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of penalty removed events
     */
    fun penaltyRemoved(guildId: String): Flow<PenaltyRemovedEvent> {
        return penaltyEventPublisher.penaltyRemovedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to loot ban applied events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of loot ban applied events
     */
    fun lootBanApplied(guildId: String): Flow<LootBanAppliedEvent> {
        return penaltyEventPublisher.lootBanAppliedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to loot ban lifted events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of loot ban lifted events
     */
    fun lootBanLifted(guildId: String): Flow<LootBanLiftedEvent> {
        return penaltyEventPublisher.lootBanLiftedEvents
            .filter { it.guildId == guildId }
    }
}

/**
 * Publisher for penalty-related events.
 *
 * This component manages the event streams for penalty subscriptions.
 * Other services can publish events through this component, and
 * subscribers will receive them via GraphQL subscriptions.
 */
@Component
class PenaltyEventPublisher {
    private val _penaltyAppliedEvents = MutableSharedFlow<PenaltyAppliedEvent>(replay = 0)
    private val _penaltyRemovedEvents = MutableSharedFlow<PenaltyRemovedEvent>(replay = 0)
    private val _lootBanAppliedEvents = MutableSharedFlow<LootBanAppliedEvent>(replay = 0)
    private val _lootBanLiftedEvents = MutableSharedFlow<LootBanLiftedEvent>(replay = 0)

    val penaltyAppliedEvents: Flow<PenaltyAppliedEvent> = _penaltyAppliedEvents
    val penaltyRemovedEvents: Flow<PenaltyRemovedEvent> = _penaltyRemovedEvents
    val lootBanAppliedEvents: Flow<LootBanAppliedEvent> = _lootBanAppliedEvents
    val lootBanLiftedEvents: Flow<LootBanLiftedEvent> = _lootBanLiftedEvents

    /**
     * Publish a penalty applied event.
     */
    suspend fun publishPenaltyApplied(event: PenaltyAppliedEvent) {
        _penaltyAppliedEvents.emit(event)
    }

    /**
     * Publish a penalty applied event using individual parameters.
     */
    suspend fun publishPenaltyApplied(
        guildId: String,
        raiderId: String,
        penaltyId: String,
        penaltyType: String,
        reason: String,
        points: Int,
    ) {
        _penaltyAppliedEvents.emit(
            PenaltyAppliedEvent(
                guildId = guildId,
                raiderId = raiderId,
                penaltyId = penaltyId,
                penaltyType = penaltyType,
                reason = reason,
                points = points,
                appliedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a penalty removed event.
     */
    suspend fun publishPenaltyRemoved(event: PenaltyRemovedEvent) {
        _penaltyRemovedEvents.emit(event)
    }

    /**
     * Publish a penalty removed event using individual parameters.
     */
    suspend fun publishPenaltyRemoved(
        guildId: String,
        raiderId: String,
        penaltyId: String,
    ) {
        _penaltyRemovedEvents.emit(
            PenaltyRemovedEvent(
                guildId = guildId,
                raiderId = raiderId,
                penaltyId = penaltyId,
                removedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a loot ban applied event.
     */
    suspend fun publishLootBanApplied(event: LootBanAppliedEvent) {
        _lootBanAppliedEvents.emit(event)
    }

    /**
     * Publish a loot ban applied event using individual parameters.
     */
    suspend fun publishLootBanApplied(
        guildId: String,
        raiderId: String,
        banId: String,
        reason: String,
        expiresAt: Instant?,
    ) {
        _lootBanAppliedEvents.emit(
            LootBanAppliedEvent(
                guildId = guildId,
                raiderId = raiderId,
                banId = banId,
                reason = reason,
                expiresAt = expiresAt,
                appliedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a loot ban lifted event.
     */
    suspend fun publishLootBanLifted(event: LootBanLiftedEvent) {
        _lootBanLiftedEvents.emit(event)
    }

    /**
     * Publish a loot ban lifted event using individual parameters.
     */
    suspend fun publishLootBanLifted(
        guildId: String,
        raiderId: String,
        banId: String,
    ) {
        _lootBanLiftedEvents.emit(
            LootBanLiftedEvent(
                guildId = guildId,
                raiderId = raiderId,
                banId = banId,
                liftedAt = Instant.now(),
            )
        )
    }
}

/**
 * Event type for penalty applied notifications.
 */
data class PenaltyAppliedEvent(
    val guildId: String,
    val raiderId: String,
    val penaltyId: String,
    val penaltyType: String,
    val reason: String,
    val points: Int,
    val appliedAt: Instant,
)

/**
 * Event type for penalty removed notifications.
 */
data class PenaltyRemovedEvent(
    val guildId: String,
    val raiderId: String,
    val penaltyId: String,
    val removedAt: Instant,
)

/**
 * Event type for loot ban applied notifications.
 */
data class LootBanAppliedEvent(
    val guildId: String,
    val raiderId: String,
    val banId: String,
    val reason: String,
    val expiresAt: Instant?,
    val appliedAt: Instant,
)

/**
 * Event type for loot ban lifted notifications.
 */
data class LootBanLiftedEvent(
    val guildId: String,
    val raiderId: String,
    val banId: String,
    val liftedAt: Instant,
)
