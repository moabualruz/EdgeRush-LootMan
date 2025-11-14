# AI Agent Development Standards

## 📝 Coding Standards & Conventions

### Kotlin Code Style

#### File Headers
Every Kotlin file should start with:
```kotlin
/**
 * EdgeRush LootMan - [Component Name]
 * 
 * Purpose: [Brief description of the file's responsibility]
 * 
 * Key Dependencies:
 * - [List major dependencies and their purpose]
 * 
 * Important Notes:
 * - [Any critical information for developers]
 * 
 * @since [Version when introduced]
 * @author AI Agent / EdgeRush Team
 */
package com.edgerush.datasync.[package]
```

#### Class Documentation
```kotlin
/**
 * Calculates Final Loot Priority Score (FLPS) for guild loot distribution.
 * 
 * The FLPS algorithm combines three components:
 * - Raider Merit Score (RMS): Player performance and commitment
 * - Item Priority Index (IPI): Upgrade value and role requirements  
 * - Recency Decay Factor (RDF): Prevents loot hoarding
 * 
 * Formula: FLPS = (RMS × IPI) × RDF
 * 
 * @see [docs/score-model.md] for detailed algorithm specification
 * @see [ScoreCalculatorTest] for usage examples and edge cases
 */
@Service
class ScoreCalculator {
    // Implementation
}
```

#### Method Documentation
```kotlin
/**
 * Calculates Item Priority Index based on upgrade value and role requirements.
 * 
 * @param character Target character receiving the item
 * @param item Item being evaluated for distribution
 * @param tierSetStatus Current tier set completion status
 * @return IPI score between 0.0 and 1.0, where 1.0 represents maximum priority
 * 
 * @throws ValidationException if character or item data is invalid
 * @throws SimulationException if gear simulation fails
 */
suspend fun calculateIPI(
    character: Character,
    item: Item,
    tierSetStatus: TierSetStatus
): Double {
    // Implementation
}
```

### Error Handling Standards

#### Exception Hierarchy
```kotlin
// Base exception for all sync-related errors
sealed class SyncException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)

// Specific exception types
class ApiConnectionException(message: String, cause: Throwable? = null) : 
    SyncException("API Connection failed: $message", cause)

class DataValidationException(field: String, value: Any?, message: String) : 
    SyncException("Validation failed for $field='$value': $message")

class ScoreCalculationException(character: String, reason: String) : 
    SyncException("Score calculation failed for $character: $reason")
```

#### Error Handling Patterns
```kotlin
// Use Result type for operations that can fail
suspend fun fetchCharacterData(name: String): Result<Character> = 
    runCatching {
        wowAuditClient.getCharacter(name)
            .validate()
            .toDomainModel()
    }.onFailure { error ->
        logger.error("Failed to fetch character data for $name", error)
        metrics.incrementCounter("character_fetch_errors")
    }

// Use circuit breaker for external API calls
@CircuitBreaker(name = "wowaudit-api")
@Retryable(value = [ConnectException::class], maxAttempts = 3)
suspend fun callExternalApi(): ApiResponse {
    // Implementation with proper timeout and error handling
}
```

### Database Entity Standards

#### Entity Definition
```kotlin
/**
 * Represents a guild member with their current progression status.
 * 
 * This entity stores both static information (name, class) and dynamic
 * data (item level, spec) that changes as the character progresses.
 * 
 * @property id Database primary key
 * @property name Character name (unique within realm)
 * @property realm Server/realm name
 * @property lastUpdated When this data was last synced from WoWAudit
 */
@Entity
@Table(name = "characters", uniqueConstraints = [
    UniqueConstraint(columnNames = ["name", "realm"])
])
data class Character(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(nullable = false, length = 100)  
    val realm: String,
    
    @Column(name = "character_class", nullable = false)
    @Enumerated(EnumType.STRING)
    val characterClass: CharacterClass,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val spec: CharacterSpec,
    
    @Column(name = "item_level", nullable = false)
    val itemLevel: Int,
    
    @Column(name = "last_updated", nullable = false)
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    
    @Version
    val version: Long = 0
) {
    /**
     * Determines the character's primary role in raid encounters.
     */
    val role: Role get() = spec.role
    
    /**
     * Validates that character data is consistent and complete.
     */
    fun validate(): ValidationResult {
        // Implementation
    }
}
```

### Testing Standards

