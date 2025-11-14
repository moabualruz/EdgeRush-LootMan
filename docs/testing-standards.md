# Testing Standards

## Overview

This document defines the testing standards for the EdgeRush LootMan project. All code must follow these standards to ensure quality, maintainability, and reliability.

## Test-Driven Development (TDD)

### The Red-Green-Refactor Cycle

All new code must be developed using TDD:

1. **Red** - Write a failing test first
2. **Green** - Write minimal code to make the test pass
3. **Refactor** - Improve code while keeping tests green
4. **Repeat** - Continue for each new feature or behavior

### Why TDD?

- Ensures code is testable from the start
- Provides living documentation
- Catches bugs early
- Enables confident refactoring
- Forces clear requirements

## Test Organization

### Directory Structure

```
src/
├── main/kotlin/com/edgerush/lootman/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── test/kotlin/com/edgerush/lootman/
    ├── unit/              # Unit tests (70% of tests)
    │   ├── domain/        # Domain logic tests
    │   └── application/   # Use case tests
    ├── integration/       # Integration tests (25% of tests)
    │   ├── api/           # API endpoint tests
    │   └── persistence/   # Database tests
    └── e2e/              # End-to-end tests (5% of tests)
        └── scenarios/     # User scenario tests
```

### Test Types

**Unit Tests (70%)**
- Test single units of code in isolation
- Use mocks for dependencies
- Fast execution (< 100ms per test)
- No external dependencies (database, network, etc.)

**Integration Tests (25%)**
- Test multiple components working together
- Use real dependencies (database via Testcontainers)
- Slower execution (< 5s per test)
- Test actual integration points

**End-to-End Tests (5%)**
- Test complete user scenarios
- Use real system components
- Slowest execution (< 30s per test)
- Test critical business workflows

## Test Naming Conventions

### Pattern

Use backtick syntax with descriptive names:

```kotlin
`should_ExpectedBehavior_When_StateUnderTest`
```

### Examples

```kotlin
class FlpsScoreTest : UnitTest() {
    
    @Test
    fun `should create valid score when value is between 0 and 1`() {
        // Test implementation
    }
    
    @Test
    fun `should throw exception when value is negative`() {
        // Test implementation
    }
    
    @Test
    fun `should throw exception when value exceeds 1`() {
        // Test implementation
    }
}
```

### Guidelines

- Start with `should`
- Describe expected behavior
- Include the condition being tested
- Be specific and descriptive
- Avoid abbreviations
- Keep under 100 characters if possible

## Test Structure: AAA Pattern

### Arrange-Act-Assert

All tests must follow the AAA pattern:

```kotlin
@Test
fun `should calculate correct FLPS score when all components are valid`() {
    // Arrange (Given) - Set up test data and mocks
    val raiderMerit = RaiderMeritScore.of(0.85)
    val itemPriority = ItemPriorityIndex.of(0.90)
    val recencyDecay = RecencyDecayFactor.of(1.0)
    
    // Act (When) - Execute the code under test
    val result = flpsService.calculate(raiderMerit, itemPriority, recencyDecay)
    
    // Assert (Then) - Verify the results
    result.value shouldBe 0.765
}
```

### Section Guidelines

**Arrange:**
- Create test data
- Set up mocks and stubs
- Configure system state
- Keep setup minimal and focused

**Act:**
- Execute the code under test
- Should be a single line or small block
- Represents the behavior being tested

**Assert:**
- Verify expected outcomes
- Check return values
- Verify mock interactions
- Validate state changes

## Mocking with MockK

### Basic Mocking

```kotlin
class LootServiceTest : UnitTest() {
    
    private lateinit var lootRepository: LootAwardRepository
    private lateinit var raiderRepository: RaiderRepository
    private lateinit var lootService: LootService
    
    @BeforeEach
    fun setup() {
        lootRepository = mockk()
        raiderRepository = mockk()
        lootService = LootService(lootRepository, raiderRepository)
    }
    
    @Test
    fun `should award loot to highest FLPS scorer`() {
        // Arrange
        val item = createTestItem()
        val raider = createTestRaider(flpsScore = 0.92)
        
        every { raiderRepository.findById(raider.id) } returns raider
        every { lootRepository.save(any()) } returns mockk()
        
        // Act
        val result = lootService.awardLoot(item.id, raider.id)
        
        // Assert
        result.isSuccess shouldBe true
        verify(exactly = 1) { lootRepository.save(any()) }
    }
}
```

