# Design Document - Warcraft Logs Integration

## Overview

This design implements Warcraft Logs API integration to provide accurate combat performance data for MAS (Mechanical Adherence Score) calculation. The design follows the existing architecture patterns established by WoWAuditClient and emphasizes configurability, resilience, and performance.

### Design Goals

1. **Configurability**: All parameters (thresholds, weights, timeframes) are configurable per guild
2. **Resilience**: Graceful degradation when Warcraft Logs API is unavailable
3. **Performance**: Async processing with caching to minimize latency impact
4. **Observability**: Comprehensive logging and metrics for monitoring
5. **Security**: Encrypted credential storage and secure API communication

## Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    FLPS Calculation Layer                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ScoreCalculator.calculateMechanicalAdherence()      │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Warcraft Logs Service Layer                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  WarcraftLogsPerformanceService                      │  │
│  │  - getMASForCharacter()                              │  │
│  │  - getPerformanceMetrics()                           │  │
│  │  - calculateSpecAverages()                           │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Warcraft Logs Sync Layer                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  WarcraftLogsSyncService                             │  │
│  │  - syncReportsForGuild()                             │  │
│  │  - processFightData()                                │  │
│  │  - extractPerformanceMetrics()                       │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Warcraft Logs Client Layer                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  WarcraftLogsClient                                  │  │
│  │  - authenticate()                                    │  │
│  │  - fetchReportsForGuild()                            │  │
│  │  - fetchFightData()                                  │  │
│  │  - fetchCharacterPerformance()                       │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                 Warcraft Logs API v2                        │
│                  (External Service)                         │
└─────────────────────────────────────────────────────────────┘
```


### Data Flow

```
1. Scheduled Sync Trigger
   ↓
2. WarcraftLogsSyncService.syncReportsForGuild()
   ↓
3. WarcraftLogsClient.fetchReportsForGuild()
   ↓
4. Parse and store report metadata
   ↓
5. For each report: WarcraftLogsClient.fetchFightData()
   ↓
6. Extract performance metrics (DPA, ADT)
   ↓
7. Store in database (WarcraftLogsPerformanceEntity)
   ↓
8. Calculate spec averages
   ↓
9. Cache results

FLPS Calculation Flow:
1. ScoreCalculator.calculateWithRealData()
   ↓
2. WarcraftLogsPerformanceService.getMASForCharacter()
   ↓
3. Check cache → if hit, return cached MAS
   ↓
4. Query performance metrics from database
   ↓
5. Calculate DPA ratio and ADT ratio
   ↓
6. Apply guild-specific weights and thresholds
   ↓
7. Calculate MAS score
   ↓
8. Cache result
   ↓
9. Return MAS to ScoreCalculator
```

## Components and Interfaces

### 1. WarcraftLogsClient

**Purpose**: HTTP client for Warcraft Logs API v2 with OAuth2 authentication

**Key Methods**:
```kotlin
interface WarcraftLogsClient {
    suspend fun authenticate(): AuthToken
    suspend fun fetchReportsForGuild(
        guildName: String,
        realm: String,
        region: String,
        startTime: Instant,
        endTime: Instant
    ): List<WarcraftLogsReport>
    
    suspend fun fetchFightData(reportCode: String): WarcraftLogsFightData
    
