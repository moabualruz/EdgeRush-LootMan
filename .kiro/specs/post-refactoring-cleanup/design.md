# Design Document

## Overview

This design document outlines the approach for post-refactoring cleanup, verification, and fixes for the EdgeRush LootMan codebase. After successfully completing the domain-driven design refactoring, we need to:

1. Verify REST API functionality and document current state
2. Clarify GraphQL implementation status (not implemented - Phase 2)
3. Fix 59 failing integration tests
4. Address 1251 code quality issues
5. Remove unused code and optimize the codebase
6. Verify no functionality was lost during refactoring

## Architecture

### Current State Analysis

#### Package Structure (Post-Refactoring)

```
com.edgerush/
├── datasync/                          # Original package (partially migrated)
│   ├── api/
│   │   ├── v1/                       # REST Controllers
│   │   │   ├── ApplicationController.kt
│   │   │   ├── FlpsController.kt
│   │   │   ├── GuildController.kt
│   │   │   ├── IntegrationController.kt
│   │   │   └── RaiderController.kt
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── exception/                # Exception handlers
│   │   ├── warcraftlogs/            # WarcraftLogs specific endpoints
│   │   └── wowaudit/                # WoWAudit specific endpoints
│   ├── application/                  # Use Cases
│   │   ├── applications/            # Guild applications
│   │   ├── flps/                    # FLPS calculations
│   │   ├── integrations/            # External API sync
│   │   ├── raids/                   # Raid management
│   │   └── shared/                  # Shared use cases
│   ├── domain/                       # Domain models
│   │   ├── applications/
│   │   ├── attendance/
│   │   ├── flps/
│   │   ├── integrations/
│   │   ├── raids/
│   │   └── shared/
│   ├── infrastructure/               # Infrastructure implementations
│   │   ├── external/                # External API clients
│   │   └── persistence/             # Database repositories
│   ├── client/                       # Legacy clients (to be moved)
│   ├── config/                       # Configuration
│   ├── model/                        # Legacy models (to be removed)
│   └── security/                     # Security configuration
│
└── lootman/                          # New domain-driven package
    ├── api/                          # REST Controllers (new style)
    │   ├── attendance/
    │   │   ├── AttendanceController.kt
    │   │   └── AttendanceDto.kt
    │   ├── flps/
    │   │   ├── FlpsController.kt
    │   │   └── FlpsDto.kt
    │   └── loot/
    │       ├── LootController.kt
    │       └── LootDto.kt
    ├── application/                  # Use Cases (new style)
    │   ├── attendance/
    │   ├── flps/
    │   └── loot/
    ├── domain/                       # Domain models (new style)
    │   ├── attendance/
    │   ├── flps/
    │   ├── loot/
    │   ├── raids/
    │   └── shared/
    └── infrastructure/               # Infrastructure (new style)
        ├── attendance/
        ├── flps/
        ├── loot/
        └── persistence/
```

#### Test Failure Analysis

**Total Tests**: 509
**Passing**: 449 (88.2%)
**Failing**: 60 (11.8%)

**Failing Test Breakdown**:
- Integration Tests: 59 failures
  - datasync.api.v1: 33 failures (ApplicationController, FlpsController, GuildController, IntegrationController, LootAwardController, RaiderController)
  - lootman.api: 26 failures (AttendanceController, FlpsController, LootController)
- Unit Tests: 1 failure
  - SyncPropertiesTest: 1 failure

**Common Failure Patterns**:
1. Database schema mismatches (tables not found, column mismatches)
2. Repository implementation issues (in-memory vs JDBC)
3. Missing dependency injection configurations
4. Test data setup issues

#### Code Quality Issues

**Detekt Issues**: 1251 total
- Trailing whitespace: ~400 issues
- Wildcard imports: ~300 issues
- Magic numbers: ~250 issues
- Long methods: ~150 issues
- Complex methods: ~100 issues
- Other style issues: ~51 issues

## Components and Interfaces

### 1. REST API Verification System

