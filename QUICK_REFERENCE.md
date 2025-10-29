# 🎯 EdgeRush LootMan - Quick Reference Guide

*AI Agent Navigation & Development Quick Start*

---

## 📋 Essential Context

### Project Summary
**EdgeRush LootMan** is a World of Warcraft guild loot distribution system implementing the **FLPS (Final Loot Priority Score)** algorithm for fair, transparent, and automated loot decisions.

### Key Business Concepts
- **FLPS**: Composite score = (RMS × IPI) × RDF
- **RMS**: Raider Merit Score (attendance + performance + preparation)
- **IPI**: Item Priority Index (upgrade value + tier impact + role multiplier)
- **RDF**: Recency Decay Factor (reduces score for recent loot recipients)

---

## 🗂️ Project Structure Navigation

```
📁 looter/                          # Root directory
├── 📁 data-sync-service/           # Main Kotlin service
│   ├── 📁 src/main/kotlin/         # Core application code
│   │   └── 📁 com/edgerush/datasync/
│   │       ├── 📁 api/             # External API integrations
│   │       ├── 📁 client/          # HTTP clients (WoWAudit)
│   │       ├── 📁 config/          # Configuration classes
│   │       ├── 📁 domain/          # Business entities
│   │       ├── 📁 repository/      # Data access layer
│   │       ├── 📁 service/         # Business logic
│   │       └── 📁 schema/          # Data validation
│   └── 📁 src/test/kotlin/         # Test suites
├── 📁 docs/                        # Documentation
├── 📁 assets/                      # Sample data & dashboards
├── 📁 deploy/                      # Deployment configs
└── 📄 docker-compose.yml           # Service orchestration
```

### Critical Files Quick Access

| File | Purpose | When to Edit |
|------|---------|--------------|
| `data-sync-service/src/main/kotlin/com/edgerush/datasync/DataSyncApplication.kt` | Main application entry | Rarely |
| `data-sync-service/src/main/kotlin/com/edgerush/datasync/service/ScoreCalculator.kt` | FLPS algorithm core | Score logic changes |
| `data-sync-service/src/main/kotlin/com/edgerush/datasync/client/WoWAuditClient.kt` | External API integration | API changes |
| `data-sync-service/src/main/kotlin/com/edgerush/datasync/domain/Character.kt` | Character entity | Data model changes |
| `data-sync-service/build.gradle.kts` | Dependencies & build config | Adding libraries |
| `docker-compose.yml` | Service orchestration | Environment changes |

---

## 🔧 Development Commands

### Local Development
```powershell
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f data-sync

# Run tests
cd data-sync-service
./gradlew test

# Build application
./gradlew build

# Check health
curl http://localhost:8080/actuator/health
```

### Database Operations
```powershell
# Connect to PostgreSQL
docker exec -it looter-postgres-1 psql -U lootman -d lootman_db

# View tables
\dt

# Check migrations
SELECT * FROM flyway_schema_history;
```

### Debugging
```powershell
# Run with debug logging
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up

# JVM debug port (if enabled)
# Attach debugger to localhost:5005
```

---

## 🎯 Common Development Tasks

### 1. Adding New External API Integration

**Files to modify:**
1. `client/` - Create new client class
2. `config/SyncProperties.kt` - Add configuration
3. `service/SyncService.kt` - Integrate into sync process

**Pattern:**
```kotlin
@Component
class NewApiClient(
    private val webClient: WebClient,
    private val properties: SyncProperties
) {
    suspend fun fetchData(): Result<DataType> = runCatching {
        // Implementation
    }
}
```

### 2. Modifying FLPS Algorithm

**Primary file:** `service/ScoreCalculator.kt`

**Test file:** `test/kotlin/.../service/ScoreCalculatorTest.kt`

