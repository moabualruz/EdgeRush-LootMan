package com.edgerush.lootman.api.graphql.subscription

import org.springframework.stereotype.Service

/**
 * Aggregated service for publishing events across all subscription types.
 *
 * This service provides a single entry point for other services to publish
 * events that will be delivered to GraphQL subscribers. It aggregates all
 * individual event publishers for convenient access.
 *
 * Usage example:
 * ```kotlin
 * @Service
 * class LootAwardService(
 *     private val eventPublisher: EventPublisherService,
 * ) {
 *     suspend fun awardLoot(award: LootAward) {
 *         // ... award loot logic
 *         eventPublisher.loot.publishLootAwarded(award)
 *     }
 * }
 * ```
 */
@Service
class EventPublisherService(
    val loot: LootEventPublisher,
    val flps: FlpsEventPublisher,
    val penalty: PenaltyEventPublisher,
    val sync: SyncEventPublisher,
)
