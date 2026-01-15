package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.time.Instant

/**
 * GraphQL Subscription resolver for penalty events.
 *
 * Provides real-time updates for penalty-related events via WebSocket subscriptions.
 * Clients can subscribe to penalty and loot ban events for specific guilds.
 *
 * Uses Reactor Flux (Publisher) for compatibility with graphql-kotlin-spring-server.
 */
@Component
class PenaltySubscriptionResolver(
    private val penaltyEventPublisher: PenaltyEventPublisher,
) : Subscription {
    /**
     * Subscribe to penalty applied events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of penalty applied events
     */
    fun penaltyApplied(guildId: String): Flux<PenaltyAppliedEvent> {
        return penaltyEventPublisher.penaltyAppliedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to penalty removed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of penalty removed events
     */
    fun penaltyRemoved(guildId: String): Flux<PenaltyRemovedEvent> {
        return penaltyEventPublisher.penaltyRemovedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to loot ban applied events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of loot ban applied events
     */
    fun lootBanApplied(guildId: String): Flux<LootBanAppliedEvent> {
        return penaltyEventPublisher.lootBanAppliedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to loot ban lifted events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of loot ban lifted events
     */
    fun lootBanLifted(guildId: String): Flux<LootBanLiftedEvent> {
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
 *
 * Uses Reactor Sinks for thread-safe event publishing.
 */
@Component
class PenaltyEventPublisher {
    private val _penaltyAppliedEvents = Sinks.many().multicast().onBackpressureBuffer<PenaltyAppliedEvent>()
    private val _penaltyRemovedEvents = Sinks.many().multicast().onBackpressureBuffer<PenaltyRemovedEvent>()
    private val _lootBanAppliedEvents = Sinks.many().multicast().onBackpressureBuffer<LootBanAppliedEvent>()
    private val _lootBanLiftedEvents = Sinks.many().multicast().onBackpressureBuffer<LootBanLiftedEvent>()

    val penaltyAppliedEvents: Flux<PenaltyAppliedEvent> = _penaltyAppliedEvents.asFlux()
    val penaltyRemovedEvents: Flux<PenaltyRemovedEvent> = _penaltyRemovedEvents.asFlux()
    val lootBanAppliedEvents: Flux<LootBanAppliedEvent> = _lootBanAppliedEvents.asFlux()
    val lootBanLiftedEvents: Flux<LootBanLiftedEvent> = _lootBanLiftedEvents.asFlux()

    /**
     * Publish a penalty applied event.
     */
    fun publishPenaltyApplied(event: PenaltyAppliedEvent) {
        _penaltyAppliedEvents.tryEmitNext(event)
    }

    /**
     * Publish a penalty applied event using individual parameters.
     */
    fun publishPenaltyApplied(
        guildId: String,
        raiderId: String,
        penaltyId: String,
        penaltyType: String,
        reason: String,
        points: Int,
    ) {
        _penaltyAppliedEvents.tryEmitNext(
            PenaltyAppliedEvent(
                guildId = guildId,
                raiderId = raiderId,
                penaltyId = penaltyId,
                penaltyType = penaltyType,
                reason = reason,
                points = points,
                appliedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a penalty removed event.
     */
    fun publishPenaltyRemoved(event: PenaltyRemovedEvent) {
        _penaltyRemovedEvents.tryEmitNext(event)
    }

    /**
     * Publish a penalty removed event using individual parameters.
     */
    fun publishPenaltyRemoved(
        guildId: String,
        raiderId: String,
        penaltyId: String,
    ) {
        _penaltyRemovedEvents.tryEmitNext(
            PenaltyRemovedEvent(
                guildId = guildId,
                raiderId = raiderId,
                penaltyId = penaltyId,
                removedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a loot ban applied event.
     */
    fun publishLootBanApplied(event: LootBanAppliedEvent) {
        _lootBanAppliedEvents.tryEmitNext(event)
    }

    /**
     * Publish a loot ban applied event using individual parameters.
     */
    fun publishLootBanApplied(
        guildId: String,
        raiderId: String,
        banId: String,
        reason: String,
        expiresAt: Instant?,
    ) {
        _lootBanAppliedEvents.tryEmitNext(
            LootBanAppliedEvent(
                guildId = guildId,
                raiderId = raiderId,
                banId = banId,
                reason = reason,
                expiresAt = expiresAt,
                appliedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a loot ban lifted event.
     */
    fun publishLootBanLifted(event: LootBanLiftedEvent) {
        _lootBanLiftedEvents.tryEmitNext(event)
    }

    /**
     * Publish a loot ban lifted event using individual parameters.
     */
    fun publishLootBanLifted(
        guildId: String,
        raiderId: String,
        banId: String,
    ) {
        _lootBanLiftedEvents.tryEmitNext(
            LootBanLiftedEvent(
                guildId = guildId,
                raiderId = raiderId,
                banId = banId,
                liftedAt = Instant.now(),
            ),
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