    suspend fun fetchCharacterPerformance(
        reportCode: String,
        fightId: Int,
        characterName: String
    ): CharacterPerformanceData
}
```

**Configuration Properties**:
```kotlin
@ConfigurationProperties("warcraft-logs")
data class WarcraftLogsProperties(
    val enabled: Boolean = false,
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String = "https://www.warcraftlogs.com/api/v2",
    val tokenUrl: String = "https://www.warcraftlogs.com/oauth/token",
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val maxConcurrentRequests: Int = 5,
    val requestTimeoutSeconds: Long = 30
)
```


### 2. WarcraftLogsSyncService

**Purpose**: Orchestrates report discovery, fight data extraction, and metric calculation

**Key Methods**:
```kotlin
@Service
class WarcraftLogsSyncService(
    private val client: WarcraftLogsClient,
    private val reportRepository: WarcraftLogsReportRepository,
    private val performanceRepository: WarcraftLogsPerformanceRepository,
    private val configService: WarcraftLogsConfigService,
    private val characterMappingService: CharacterMappingService
) {
    suspend fun syncReportsForGuild(guildId: String): SyncResult
    
    suspend fun processFightData(report: WarcraftLogsReport, guildId: String)
    
    suspend fun extractPerformanceMetrics(
        fightData: WarcraftLogsFightData,
        guildId: String
    ): List<CharacterPerformanceMetrics>
    
    suspend fun calculateSpecAverages(
        guildId: String,
        spec: String,
        percentile: Int = 50
    ): SpecAverages
}
```

**Scheduled Execution**:
```kotlin
@Scheduled(cron = "\${warcraft-logs.sync.cron:0 0 */6 * * *}")
suspend fun scheduledSync() {
    val guilds = guildConfigRepository.findAllWithWarcraftLogsEnabled()
    guilds.forEach { guild ->
        try {
            syncReportsForGuild(guild.id)
        } catch (ex: Exception) {
            logger.error("Failed to sync Warcraft Logs for guild ${guild.id}", ex)
        }
    }
}
```

### 3. WarcraftLogsPerformanceService

**Purpose**: Provides MAS calculation using Warcraft Logs performance data

**Key Methods**:
```kotlin
@Service
class WarcraftLogsPerformanceService(
    private val performanceRepository: WarcraftLogsPerformanceRepository,
    private val configService: WarcraftLogsConfigService,
    private val cacheManager: CacheManager
) {
    fun getMASForCharacter(
        characterName: String,
        characterRealm: String,
        guildId: String
    ): Double
    
    fun getPerformanceMetrics(
        characterName: String,
        characterRealm: String,
        guildId: String,
        timeWindow: Duration = Duration.ofDays(30)
    ): PerformanceMetrics
    
    fun calculateDPARatio(
        characterDPA: Double,
        specAverageDPA: Double
    ): Double
    
    fun calculateADTRatio(
        characterADT: Double,
        specAverageADT: Double
    ): Double
}
```


### 4. WarcraftLogsConfigService

**Purpose**: Manages guild-specific Warcraft Logs configuration

**Configuration Model**:
```kotlin
data class WarcraftLogsGuildConfig(
    val guildId: String,
    val enabled: Boolean = true,
    val guildName: String,
    val realm: String,
    val region: String,
    val clientId: String? = null, // Guild-specific credentials (optional)
    val clientSecret: String? = null,
    
    // Sync configuration
    val syncIntervalHours: Int = 6,
    val syncTimeWindowDays: Int = 30,
    val includedDifficulties: List<String> = listOf("Mythic", "Heroic"),
    
    // MAS calculation configuration
    val dpaWeight: Double = 0.25,
    val adtWeight: Double = 0.25,
    val criticalThreshold: Double = 1.5,
    val fallbackMAS: Double = 0.0,
    val fallbackDPA: Double = 0.5,
    val fallbackADT: Double = 10.0,
    
    // Time weighting configuration
    val recentPerformanceWeightMultiplier: Double = 2.0,
    val recentPerformanceDays: Int = 14,
    
    // Spec average calculation
    val specAveragePercentile: Int = 50,
    val minimumSampleSize: Int = 5,
    
    // Cache configuration
    val masCacheTTLMinutes: Int = 60,
    
    // Character mapping
    val characterNameMappings: Map<String, String> = emptyMap() // WoWAudit name -> WCL name
)
```

**Key Methods**:
```kotlin
@Service
class WarcraftLogsConfigService(
    private val configRepository: WarcraftLogsConfigRepository
) {
    fun getConfig(guildId: String): WarcraftLogsGuildConfig
    fun updateConfig(guildId: String, config: WarcraftLogsGuildConfig)
    fun getEffectiveClientCredentials(guildId: String): ClientCredentials
}
```

## Data Models

### Database Entities

#### WarcraftLogsReportEntity
```kotlin
@Table("warcraft_logs_reports")
data class WarcraftLogsReportEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val reportCode: String,
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val owner: String,
    val zone: Int,
    val syncedAt: Instant,
    val rawMetadata: String // JSON
)
```

#### WarcraftLogsFightEntity
```kotlin
@Table("warcraft_logs_fights")
data class WarcraftLogsFightEntity(
    @Id val id: Long? = null,
    val reportId: Long,
    val fightId: Int,
    val encounterId: Int,
    val encounterName: String,
    val difficulty: String,
    val kill: Boolean,
    val startTime: Long,
    val endTime: Long,
    val bossPercentage: Double?
)
```


#### WarcraftLogsPerformanceEntity
```kotlin
@Table("warcraft_logs_performance")
data class WarcraftLogsPerformanceEntity(
    @Id val id: Long? = null,
    val fightId: Long,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val characterSpec: String,
    val deaths: Int,
    val damageTaken: Long,
    val avoidableDamageTaken: Long,
    val avoidableDamagePercentage: Double,
    val performancePercentile: Double?,
    val itemLevel: Int,
    val calculatedAt: Instant
)
```

#### WarcraftLogsConfigEntity
```kotlin
@Table("warcraft_logs_config")
data class WarcraftLogsConfigEntity(
    @Id val guildId: String,
    val enabled: Boolean,
    val guildName: String,
    val realm: String,
    val region: String,
    val encryptedClientId: String?,
    val encryptedClientSecret: String?,
    val configJson: String, // All other config as JSON
    val updatedAt: Instant,
    val updatedBy: String?
)
```

#### WarcraftLogsCharacterMappingEntity
```kotlin
@Table("warcraft_logs_character_mappings")
data class WarcraftLogsCharacterMappingEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val wowauditName: String,
    val wowauditRealm: String,
    val warcraftLogsName: String,
    val warcraftLogsRealm: String,
    val createdAt: Instant,
    val createdBy: String?
)
```

### API Response Models

#### WarcraftLogsReport
```kotlin
data class WarcraftLogsReport(
    val code: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val owner: String,
    val zone: Int,
    val fights: List<WarcraftLogsFight>
)
```

#### WarcraftLogsFight
```kotlin
data class WarcraftLogsFight(
    val id: Int,
    val encounterID: Int,
    val name: String,
    val difficulty: Int,
    val kill: Boolean,
    val startTime: Long,
    val endTime: Long,
    val bossPercentage: Double?
)
```

#### CharacterPerformanceData
```kotlin
data class CharacterPerformanceData(
    val name: String,
    val server: String,
    val class: String,
    val spec: String,
    val deaths: Int,
    val damageTaken: Long,
    val avoidableDamageTaken: Long,
    val itemLevel: Int,
    val performancePercentile: Double?
)
```


## Error Handling

### Error Hierarchy

```kotlin
sealed class WarcraftLogsException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)

