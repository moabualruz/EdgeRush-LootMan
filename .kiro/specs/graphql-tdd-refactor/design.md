# Design Document

## Overview

This design document outlines the comprehensive refactoring of the EdgeRush LootMan codebase to establish test-driven development (TDD) standards and reorganize the project structure using domain-driven design (DDD) principles. The refactoring will be executed in phases to maintain system stability while improving code quality, testability, and maintainability.

## Architecture

### High-Level Architecture

The refactored architecture follows domain-driven design with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     API Layer                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ REST         │  │ GraphQL      │  │ WebSocket    │     │
│  │ Controllers  │  │ Resolvers    │  │ Handlers     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Application Layer (Use Cases)                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Bounded Context Services                            │  │
│  │  - FLPS Calculation Service                          │  │
│  │  - Loot Distribution Service                         │  │
│  │  - Attendance Tracking Service                       │  │
│  │  - Raid Management Service                           │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   Domain Layer                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Domain Models & Business Logic                      │  │
│  │  - Entities, Value Objects, Aggregates               │  │
│  │  - Domain Services                                    │  │
│  │  - Domain Events                                      │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              Infrastructure Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repositories, External APIs, Database               │  │
│  │  - Spring Data Repositories                          │  │
│  │  - WoWAudit Client, Warcraft Logs Client            │  │
│  │  - PostgreSQL Database                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Target Package Structure


```
com.edgerush.lootman/
├── api/                                    # API Layer (REST, GraphQL)
│   ├── rest/
│   │   ├── v1/
│   │   │   ├── flps/                      # FLPS endpoints
│   │   │   ├── loot/                      # Loot endpoints
│   │   │   ├── attendance/                # Attendance endpoints
│   │   │   └── raids/                     # Raid endpoints
│   │   └── common/                        # Shared REST components
│   ├── graphql/                           # (Phase 2)
│   │   ├── resolvers/
│   │   ├── schema/
│   │   └── dataloaders/
│   └── dto/                               # Data Transfer Objects
│       ├── request/
│       └── response/
│
├── application/                           # Application Layer (Use Cases)
│   ├── flps/                             # FLPS bounded context
│   │   ├── CalculateFlpsScoreUseCase.kt
│   │   ├── UpdateModifiersUseCase.kt
│   │   └── GetFlpsReportUseCase.kt
│   ├── loot/                             # Loot bounded context
│   │   ├── AwardLootUseCase.kt
│   │   ├── GetLootHistoryUseCase.kt
│   │   └── ManageLootBansUseCase.kt
│   ├── attendance/                       # Attendance bounded context
│   │   ├── TrackAttendanceUseCase.kt
│   │   └── GetAttendanceReportUseCase.kt
│   ├── raids/                            # Raids bounded context
│   │   ├── ScheduleRaidUseCase.kt
│   │   ├── ManageSignupsUseCase.kt
│   │   └── RecordRaidResultsUseCase.kt
│   └── common/                           # Shared application services
│       └── ValidationService.kt
│
├── domain/                               # Domain Layer (Business Logic)
│   ├── flps/                            # FLPS domain
│   │   ├── model/
│   │   │   ├── FlpsScore.kt            # Value Object
│   │   │   ├── RaiderMeritScore.kt     # Value Object
│   │   │   └── ItemPriorityIndex.kt    # Value Object
│   │   ├── service/
│   │   │   └── FlpsCalculationService.kt
│   │   └── repository/
│   │       └── FlpsModifierRepository.kt (interface)
│   ├── loot/                            # Loot domain
│   │   ├── model/
│   │   │   ├── LootAward.kt            # Aggregate Root
│   │   │   ├── LootBan.kt              # Entity
│   │   │   └── LootTier.kt             # Value Object
│   │   ├── service/
│   │   │   └── LootDistributionService.kt
│   │   └── repository/
│   │       ├── LootAwardRepository.kt (interface)
│   │       └── LootBanRepository.kt (interface)
│   ├── attendance/                      # Attendance domain
│   │   ├── model/
│   │   │   ├── AttendanceRecord.kt     # Entity
│   │   │   └── AttendanceStats.kt      # Value Object
│   │   ├── service/
│   │   │   └── AttendanceCalculationService.kt
│   │   └── repository/
│   │       └── AttendanceRepository.kt (interface)
│   ├── raids/                           # Raids domain
│   │   ├── model/
│   │   │   ├── Raid.kt                 # Aggregate Root
│   │   │   ├── RaidEncounter.kt        # Entity
│   │   │   └── RaidSignup.kt           # Entity
│   │   ├── service/
│   │   │   └── RaidSchedulingService.kt
│   │   └── repository/
│   │       └── RaidRepository.kt (interface)
│   └── shared/                          # Shared domain concepts
│       ├── model/
│       │   ├── Raider.kt               # Shared entity
│       │   └── Guild.kt                # Shared entity
│       └── events/
│           └── DomainEvent.kt
│
├── infrastructure/                      # Infrastructure Layer
│   ├── persistence/                    # Database implementations
│   │   ├── entity/                     # JPA/JDBC entities
│   │   │   ├── FlpsModifierEntity.kt
│   │   │   ├── LootAwardEntity.kt
│   │   │   └── RaiderEntity.kt
│   │   ├── repository/                 # Repository implementations
│   │   │   ├── JdbcFlpsModifierRepository.kt
│   │   │   ├── JdbcLootAwardRepository.kt
│   │   │   └── JdbcRaiderRepository.kt
│   │   └── mapper/                     # Entity ↔ Domain mappers
│   │       ├── FlpsModifierMapper.kt
│   │       └── LootAwardMapper.kt
│   ├── external/                       # External API clients
│   │   ├── wowaudit/
│   │   │   ├── WoWAuditClient.kt
│   │   │   └── WoWAuditMapper.kt
│   │   ├── warcraftlogs/
│   │   │   ├── WarcraftLogsClient.kt
│   │   │   └── WarcraftLogsMapper.kt
│   │   └── raidbots/
│   │       ├── RaidbotsClient.kt
│   │       └── RaidbotsMapper.kt
│   └── config/                         # Configuration
│       ├── DatabaseConfig.kt
│       ├── SecurityConfig.kt
│       └── WebClientConfig.kt
│
├── shared/                             # Shared utilities
│   ├── exception/                      # Custom exceptions
│   ├── validation/                     # Validation utilities
│   └── util/                          # Common utilities
│
└── test/                              # Test organization
    ├── unit/                          # Unit tests
    │   ├── domain/                    # Domain logic tests
    │   └── application/               # Use case tests
    ├── integration/                   # Integration tests
    │   ├── api/                       # API endpoint tests
    │   └── persistence/               # Database tests
    └── e2e/                          # End-to-end tests
        └── scenarios/                 # User scenario tests
```


