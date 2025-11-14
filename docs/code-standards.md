# Code Standards

## Overview

This document defines the coding standards for the EdgeRush LootMan project. All code must follow these standards to ensure consistency, maintainability, and quality.

## Kotlin Conventions

### General Principles

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write idiomatic Kotlin code
- Prefer immutability over mutability
- Use data classes for simple data containers
- Leverage Kotlin's null safety features
- Use extension functions appropriately

### Code Formatting

**Automated with ktlint:**
```bash
# Check formatting
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

**Key Rules:**
- 4 spaces for indentation (no tabs)
- 120 characters maximum line length
- No trailing whitespace
- Single blank line at end of file
- No wildcard imports

### Naming Conventions

**Classes and Interfaces:**
```kotlin
// PascalCase for classes
class FlpsCalculationService
class LootAward
interface LootAwardRepository

// Suffix interfaces with descriptive names (not "I" prefix)
interface Repository  // ❌ Bad
interface LootAwardRepository  // ✅ Good
```

**Functions and Variables:**
```kotlin
// camelCase for functions and variables
fun calculateFlpsScore(): FlpsScore
val raiderMeritScore: Double
var currentState: State

// Boolean variables should be questions
val isActive: Boolean
val hasLootBan: Boolean
val canReceiveLoot: Boolean
```

**Constants:**
```kotlin
// UPPER_SNAKE_CASE for constants
const val MAX_FLPS_SCORE = 1.0
const val MIN_ATTENDANCE_THRESHOLD = 0.75
```

**Packages:**
```kotlin
// lowercase, no underscores
package com.edgerush.lootman.domain.flps
package com.edgerush.lootman.application.loot
```

## Domain-Driven Design Patterns

### Value Objects

**Characteristics:**
- Immutable
- Defined by their attributes
- No identity
- Validate in constructor

**Example:**
```kotlin
data class FlpsScore private constructor(val value: Double) {
    
    init {
        require(value in 0.0..1.0) { 
            "FLPS score must be between 0.0 and 1.0, got $value" 
        }
    }
    
    companion object {
        fun of(value: Double): FlpsScore = FlpsScore(value)
        
        fun zero(): FlpsScore = FlpsScore(0.0)
        
        fun max(): FlpsScore = FlpsScore(1.0)
    }
    
    operator fun plus(other: FlpsScore): FlpsScore = 
        FlpsScore((value + other.value).coerceIn(0.0, 1.0))
    
