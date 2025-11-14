/**
 * Raids Bounded Context.
 *
 * This bounded context manages raid scheduling, signups, and execution.
 *
 * ## Key Components
 *
 * - **Raid**: Aggregate root for raids
 * - **RaidEncounter**: Entity for individual boss encounters
 * - **RaidSignup**: Entity for raider signups
 * - **RaidSchedulingService**: Domain service for scheduling logic
 * - **RaidRepository**: Repository for raids
 *
 * ## Business Rules
 *
 * - Raids must be scheduled before signups
 * - Minimum roster size required to start
 * - Encounters track kills and wipes
 * - Signups have role assignments
 * - Raids have status lifecycle (scheduled → in progress → completed)
 *
 * @since 1.0.0
 */
package com.edgerush.lootman.domain.raids
