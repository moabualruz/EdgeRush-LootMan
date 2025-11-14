/**
 * FLPS (Final Loot Priority Score) Bounded Context.
 *
 * This bounded context is responsible for calculating loot priority scores based on:
 * - Raider Merit Score (RMS): attendance, performance, preparation, behavior
 * - Item Priority Index (IPI): upgrade value, tier impact, role multiplier
 * - Recency Decay Factor (RDF): reduces score for recent loot recipients
 *
 * ## Formula
 *
 * ```
 * FLPS = (RMS × IPI) × RDF
 * ```
 *
 * ## Key Components
 *
 * - **FlpsScore**: Value object representing the final score (0.0-1.0)
 * - **FlpsCalculationService**: Domain service for score calculation
 * - **FlpsModifierRepository**: Repository for guild-specific modifiers
 *
 * @since 1.0.0
 */
package com.edgerush.lootman.domain.flps