### MockK Guidelines

**Use `every` for stubbing:**
```kotlin
every { repository.findById(id) } returns entity
every { service.calculate(any()) } returns 0.85
```

**Use `verify` for verification:**
```kotlin
verify(exactly = 1) { repository.save(any()) }
verify(atLeast = 1) { service.process(any()) }
verify { repository.delete(id) }
```

**Use `slot` for capturing arguments:**
```kotlin
val slot = slot<LootAward>()
every { repository.save(capture(slot)) } returns mockk()

// Later in test
slot.captured.raiderId shouldBe expectedRaiderId
```

**Use `relaxed` mocks sparingly:**
```kotlin
val repository = mockk<Repository>(relaxed = true)
// Only use when you don't care about specific interactions
```

## Base Test Classes

### UnitTest Base Class

```kotlin
abstract class UnitTest {
    
    @BeforeEach
    fun setupMocks() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
```

Usage:
```kotlin
class MyServiceTest : UnitTest() {
    
    @MockK
    private lateinit var dependency: Dependency
    
    @InjectMockKs
    private lateinit var service: MyService
    
    @Test
    fun `should do something`() {
        // Test implementation
    }
}
```

### IntegrationTest Base Class

```kotlin
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
        
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    
    @Autowired
    protected lateinit var testRestTemplate: TestRestTemplate
    
    @BeforeEach
    fun cleanDatabase() {
        // Clean database between tests
    }
}
```

Usage:
```kotlin
class LootControllerIntegrationTest : IntegrationTest() {
    
    @Test
    fun `should award loot and return 201 Created`() {
        // Test implementation using testRestTemplate
    }
}
```

## Test Data Builders

### Factory Pattern

```kotlin
object TestDataFactory {
    
    fun createTestRaider(
        id: RaiderId = RaiderId.generate(),
        name: String = "TestRaider",
        flpsScore: Double = 0.85,
        guildId: GuildId = GuildId("test-guild")
    ): Raider = Raider(
        id = id,
        name = name,
        flpsScore = FlpsScore.of(flpsScore),
        guildId = guildId
    )
    
    fun createTestLootAward(
        id: LootAwardId = LootAwardId.generate(),
        itemId: ItemId = ItemId(12345),
        raiderId: RaiderId = RaiderId.generate(),
        flpsScore: Double = 0.85
    ): LootAward = LootAward.create(
        itemId = itemId,
        raiderId = raiderId,
        guildId = GuildId("test-guild"),
        flpsScore = FlpsScore.of(flpsScore),
        tier = LootTier.MYTHIC
    )
}
```

Usage:
```kotlin
@Test
fun `should process loot award`() {
    // Arrange
    val raider = createTestRaider(flpsScore = 0.92)
    val lootAward = createTestLootAward(raiderId = raider.id)
    
    // Act & Assert
    // ...
}
```

## Assertions

### Kotest Matchers (Recommended)

```kotlin
// Equality
result shouldBe expected
result shouldNotBe unexpected

// Numeric comparisons
score shouldBeGreaterThan 0.0
score shouldBeLessThanOrEqual 1.0
score shouldBeInRange 0.0..1.0

// Collections
list shouldContain element
list shouldHaveSize 5
list.shouldBeEmpty()

// Exceptions
shouldThrow<IllegalArgumentException> {
    FlpsScore.of(-0.1)
}

// Nullability
result.shouldNotBeNull()
result.shouldBeNull()
```

### JUnit Assertions (Alternative)

```kotlin
assertEquals(expected, actual)
assertNotNull(result)
assertTrue(condition)
assertThrows<IllegalArgumentException> {
    // Code that should throw
}
```

## Code Coverage Requirements

### Minimum Coverage

- **Phase 1-2:** 70% minimum coverage
- **Phase 3+:** 85% minimum coverage
- **New code:** Must meet current phase threshold
- **Critical paths:** 100% coverage required

### Coverage by Layer