## Components and Interfaces

### 1. Testing Infrastructure

#### Test Configuration

```kotlin
// build.gradle.kts
dependencies {
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    
    // Code Coverage
    jacoco
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}
```

#### Base Test Classes

```kotlin
// Unit Test Base
abstract class UnitTest {
    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}

// Integration Test Base
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTest {
    
    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:18").apply {
            withDatabaseName("lootman_test")
            withUsername("test")
            withPassword("test")
        }
    }
    
    @Autowired
    protected lateinit var restTemplate: TestRestTemplate
    
    @BeforeEach
    fun cleanDatabase() {
        // Clean database between tests
    }
}
```

### 2. TDD Standards Documentation

#### Test Naming Convention

```kotlin
// Pattern: `should_ExpectedBehavior_When_StateUnderTest`
class FlpsCalculationServiceTest : UnitTest() {
    
    @Test
    fun `should calculate correct FLPS score when all components are valid`() {
        // Given
        val raiderMerit = RaiderMeritScore(0.85)
        val itemPriority = ItemPriorityIndex(0.90)
        val recencyDecay = RecencyDecayFactor(1.0)
        
        // When
        val result = flpsService.calculate(raiderMerit, itemPriority, recencyDecay)
        
        // Then
        result.value shouldBe 0.765
    }
    
    @Test
    fun `should throw exception when raider merit score is negative`() {
        // Given
        val invalidScore = -0.1
        
        // When/Then
        shouldThrow<IllegalArgumentException> {
            RaiderMeritScore(invalidScore)
        }
    }
}
```

#### Test Organization Pattern (AAA)

