# Claude/Sonnet AI Context

## Project: EdgeRush LootMan
*World of Warcraft Guild Loot Distribution System with FLPS Algorithm*

### Executive Summary
You are working on a Kotlin-based Spring Boot application that automates fair loot distribution for World of Warcraft guilds using a sophisticated scoring algorithm called FLPS (Final Loot Priority Score).

### Core Algorithm Understanding
**FLPS = (RMS × IPI) × RDF**

This formula balances three key factors:
- **Raider Merit Score (RMS)**: How deserving is the player?
- **Item Priority Index (IPI)**: How valuable is this item to the player?
- **Recency Decay Factor (RDF)**: Has the player received loot recently?

### Technical Context
- **Language**: Kotlin with coroutines for async programming
- **Framework**: Spring Boot 3.x with reactive patterns
- **Database**: PostgreSQL 18 with Flyway for schema management
- **External API**: WoWAudit integration for guild data synchronization
- **Deployment**: Docker Compose with custom JDK 24 container
- **Build**: Gradle 8.10.1 with Kotlin DSL

### Architecture Principles
1. **Reactive Programming**: Use suspend functions for I/O operations
2. **Error Handling**: Explicit Result<T> types instead of exceptions
3. **Clean Architecture**: Layered design with clear boundaries
4. **Domain-Driven Design**: Business logic isolated in service layer
5. **Test-Driven Development**: Comprehensive test coverage required

### Key Code Patterns

#### Error Handling
```kotlin
suspend fun operation(): Result<Data> = runCatching {
    // Implementation
}.onFailure { 
    logger.error("Operation failed", it)
    metrics.recordFailure()
}
```

#### API Integration
```kotlin
@Retryable(maxAttempts = 3)
suspend fun fetchExternalData(): Result<ApiResponse> = 
    webClient.get().uri("/endpoint").awaitExchange { 
        // Handle response
    }
```

#### Business Logic
```kotlin
class ScoreCalculator {
    suspend fun calculateFLPS(character: Character, item: Item): FLPSResult {
        val rms = calculateRaiderMeritScore(character)
        val ipi = calculateItemPriorityIndex(character, item)
        val rdf = calculateRecencyDecayFactor(character)
        return FLPSResult(rms * ipi * rdf)
    }
}
```

### Business Domain Context

#### World of Warcraft Knowledge
- **Guilds**: Groups of players who raid together
- **Loot Council**: Traditional manual loot distribution method
- **Item Level**: Gear power measurement (higher = better)
- **Tier Sets**: Equipment sets with special bonuses
- **Roles**: Tank (damage mitigation), Healer (health restoration), DPS (damage dealer)

#### Fairness Considerations
- Attendance should be rewarded but not punish occasional absences
- Performance matters but shouldn't create elitism
- Recent loot recipients should have lower priority temporarily
- Upgrades should be prioritized based on impact magnitude

### Development Guidelines

#### Code Quality Standards
- All public methods require KDoc documentation
- Business logic must have unit tests with edge cases
- External integrations need integration tests
- Performance-critical paths require benchmarks
- Database operations should be transaction-aware

#### File Organization Context
```
data-sync-service/src/main/kotlin/com/edgerush/datasync/
├── domain/        # Core entities (Character, LootHistory, etc.)
├── service/       # Business logic (ScoreCalculator, SyncService)
├── repository/    # Data access (Spring Data JPA)
├── client/        # External API clients (WoWAuditClient)
├── config/        # Spring configuration
└── api/           # External API models and serialization
```

#### Testing Strategy
- **Unit Tests**: Fast, isolated, comprehensive coverage
- **Integration Tests**: Database and external API interactions
- **Contract Tests**: API response validation and evolution
- **End-to-End Tests**: Critical user journeys through full stack

### Common Development Tasks

#### Algorithm Updates
1. Modify calculation logic in `service/ScoreCalculator.kt`
2. Update corresponding tests with new scenarios
3. Verify scores remain in valid 0.0-1.0 range
4. Document changes in algorithm specification

#### External API Changes
1. Update models in `api/wowaudit/` package
2. Modify client implementation in `client/WoWAuditClient.kt`
3. Add integration tests for new endpoints
4. Update error handling for new failure modes

#### Database Schema Evolution
1. Create Flyway migration in `resources/db/migration/`
2. Update JPA entities in `domain/` package
3. Modify repository interfaces if needed
4. Test migration with representative data

### Troubleshooting Context

#### Performance Considerations
- FLPS calculations run frequently during loot distribution
- Database queries should be optimized for character lookups
- External API calls need caching and circuit breakers
- Memory allocation should be monitored in hot paths

#### Common Issues
- **NaN in calculations**: Usually indicates division by zero
- **API timeouts**: Check WoWAudit service status and rate limits  
- **Database deadlocks**: Review transaction boundaries and query order
- **Inconsistent scores**: Verify input data completeness and validation

### Quality Assurance
- Health checks must pass: `/actuator/health`
- All tests must pass: `./gradlew test`
- No critical security vulnerabilities
- Performance benchmarks within acceptable ranges
- Documentation updated for significant changes

This context enables effective collaboration on the EdgeRush LootMan codebase with understanding of both technical implementation and business domain requirements.