    operator fun times(multiplier: Double): FlpsScore = 
        FlpsScore((value * multiplier).coerceIn(0.0, 1.0))
}
```

**Guidelines:**
- Use `private constructor` with `companion object` factory methods
- Validate all inputs in `init` block
- Provide meaningful factory methods
- Implement operators when appropriate
- Make all properties `val` (immutable)

### Entities

**Characteristics:**
- Have identity (ID)
- Mutable state (but controlled)
- Lifecycle methods
- Business logic

**Example:**
```kotlin
data class LootAward(
    val id: LootAwardId,
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val awardedAt: Instant,
    val flpsScore: FlpsScore,
    val tier: LootTier,
    private var status: LootAwardStatus = LootAwardStatus.ACTIVE
) {
    
    fun revoke(reason: String): LootAward {
        require(status == LootAwardStatus.ACTIVE) { 
            "Can only revoke active loot awards" 
        }
        return copy(status = LootAwardStatus.REVOKED)
    }
    
    fun isActive(): Boolean = status == LootAwardStatus.ACTIVE
    
    companion object {
        fun create(
            itemId: ItemId,
            raiderId: RaiderId,
            guildId: GuildId,
            flpsScore: FlpsScore,
            tier: LootTier
        ): LootAward = LootAward(
            id = LootAwardId.generate(),
            itemId = itemId,
            raiderId = raiderId,
            guildId = guildId,
            awardedAt = Instant.now(),
            flpsScore = flpsScore,
            tier = tier
        )
    }
}
```

**Guidelines:**
- Always have an ID
- Use `data class` for automatic equals/hashCode based on ID
- Provide factory methods in companion object
- Encapsulate state changes in methods
- Use `copy()` for immutable updates
- Validate state transitions

### Aggregate Roots

**Characteristics:**
- Entity that controls a consistency boundary
- Manages child entities
- Enforces invariants
- Entry point for operations

**Example:**
```kotlin
class Raid private constructor(
    val id: RaidId,
    val guildId: GuildId,
    val scheduledAt: Instant,
    val difficulty: RaidDifficulty,
    private val encounters: MutableList<RaidEncounter> = mutableListOf(),
    private val signups: MutableList<RaidSignup> = mutableListOf(),
    private var status: RaidStatus = RaidStatus.SCHEDULED
) {
    
    fun addEncounter(encounter: RaidEncounter): Raid {
        require(status == RaidStatus.SCHEDULED) { 
            "Cannot add encounters to non-scheduled raid" 
        }
        encounters.add(encounter)
        return this
    }
    
    fun addSignup(raider: RaiderId, role: RaidRole): Raid {
        require(status == RaidStatus.SCHEDULED) { 
            "Cannot signup for non-scheduled raid" 
        }
        require(signups.none { it.raiderId == raider }) { 
            "Raider already signed up" 
        }
        
        signups.add(RaidSignup.create(raider, role))
        return this
    }
    
    fun start(): Raid {
        require(status == RaidStatus.SCHEDULED) { 
            "Can only start scheduled raids" 
        }
        require(signups.size >= 10) { 
            "Need at least 10 signups to start raid" 
        }
        
        return copy(status = RaidStatus.IN_PROGRESS)
    }
    
    fun getSignups(): List<RaidSignup> = signups.toList()
    
    fun getEncounters(): List<RaidEncounter> = encounters.toList()
    
    companion object {
        fun schedule(
            guildId: GuildId,
            scheduledAt: Instant,
            difficulty: RaidDifficulty
        ): Raid = Raid(
            id = RaidId.generate(),
            guildId = guildId,
            scheduledAt = scheduledAt,
            difficulty = difficulty
        )
    }
}
```

**Guidelines:**
- Control access to child entities
- Enforce business rules and invariants
- Provide methods for all operations
- Return defensive copies of collections
- Use factory methods for creation

### Domain Services

**Characteristics:**
- Stateless
- Contain domain logic that doesn't fit in entities
- Coordinate between multiple entities
- Named after domain concepts

**Example:**
```kotlin
@Service
class FlpsCalculationService(
    private val modifierRepository: FlpsModifierRepository,
    private val behavioralScoreService: BehavioralScoreService
) {
    
    fun calculateFlpsScore(
        raider: Raider,
        item: Item,
        guildId: GuildId
    ): FlpsScore {
        val modifiers = modifierRepository.findByGuildId(guildId)
        
        // Calculate Raider Merit Score (RMS)
        val attendanceScore = calculateAttendanceScore(raider, modifiers)
        val performanceScore = calculatePerformanceScore(raider, modifiers)
        val preparationScore = calculatePreparationScore(raider, modifiers)
        val behavioralScore = behavioralScoreService.calculateScore(raider, guildId)
        
        val rms = (attendanceScore + performanceScore + preparationScore + behavioralScore) / 4.0
        
        // Calculate Item Priority Index (IPI)
        val upgradeValue = calculateUpgradeValue(raider, item)
        val tierImpact = calculateTierImpact(item)
        val roleMultiplier = calculateRoleMultiplier(raider, item)
        
        val ipi = (upgradeValue + tierImpact) * roleMultiplier
        
        // Calculate Recency Decay Factor (RDF)
        val rdf = calculateRecencyDecay(raider, guildId)
        
        // Final FLPS = (RMS × IPI) × RDF
        return FlpsScore.of((rms * ipi * rdf).coerceIn(0.0, 1.0))
    }
    
    private fun calculateAttendanceScore(raider: Raider, modifiers: FlpsModifiers): Double {
        // Implementation
    }
    
    // Other private calculation methods...
}
```

**Guidelines:**
- Inject dependencies via constructor
- Keep methods focused and single-purpose
- Use private methods for internal logic
- Return domain objects, not primitives
- Document complex algorithms

### Repository Interfaces

**Characteristics:**
- Define in domain layer
- Implement in infrastructure layer
- Collection-like interface
- Domain-focused methods

**Example:**
```kotlin
// Domain layer - interface only
interface LootAwardRepository {
    fun findById(id: LootAwardId): LootAward?
    fun findByRaiderId(raiderId: RaiderId): List<LootAward>
    fun findByGuildId(guildId: GuildId, limit: Int = 100): List<LootAward>
    fun save(lootAward: LootAward): LootAward
    fun delete(id: LootAwardId)
}

// Infrastructure layer - Spring Data repository
@Repository
interface SpringLootAwardRepository : CrudRepository<LootAwardEntity, Long> {
    fun findByRaiderId(raiderId: Long): List<LootAwardEntity>
    fun findByGuildId(guildId: String): List<LootAwardEntity>
}

