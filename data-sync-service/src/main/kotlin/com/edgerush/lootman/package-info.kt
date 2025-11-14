/**
 * EdgeRush LootMan - Root package for the loot management system.
 *
 * This package contains the complete implementation of the FLPS (Final Loot Priority Score)
 * system for World of Warcraft guild loot distribution.
 *
 * ## Architecture
 *
 * The codebase follows Domain-Driven Design (DDD) principles with clear separation of concerns:
 *
 * - **api**: REST and GraphQL API endpoints (presentation layer)
 * - **application**: Use cases and application services (application layer)
 * - **domain**: Business logic and domain models (domain layer)
 * - **infrastructure**: Technical implementations (infrastructure layer)
 * - **shared**: Cross-cutting concerns and utilities
 *
 * ## Bounded Contexts
 *
 * The domain is organized into the following bounded contexts:
 *
 * - **flps**: FLPS calculation and scoring
 * - **loot**: Loot distribution and awards
 * - **attendance**: Raid attendance tracking
 * - **raids**: Raid scheduling and management
 * - **applications**: Guild application system
 * - **integrations**: External API integrations (WoWAudit, Warcraft Logs, Raidbots)
 *
 * @since 1.0.0
 */
package com.edgerush.lootman
