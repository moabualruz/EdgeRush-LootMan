package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.time.Instant

/**
 * GraphQL Subscription resolver for sync events.
 *
 * Provides real-time updates for data synchronization events via WebSocket subscriptions.
 * Clients can subscribe to sync completion events for specific guilds.
 *
 * Uses Reactor Flux (Publisher) for compatibility with graphql-kotlin-spring-server.
 */
@Component
class SyncSubscriptionResolver(
    private val syncEventPublisher: SyncEventPublisher,
) : Subscription {
    /**
     * Subscribe to sync completed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of sync completed events
     */
    fun syncCompleted(guildId: String): Flux<SyncCompletedEvent> {
        return syncEventPublisher.syncCompletedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync started events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of sync started events
     */
    fun syncStarted(guildId: String): Flux<SyncStartedEvent> {
        return syncEventPublisher.syncStartedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync failed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of sync failed events
     */
    fun syncFailed(guildId: String): Flux<SyncFailedEvent> {
        return syncEventPublisher.syncFailedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync progress events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flux of sync progress events
     */
    fun syncProgress(guildId: String): Flux<SyncProgressEvent> {
        return syncEventPublisher.syncProgressEvents
            .filter { it.guildId == guildId }
    }
}

/**
 * Publisher for sync-related events.
 *
 * This component manages the event streams for sync subscriptions.
 * Other services can publish events through this component, and
 * subscribers will receive them via GraphQL subscriptions.
 *
 * Uses Reactor Sinks for thread-safe event publishing.
 */
@Component
class SyncEventPublisher {
    private val _syncCompletedEvents = Sinks.many().multicast().onBackpressureBuffer<SyncCompletedEvent>()
    private val _syncStartedEvents = Sinks.many().multicast().onBackpressureBuffer<SyncStartedEvent>()
    private val _syncFailedEvents = Sinks.many().multicast().onBackpressureBuffer<SyncFailedEvent>()
    private val _syncProgressEvents = Sinks.many().multicast().onBackpressureBuffer<SyncProgressEvent>()

    val syncCompletedEvents: Flux<SyncCompletedEvent> = _syncCompletedEvents.asFlux()
    val syncStartedEvents: Flux<SyncStartedEvent> = _syncStartedEvents.asFlux()
    val syncFailedEvents: Flux<SyncFailedEvent> = _syncFailedEvents.asFlux()
    val syncProgressEvents: Flux<SyncProgressEvent> = _syncProgressEvents.asFlux()

    /**
     * Publish a sync completed event.
     */
    fun publishSyncCompleted(event: SyncCompletedEvent) {
        _syncCompletedEvents.tryEmitNext(event)
    }

    /**
     * Publish a sync completed event using individual parameters.
     */
    fun publishSyncCompleted(
        guildId: String,
        syncType: SyncType,
        recordsProcessed: Int,
        recordsCreated: Int,
        recordsUpdated: Int,
        durationMs: Long,
    ) {
        _syncCompletedEvents.tryEmitNext(
            SyncCompletedEvent(
                guildId = guildId,
                syncType = syncType,
                recordsProcessed = recordsProcessed,
                recordsCreated = recordsCreated,
                recordsUpdated = recordsUpdated,
                durationMs = durationMs,
                completedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a sync started event.
     */
    fun publishSyncStarted(event: SyncStartedEvent) {
        _syncStartedEvents.tryEmitNext(event)
    }

    /**
     * Publish a sync started event using individual parameters.
     */
    fun publishSyncStarted(
        guildId: String,
        syncType: SyncType,
    ) {
        _syncStartedEvents.tryEmitNext(
            SyncStartedEvent(
                guildId = guildId,
                syncType = syncType,
                startedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a sync failed event.
     */
    fun publishSyncFailed(event: SyncFailedEvent) {
        _syncFailedEvents.tryEmitNext(event)
    }

    /**
     * Publish a sync failed event using individual parameters.
     */
    fun publishSyncFailed(
        guildId: String,
        syncType: SyncType,
        errorMessage: String,
        errorCode: String?,
    ) {
        _syncFailedEvents.tryEmitNext(
            SyncFailedEvent(
                guildId = guildId,
                syncType = syncType,
                errorMessage = errorMessage,
                errorCode = errorCode,
                failedAt = Instant.now(),
            ),
        )
    }

    /**
     * Publish a sync progress event.
     */
    fun publishSyncProgress(event: SyncProgressEvent) {
        _syncProgressEvents.tryEmitNext(event)
    }

    /**
     * Publish a sync progress event using individual parameters.
     */
    fun publishSyncProgress(
        guildId: String,
        syncType: SyncType,
        currentStep: String,
        processedCount: Int,
        totalCount: Int?,
        percentComplete: Int?,
    ) {
        _syncProgressEvents.tryEmitNext(
            SyncProgressEvent(
                guildId = guildId,
                syncType = syncType,
                currentStep = currentStep,
                processedCount = processedCount,
                totalCount = totalCount,
                percentComplete = percentComplete,
                timestamp = Instant.now(),
            ),
        )
    }
}

/**
 * Enum representing different types of data synchronization.
 */
enum class SyncType {
    WOWAUDIT_FULL,
    WOWAUDIT_INCREMENTAL,
    WARCRAFT_LOGS,
    SIMULATION,
    CHARACTER,
    ATTENDANCE,
    LOOT,
}

/**
 * Event type for sync completed notifications.
 */
data class SyncCompletedEvent(
    val guildId: String,
    val syncType: SyncType,
    val recordsProcessed: Int,
    val recordsCreated: Int,
    val recordsUpdated: Int,
    val durationMs: Long,
    val completedAt: Instant,
)

/**
 * Event type for sync started notifications.
 */
data class SyncStartedEvent(
    val guildId: String,
    val syncType: SyncType,
    val startedAt: Instant,
)

/**
 * Event type for sync failed notifications.
 */
data class SyncFailedEvent(
    val guildId: String,
    val syncType: SyncType,
    val errorMessage: String,
    val errorCode: String?,
    val failedAt: Instant,
)

/**
 * Event type for sync progress notifications.
 */
data class SyncProgressEvent(
    val guildId: String,
    val syncType: SyncType,
    val currentStep: String,
    val processedCount: Int,
    val totalCount: Int?,
    val percentComplete: Int?,
    val timestamp: Instant,
)
