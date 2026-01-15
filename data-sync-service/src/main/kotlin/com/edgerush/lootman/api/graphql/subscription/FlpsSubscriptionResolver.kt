package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.time.Instant

/**
 * GraphQL Subscription resolver for FLPS score events.
 *
 * Provides real-time updates for FLPS-related events via WebSocket subscriptions.
 * Clients can subscribe to score updates for specific guilds.
 *
 * Uses Reactor Flux (Publisher) for compatibility with graphql-kotlin-spring-server.
 */
@Component
class FlpsSubscriptionResolver(
    private val flpsEventPublisher: FlpsEventPublisher,
) : Subscription {
    /**
     * Subscribe to FLPS score updates for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of FLPS score updated events
     */
    fun flpsScoreUpdated(guildId: String): Flux<FlpsScoreUpdatedEvent> {
        return flpsEventPublisher.scoreUpdatedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to RDF expiry events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of RDF expired events
     */
    fun rdfExpired(guildId: String): Flux<RdfExpiredEvent> {
        return flpsEventPublisher.rdfExpiredEvents
            .filter { it.guildId == guildId }
    }
}

/**
 * Publisher for FLPS-related events.
 *
 * This component manages the event streams for FLPS subscriptions.
 * Other services can publish events through this component, and
 * subscribers will receive them via GraphQL subscriptions.
 *
 * Uses Reactor Sinks for thread-safe event publishing.
 */
@Component
class FlpsEventPublisher {
    private val _scoreUpdatedEvents = Sinks.many().multicast().onBackpressureBuffer<FlpsScoreUpdatedEvent>()
    private val _rdfExpiredEvents = Sinks.many().multicast().onBackpressureBuffer<RdfExpiredEvent>()

    val scoreUpdatedEvents: Flux<FlpsScoreUpdatedEvent> = _scoreUpdatedEvents.asFlux()
    val rdfExpiredEvents: Flux<RdfExpiredEvent> = _rdfExpiredEvents.asFlux()

    /**
     * Publish an FLPS score updated event.
     *
     * @param event The score updated event
     */
    fun publishScoreUpdated(event: FlpsScoreUpdatedEvent) {
        _scoreUpdatedEvents.tryEmitNext(event)
    }

    /**
     * Publish an FLPS score updated event using individual parameters.
     *
     * @param guildId The guild ID
     * @param raiderId The raider ID whose score was updated
     * @param itemId The item ID for which the score was calculated
     * @param oldScore The previous FLPS score (null if first calculation)
     * @param newScore The new FLPS score
     */
    fun publishScoreUpdated(
        guildId: String,
        raiderId: String,
        itemId: Long,
        oldScore: Double?,
        newScore: Double,
    ) {
        _scoreUpdatedEvents.tryEmitNext(
            FlpsScoreUpdatedEvent(
                guildId = guildId,
                raiderId = raiderId,
                itemId = itemId,
                oldScore = oldScore,
                newScore = newScore,
                updatedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish an RDF expired event.
     *
     * @param event The RDF expired event
     */
    fun publishRdfExpired(event: RdfExpiredEvent) {
        _rdfExpiredEvents.tryEmitNext(event)
    }

    /**
     * Publish an RDF expired event using individual parameters.
     *
     * @param guildId The guild ID
     * @param raiderId The raider ID whose RDF expired
     * @param itemId The item ID for which the RDF expired
     * @param expiredRdf The RDF value that expired
     */
    fun publishRdfExpired(
        guildId: String,
        raiderId: String,
        itemId: Long,
        expiredRdf: Double,
    ) {
        _rdfExpiredEvents.tryEmitNext(
            RdfExpiredEvent(
                guildId = guildId,
                raiderId = raiderId,
                itemId = itemId,
                expiredRdf = expiredRdf,
                expiredAt = Instant.now(),
            ),
        )
    }
}

/**
 * Event type for FLPS score update notifications.
 */
data class FlpsScoreUpdatedEvent(
    val guildId: String,
    val raiderId: String,
    val itemId: Long,
    val oldScore: Double?,
    val newScore: Double,
    val updatedAt: Instant,
)

/**
 * Event type for RDF expiry notifications.
 */
data class RdfExpiredEvent(
    val guildId: String,
    val raiderId: String,
    val itemId: Long,
    val expiredRdf: Double,
    val expiredAt: Instant,
)