// Infrastructure layer - adapter implementation
@Repository
class JdbcLootAwardRepository(
    private val springRepository: SpringLootAwardRepository,
    private val mapper: LootAwardMapper
) : LootAwardRepository {
    
    override fun findById(id: LootAwardId): LootAward? {
        return springRepository.findById(id.value)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun save(lootAward: LootAward): LootAward {
        val entity = mapper.toEntity(lootAward)
        springRepository.save(entity)
        return lootAward
    }
    
    // Other implementations...
}
```

**Guidelines:**
- Interface in domain, implementation in infrastructure
- Use domain types in signatures
- Provide collection-like methods
- Keep queries domain-focused
- Use mappers to convert between domain and persistence
- ALWAYS use Spring Data repositories (see Database Access section below)

### Use Cases (Application Layer)

**Characteristics:**
- Orchestrate domain logic
- Handle transactions
- Coordinate repositories and services
- Return Result types

**Example:**
```kotlin
@Service
class AwardLootUseCase(
    private val lootAwardRepository: LootAwardRepository,
    private val raiderRepository: RaiderRepository,
    private val flpsCalculationService: FlpsCalculationService,
    private val lootBanRepository: LootBanRepository,
    private val eventPublisher: DomainEventPublisher
) {
    
    @Transactional
    fun execute(command: AwardLootCommand): Result<LootAward> = runCatching {
        // Validate inputs
        val raider = raiderRepository.findById(command.raiderId)
            ?: throw RaiderNotFoundException(command.raiderId)
        
        val item = itemRepository.findById(command.itemId)
            ?: throw ItemNotFoundException(command.itemId)
        
        // Check for loot bans
        val activeBans = lootBanRepository.findActiveByRaiderId(command.raiderId)
        if (activeBans.isNotEmpty()) {
            throw LootBanActiveException(raider, activeBans)
        }
        
        // Calculate FLPS score
        val flpsScore = flpsCalculationService.calculateFlpsScore(
            raider = raider,
            item = item,
            guildId = command.guildId
        )
        
        // Create loot award
        val lootAward = LootAward.create(
            itemId = command.itemId,
            raiderId = command.raiderId,
            guildId = command.guildId,
            flpsScore = flpsScore,
            tier = item.tier
        )
        
        // Persist
        val saved = lootAwardRepository.save(lootAward)
        
        // Publish domain event
        eventPublisher.publish(LootAwardedEvent(saved))
        
        saved
    }
}

data class AwardLootCommand(
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId
)
```

**Guidelines:**
- One use case per business operation
- Use `@Transactional` for database operations
- Return `Result<T>` for error handling
- Validate inputs early
- Publish domain events
- Keep orchestration logic simple

## Database Access

### Required: Spring Data Repositories Only

All database operations MUST use Spring Data repositories. Raw JDBC operations are PROHIBITED.

**Rationale:**
- Consistency: All 10 repositories in the codebase follow this pattern
- Maintainability: ~70% reduction in code (from ~310 to ~80 lines per repository)
- Automatic query generation and optimization
- Type-safe query methods
- Better transaction management
- Easier testing with `@DataJdbcTest`

### Correct Pattern

```kotlin
// ✅ CORRECT: Spring Data repository
@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long> {
    // Derived query methods (Spring Data generates SQL)
    fun findByDate(date: LocalDate): List<RaidEntity>
    fun findByGuildId(guildId: String): List<RaidEntity>
    
    // Custom query with @Query annotation
    @Query("SELECT r FROM RaidEntity r WHERE r.date BETWEEN :start AND :end")
    fun findByDateRange(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<RaidEntity>
}

// ✅ CORRECT: Adapter wrapping Spring Data repository
@Repository
class JdbcRaidRepository(
    private val springRaidRepository: RaidRepository,
    private val encounterRepository: RaidEncounterRepository,
    private val mapper: RaidMapper
) : com.edgerush.datasync.domain.raids.repository.RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val entity = springRaidRepository.findById(id.value).orElse(null) ?: return null
        val encounters = encounterRepository.findByRaidId(id.value)
        return mapper.toDomain(entity, encounters)
    }
    
    @Transactional
    override fun save(raid: Raid): Raid {
        val entity = mapper.toEntity(raid)
        springRaidRepository.save(entity)
        
        // Handle child entities
        encounterRepository.deleteByRaidId(entity.raidId)
        encounterRepository.saveAll(mapper.toEncounterEntities(raid))
        
        return raid
    }
}
```

### Incorrect Pattern

```kotlin
// ❌ INCORRECT: Raw JDBC with JdbcTemplate
@Repository
class JdbcRaidRepository(
    private val jdbcTemplate: JdbcTemplate  // ❌ Don't use JdbcTemplate
) : RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        // ❌ Manual SQL strings are prohibited
        val sql = "SELECT * FROM raids WHERE raid_id = ?"
        return jdbcTemplate.query(sql, raidRowMapper, id.value).firstOrNull()
    }
    
    // ❌ Manual RowMappers are prohibited
    private val raidRowMapper = RowMapper<RaidEntity> { rs, _ ->
        RaidEntity(
            raidId = rs.getLong("raid_id"),
            date = rs.getDate("date").toLocalDate()
        )
    }
}
```

### Query Method Patterns

**Derived Query Methods:**
```kotlin
// Spring Data generates SQL from method name
fun findByGuildId(guildId: String): List<RaidEntity>
fun findByDateBetween(start: LocalDate, end: LocalDate): List<RaidEntity>
fun findByGuildIdAndDate(guildId: String, date: LocalDate): List<RaidEntity>
fun deleteByRaidId(raidId: Long)
```

**Custom JPQL Queries:**
```kotlin
@Query("SELECT r FROM RaidEntity r WHERE r.guildId = :guildId ORDER BY r.date DESC")
fun findRecentByGuildId(@Param("guildId") guildId: String): List<RaidEntity>
```

**Native SQL Queries (use sparingly):**
```kotlin
@Query(
    value = "SELECT * FROM raids WHERE guild_id = :guildId LIMIT :limit",
    nativeQuery = true
)
fun findLatestByGuildId(
    @Param("guildId") guildId: String,
    @Param("limit") limit: Int
): List<RaidEntity>
```

### Exception: Database Migrations

Raw SQL is ONLY allowed in Flyway migration scripts:

```sql
-- ✅ CORRECT: Flyway migration (V0001__create_raids_table.sql)
CREATE TABLE raids (
    raid_id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Benefits

**Code Reduction:**
- Before: ~310 lines with manual SQL and RowMappers
- After: ~80 lines with Spring Data
- Reduction: 74% less code to maintain

**Improved Quality:**
- No manual SQL string maintenance
- No manual parameter binding
- Type-safe query methods
- Automatic transaction management
- Better IDE support and refactoring

## Package Organization

### Structure

```
com.edgerush.lootman/
├── api/                          # API Layer
│   ├── rest/
│   │   └── v1/
│   │       ├── flps/            # FLPS endpoints
│   │       ├── loot/            # Loot endpoints
│   │       ├── attendance/      # Attendance endpoints
│   │       └── raids/           # Raid endpoints
│   └── dto/                     # Data Transfer Objects
│       ├── request/
│       └── response/
│
├── application/                 # Application Layer (Use Cases)
│   ├── flps/                   # FLPS use cases
│   ├── loot/                   # Loot use cases
│   ├── attendance/             # Attendance use cases
│   └── raids/                  # Raid use cases
│
├── domain/                     # Domain Layer
│   ├── flps/                  # FLPS bounded context
│   │   ├── model/             # Domain models
│   │   ├── service/           # Domain services
│   │   └── repository/        # Repository interfaces
│   ├── loot/                  # Loot bounded context
│   │   ├── model/
│   │   ├── service/
│   │   └── repository/
│   ├── attendance/            # Attendance bounded context
│   │   ├── model/
│   │   ├── service/
│   │   └── repository/
│   └── raids/                 # Raids bounded context
│       ├── model/
│       ├── service/
│       └── repository/
│
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/          # Database implementations
│   │   ├── entity/           # JPA/JDBC entities
│   │   ├── repository/       # Repository implementations
│   │   └── mapper/           # Entity ↔ Domain mappers
│   ├── external/             # External API clients
│   │   ├── wowaudit/
│   │   ├── warcraftlogs/
│   │   └── raidbots/
│   └── config/               # Configuration
│
└── shared/                   # Shared utilities
    ├── exception/            # Custom exceptions
    ├── validation/           # Validation utilities
    └── util/                # Common utilities
```

### Package Rules

**Domain Layer:**
- No dependencies on other layers
- Pure business logic
- Framework-agnostic
- Contains interfaces only (no implementations)

**Application Layer:**
- Depends on domain layer
- Orchestrates use cases
- Handles transactions
- Minimal business logic

**Infrastructure Layer:**
- Depends on domain and application layers
- Implements interfaces from domain
- Contains framework-specific code
- Database, external APIs, configuration

**API Layer:**
- Depends on application layer
- HTTP/REST concerns only
- DTOs for request/response
- No business logic

## Error Handling

### Domain Exceptions

```kotlin
// Base domain exception
sealed class DomainException(message: String, cause: Throwable? = null) : 
    Exception(message, cause)

// Specific domain exceptions
class RaiderNotFoundException(val raiderId: RaiderId) : 
    DomainException("Raider not found: ${raiderId.value}")

class LootBanActiveException(val raider: Raider, val bans: List<LootBan>) : 
    DomainException("Raider ${raider.name} has active loot bans")

class InvalidFlpsScoreException(val score: Double) : 
    DomainException("Invalid FLPS score: $score. Must be between 0.0 and 1.0")
```

### Result Type Pattern

```kotlin
// Use Result<T> for operations that can fail
fun calculateScore(): Result<FlpsScore> = runCatching {
    // Logic that might throw
    FlpsScore.of(value)
}

// Handle results
when (val result = calculateScore()) {
    is Success -> handleSuccess(result.value)
    is Failure -> handleFailure(result.exception)
}
```

### API Exception Handling

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is RaiderNotFoundException -> HttpStatus.NOT_FOUND
            is LootBanActiveException -> HttpStatus.FORBIDDEN
            is InvalidFlpsScoreException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = Instant.now(),
                status = status.value(),
                error = status.reasonPhrase,
                message = ex.message ?: "An error occurred",
                path = getCurrentRequestPath()
            )
        )
    }
}
```

## Documentation

### KDoc Comments

```kotlin
/**
 * Calculates the Final Loot Priority Score (FLPS) for a raider and item.
 *
 * The FLPS algorithm combines three components:
 * - Raider Merit Score (RMS): attendance, performance, preparation, behavior
 * - Item Priority Index (IPI): upgrade value, tier impact, role multiplier
 * - Recency Decay Factor (RDF): reduces score for recent loot recipients
 *
 * Formula: FLPS = (RMS × IPI) × RDF
 *
 * @param raider The raider requesting the item
 * @param item The item being distributed
 * @param guildId The guild context for modifiers
 * @return FlpsScore between 0.0 and 1.0
 * @throws RaiderNotFoundException if raider doesn't exist
 */