#### Unit Test Structure
```kotlin
/**
 * Tests for ScoreCalculator FLPS algorithm implementation.
 * 
 * These tests verify the core business logic using deterministic mock data
 * to ensure consistent and fair loot distribution calculations.
 */
@ExtendWith(MockitoExtension::class)
class ScoreCalculatorTest {
    
    @Mock
    private lateinit var attendanceService: AttendanceService
    
    @Mock  
    private lateinit var simulationService: SimulationService
    
    @InjectMocks
    private lateinit var scoreCalculator: ScoreCalculator
    
    /**
     * Tests FLPS calculation with perfect attendance and high-value upgrade.
     * Expected: High FLPS score reflecting both commitment and gear improvement.
     */
    @Test
    fun `calculateFLPS returns high score for committed player with major upgrade`() {
        // Given
        val character = createMockCharacter(
            name = "TestPlayer",
            attendanceRate = 1.0,
            mechanicalScore = 0.95
        )
        val item = createMockItem(
            upgradeValue = 50.0, // Significant DPS increase
            isTierPiece = true
        )
        
        // When
        val result = scoreCalculator.calculateFLPS(character, item)
        
        // Then
        assertThat(result.totalScore).isGreaterThan(0.8)
        assertThat(result.components.rms).isEqualTo(0.95)
        assertThat(result.reasoning).contains("tier set completion")
    }
    
    /**
     * Tests RDF penalty application for recent loot recipients.
     * Expected: Score reduction proportional to loot tier and recency.
     */
    @Test
    fun `calculateFLPS applies RDF penalty for recent A-tier loot`() {
        // Implementation with clear assertions
    }
}
```

#### Integration Test Patterns
```kotlin
/**
 * Integration tests for WoWAudit client API interaction.
 * Uses WireMock to simulate external API responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "wowaudit.base-url=http://localhost:\${wiremock.server.port}"
])
class WoWAuditClientIntegrationTest {
    
    @RegisterExtension
    val wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(0))
        .build()
    
    @Test
    fun `fetchCharacters handles rate limiting gracefully`() {
        // Setup mock responses for rate limiting scenario
        // Verify retry behavior and eventual success
    }
}
```

### Database Access Pattern

#### Required: Spring Data Repositories

All database operations MUST use Spring Data repositories. Raw JDBC operations (JdbcTemplate, NamedParameterJdbcTemplate) are PROHIBITED except for database migrations.

**Rationale:**
- Consistency across codebase (all 10 repositories follow this pattern)
- Reduced boilerplate and maintenance overhead (~70% code reduction)
- Automatic query generation and optimization
- Better transaction management
- Type-safe query methods
- Easier testing with Spring Data test support

#### Correct Pattern: Spring Data Repository

```kotlin
// ✅ CORRECT: Spring Data repository interface
@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long> {
    fun findByDate(date: LocalDate): List<RaidEntity>
    fun findByGuildId(guildId: String): List<RaidEntity>
}

// ✅ CORRECT: Adapter wrapping Spring Data repository
@Repository
class JdbcRaidRepository(
    private val springRaidRepository: RaidRepository,
    private val encounterRepository: RaidEncounterRepository,
    private val signupRepository: RaidSignupRepository,
    private val mapper: RaidMapper
) : com.edgerush.datasync.domain.raids.repository.RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val entity = springRaidRepository.findById(id.value).orElse(null) ?: return null
        val encounters = encounterRepository.findByRaidId(id.value)
        val signups = signupRepository.findByRaidId(id.value)
        return mapper.toDomain(entity, encounters, signups)
    }
    
    @Transactional
    override fun save(raid: Raid): Raid {
        val entity = mapper.toEntity(raid)
        springRaidRepository.save(entity)
        
        // Delete and re-insert child entities
        encounterRepository.deleteByRaidId(entity.raidId)
        signupRepository.deleteByRaidId(entity.raidId)
        
        val encounterEntities = mapper.encounterMapper.toEntities(raid.getEncounters(), entity.raidId)
        val signupEntities = mapper.signupMapper.toEntities(raid.getSignups(), entity.raidId)
        
        encounterRepository.saveAll(encounterEntities)
        signupRepository.saveAll(signupEntities)
        
        return raid
    }
}
```

#### Incorrect Pattern: Raw JDBC