#### API Inventory Tool

```kotlin
/**
 * Scans the codebase and generates a comprehensive REST API inventory
 */
object RestApiInventory {
    data class EndpointInfo(
        val path: String,
        val method: HttpMethod,
        val controller: String,
        val handler: String,
        val requestType: String?,
        val responseType: String,
        val authenticated: Boolean,
        val roles: List<String>
    )
    
    fun scanControllers(): List<EndpointInfo> {
        // Scan all @RestController classes
        // Extract @RequestMapping, @GetMapping, @PostMapping, etc.
        // Build comprehensive endpoint list
    }
    
    fun generateMarkdownReport(): String {
        // Generate markdown documentation of all endpoints
    }
    
    fun compareWithPrevious(previousInventory: List<EndpointInfo>): ApiDiff {
        // Compare current vs previous to find removed/changed endpoints
    }
}
```

#### API Test Verification

```kotlin
/**
 * Verifies all REST endpoints are functional
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestApiVerificationTest {
    
    @Autowired
    lateinit var restTemplate: TestRestTemplate
    
    @Test
    fun `should verify all GET endpoints return 200 or 401`() {
        val endpoints = RestApiInventory.scanControllers()
            .filter { it.method == HttpMethod.GET }
        
        endpoints.forEach { endpoint ->
            val response = restTemplate.getForEntity(endpoint.path, String::class.java)
            assertThat(response.statusCode).isIn(HttpStatus.OK, HttpStatus.UNAUTHORIZED)
        }
    }
}
```

### 2. Integration Test Fixing Strategy

#### Root Cause Analysis

```kotlin
/**
 * Analyzes integration test failures and categorizes them
 */
object IntegrationTestAnalyzer {
    enum class FailureCategory {
        DATABASE_SCHEMA_MISMATCH,
        REPOSITORY_NOT_IMPLEMENTED,
        MISSING_DEPENDENCY,
        TEST_DATA_ISSUE,
        CONFIGURATION_ERROR,
        UNKNOWN
    }
    
    data class TestFailure(
        val testClass: String,
        val testMethod: String,
        val exception: String,
        val stackTrace: String,
        val category: FailureCategory
    )
    
    fun analyzeFailures(testResults: List<TestResult>): Map<FailureCategory, List<TestFailure>> {
        return testResults
            .filter { !it.passed }
            .map { categorizeFailure(it) }
            .groupBy { it.category }
    }
    
    private fun categorizeFailure(result: TestResult): TestFailure {
        val category = when {
            result.exception.contains("Table") && result.exception.contains("not found") -> 
                FailureCategory.DATABASE_SCHEMA_MISMATCH
            result.exception.contains("No qualifying bean") -> 
                FailureCategory.MISSING_DEPENDENCY
            result.exception.contains("NotImplementedError") -> 
                FailureCategory.REPOSITORY_NOT_IMPLEMENTED
            else -> FailureCategory.UNKNOWN
        }
        
        return TestFailure(
            testClass = result.className,
            testMethod = result.methodName,
            exception = result.exception,
            stackTrace = result.stackTrace,
            category = category
        )
    }
}
```

#### Fix Patterns

**Pattern 1: Database Schema Mismatch**
```kotlin
// Problem: Test expects table that doesn't exist
@Test
fun `should get loot history`() {
    // Fails with: Table "loot_awards" not found
}

// Solution: Verify Flyway migrations are applied in test context
@TestConfiguration
class TestDatabaseConfig {
    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            flyway.clean()  // Clean test database
            flyway.migrate() // Apply all migrations
        }
    }
}
```

**Pattern 2: Repository Not Implemented**
```kotlin
// Problem: Test uses in-memory repository but expects JDBC behavior
class InMemoryLootAwardRepository : LootAwardRepository {
    override fun findByGuildId(guildId: GuildId): List<LootAward> {
        throw NotImplementedError("Not yet implemented")
    }
}

// Solution: Implement missing methods or use real JDBC repository in tests
class InMemoryLootAwardRepository : LootAwardRepository {
    private val storage = mutableMapOf<LootAwardId, LootAward>()
    
    override fun findByGuildId(guildId: GuildId): List<LootAward> {
        return storage.values.filter { it.guildId == guildId }
    }
}
```