class WarcraftLogsAuthenticationException(message: String, cause: Throwable? = null) : 
    WarcraftLogsException(message, cause)

class WarcraftLogsRateLimitException(
    message: String,
    val retryAfterSeconds: Long? = null
) : WarcraftLogsException(message)

class WarcraftLogsApiException(
    message: String,
    val statusCode: Int,
    cause: Throwable? = null
) : WarcraftLogsException(message, cause)

class WarcraftLogsDataException(message: String, cause: Throwable? = null) : 
    WarcraftLogsException(message, cause)
```

### Error Handling Strategy

1. **Authentication Failures**: Log error, disable integration, alert administrators
2. **Rate Limiting**: Implement exponential backoff, respect Retry-After headers
3. **API Errors (4xx)**: Log with context, skip problematic data, continue processing
4. **API Errors (5xx)**: Retry with backoff, fall back to cached data if available
5. **Data Parsing Errors**: Log with report context, skip fight, continue with next
6. **Network Errors**: Retry with exponential backoff, use circuit breaker pattern

### Circuit Breaker Configuration

```kotlin
@Configuration
class WarcraftLogsResilienceConfig {
    @Bean
    fun warcraftLogsCircuitBreaker(): CircuitBreaker {
        return CircuitBreaker.of("warcraftLogs", CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofMinutes(5))
            .slidingWindowSize(10)
            .build())
    }
    
    @Bean
    fun warcraftLogsRetry(): Retry {
        return Retry.of("warcraftLogs", RetryConfig.custom<Any>()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(2))
            .retryExceptions(IOException::class.java, TimeoutException::class.java)
            .ignoreExceptions(WarcraftLogsAuthenticationException::class.java)
            .build())
    }
}
```

## Testing Strategy

### Unit Tests

1. **WarcraftLogsClient**
   - Mock WebClient responses
   - Test OAuth2 authentication flow
   - Test error handling for various HTTP status codes
   - Test rate limit handling

2. **WarcraftLogsSyncService**
   - Mock client responses
   - Test fight data extraction logic
   - Test performance metric calculation
   - Test spec average calculation

3. **WarcraftLogsPerformanceService**
   - Test MAS calculation with various inputs
   - Test DPA/ADT ratio calculations
   - Test critical threshold handling
   - Test fallback value usage

4. **WarcraftLogsConfigService**
   - Test configuration retrieval and defaults
   - Test guild-specific overrides
   - Test credential encryption/decryption


### Integration Tests

1. **WarcraftLogsClient Integration**
   - Use MockWebServer to simulate Warcraft Logs API
   - Test complete authentication flow
   - Test report fetching with pagination
   - Test fight data retrieval

2. **Database Integration**
   - Test entity persistence and retrieval
   - Test query performance with large datasets
   - Test transaction handling

3. **End-to-End Sync Flow**
   - Test complete sync process from API to database
   - Test performance metric calculation
   - Test MAS integration with ScoreCalculator

### Test Data

Create mock Warcraft Logs responses in `src/test/resources/warcraft-logs/`:
- `reports-response.json` - Sample reports list
- `fight-data-response.json` - Sample fight data
- `character-performance-response.json` - Sample performance data

## Performance Considerations

### Caching Strategy

1. **MAS Score Cache**
   - Key: `mas:{guildId}:{characterName}:{characterRealm}`
   - TTL: Configurable (default 60 minutes)
   - Invalidation: On new Warcraft Logs data sync

2. **Spec Average Cache**
   - Key: `spec-avg:{guildId}:{spec}:{metric}`
   - TTL: Configurable (default 6 hours)
   - Invalidation: On new performance data

3. **Report Metadata Cache**
   - Key: `wcl-reports:{guildId}:{startTime}:{endTime}`
   - TTL: Configurable (default 24 hours)

### Database Indexing

```sql
-- Performance queries
CREATE INDEX idx_wcl_perf_char ON warcraft_logs_performance(character_name, character_realm, fight_id);
CREATE INDEX idx_wcl_perf_spec ON warcraft_logs_performance(character_spec, calculated_at);
CREATE INDEX idx_wcl_perf_fight ON warcraft_logs_performance(fight_id);