```kotlin
// ❌ INCORRECT: Raw JDBC with JdbcTemplate
@Repository
class JdbcRaidRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: RaidMapper
) : RaidRepository {
    
    override fun findById(id: RaidId): Raid? {
        val sql = "SELECT * FROM raids WHERE raid_id = ?"
        return jdbcTemplate.query(sql, raidEntityRowMapper, id.value)
            .firstOrNull()
            ?.let { entity ->
                val encounters = loadEncounters(id.value)
                val signups = loadSignups(id.value)
                mapper.toDomain(entity, encounters, signups)
            }
    }
    
    private val raidEntityRowMapper = RowMapper<RaidEntity> { rs, _ ->
        RaidEntity(
            raidId = rs.getLong("raid_id"),
            date = rs.getDate("date").toLocalDate(),
            // ... manual mapping
        )
    }
    
    // ❌ Manual SQL strings and RowMappers are prohibited
}
```

#### Custom Query Methods

For complex queries, use Spring Data query methods:

```kotlin
@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long> {
    
    // Derived query method (Spring Data generates SQL)
    fun findByDateBetween(start: LocalDate, end: LocalDate): List<RaidEntity>
    
    // Custom JPQL query
    @Query("SELECT r FROM RaidEntity r WHERE r.guildId = :guildId AND r.date >= :since")
    fun findRecentByGuildId(
        @Param("guildId") guildId: String,
        @Param("since") since: LocalDate
    ): List<RaidEntity>
    
    // Native SQL query (use sparingly)
    @Query(
        value = "SELECT * FROM raids WHERE guild_id = :guildId ORDER BY date DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findLatestByGuildId(
        @Param("guildId") guildId: String,
        @Param("limit") limit: Int
    ): List<RaidEntity>
}
```

#### Exception: Database Migrations

Raw SQL is ONLY allowed in Flyway migration scripts:

```sql
-- ✅ CORRECT: Flyway migration (V0001__create_raids_table.sql)
CREATE TABLE raids (
    raid_id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_raids_guild_id ON raids(guild_id);
CREATE INDEX idx_raids_date ON raids(date);
```

#### Benefits of Spring Data Pattern

**Code Reduction:**
- Before (Raw JDBC): ~310 lines with manual SQL and RowMappers
- After (Spring Data): ~80 lines with automatic query generation
- Reduction: ~74% less code to maintain

**Improved Maintainability:**
- No manual SQL string maintenance
- No manual RowMapper implementations
- Automatic parameter binding
- Type-safe query methods
- Better IDE support and refactoring

**Better Testing:**
- Use `@DataJdbcTest` for repository tests
- Automatic test database configuration
- Transaction rollback between tests
- No need to mock JDBC infrastructure

### Configuration Management

#### Properties Configuration
```kotlin
/**
 * Configuration properties for sync behavior and external API integration.
 * 
 * All properties are validated at startup to fail fast if misconfigured.
 * Environment variables take precedence over application.yaml values.
 */
@ConfigurationProperties(prefix = "sync")
@ConstructorBinding
@Validated
data class SyncProperties(
    /**
     * Cron expression for scheduled sync jobs.
     * Default: "0 0 4 * * *" (daily at 4 AM)
     */
    @field:NotBlank
    val cron: String = "0 0 4 * * *",
    
    /**
     * Whether to run sync on application startup.
     * Useful for development and testing environments.
     */
    val runOnStartup: Boolean = false,
    
    /**
     * WoWAudit API configuration
     */
    val wowaudit: WoWAuditConfig,
    
    /**
     * Timeout configuration for external API calls
     */
    val timeouts: TimeoutConfig = TimeoutConfig()
) {
    @Validated
    data class WoWAuditConfig(
        @field:NotBlank
        @field:URL
        val baseUrl: String,
        
        @field:NotBlank  
        val guildProfileUri: String,
        
        @field:NotBlank
        val apiKey: String
    )
    
    data class TimeoutConfig(
        val connectTimeout: Duration = Duration.ofSeconds(10),
        val readTimeout: Duration = Duration.ofSeconds(30),
        val writeTimeout: Duration = Duration.ofSeconds(10)
    )
}
```

### API Design Standards