**Pattern 3: Missing Dependency Injection**
```kotlin
// Problem: Controller can't find use case bean
@RestController
class LootController(
    private val awardLootUseCase: AwardLootUseCase  // Not found!
)

// Solution: Ensure use case is annotated with @Service
@Service
class AwardLootUseCase(
    private val repository: LootAwardRepository
) {
    // Implementation
}
```

### 3. Code Quality Improvement System

#### Automated Fixes

```kotlin
/**
 * Applies automated fixes for common code quality issues
 */
object CodeQualityFixer {
    
    fun removeTrailingWhitespace(file: File) {
        val content = file.readText()
        val fixed = content.lines()
            .joinToString("\n") { it.trimEnd() }
        file.writeText(fixed)
    }
    
    fun replaceWildcardImports(file: File) {
        val content = file.readText()
        val imports = extractImports(content)
        val wildcardImports = imports.filter { it.endsWith(".*") }
        
        wildcardImports.forEach { wildcardImport ->
            val usedClasses = findUsedClasses(content, wildcardImport)
            val explicitImports = usedClasses.map { 
                wildcardImport.replace(".*", ".$it") 
            }
            
            val fixed = content.replace(
                wildcardImport,
                explicitImports.joinToString("\n")
            )
            file.writeText(fixed)
        }
    }
    
    fun extractMagicNumbers(file: File) {
        // Identify magic numbers
        // Suggest constant names
        // Generate companion object with constants
    }
}
```

#### Detekt Configuration Tuning

```yaml
# detekt.yml - Adjusted for current codebase
build:
  maxIssues: 0  # Fail on any issues
  excludeCorrectable: false

complexity:
  LongMethod:
    threshold: 60  # Allow slightly longer methods for now
    active: true
  ComplexMethod:
    threshold: 15
    active: true

style:
  MagicNumber:
    active: true
    ignoreNumbers: ['-1', '0', '1', '2', '100', '1000']  # Common numbers
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true
    ignoreAnnotation: true
  
  WildcardImport:
    active: true
    excludeImports: []  # No exceptions
  
  TrailingWhitespace:
    active: true

formatting:
  MaximumLineLength:
    maxLineLength: 120
    active: true
```

### 4. Unused Code Detection

#### Static Analysis

```kotlin
/**
 * Detects unused code through static analysis
 */
object UnusedCodeDetector {
    
    data class CodeElement(
        val type: ElementType,
        val name: String,
        val file: File,
        val references: Int
    )
    
    enum class ElementType {
        CLASS, INTERFACE, FUNCTION, PROPERTY, FILE
    }
    
    fun findUnusedClasses(sourceRoot: File): List<CodeElement> {
        val allClasses = scanAllClasses(sourceRoot)
        val referencedClasses = findAllReferences(sourceRoot)
        
        return allClasses.filter { it.name !in referencedClasses }
    }
    
    fun findEmptyPackages(sourceRoot: File): List<File> {
        return sourceRoot.walkTopDown()
            .filter { it.isDirectory }
            .filter { dir ->
                dir.listFiles()?.all { it.isDirectory } == true
            }
            .toList()
    }
    
    fun findUnusedImports(file: File): List<String> {
        val content = file.readText()
        val imports = extractImports(content)
        val usedSymbols = extractUsedSymbols(content)
        
        return imports.filter { import ->
            val className = import.substringAfterLast('.')
            className !in usedSymbols
        }
    }
}
```

### 5. Functionality Verification Framework

#### Feature Comparison