**Pattern:**
```kotlin
private suspend fun calculateNewComponent(character: Character): Double {
    // 1. Validate inputs
    // 2. Apply business logic
    // 3. Normalize to 0.0-1.0 range
    // 4. Log reasoning
    return score.coerceIn(0.0, 1.0)
}
```

### 3. Adding New Entity/Data Model

**Files to create/modify:**
1. `domain/NewEntity.kt` - Entity class
2. `repository/NewEntityRepository.kt` - Data access
3. `src/main/resources/db/migration/` - SQL migration

**Pattern:**
```kotlin
@Entity
@Table(name = "new_entities")
data class NewEntity(
    @Id @GeneratedValue
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### 4. Creating New REST Endpoint

**File:** `api/controller/` (create if needed)

**Pattern:**
```kotlin
@RestController
@RequestMapping("/api/v1/new-feature")
class NewFeatureController(
    private val service: NewFeatureService
) {
    @GetMapping
    suspend fun getFeatures(): ResponseEntity<List<Feature>> {
        return ResponseEntity.ok(service.getAllFeatures())
    }
}
```

---

## 🧪 Testing Patterns

### Unit Tests
```kotlin
@ExtendWith(MockKExtension::class)
class ServiceTest {
    @MockK private lateinit var dependency: Dependency
    @InjectMockKs private lateinit var service: Service
    
    @Test
    fun `should handle valid input correctly`() = runTest {
        // Given
        every { dependency.method() } returns expectedValue
        
        // When
        val result = service.processInput(input)
        
        // Then
        result.shouldBe(expected)
        verify { dependency.method() }
    }
}
```

### Integration Tests
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["spring.profiles.active=test"])
class IntegrationTest {
    @Autowired private lateinit var testRestTemplate: TestRestTemplate
    
    @Test
    fun `should integrate with database correctly`() {
        // Test full stack integration
    }
}
```

---

## 📊 Monitoring & Debugging

### Key Metrics to Watch
- **Sync Success Rate**: `sync.success.rate`
- **API Response Times**: `api.response.time`
- **Score Calculations**: `score.calculations.count`
- **Database Health**: `db.connection.pool.active`

### Health Check Endpoints
```
GET /actuator/health              # Basic health
GET /actuator/health/detailed     # Component health
GET /actuator/metrics            # Prometheus metrics
GET /actuator/info               # Application info
```

### Log Patterns to Search For
```
ERROR.*WoWAuditClient            # API integration issues
ERROR.*ScoreCalculator           # FLPS calculation errors
WARN.*SyncService               # Data sync warnings
INFO.*character.updated         # Successful updates
```

---

## 🚨 Common Issues & Solutions

### Problem: WoWAudit API Returning 401/403
**Solution:**
1. Check API key in `application.yaml`
2. Verify key hasn't expired
3. Check rate limiting status

### Problem: Database Migration Failed
**Solution:**
1. Check SQL syntax in migration file
2. Verify database user permissions
3. Review Flyway schema history table

### Problem: Score Calculation Returning NaN
**Solution:**
1. Check for division by zero in calculations
2. Validate input data completeness
3. Review null handling in score components

### Problem: Docker Services Not Starting
**Solution:**
1. Check port conflicts: `netstat -an | findstr :8080`
2. Verify Docker daemon is running
3. Check docker-compose.yml syntax

---

## 📚 Additional Resources

### Documentation Files
- `AI_AGENT_GUIDE.md` - Comprehensive project guide
- `CODE_ARCHITECTURE.md` - Technical architecture details
- `AI_DEVELOPMENT_STANDARDS.md` - Coding conventions
- `API_REFERENCE.md` - Interface documentation

### External References
- [WoWAudit API Documentation](https://wowaudit.com/api/docs)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

### Project-Specific Resources
- `docs/score-model.md` - FLPS algorithm details
- `docs/wowaudit-data-map.md` - API data mapping
- `assets/dashboard/` - Sample data and dashboards

---

*This quick reference provides immediate access to essential information for efficient AI agent development on EdgeRush LootMan.*