fun calculateFlpsScore(
    raider: Raider,
    item: Item,
    guildId: GuildId
): FlpsScore
```

### Package Documentation

```kotlin
/**
 * FLPS (Final Loot Priority Score) bounded context.
 *
 * This package contains the domain model and business logic for calculating
 * loot priority scores based on raider merit, item priority, and recency decay.
 *
 * Key components:
 * - [FlpsScore]: Value object representing the final score
 * - [FlpsCalculationService]: Domain service for score calculation
 * - [FlpsModifierRepository]: Repository for guild-specific modifiers
 */
package com.edgerush.lootman.domain.flps
```

### README Files

Each major package should have a README.md explaining:
- Purpose of the package
- Key components
- Usage examples
- Design decisions

## Code Quality Tools

### ktlint Configuration

```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3"
}

ktlint {
    version.set("1.0.1")
    android.set(false)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(false)
    
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
```

### detekt Configuration

```yaml
# detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false

complexity:
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 6
  ComplexMethod:
    threshold: 15

naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
  ClassNaming:
    classPattern: '[A-Z][a-zA-Z0-9]*'

style:
  MaxLineLength:
    maxLineLength: 120
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2']
```

## Best Practices

### DO

✅ Use immutable data structures
✅ Prefer composition over inheritance
✅ Write small, focused functions
✅ Use meaningful variable names
✅ Validate inputs early
✅ Use Kotlin's null safety features
✅ Leverage extension functions
✅ Use sealed classes for state
✅ Document public APIs
✅ Follow SOLID principles

### DON'T

❌ Use mutable state unnecessarily
❌ Create god classes
❌ Use magic numbers
❌ Ignore compiler warnings
❌ Use `!!` (non-null assertion)
❌ Catch generic exceptions
❌ Use `Any` type
❌ Create circular dependencies
❌ Mix layers (domain calling infrastructure)
❌ Put business logic in controllers

## Code Review Checklist

- [ ] Follows Kotlin conventions
- [ ] Follows DDD patterns
- [ ] Proper package organization
- [ ] Comprehensive tests (85% coverage)
- [ ] No ktlint violations
- [ ] No detekt violations
- [ ] Meaningful names
- [ ] Proper error handling
- [ ] Documentation for public APIs
- [ ] No code duplication

## Resources

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Domain-Driven Design](https://www.amazon.com/Domain-Driven-Design-Tackling-Complexity-Software/dp/0321125215)
- [Clean Architecture](https://www.amazon.com/Clean-Architecture-Craftsmans-Software-Structure/dp/0134494164)
- [Effective Kotlin](https://kt.academy/book/effectivekotlin)

## Questions?

If you have questions about code standards, ask in the team channel or review existing code in the `lootman` package for examples.