-- Report queries
CREATE INDEX idx_wcl_reports_guild ON warcraft_logs_reports(guild_id, start_time DESC);
CREATE INDEX idx_wcl_reports_code ON warcraft_logs_reports(report_code);

-- Fight queries
CREATE INDEX idx_wcl_fights_report ON warcraft_logs_fights(report_id, encounter_id);
CREATE INDEX idx_wcl_fights_encounter ON warcraft_logs_fights(encounter_id, difficulty, kill);
```

### Async Processing

```kotlin
@Async("warcraftLogsExecutor")
suspend fun syncReportsForGuild(guildId: String): CompletableFuture<SyncResult> {
    return CompletableFuture.supplyAsync {
        // Sync logic
    }
}

@Configuration
class AsyncConfig {
    @Bean("warcraftLogsExecutor")
    fun warcraftLogsExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 5
            queueCapacity = 100
            setThreadNamePrefix("wcl-sync-")
            initialize()
        }
    }
}
```


## Security Considerations

### Credential Encryption

```kotlin
@Service
class CredentialEncryptionService(
    @Value("\${encryption.key}") private val encryptionKey: String
) {
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val secretKey = SecretKeySpec(encryptionKey.toByteArray(), "AES")
    
    fun encrypt(plaintext: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        return Base64.getEncoder().encodeToString(iv + encrypted)
    }
    
    fun decrypt(ciphertext: String): String {
        val decoded = Base64.getDecoder().decode(ciphertext)
        val iv = decoded.copyOfRange(0, 12)
        val encrypted = decoded.copyOfRange(12, decoded.size)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted))
    }
}
```

### API Key Management

- Store encryption key in environment variable or secrets manager
- Never log or expose API credentials
- Support credential rotation without downtime
- Restrict configuration endpoints to admin roles

### HTTPS/TLS

- All Warcraft Logs API calls use HTTPS
- Validate SSL certificates
- Use TLS 1.2 or higher

## Monitoring and Observability

### Metrics

```kotlin
@Component
class WarcraftLogsMetrics(
    private val meterRegistry: MeterRegistry
) {
    private val syncSuccessCounter = meterRegistry.counter("wcl.sync.success")
    private val syncFailureCounter = meterRegistry.counter("wcl.sync.failure")
    private val apiLatencyTimer = meterRegistry.timer("wcl.api.latency")
    private val cacheHitCounter = meterRegistry.counter("wcl.cache.hit")
    private val cacheMissCounter = meterRegistry.counter("wcl.cache.miss")
    
    fun recordSyncSuccess() = syncSuccessCounter.increment()
    fun recordSyncFailure() = syncFailureCounter.increment()
    fun recordApiCall(duration: Duration) = apiLatencyTimer.record(duration)
    fun recordCacheHit() = cacheHitCounter.increment()
    fun recordCacheMiss() = cacheMissCounter.increment()
}
```

### Logging

```kotlin
private val logger = LoggerFactory.getLogger(javaClass)