```kotlin
@Test
fun `should award loot to highest FLPS scorer when multiple raiders want item`() {
    // Arrange (Given)
    val item = createTestItem(itemId = 12345)
    val raider1 = createTestRaider(flpsScore = 0.85)
    val raider2 = createTestRaider(flpsScore = 0.92)
    val raider3 = createTestRaider(flpsScore = 0.78)
    
    every { lootRepository.findById(item.id) } returns item
    every { raiderRepository.findByIds(any()) } returns listOf(raider1, raider2, raider3)
    
    // Act (When)
    val result = lootService.awardLoot(
        itemId = item.id,
        candidates = listOf(raider1.id, raider2.id, raider3.id)
    )
    
    // Assert (Then)
    result.winner shouldBe raider2
    result.flpsScore shouldBe 0.92
    verify(exactly = 1) { lootRepository.save(any()) }
}
```


### 3. Domain-Driven Design Patterns

#### Value Objects

```kotlin
// Immutable value object with validation
data class FlpsScore private constructor(val value: Double) {
    
    init {
        require(value in 0.0..1.0) { "FLPS score must be between 0.0 and 1.0" }
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

#### Entities

```kotlin
// Entity with identity and lifecycle
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
        require(status == LootAwardStatus.ACTIVE) { "Can only revoke active loot awards" }
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

#### Aggregate Roots

```kotlin
// Aggregate root managing consistency boundary
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
        require(status == RaidStatus.SCHEDULED) { "Cannot add encounters to non-scheduled raid" }
        encounters.add(encounter)
        return this
    }
    
    fun addSignup(raider: RaiderId, role: RaidRole): Raid {
        require(status == RaidStatus.SCHEDULED) { "Cannot signup for non-scheduled raid" }
        require(signups.none { it.raiderId == raider }) { "Raider already signed up" }
        
        signups.add(RaidSignup.create(raider, role))
        return this
    }
    
    fun start(): Raid {
        require(status == RaidStatus.SCHEDULED) { "Can only start scheduled raids" }
        require(signups.size >= 10) { "Need at least 10 signups to start raid" }
        
        return copy(status = RaidStatus.IN_PROGRESS)
    }
    
    fun complete(): Raid {
        require(status == RaidStatus.IN_PROGRESS) { "Can only complete in-progress raids" }
        return copy(status = RaidStatus.COMPLETED)
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

#### Domain Services

```kotlin
// Domain service for complex business logic
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
    
    // Other calculation methods...
}
```


### 4. Repository Pattern

#### Domain Repository Interface

```kotlin
// Domain layer - interface only
interface LootAwardRepository {
    fun findById(id: LootAwardId): LootAward?
    fun findByRaiderId(raiderId: RaiderId): List<LootAward>
    fun findByGuildId(guildId: GuildId, limit: Int = 100): List<LootAward>
    fun save(lootAward: LootAward): LootAward
    fun delete(id: LootAwardId)
}
```

#### Infrastructure Repository Implementation

```kotlin
// Infrastructure layer - implementation
@Repository
class JdbcLootAwardRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: LootAwardMapper
) : LootAwardRepository {
    
    override fun findById(id: LootAwardId): LootAward? {
        val sql = "SELECT * FROM loot_awards WHERE id = ?"
        return jdbcTemplate.query(sql, { rs, _ -> mapper.toDomain(rs) }, id.value)
            .firstOrNull()
    }
    
    override fun findByRaiderId(raiderId: RaiderId): List<LootAward> {
        val sql = "SELECT * FROM loot_awards WHERE raider_id = ? ORDER BY awarded_at DESC"
        return jdbcTemplate.query(sql, { rs, _ -> mapper.toDomain(rs) }, raiderId.value)
    }
    
    override fun save(lootAward: LootAward): LootAward {
        val entity = mapper.toEntity(lootAward)
        val sql = """
            INSERT INTO loot_awards (id, item_id, raider_id, guild_id, awarded_at, flps_score, tier, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                status = EXCLUDED.status
        """
        jdbcTemplate.update(sql, 
            entity.id, entity.itemId, entity.raiderId, entity.guildId,
            entity.awardedAt, entity.flpsScore, entity.tier, entity.status
        )
        return lootAward
    }
    
    override fun delete(id: LootAwardId) {
        jdbcTemplate.update("DELETE FROM loot_awards WHERE id = ?", id.value)
    }
}
```

### 5. Use Case Pattern

```kotlin
// Application layer - orchestrates domain logic
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

### 6. API Layer Integration

#### REST Controller

