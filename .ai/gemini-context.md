# AI Context for Gemini

## EdgeRush LootMan Project Overview

You are working on **EdgeRush LootMan**, a sophisticated World of Warcraft guild loot distribution system that implements the FLPS (Final Loot Priority Score) algorithm for fair and automated loot decisions.

## Core Business Logic

### FLPS Algorithm Formula
```
FLPS = (RMS × IPI) × RDF
```

**Components:**
1. **RMS (Raider Merit Score)**: Player contribution metric (0.0-1.0)
   - Attendance tracking (40%)
   - Performance metrics (40%) 
   - Preparation assessment (20%)

2. **IPI (Item Priority Index)**: Item value assessment (0.0-1.0)
   - Upgrade simulation value (45%)
   - Tier set completion impact (35%)
   - Role-specific multipliers (20%)

3. **RDF (Recency Decay Factor)**: Anti-spam mechanism (0.0-1.0)
   - Exponential decay based on recent loot awards
   - Prevents single player from dominating loot distribution

## Technical Architecture

### Technology Stack
- **Primary Language**: Kotlin (JDK 24)
- **Framework**: Spring Boot 3.x with WebFlux
- **Database**: PostgreSQL 18 with Flyway migrations
- **Build System**: Gradle 8.10.1
- **Containerization**: Docker Compose
- **External Integration**: WoWAudit API for guild data

### Key Design Patterns
1. **Reactive Programming**: Kotlin Coroutines for async operations
2. **Error Handling**: Result<T> types for explicit error management
3. **Circuit Breaker**: Resilience patterns for external API calls
4. **Repository Pattern**: Clean separation of data access
5. **Dependency Injection**: Spring-managed components

### Code Organization
```
src/main/kotlin/com/edgerush/datasync/
├── api/           # External API models and deserializers
├── client/        # HTTP clients with retry mechanisms  
├── config/        # Spring configuration and properties
├── domain/        # Core business entities (JPA)
├── repository/    # Data access interfaces
├── service/       # Business logic implementation
└── schema/        # Data validation and transformation
```

## Development Standards

### Kotlin Conventions
- Use data classes for DTOs and value objects
- Leverage sealed classes for type-safe state management
- Implement suspend functions for all I/O operations
- Apply extension functions for utility operations
- Use companion objects for constants and factory methods

### Error Handling Strategy
```kotlin
// Preferred pattern for operations that can fail
suspend fun performOperation(): Result<SuccessType> = runCatching {
    // Implementation
}.onFailure { exception ->
    logger.error("Operation failed", exception)
    metrics.incrementCounter("operation_failures")
}
```

### Testing Requirements
- **Unit Tests**: All business logic methods with edge cases
- **Integration Tests**: Repository and external API interactions
- **Contract Tests**: API response validation
- **Performance Tests**: FLPS calculation benchmarks
- **Coverage Target**: Minimum 80% line coverage

### Database Patterns
- Use JPA entities with Kotlin data classes
- Implement custom repository methods for complex queries
- Create Flyway migrations for all schema changes
- Apply database constraints for data integrity
- Use connection pooling for performance

## Business Domain Knowledge

### World of Warcraft Context
- **Raids**: Group PvE content with item rewards
- **Loot Council**: Guild leadership deciding loot distribution
- **Item Level**: Numerical power indicator for equipment
- **Tier Sets**: Coordinated equipment pieces with set bonuses
- **Roles**: Tank, Healer, DPS (damage dealer) specializations

### Guild Management Concepts
- **Attendance**: Participation in scheduled raid events
- **Performance**: Individual contribution to raid success
- **Preparation**: Consumables, enchants, and optimization
- **Progression**: Guild advancement through raid content
- **Drama Prevention**: Fair, transparent loot decisions

## External Dependencies

### WoWAudit API Integration
- **Purpose**: Retrieve guild roster and progression data
- **Authentication**: API key-based access
- **Rate Limiting**: Respectful request patterns required
- **Data Freshness**: Periodic synchronization strategy
- **Error Recovery**: Graceful degradation when API unavailable

### Key Endpoints
- Character roster with equipment details
- Attendance tracking and raid logs
- Loot history and distribution records
- Team composition and role assignments

## Development Workflow

### Local Development Setup
1. Start PostgreSQL and services: `docker-compose up -d`
2. Verify application health: `curl http://localhost/api/actuator/health`
3. Run test suite: `./gradlew test`
4. Build application: `./gradlew build`

### Code Quality Gates
- All tests must pass before deployment
- Static analysis tools must report clean results
- Performance benchmarks must meet established baselines
- Documentation must be updated for API changes
- Security scans must pass without critical vulnerabilities

## Troubleshooting Guidelines

### Common Issues
- **NaN Scores**: Check for division by zero in calculations
- **API Timeouts**: Verify WoWAudit service availability
- **Database Locks**: Review transaction boundaries
- **Memory Usage**: Monitor object allocation in hot paths

### Debugging Tools
- Application metrics via `/actuator/metrics`
- Health checks via `/actuator/health`
- Database query logs via container inspection
- JVM profiling for performance analysis

This context provides comprehensive understanding of the EdgeRush LootMan project for effective AI assistance with development tasks.