| Layer | Minimum Coverage |
|-------|-----------------|
| Domain | 95% |
| Application | 90% |
| Infrastructure | 80% |
| API | 85% |

### Excluded from Coverage

- Data classes with no logic
- Configuration classes
- Main application class
- Generated code

### Running Coverage Reports

```bash
# Run tests with coverage
./gradlew test jacocoTestReport

# View report
open data-sync-service/build/reports/jacoco/test/html/index.html

# Verify coverage threshold
./gradlew jacocoTestCoverageVerification
```

## Testing Best Practices

### DO

✅ Write tests before implementation (TDD)
✅ Test one behavior per test
✅ Use descriptive test names
✅ Follow AAA pattern
✅ Keep tests simple and focused
✅ Use test data builders
✅ Mock external dependencies
✅ Test edge cases and error conditions
✅ Keep tests fast (< 100ms for unit tests)
✅ Make tests deterministic (no random data)

### DON'T

❌ Test implementation details
❌ Write tests after code is "done"
❌ Use real databases in unit tests
❌ Share state between tests
❌ Use Thread.sleep() in tests
❌ Ignore failing tests
❌ Skip tests to make builds pass
❌ Test private methods directly
❌ Use production data in tests
❌ Write tests that depend on execution order

## Common Testing Patterns

### Testing Value Objects

```kotlin
class FlpsScoreTest : UnitTest() {
    
    @Test
    fun `should create valid score when value is in range`() {
        val score = FlpsScore.of(0.85)
        score.value shouldBe 0.85
    }
    
    @Test
    fun `should throw exception when value is negative`() {
        shouldThrow<IllegalArgumentException> {
            FlpsScore.of(-0.1)
        }
    }
    
    @Test
    fun `should throw exception when value exceeds 1`() {
        shouldThrow<IllegalArgumentException> {
            FlpsScore.of(1.5)
        }
    }
    
    @Test
    fun `should support arithmetic operations`() {
        val score1 = FlpsScore.of(0.5)
        val score2 = FlpsScore.of(0.3)
        
        val result = score1 + score2
        
        result.value shouldBe 0.8
    }
}
```

### Testing Entities

```kotlin
class LootAwardTest : UnitTest() {
    
    @Test
    fun `should create loot award with valid data`() {
        val lootAward = LootAward.create(
            itemId = ItemId(12345),
            raiderId = RaiderId.generate(),
            guildId = GuildId("test-guild"),
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC
        )
        
        lootAward.isActive() shouldBe true
        lootAward.flpsScore.value shouldBe 0.85
    }
    
    @Test
    fun `should revoke active loot award`() {
        val lootAward = createTestLootAward()
        
        val revoked = lootAward.revoke("Mistake")
        
        revoked.isActive() shouldBe false
    }
    
    @Test
    fun `should throw exception when revoking non-active award`() {
        val lootAward = createTestLootAward().revoke("First revoke")
        
        shouldThrow<IllegalStateException> {
            lootAward.revoke("Second revoke")
        }
    }
}
```

### Testing Use Cases

```kotlin
class AwardLootUseCaseTest : UnitTest() {
    
    @MockK
    private lateinit var lootRepository: LootAwardRepository
    
    @MockK
    private lateinit var raiderRepository: RaiderRepository
    
    @MockK
    private lateinit var flpsService: FlpsCalculationService
    
    @InjectMockKs
    private lateinit var useCase: AwardLootUseCase
    
    @Test
    fun `should award loot successfully when all validations pass`() {
        // Arrange
        val command = AwardLootCommand(
            itemId = ItemId(12345),
            raiderId = RaiderId.generate(),
            guildId = GuildId("test-guild")
        )
        val raider = createTestRaider(id = command.raiderId)
        val flpsScore = FlpsScore.of(0.85)
        
        every { raiderRepository.findById(command.raiderId) } returns raider
        every { flpsService.calculateFlpsScore(any(), any(), any()) } returns flpsScore
        every { lootRepository.save(any()) } answers { firstArg() }
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isSuccess shouldBe true
        result.getOrNull()?.flpsScore shouldBe flpsScore
        verify(exactly = 1) { lootRepository.save(any()) }
    }
    
    @Test
    fun `should fail when raider not found`() {
        // Arrange
        val command = AwardLootCommand(
            itemId = ItemId(12345),
            raiderId = RaiderId.generate(),
            guildId = GuildId("test-guild")
        )
        
        every { raiderRepository.findById(command.raiderId) } returns null
        
        // Act
        val result = useCase.execute(command)
        
        // Assert
        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBeInstanceOf RaiderNotFoundException::class
        verify(exactly = 0) { lootRepository.save(any()) }
    }
}
```