```kotlin
/**
 * Compares pre-refactoring and post-refactoring functionality
 */
object FunctionalityVerifier {
    
    data class Feature(
        val name: String,
        val endpoints: List<String>,
        val useCases: List<String>,
        val domainModels: List<String>
    )
    
    val expectedFeatures = listOf(
        Feature(
            name = "FLPS Calculation",
            endpoints = listOf(
                "GET /api/v1/flps/{guildId}",
                "POST /api/v1/flps/calculate",
                "PUT /api/v1/flps/modifiers"
            ),
            useCases = listOf(
                "CalculateFlpsScoreUseCase",
                "GetFlpsReportUseCase",
                "UpdateModifiersUseCase"
            ),
            domainModels = listOf(
                "FlpsScore",
                "RaiderMeritScore",
                "ItemPriorityIndex",
                "RecencyDecayFactor"
            )
        ),
        Feature(
            name = "Loot Distribution",
            endpoints = listOf(
                "POST /api/v1/loot/awards",
                "GET /api/v1/loot/awards/guild/{guildId}",
                "POST /api/v1/loot/bans",
                "GET /api/v1/loot/bans/raider/{raiderId}"
            ),
            useCases = listOf(
                "AwardLootUseCase",
                "GetLootHistoryUseCase",
                "ManageLootBansUseCase"
            ),
            domainModels = listOf(
                "LootAward",
                "LootBan",
                "LootTier"
            )
        ),
        Feature(
            name = "Attendance Tracking",
            endpoints = listOf(
                "POST /api/v1/attendance/track",
                "GET /api/v1/attendance/report/{guildId}"
            ),
            useCases = listOf(
                "TrackAttendanceUseCase",
                "GetAttendanceReportUseCase"
            ),
            domainModels = listOf(
                "AttendanceRecord",
                "AttendanceStats"
            )
        ),
        Feature(
            name = "Raid Management",
            endpoints = listOf(
                "POST /api/v1/raids",
                "GET /api/v1/raids/{raidId}",
                "POST /api/v1/raids/{raidId}/signups",
                "POST /api/v1/raids/{raidId}/results"
            ),
            useCases = listOf(
                "ScheduleRaidUseCase",
                "ManageSignupsUseCase",
                "RecordRaidResultsUseCase"
            ),
            domainModels = listOf(
                "Raid",
                "RaidEncounter",
                "RaidSignup"
            )
        ),
        Feature(
            name = "Guild Applications",
            endpoints = listOf(
                "POST /api/v1/applications",
                "GET /api/v1/applications/guild/{guildId}",
                "PUT /api/v1/applications/{applicationId}/review"
            ),
            useCases = listOf(
                "SubmitApplicationUseCase",
                "GetApplicationsUseCase",
                "ReviewApplicationUseCase"
            ),
            domainModels = listOf(
                "Application",
                "ApplicationStatus",
                "CharacterInfo"
            )
        ),
        Feature(
            name = "External Integrations",
            endpoints = listOf(
                "POST /api/v1/integrations/wowaudit/sync",
                "POST /api/v1/integrations/warcraftlogs/sync",
                "GET /api/v1/integrations/status"
            ),
            useCases = listOf(
                "SyncWoWAuditDataUseCase",
                "SyncWarcraftLogsDataUseCase",
                "GetSyncStatusUseCase"
            ),
            domainModels = listOf(
                "SyncOperation",
                "SyncResult",
                "SyncStatus"
            )
        )
    )
    
    fun verifyAllFeatures(): FeatureVerificationReport {
        val results = expectedFeatures.map { feature ->
            FeatureVerificationResult(
                feature = feature,
                endpointsPresent = verifyEndpoints(feature.endpoints),
                useCasesPresent = verifyUseCases(feature.useCases),
                domainModelsPresent = verifyDomainModels(feature.domainModels)
            )
        }
        
        return FeatureVerificationReport(results)
    }
    
    private fun verifyEndpoints(endpoints: List<String>): Boolean {
        // Check if endpoints exist in controllers
        return endpoints.all { endpoint ->
            RestApiInventory.scanControllers()
                .any { it.path == endpoint.substringAfter(" ") }
        }
    }
    
    private fun verifyUseCases(useCases: List<String>): Boolean {
        // Check if use case classes exist
        return useCases.all { useCase ->
            findClass(useCase) != null
        }
    }
    
    private fun verifyDomainModels(models: List<String>): Boolean {
        // Check if domain model classes exist
        return models.all { model ->
            findClass(model) != null
        }
    }
}
```