#### REST Controller Pattern
```kotlin
/**
 * Health and monitoring endpoints for EdgeRush LootMan.
 * 
 * These endpoints provide operational visibility without exposing
 * sensitive guild data or business logic details.
 */
@RestController
@RequestMapping("/api/v1/health")
@Validated
class HealthController(
    private val syncService: WoWAuditSyncService,
    private val scoreCalculator: ScoreCalculator
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * Provides detailed system health including external dependencies.
     * 
     * @return Health status with component details
     */
    @GetMapping("/detailed")
    suspend fun getDetailedHealth(): ResponseEntity<HealthStatus> {
        return try {
            val status = HealthStatus(
                overall = determineOverallHealth(),
                components = listOf(
                    checkDatabaseHealth(),
                    checkWoWAuditApiHealth(),
                    checkLastSyncStatus()
                ),
                timestamp = Instant.now()
            )
            ResponseEntity.ok(status)
        } catch (exception: Exception) {
            logger.error(exception) { "Health check failed" }
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(HealthStatus.unavailable(exception.message))
        }
    }
    
    private suspend fun checkWoWAuditApiHealth(): ComponentHealth {
        // Implementation with timeout and error handling
    }
}
```

### Performance Standards

#### Database Query Optimization
```kotlin
/**
 * Repository for character data with optimized queries for common operations.
 * 
 * All queries are designed to minimize database load and support pagination
 * for large guild rosters.
 */
@Repository
interface CharacterRepository : JpaRepository<Character, Long> {
    
    /**
     * Finds active characters with their latest loot history.
     * Uses JOIN FETCH to avoid N+1 query problems.
     */
    @Query("""
        SELECT DISTINCT c FROM Character c 
        LEFT JOIN FETCH c.lootHistory lh 
        WHERE c.lastUpdated > :since 
        AND c.status = 'ACTIVE'
        ORDER BY c.name
    """)
    fun findActiveCharactersWithRecentLoot(
        @Param("since") since: LocalDateTime,
        pageable: Pageable
    ): Page<Character>
    
    /**
     * Bulk update operation for sync efficiency.
     * Updates multiple characters in a single transaction.
     */
    @Modifying
    @Query("""
        UPDATE Character c SET 
        c.itemLevel = :itemLevel,
        c.lastUpdated = :timestamp
        WHERE c.name = :name AND c.realm = :realm
    """)
    fun updateCharacterProgress(
        @Param("name") name: String,
        @Param("realm") realm: String,
        @Param("itemLevel") itemLevel: Int,
        @Param("timestamp") timestamp: LocalDateTime
    ): Int
}
```

#### Async Processing
```kotlin
/**
 * Async processing for CPU-intensive score calculations.
 * Uses coroutines for concurrent processing while respecting resource limits.
 */
@Service
class AsyncScoreService(
    private val scoreCalculator: ScoreCalculator
) {
    
    /**
     * Calculates FLPS for multiple characters concurrently.
     * Limits concurrency to prevent resource exhaustion.
     */
    @Async("scoreCalculationExecutor")
    suspend fun calculateScoresForRoster(
        characters: List<Character>,
        item: Item
    ): List<FLPSResult> = coroutineScope {
        characters.chunked(10) // Process in batches of 10
            .map { batch ->
                async {
                    batch.map { character ->
                        scoreCalculator.calculateFLPS(character, item)
                    }
                }
            }
            .flatMap { it.await() }
    }
}
```

---

## 🎯 AI Agent Checklist

### Before Making Changes
- [ ] Read relevant documentation in `/docs`
- [ ] Understand the business impact of changes
- [ ] Check existing tests for similar patterns
- [ ] Verify environment variables are properly configured

### Code Quality Checklist
- [ ] Added comprehensive KDoc documentation
- [ ] Included error handling and validation
- [ ] Added unit tests with >80% coverage
- [ ] Followed Kotlin coding conventions
- [ ] Used appropriate Spring Boot patterns
- [ ] Optimized database queries where applicable

### Testing Checklist
- [ ] Unit tests cover happy path and edge cases
- [ ] Integration tests verify external API integration
- [ ] Mock external dependencies appropriately
- [ ] Tests are deterministic and repeatable
- [ ] Performance tests for critical algorithms

### Documentation Checklist
- [ ] Updated relevant documentation in `/docs`
- [ ] Added inline code documentation
- [ ] Updated `PROJECT_PROGRESS.md` if applicable
- [ ] Included usage examples where helpful

### Deployment Checklist
- [ ] Changes work in Docker environment
- [ ] Database migrations are backwards compatible
- [ ] Configuration changes are documented
- [ ] Health checks continue to pass
- [ ] No breaking changes to existing APIs

Remember: This system impacts real guild relationships. Prioritize correctness, transparency, and reliability in all implementations.