### Testing Repositories (Integration)

#### Repository Test Requirements

All repository tests MUST follow these standards:

**Required Annotations:**
- `@DataJdbcTest` - Configures Spring Data JDBC test slice
- `@Transactional` - Ensures test isolation with automatic rollback
- `@AutoConfigureTestDatabase(replace = NONE)` - Use real database (Testcontainers)

**Prohibited:**
- Raw JDBC operations (JdbcTemplate) in tests
- Manual SQL for test data setup (use repository methods)
- Shared state between tests

#### Repository Test Pattern

```kotlin
@DataJdbcTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcRaidRepositoryTest {
    
    @Autowired
    private lateinit var springRaidRepository: RaidRepository
    
    @Autowired
    private lateinit var encounterRepository: RaidEncounterRepository
    
    @Autowired
    private lateinit var signupRepository: RaidSignupRepository
    
    private lateinit var repository: JdbcRaidRepository
    private lateinit var mapper: RaidMapper
    
    @BeforeEach
    fun setup() {
        mapper = RaidMapper(RaidEncounterMapper(), RaidSignupMapper())
        repository = JdbcRaidRepository(
            springRaidRepository,
            encounterRepository,
            signupRepository,
            mapper
        )
    }
    
    @AfterEach
    fun cleanup() {
        // Spring @Transactional handles rollback automatically
        // No manual cleanup needed
    }
    
    @Test
    fun `should save and retrieve raid with encounters and signups`() {
        // Arrange
        val raid = createTestRaid(
            encounters = listOf(createTestEncounter()),
            signups = listOf(createTestSignup())
        )
        
        // Act
        val saved = repository.save(raid)
        val retrieved = repository.findById(saved.id)
        
        // Assert
        retrieved.shouldNotBeNull()
        retrieved.id shouldBe saved.id
        retrieved.getEncounters() shouldHaveSize 1
        retrieved.getSignups() shouldHaveSize 1
    }
    
    @Test
    fun `should delete raid and cascade to child entities`() {
        // Arrange
        val raid = createTestRaid(
            encounters = listOf(createTestEncounter()),
            signups = listOf(createTestSignup())
        )
        val saved = repository.save(raid)
        
        // Act
        repository.delete(saved.id)
        
        // Assert
        val retrieved = repository.findById(saved.id)
        retrieved.shouldBeNull()
        
        // Verify cascade delete
        val encounters = encounterRepository.findByRaidId(saved.id.value)
        val signups = signupRepository.findByRaidId(saved.id.value)
        encounters.shouldBeEmpty()
        signups.shouldBeEmpty()
    }
    
    @Test
    fun `should handle concurrent saves with optimistic locking`() {
        // Test transaction isolation and optimistic locking
    }
}
```

#### Testing Custom Query Methods

```kotlin
@DataJdbcTest
@Transactional
class RaidRepositoryQueryTest {
    
    @Autowired
    private lateinit var repository: RaidRepository
    
    @Test
    fun `should find raids by date`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)
        val entity1 = RaidEntity(raidId = 0, date = date, difficulty = "MYTHIC")
        val entity2 = RaidEntity(raidId = 0, date = date, difficulty = "HEROIC")
        val entity3 = RaidEntity(raidId = 0, date = date.plusDays(1), difficulty = "MYTHIC")
        
        repository.save(entity1)
        repository.save(entity2)
        repository.save(entity3)
        
        // Act
        val results = repository.findByDate(date)
        
        // Assert
        results shouldHaveSize 2
        results.all { it.date == date } shouldBe true
    }
    
    @Test
    fun `should delete by raid id`() {
        // Arrange
        val entity = RaidEntity(raidId = 0, date = LocalDate.now(), difficulty = "MYTHIC")
        val saved = repository.save(entity)
        
        // Act
        repository.deleteByRaidId(saved.raidId)
        
        // Assert
        repository.findById(saved.raidId).isEmpty shouldBe true
    }
}
```

