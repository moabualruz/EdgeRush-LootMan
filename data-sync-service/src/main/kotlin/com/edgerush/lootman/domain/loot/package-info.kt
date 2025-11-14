/**
 * Loot Bounded Context.
 *
 * This bounded context manages loot distribution, awards, and bans.
 *
 * ## Key Components
 *
 * - **LootAward**: Aggregate root for loot awards
 * - **LootBan**: Entity for temporary loot restrictions
 * - **LootDistributionService**: Domain service for loot distribution logic
 * - **LootAwardRepository**: Repository for loot awards
 * - **LootBanRepository**: Repository for loot bans
 *
 * ## Business Rules
 *
 * - Raiders with active bans cannot receive loot
 * - Loot awards record the FLPS score at time of award
 * - Awards can be revoked with a reason
 * - Bans have expiration dates
 *
 * @since 1.0.0
 */
package com.edgerush.lootman.domain.loot