// Structured logging with context
logger.info(
    "Syncing Warcraft Logs reports",
    kv("guildId", guildId),
    kv("startTime", startTime),
    kv("endTime", endTime)
)

logger.error(
    "Failed to fetch Warcraft Logs data",
    kv("guildId", guildId),
    kv("reportCode", reportCode),
    kv("error", ex.message),
    ex
)
```

### Health Checks

```kotlin
@Component
class WarcraftLogsHealthIndicator(
    private val configService: WarcraftLogsConfigService,
    private val syncService: WarcraftLogsSyncService
) : HealthIndicator {
    override fun health(): Health {
        val guilds = configService.getAllEnabledGuilds()
        val statuses = guilds.map { guild ->
            val lastSync = syncService.getLastSyncTime(guild.id)
            val nextSync = syncService.getNextSyncTime(guild.id)
            guild.id to mapOf(
                "lastSync" to lastSync,
                "nextSync" to nextSync,
                "status" to if (lastSync != null) "OK" else "NEVER_SYNCED"
            )
        }.toMap()
        
        return Health.up()
            .withDetail("guilds", statuses)
            .build()
    }
}
```


## Configuration Reference

### Application Properties

```yaml
warcraft-logs:
  enabled: true
  client-id: ${WARCRAFT_LOGS_CLIENT_ID}
  client-secret: ${WARCRAFT_LOGS_CLIENT_SECRET}
  base-url: https://www.warcraftlogs.com/api/v2
  token-url: https://www.warcraftlogs.com/oauth/token
  
  # Sync configuration
  sync:
    cron: "0 0 */6 * * *"  # Every 6 hours
    time-window-days: 30
    included-difficulties:
      - Mythic
      - Heroic
  
  # API client configuration
  client:
    max-retries: 3
    retry-delay-ms: 1000
    max-concurrent-requests: 5
    request-timeout-seconds: 30
    connection-timeout-seconds: 10
  
  # Default MAS calculation configuration
  mas:
    dpa-weight: 0.25
    adt-weight: 0.25
    critical-threshold: 1.5
    fallback-mas: 0.0
    fallback-dpa: 0.5
    fallback-adt: 10.0
  
  # Time weighting configuration
  time-weighting:
    recent-performance-multiplier: 2.0
    recent-performance-days: 14
  
  # Spec average configuration
  spec-averages:
    percentile: 50
    minimum-sample-size: 5
  
  # Cache configuration
  cache:
    mas-ttl-minutes: 60
    spec-avg-ttl-minutes: 360
    report-metadata-ttl-minutes: 1440