#### Testing with Testcontainers

```kotlin
@SpringBootTest
@Testcontainers
@Transactional
class LootAwardRepositoryIntegrationTest {
    
    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:18").apply {
            withDatabaseName("lootman_test")
            withUsername("test")
            withPassword("test")
        }
        
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    
    @Autowired
    private lateinit var repository: LootAwardRepository
    
    @Test
    fun `should save and retrieve loot award`() {
        // Arrange
        val lootAward = createTestLootAward()
        
        // Act
        val saved = repository.save(lootAward)
        val retrieved = repository.findById(saved.id)
        
        // Assert
        retrieved.shouldNotBeNull()
        retrieved.id shouldBe saved.id
        retrieved.flpsScore shouldBe saved.flpsScore
    }
    
    @Test
    fun `should find loot awards by raider id`() {
        // Arrange
        val raiderId = RaiderId.generate()
        val award1 = createTestLootAward(raiderId = raiderId)
        val award2 = createTestLootAward(raiderId = raiderId)
        repository.save(award1)
        repository.save(award2)
        
        // Act
        val awards = repository.findByRaiderId(raiderId)
        
        // Assert
        awards shouldHaveSize 2
        awards.map { it.id } shouldContainAll listOf(award1.id, award2.id)
    }
}
```

#### Repository Testing Best Practices

**DO:**
- ✅ Use `@DataJdbcTest` for Spring Data repository tests
- ✅ Use `@Transactional` for automatic rollback between tests
- ✅ Test with real database (Testcontainers)
- ✅ Test custom query methods
- ✅ Test cascade operations (save, delete)
- ✅ Test transaction boundaries
- ✅ Verify constraint violations
- ✅ Test optimistic locking scenarios

**DON'T:**
- ❌ Use JdbcTemplate in repository tests
- ❌ Use manual SQL for test data setup
- ❌ Share state between tests
- ❌ Use in-memory H2 database (use Testcontainers with PostgreSQL)
- ❌ Mock Spring Data repositories in integration tests
- ❌ Test Spring Data framework behavior (trust the framework)
- ❌ Write tests for simple CRUD operations (focus on custom logic)

### Testing API Endpoints (Integration)

```kotlin
class LootControllerIntegrationTest : IntegrationTest() {
    
    @Test
    fun `should award loot and return 201 Created`() {
        // Arrange
        val request = AwardLootRequest(
            itemId = 12345,
            raiderId = "test-raider-id",
            guildId = "test-guild"
        )
        
        // Act
        val response = testRestTemplate.postForEntity(
            "/api/v1/loot/awards",
            request,
            LootAwardResponse::class.java
        )
        
        // Assert
        response.statusCode shouldBe HttpStatus.CREATED
        response.body.shouldNotBeNull()
        response.body!!.itemId shouldBe request.itemId
    }
    
    @Test
    fun `should return 404 when raider not found`() {
        // Arrange
        val request = AwardLootRequest(
            itemId = 12345,
            raiderId = "non-existent-raider",
            guildId = "test-guild"
        )
        
        // Act
        val response = testRestTemplate.postForEntity(
            "/api/v1/loot/awards",
            request,
            ErrorResponse::class.java
        )
        
        // Assert
        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }
}
```

## Continuous Integration

### Pre-commit Checks

```bash
# Run before committing
./gradlew test jacocoTestCoverageVerification ktlintCheck detekt
```

### CI Pipeline

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: ./gradlew test
  
- name: Check Coverage
  run: ./gradlew jacocoTestCoverageVerification
  
- name: Code Quality
  run: ./gradlew ktlintCheck detekt
```

## Resources

- [MockK Documentation](https://mockk.io/)
- [Kotest Matchers](https://kotest.io/docs/assertions/matchers.html)
- [Testcontainers](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Test-Driven Development by Example](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)

## Questions?

If you have questions about testing standards, ask in the team channel or review existing tests in the codebase for examples.