## Data Models

### Test Failure Data Model

```kotlin
data class TestResult(
    val className: String,
    val methodName: String,
    val passed: Boolean,
    val duration: Duration,
    val exception: String?,
    val stackTrace: String?,
    val category: TestCategory
)

enum class TestCategory {
    UNIT, INTEGRATION, E2E
}

data class TestSuiteReport(
    val totalTests: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val duration: Duration,
    val failures: List<TestResult>
)
```

### Code Quality Data Model

```kotlin
data class CodeQualityIssue(
    val file: File,
    val line: Int,
    val column: Int,
    val rule: String,
    val severity: Severity,
    val message: String,
    val autoFixable: Boolean
)

enum class Severity {
    ERROR, WARNING, INFO
}

data class CodeQualityReport(
    val totalIssues: Int,
    val errors: Int,
    val warnings: Int,
    val issuesByRule: Map<String, Int>,
    val issuesByFile: Map<File, List<CodeQualityIssue>>
)
```

### API Inventory Data Model

```kotlin
data class RestEndpoint(
    val path: String,
    val method: HttpMethod,
    val controller: KClass<*>,
    val handlerMethod: String,
    val requestBody: KClass<*>?,
    val responseBody: KClass<*>,
    val authenticated: Boolean,
    val requiredRoles: List<String>,
    val deprecated: Boolean
)

data class ApiInventoryReport(
    val endpoints: List<RestEndpoint>,
    val totalEndpoints: Int,
    val byController: Map<KClass<*>, List<RestEndpoint>>,
    val byPath: Map<String, List<RestEndpoint>>,
    val authenticated: Int,
    val public: Int
)
```

## Error Handling

### Test Failure Handling

```kotlin
sealed class TestFixStrategy {
    data class DatabaseMigration(val migrations: List<String>) : TestFixStrategy()
    data class ImplementRepository(val repository: KClass<*>) : TestFixStrategy()
    data class AddDependency(val bean: KClass<*>) : TestFixStrategy()
    data class FixTestData(val dataSetup: String) : TestFixStrategy()
    data class UpdateConfiguration(val config: String) : TestFixStrategy()
}

class TestFixOrchestrator {
    fun analyzeAndFix(failure: TestResult): TestFixStrategy {
        return when {
            failure.exception?.contains("Table") == true -> 
                TestFixStrategy.DatabaseMigration(findRequiredMigrations(failure))
            failure.exception?.contains("No qualifying bean") == true -> 
                TestFixStrategy.AddDependency(findMissingBean(failure))
            failure.exception?.contains("NotImplementedError") == true -> 
                TestFixStrategy.ImplementRepository(findRepository(failure))
            else -> 
                TestFixStrategy.FixTestData("Manual investigation required")
        }
    }
}
```

### Code Quality Issue Handling

```kotlin
class CodeQualityFixer {
    fun applyAutoFixes(issues: List<CodeQualityIssue>): FixResult {
        val fixed = mutableListOf<CodeQualityIssue>()
        val failed = mutableListOf<CodeQualityIssue>()
        
        issues.filter { it.autoFixable }.forEach { issue ->
            try {
                when (issue.rule) {
                    "TrailingWhitespace" -> removeTrailingWhitespace(issue.file)
                    "WildcardImport" -> replaceWildcardImports(issue.file)
                    "MagicNumber" -> extractMagicNumber(issue.file, issue.line)
                    else -> failed.add(issue)
                }
                fixed.add(issue)
            } catch (e: Exception) {
                failed.add(issue)
            }
        }
        
        return FixResult(fixed, failed)
    }
}

data class FixResult(
    val fixed: List<CodeQualityIssue>,
    val failed: List<CodeQualityIssue>
)
```