```kotlin
@RestController
@RequestMapping("/api/v1/loot")
class LootController(
    private val awardLootUseCase: AwardLootUseCase,
    private val getLootHistoryUseCase: GetLootHistoryUseCase
) {
    
    @PostMapping("/awards")
    @PreAuthorize("hasRole('GUILD_ADMIN')")
    fun awardLoot(@RequestBody request: AwardLootRequest): ResponseEntity<LootAwardResponse> {
        val command = AwardLootCommand(
            itemId = ItemId(request.itemId),
            raiderId = RaiderId(request.raiderId),
            guildId = GuildId(request.guildId)
        )
        
        return awardLootUseCase.execute(command)
            .map { lootAward -> 
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(LootAwardResponse.from(lootAward))
            }
            .getOrElse { exception ->
                throw ApiException.from(exception)
            }
    }
    
    @GetMapping("/awards/guild/{guildId}")
    fun getLootHistory(
        @PathVariable guildId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PagedResponse<LootAwardResponse> {
        val query = GetLootHistoryQuery(
            guildId = GuildId(guildId),
            page = page,
            size = size
        )
        
        return getLootHistoryUseCase.execute(query)
            .map { pagedResult ->
                PagedResponse(
                    content = pagedResult.content.map { LootAwardResponse.from(it) },
                    page = pagedResult.page,
                    size = pagedResult.size,
                    totalElements = pagedResult.totalElements
                )
            }
            .getOrThrow()
    }
}
```


### 7. Code Quality Tools Configuration

#### ktlint Configuration

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

#### detekt Configuration

```yaml
# detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false

config:
  validation: true
  warningsAsErrors: true

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
  VariableNaming:
    variablePattern: '[a-z][a-zA-Z0-9]*'

style:
  MaxLineLength:
    maxLineLength: 120
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2']
```

## Data Models

### Bounded Context Models

#### FLPS Bounded Context

```kotlin
// Domain Models
data class FlpsScore(val value: Double)
data class RaiderMeritScore(val value: Double)
data class ItemPriorityIndex(val value: Double)
data class RecencyDecayFactor(val value: Double)

data class FlpsModifiers(
    val attendanceWeight: Double,
    val performanceWeight: Double,
    val preparationWeight: Double,
    val behavioralWeight: Double,
    val tierMultipliers: Map<LootTier, Double>
)

// Database Entities
@Table("flps_guild_modifiers")
data class FlpsGuildModifierEntity(
    @Id val id: Long?,
    val guildId: String,
    val attendanceWeight: Double,
    val performanceWeight: Double,
    val preparationWeight: Double,
    val behavioralWeight: Double,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

#### Loot Bounded Context

```kotlin
// Domain Models
data class LootAward(
    val id: LootAwardId,
    val itemId: ItemId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val awardedAt: Instant,
    val flpsScore: FlpsScore,
    val tier: LootTier
)

data class LootBan(
    val id: LootBanId,
    val raiderId: RaiderId,
    val guildId: GuildId,
    val reason: String,
    val expiresAt: Instant,
    val createdBy: UserId
)

enum class LootTier {
    MYTHIC, HEROIC, NORMAL, LFR
}

// Database Entities
@Table("loot_awards")
data class LootAwardEntity(
    @Id val id: Long?,
    val itemId: Long,
    val raiderId: Long,
    val guildId: String,
    val awardedAt: Instant,
    val flpsScore: Double,
    val tier: String,
    val status: String
)
```

## Error Handling

### Domain Exceptions

```kotlin
// Base domain exception
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause)

// Specific domain exceptions
class RaiderNotFoundException(val raiderId: RaiderId) : 
    DomainException("Raider not found: ${raiderId.value}")

class LootBanActiveException(val raider: Raider, val bans: List<LootBan>) : 
    DomainException("Raider ${raider.name} has active loot bans")

class InvalidFlpsScoreException(val score: Double) : 
    DomainException("Invalid FLPS score: $score. Must be between 0.0 and 1.0")

class RaidNotScheduledException(val raidId: RaidId) : 
    DomainException("Raid ${raidId.value} is not in scheduled state")
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

## Testing Strategy

### Test Pyramid

```
                    ┌─────────────┐
                    │   E2E (5%)  │  Full user scenarios
                    └─────────────┘
                  ┌─────────────────┐
                  │ Integration(25%)│  API + DB + External
                  └─────────────────┘
              ┌───────────────────────┐
              │   Unit Tests (70%)    │  Domain + Application
              └───────────────────────┘
```