# Encryption configuration
encryption:
  key: ${ENCRYPTION_KEY}  # 32-byte key for AES-256

# Async executor configuration
async:
  warcraft-logs:
    core-pool-size: 2
    max-pool-size: 5
    queue-capacity: 100
```

### Guild-Specific Configuration Example

```json
{
  "guildId": "my-guild",
  "enabled": true,
  "guildName": "My Awesome Guild",
  "realm": "Area 52",
  "region": "US",
  "syncIntervalHours": 4,
  "syncTimeWindowDays": 45,
  "includedDifficulties": ["Mythic"],
  "dpaWeight": 0.3,
  "adtWeight": 0.2,
  "criticalThreshold": 1.8,
  "recentPerformanceWeightMultiplier": 3.0,
  "recentPerformanceDays": 7,
  "characterNameMappings": {
    "PlayerOne-AreaFiftyTwo": "PlayerOne-Area52",
    "PlayerTwo-AreaFiftyTwo": "AltName-Area52"
  }
}
```

## Migration Strategy

### Database Migration

```sql
-- V0016__add_warcraft_logs_tables.sql

CREATE TABLE warcraft_logs_config (
    guild_id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    guild_name VARCHAR(255) NOT NULL,
    realm VARCHAR(255) NOT NULL,
    region VARCHAR(10) NOT NULL,
    encrypted_client_id TEXT,
    encrypted_client_secret TEXT,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255)
);

CREATE TABLE warcraft_logs_reports (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    report_code VARCHAR(50) NOT NULL,
    title VARCHAR(500),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    owner VARCHAR(255),
    zone INT,
    synced_at TIMESTAMP NOT NULL,
    raw_metadata TEXT,
    UNIQUE(report_code),
    FOREIGN KEY (guild_id) REFERENCES warcraft_logs_config(guild_id)
);

CREATE TABLE warcraft_logs_fights (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL,
    fight_id INT NOT NULL,
    encounter_id INT NOT NULL,
    encounter_name VARCHAR(255) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    kill BOOLEAN NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    boss_percentage DOUBLE PRECISION,
    UNIQUE(report_id, fight_id),
    FOREIGN KEY (report_id) REFERENCES warcraft_logs_reports(id) ON DELETE CASCADE
);

CREATE TABLE warcraft_logs_performance (
    id BIGSERIAL PRIMARY KEY,
    fight_id BIGINT NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(50) NOT NULL,
    character_spec VARCHAR(50) NOT NULL,
    deaths INT NOT NULL,
    damage_taken BIGINT NOT NULL,
    avoidable_damage_taken BIGINT NOT NULL,
    avoidable_damage_percentage DOUBLE PRECISION NOT NULL,
    performance_percentile DOUBLE PRECISION,
    item_level INT NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (fight_id) REFERENCES warcraft_logs_fights(id) ON DELETE CASCADE
);