## Testing Strategy

### Integration Test Fixing Approach

```
Phase 1: Analyze Failures (1-2 hours)
├── Run full test suite
├── Categorize failures by root cause
├── Identify common patterns
└── Prioritize by impact

Phase 2: Fix Database Issues (2-3 hours)
├── Verify Flyway migrations in test context
├── Add missing test database configuration
├── Ensure schema matches entity expectations
└── Test with clean database

Phase 3: Fix Repository Issues (2-3 hours)
├── Implement missing repository methods
├── Fix in-memory repository implementations
├── Add JDBC repository tests
└── Verify repository behavior

Phase 4: Fix Dependency Injection (1-2 hours)
├── Add missing @Service annotations
├── Fix circular dependencies
├── Configure test context properly
└── Verify bean wiring

Phase 5: Fix Test Data (1-2 hours)
├── Create test data builders
├── Fix test data setup
├── Add database cleanup
└── Verify test isolation

Phase 6: Verify All Tests Pass (1 hour)
├── Run full test suite
├── Verify 100% pass rate
├── Check test coverage
└── Document any remaining issues
```

### Code Quality Improvement Approach

```
Phase 1: Auto-Fix Simple Issues (1 hour)
├── Remove trailing whitespace (400 issues)
├── Format code with ktlint
└── Run detekt to verify fixes

Phase 2: Fix Wildcard Imports (2 hours)
├── Identify wildcard imports (300 issues)
├── Determine used classes
├── Replace with explicit imports
└── Verify compilation

Phase 3: Extract Magic Numbers (3 hours)
├── Identify magic numbers (250 issues)
├── Determine appropriate constant names
├── Create companion objects with constants
└── Replace magic numbers with constants

Phase 4: Refactor Complex Methods (4 hours)
├── Identify long/complex methods (250 issues)
├── Extract helper methods
├── Simplify logic
└── Verify tests still pass

Phase 5: Final Verification (1 hour)
├── Run detekt
├── Verify zero critical issues
├── Document remaining warnings
└── Update detekt configuration if needed
```

## Migration Strategy

### Phased Approach

**Phase 1: Analysis and Planning (Day 1)**
- Run comprehensive analysis
- Document current state
- Identify all issues
- Create prioritized fix list
- Estimate effort

**Phase 2: Fix Critical Issues (Days 2-3)**
- Fix failing integration tests
- Fix failing unit test
- Verify compilation
- Ensure basic functionality works

**Phase 3: Code Quality (Days 4-5)**
- Auto-fix simple issues
- Fix wildcard imports
- Extract magic numbers
- Refactor complex methods

**Phase 4: Cleanup (Day 6)**
- Remove unused code
- Delete empty packages
- Consolidate duplicate code
- Optimize imports

**Phase 5: Verification (Day 7)**
- Run full test suite
- Verify test coverage
- Run performance tests
- Update documentation

**Phase 6: Documentation (Day 8)**
- Update CODE_ARCHITECTURE.md
- Update API_REFERENCE.md
- Create migration guide
- Document lessons learned

## Performance Considerations

### Test Execution Performance

- Use Testcontainers with reusable containers
- Implement database cleanup instead of recreation
- Use parallel test execution where possible
- Cache test data builders

### Build Performance

- Enable Gradle build cache
- Use incremental compilation
- Parallelize detekt and ktlint checks
- Optimize test execution order

## Success Criteria

1. ✅ All 509 tests passing (100% pass rate)
2. ✅ Zero critical detekt violations
3. ✅ Test coverage ≥85%
4. ✅ All REST endpoints documented
5. ✅ GraphQL status clarified (not implemented)
6. ✅ No unused code remaining
7. ✅ Performance benchmarks met
8. ✅ Documentation updated
9. ✅ Build time <2 minutes
10. ✅ Zero compilation warnings