### Unit Test Examples

```kotlin
class FlpsCalculationServiceTest : UnitTest() {
    
    private lateinit var service: FlpsCalculationService
    private lateinit var modifierRepository: FlpsModifierRepository
    private lateinit var behavioralScoreService: BehavioralScoreService
    
    @BeforeEach
    fun setup() {
        modifierRepository = mockk()
        behavioralScoreService = mockk()
        service = FlpsCalculationService(modifierRepository, behavioralScoreService)
    }
    
    @Test
    fun `should calculate FLPS score correctly with all positive factors`() {
        // Given
        val raider = createTestRaider(attendance = 0.95, performance = 0.88)
        val item = createTestItem(tier = LootTier.MYTHIC)
        val guildId = GuildId("test-guild")
        val modifiers = createTestModifiers()
        
        every { modifierRepository.findByGuildId(guildId) } returns modifiers
        every { behavioralScoreService.calculateScore(raider, guildId) } returns 1.0
        
        // When
        val result = service.calculateFlpsScore(raider, item, guildId)
        
        // Then
        result.value shouldBeGreaterThan 0.0
        result.value shouldBeLessThanOrEqual 1.0
        verify(exactly = 1) { modifierRepository.findByGuildId(guildId) }
    }
}
```

### Integration Test Examples

```kotlin
@SpringBootTest
@Testcontainers
class LootControllerIntegrationTest : IntegrationTest() {
    
    @Test
    fun `should award loot and return 201 Created`() {
        // Given
        val raider = createAndSaveTestRaider()
        val item = createTestItem()
        val request = AwardLootRequest(
            itemId = item.id,
            raiderId = raider.id,
            guildId = "test-guild"
        )
        
        // When
        val response = restTemplate.postForEntity(
            "/api/v1/loot/awards",
            request,
            LootAwardResponse::class.java
        )
        
        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldNotBe null
        response.body!!.raiderId shouldBe raider.id
    }
}
```


## Migration Strategy

### Phase 1: Foundation (Weeks 1-2)

**Goal:** Establish testing infrastructure and documentation

1. **Set up testing tools**
   - Configure JUnit 5, MockK, Testcontainers
   - Configure JaCoCo with 85% coverage threshold
   - Set up ktlint and detekt

2. **Create documentation**
   - Write TDD standards guide
   - Write code standards guide
   - Create test templates and examples

3. **Create base test classes**
   - UnitTest base class
   - IntegrationTest base class
   - Test utilities and helpers

### Phase 2: FLPS Bounded Context (Weeks 3-4)

**Goal:** Refactor FLPS calculation as first bounded context

1. **Create domain layer**
   - Define value objects (FlpsScore, RaiderMeritScore, etc.)
   - Create domain services (FlpsCalculationService)
   - Write unit tests for domain logic

2. **Create application layer**
   - Implement use cases (CalculateFlpsScoreUseCase)
   - Write unit tests for use cases

3. **Create infrastructure layer**
   - Implement repositories
   - Create entity mappers
   - Write integration tests

4. **Update API layer**
   - Refactor FlpsController to use new use cases
   - Write API integration tests

### Phase 3: Loot Bounded Context (Weeks 5-6)

**Goal:** Refactor loot management as second bounded context

1. **Create domain layer**
   - Define aggregates (LootAward)
   - Define entities (LootBan)
   - Create domain services
   - Write unit tests

2. **Create application layer**
   - Implement use cases (AwardLootUseCase, ManageLootBansUseCase)
   - Write unit tests

3. **Create infrastructure layer**
   - Implement repositories
   - Create entity mappers
   - Write integration tests

4. **Update API layer**
   - Refactor loot controllers
   - Write API integration tests

### Phase 4: Attendance Bounded Context (Weeks 7-8)

**Goal:** Refactor attendance tracking

1. **Create domain layer**
   - Define entities and value objects
   - Create domain services
   - Write unit tests

2. **Create application layer**
   - Implement use cases
   - Write unit tests

3. **Create infrastructure layer**
   - Implement repositories
   - Write integration tests

4. **Update API layer**
   - Refactor attendance controllers
   - Write API integration tests

### Phase 5: Raids Bounded Context (Weeks 9-10)

**Goal:** Refactor raid management

