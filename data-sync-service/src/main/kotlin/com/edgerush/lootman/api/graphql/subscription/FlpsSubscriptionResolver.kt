package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * GraphQL Subscription resolver for FLPS score events.
 *
 * Provides real-time updates for FLPS-related events via WebSocket subscriptions.
 * Clients can subscribe to score updates for specific guilds.
 */
@Component
class FlpsSubscriptionResolver(
    private val flpsEventPublisher: FlpsEventPublisher,
) : Subscription {
    /**
     * Subscribe to FLPS score updates for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of FLPS score updated events
     */
    fun flpsScoreUpdated(guildId: String): Flow<FlpsScoreUpdatedEvent> {
        return flpsEventPublisher.scoreUpdatedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to RDF expiry events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of RDF expired events
     */
    fun rdfExpired(guildId: String): Flow<RdfExpiredEvent> {
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
 */
@Component
class FlpsEventPublisher {
    private val _scoreUpdatedEvents = MutableSharedFlow<FlpsScoreUpdatedEvent>(replay = 0)
    private val _rdfExpiredEvents = MutableSharedFlow<RdfExpiredEvent>(replay = 0)

    val scoreUpdatedEvents: Flow<FlpsScoreUpdatedEvent> = _scoreUpdatedEvents
    val rdfExpiredEvents: Flow<RdfExpiredEvent> = _rdfExpiredEvents

    /**
     * Publish an FLPS score updated event.
     *
     * @param event The score updated event
     */
    suspend fun publishScoreUpdated(event: FlpsScoreUpdatedEvent) {
        _scoreUpdatedEvents.emit(event)
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
    suspend fun publishScoreUpdated(
        guildId: String,
        raiderId: String,
        itemId: Long,
        oldScore: Double?,
        newScore: Double,
    ) {
        _scoreUpdatedEvents.emit(
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
    suspend fun publishRdfExpired(event: RdfExpiredEvent) {
        _rdfExpiredEvents.emit(event)
    }

    /**
     * Publish an RDF expired event using individual parameters.
     *
     * @param guildId The guild ID
     * @param raiderId The raider ID whose RDF expired
     * @param itemId The item ID for which the RDF expired
     * @param expiredRdf The RDF value that expired
     */
    suspend fun publishRdfExpired(
        guildId: String,
        raiderId: String,
        itemId: Long,
        expiredRdf: Double,
    ) {
        _rdfExpiredEvents.emit(
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
