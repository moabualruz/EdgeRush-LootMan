package com.edgerush.lootman.api.graphql.subscription

import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * GraphQL Subscription resolver for sync events.
 *
 * Provides real-time updates for data synchronization events via WebSocket subscriptions.
 * Clients can subscribe to sync completion events for specific guilds.
 */
@Component
class SyncSubscriptionResolver(
    private val syncEventPublisher: SyncEventPublisher,
) : Subscription {

    /**
     * Subscribe to sync completed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of sync completed events
     */
    fun syncCompleted(guildId: String): Flow<SyncCompletedEvent> {
        return syncEventPublisher.syncCompletedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync started events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of sync started events
     */
    fun syncStarted(guildId: String): Flow<SyncStartedEvent> {
        return syncEventPublisher.syncStartedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync failed events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of sync failed events
     */
    fun syncFailed(guildId: String): Flow<SyncFailedEvent> {
        return syncEventPublisher.syncFailedEvents
            .filter { it.guildId == guildId }
    }

    /**
     * Subscribe to sync progress events for a guild.
     *
     * @param guildId The guild ID to filter events for
     * @return Flow of sync progress events
     */
    fun syncProgress(guildId: String): Flow<SyncProgressEvent> {
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
 */
@Component
class SyncEventPublisher {
    private val _syncCompletedEvents = MutableSharedFlow<SyncCompletedEvent>(replay = 0)
    private val _syncStartedEvents = MutableSharedFlow<SyncStartedEvent>(replay = 0)
    private val _syncFailedEvents = MutableSharedFlow<SyncFailedEvent>(replay = 0)
    private val _syncProgressEvents = MutableSharedFlow<SyncProgressEvent>(replay = 0)

    val syncCompletedEvents: Flow<SyncCompletedEvent> = _syncCompletedEvents
    val syncStartedEvents: Flow<SyncStartedEvent> = _syncStartedEvents
    val syncFailedEvents: Flow<SyncFailedEvent> = _syncFailedEvents
    val syncProgressEvents: Flow<SyncProgressEvent> = _syncProgressEvents

    /**
     * Publish a sync completed event.
     */
    suspend fun publishSyncCompleted(event: SyncCompletedEvent) {
        _syncCompletedEvents.emit(event)
    }

    /**
     * Publish a sync completed event using individual parameters.
     */
    suspend fun publishSyncCompleted(
        guildId: String,
        syncType: SyncType,
        recordsProcessed: Int,
        recordsCreated: Int,
        recordsUpdated: Int,
        durationMs: Long,
    ) {
        _syncCompletedEvents.emit(
            SyncCompletedEvent(
                guildId = guildId,
                syncType = syncType,
                recordsProcessed = recordsProcessed,
                recordsCreated = recordsCreated,
                recordsUpdated = recordsUpdated,
                durationMs = durationMs,
                completedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a sync started event.
     */
    suspend fun publishSyncStarted(event: SyncStartedEvent) {
        _syncStartedEvents.emit(event)
    }

    /**
     * Publish a sync started event using individual parameters.
     */
    suspend fun publishSyncStarted(
        guildId: String,
        syncType: SyncType,
    ) {
        _syncStartedEvents.emit(
            SyncStartedEvent(
                guildId = guildId,
                syncType = syncType,
                startedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a sync failed event.
     */
    suspend fun publishSyncFailed(event: SyncFailedEvent) {
        _syncFailedEvents.emit(event)
    }

    /**
     * Publish a sync failed event using individual parameters.
     */
    suspend fun publishSyncFailed(
        guildId: String,
        syncType: SyncType,
        errorMessage: String,
        errorCode: String?,
    ) {
        _syncFailedEvents.emit(
            SyncFailedEvent(
                guildId = guildId,
                syncType = syncType,
                errorMessage = errorMessage,
                errorCode = errorCode,
                failedAt = Instant.now(),
            )
        )
    }

    /**
     * Publish a sync progress event.
     */
    suspend fun publishSyncProgress(event: SyncProgressEvent) {
        _syncProgressEvents.emit(event)
    }

    /**
     * Publish a sync progress event using individual parameters.
     */
    suspend fun publishSyncProgress(
        guildId: String,
        syncType: SyncType,
        currentStep: String,
        processedCount: Int,
        totalCount: Int?,
        percentComplete: Int?,
    ) {
        _syncProgressEvents.emit(
            SyncProgressEvent(
                guildId = guildId,
                syncType = syncType,
                currentStep = currentStep,
                processedCount = processedCount,
                totalCount = totalCount,
                percentComplete = percentComplete,
                timestamp = Instant.now(),
            )
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