1. **Create domain layer**
   - Define aggregate root (Raid)
   - Define entities (RaidEncounter, RaidSignup)
   - Create domain services
   - Write unit tests

2. **Create application layer**
   - Implement use cases
   - Write unit tests

3. **Create infrastructure layer**
   - Implement repositories
   - Write integration tests

4. **Update API layer**
   - Refactor raid controllers
   - Write API integration tests

### Phase 6: Remaining Contexts (Weeks 11-12)

**Goal:** Complete refactoring of remaining bounded contexts

1. **Applications bounded context**
   - Guild application management
   - Application questions and files

2. **Integrations bounded context**
   - WoWAudit integration
   - Warcraft Logs integration
   - Raidbots integration

3. **Shared context**
   - Raider management
   - Guild configuration

### Phase 7: Cleanup and Optimization (Week 13)

**Goal:** Remove old code and optimize

1. **Remove deprecated code**
   - Delete old service layer
   - Remove unused entities
   - Clean up imports

2. **Optimize performance**
   - Add database indexes
   - Optimize queries
   - Add caching where appropriate

3. **Final verification**
   - Run full test suite
   - Verify 85% coverage
   - Run performance tests
   - Update documentation

## Rollback Procedures

### Per-Phase Rollback

Each phase maintains backward compatibility, allowing rollback:

1. **Keep old code during migration**
   - Don't delete old services until new ones are tested
   - Use feature flags to switch between old/new implementations

2. **Database migrations are additive**
   - Don't drop columns until migration is complete
   - Use views to maintain old API if needed

3. **API compatibility**
   - Maintain existing REST endpoints
   - New endpoints can coexist with old ones

### Emergency Rollback

If critical issues arise:

1. **Revert to previous Git commit**
2. **Roll back database migrations** (if any were applied)
3. **Restart services**
4. **Verify system health**

## Success Criteria

### Per-Phase Criteria

- All tests pass (unit + integration)
- Code coverage ≥ 85%
- No ktlint or detekt violations
- All existing REST endpoints still functional
- Performance benchmarks met

### Overall Success Criteria

- Complete test suite with 85%+ coverage
- All bounded contexts refactored
- Documentation complete and up-to-date
- Zero regression in existing functionality
- Performance equal or better than before
- Team trained on new architecture

## Performance Considerations

### Database Query Optimization

```kotlin
// Before: N+1 query problem
fun getLootHistory(guildId: GuildId): List<LootAwardWithRaider> {
    val awards = lootAwardRepository.findByGuildId(guildId)
    return awards.map { award ->
        val raider = raiderRepository.findById(award.raiderId) // N queries!
        LootAwardWithRaider(award, raider)
    }
}

// After: Single query with join
fun getLootHistory(guildId: GuildId): List<LootAwardWithRaider> {
    return lootAwardRepository.findByGuildIdWithRaiders(guildId) // 1 query
}
```

### Caching Strategy

```kotlin
@Service
class CachedFlpsCalculationService(
    private val flpsCalculationService: FlpsCalculationService,
    private val cacheManager: CacheManager
) {
    
    @Cacheable(value = ["flps-scores"], key = "#raider.id + '-' + #item.id")
    fun calculateFlpsScore(
        raider: Raider,
        item: Item,
        guildId: GuildId
    ): FlpsScore {
        return flpsCalculationService.calculateFlpsScore(raider, item, guildId)
    }
    
    @CacheEvict(value = ["flps-scores"], allEntries = true)
    fun invalidateCache() {
        // Called when modifiers change
    }
}
```

## Documentation Structure

### Testing Standards Guide

Location: `docs/testing-standards.md`

Contents:
- TDD workflow and principles
- Test naming conventions
- Test organization patterns (AAA, Given-When-Then)
- Mocking guidelines
- Integration test setup
- Performance test guidelines
- Examples for common scenarios

### Code Standards Guide

Location: `docs/code-standards.md`

Contents:
- Kotlin coding conventions
- Package organization rules
- Naming conventions
- Error handling patterns
- Documentation requirements
- DDD patterns and practices
- Examples and anti-patterns

### Architecture Decision Records

Location: `docs/adr/`

Contents:
- ADR-001: Adoption of Domain-Driven Design
- ADR-002: Test-Driven Development Standards
- ADR-003: Bounded Context Boundaries
- ADR-004: Repository Pattern Implementation
- ADR-005: Use Case Pattern for Application Layer