CREATE TABLE warcraft_logs_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    wowaudit_name VARCHAR(255) NOT NULL,
    wowaudit_realm VARCHAR(255) NOT NULL,
    warcraft_logs_name VARCHAR(255) NOT NULL,
    warcraft_logs_realm VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    UNIQUE(guild_id, wowaudit_name, wowaudit_realm),
    FOREIGN KEY (guild_id) REFERENCES warcraft_logs_config(guild_id)
);

-- Indexes
CREATE INDEX idx_wcl_reports_guild ON warcraft_logs_reports(guild_id, start_time DESC);
CREATE INDEX idx_wcl_reports_code ON warcraft_logs_reports(report_code);
CREATE INDEX idx_wcl_fights_report ON warcraft_logs_fights(report_id, encounter_id);
CREATE INDEX idx_wcl_fights_encounter ON warcraft_logs_fights(encounter_id, difficulty, kill);
CREATE INDEX idx_wcl_perf_char ON warcraft_logs_performance(character_name, character_realm, fight_id);
CREATE INDEX idx_wcl_perf_spec ON warcraft_logs_performance(character_spec, calculated_at);
CREATE INDEX idx_wcl_perf_fight ON warcraft_logs_performance(fight_id);
```

## API Endpoints

### Configuration Management

```
GET    /api/warcraft-logs/config/{guildId}
PUT    /api/warcraft-logs/config/{guildId}
POST   /api/warcraft-logs/config/{guildId}/character-mapping
DELETE /api/warcraft-logs/config/{guildId}/character-mapping/{mappingId}
```

### Sync Management

```
POST   /api/warcraft-logs/sync/{guildId}  # Trigger manual sync
GET    /api/warcraft-logs/sync/{guildId}/status
GET    /api/warcraft-logs/sync/{guildId}/history
```

### Performance Data

```
GET    /api/warcraft-logs/performance/{guildId}/{characterName}
GET    /api/warcraft-logs/reports/{guildId}
GET    /api/warcraft-logs/reports/{reportCode}/fights
```

### Health and Monitoring

```
GET    /actuator/health/warcraftLogs
GET    /actuator/metrics/wcl.*
```

## Design Decisions and Rationale

1. **OAuth2 over API Key**: Warcraft Logs v2 uses OAuth2, providing better security and token refresh capabilities

2. **Async Processing**: Sync operations are async to avoid blocking FLPS calculations during API calls

3. **Guild-Specific Configuration**: Different guilds have different priorities (e.g., Mythic-only vs Heroic+Mythic)

4. **Caching Strategy**: MAS scores are cached to minimize database queries and improve FLPS calculation performance

5. **Fallback Values**: When Warcraft Logs data is unavailable, configurable fallbacks ensure FLPS calculations continue

6. **Time Weighting**: Recent performance is weighted higher to reflect current player skill level

7. **Spec Averages**: Normalization by spec ensures fair comparison across different classes and roles

8. **Character Mapping**: Handles cases where character names differ between WoWAudit and Warcraft Logs

9. **Encrypted Credentials**: API credentials are encrypted at rest for security compliance

10. **Circuit Breaker**: Prevents cascading failures when Warcraft Logs API is degraded

## Future Enhancements

1. **GraphQL Support**: Warcraft Logs v2 uses GraphQL; consider using GraphQL client for more efficient queries
2. **Webhook Integration**: Subscribe to Warcraft Logs webhooks for real-time report notifications
3. **Advanced Metrics**: Parse additional metrics like interrupts, dispels, and cooldown usage
4. **Performance Trends**: Track performance over time and show improvement/decline trends
5. **Comparative Analysis**: Compare guild performance against server/region averages
6. **Custom Queries**: Allow guild admins to define custom performance queries
