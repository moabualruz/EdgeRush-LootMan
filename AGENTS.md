# AI Agents Documentation

## Overview

This document provides AI agents with essential context and guidelines for working effectively with the EdgeRush LootMan codebase.

## Quick Start for AI Agents

### 1. Essential Reading Order

1. **[AI_AGENT_GUIDE.md](./AI_AGENT_GUIDE.md)** - Start here for project context
2. **[CODE_ARCHITECTURE.md](./CODE_ARCHITECTURE.md)** - Technical architecture overview  
3. **[AI_DEVELOPMENT_STANDARDS.md](./AI_DEVELOPMENT_STANDARDS.md)** - Coding conventions
4. **[API_REFERENCE.md](./API_REFERENCE.md)** - Interface documentation
5. **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** - Common tasks and troubleshooting

### 2. Project Context

**EdgeRush LootMan** is a World of Warcraft guild loot distribution system implementing the FLPS (Final Loot Priority Score) algorithm for fair and transparent loot decisions.

**Core Algorithm**: FLPS = (RMS × IPI) × RDF

- **RMS**: Raider Merit Score (attendance + performance + preparation)
- **IPI**: Item Priority Index (upgrade value + tier impact + role multiplier)  
- **RDF**: Recency Decay Factor (reduces score for recent loot recipients)

### 3. Technology Stack

- **Language**: Kotlin with Coroutines
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 18 with Flyway migrations
- **Build Tool**: Gradle 8.10.1 with JDK 24
- **Containerization**: Docker Compose
- **External API**: WoWAudit for guild data

### 4. Key Directories

```
data-sync-service/src/main/kotlin/com/edgerush/datasync/
├── api/           # External API integrations
├── client/        # HTTP clients (WoWAudit)
├── config/        # Configuration classes
├── domain/        # Business entities
├── repository/    # Data access layer
├── service/       # Business logic (FLPS algorithm)
└── schema/        # Data validation
```

## Development Guidelines

### Code Style

- Follow Kotlin idioms and coroutines best practices
- Use `Result<T>` for error handling
- Implement comprehensive testing (unit + integration)
- Document business logic with clear comments
- Use Spring dependency injection

### Business Rules

- FLPS scores must be normalized to 0.0-1.0 range
- All external API calls require proper error handling and retries
- Character data synchronization is critical for accurate scoring
- Database migrations must be backwards compatible

### Testing Standards

- Unit tests for all business logic methods
- Integration tests for repository and API layers
- End-to-end tests for critical FLPS workflows
- Minimum 80% code coverage target
- Use TestContainers for database integration tests

### API Integration Patterns

```kotlin
// Example pattern for external API calls
@Retryable(value = [ConnectException::class], maxAttempts = 3)
suspend fun fetchData(): Result<DataType> = runCatching {
    webClient.get()
        .uri("/endpoint")
        .retrieve()
        .awaitBody<ResponseType>()
}.onFailure { exception ->
    logger.error("API call failed", exception)
    metrics.incrementCounter("api_errors")
}
```

### Error Handling

```kotlin
// Preferred error handling pattern
when (val result = service.performOperation()) {
    is Success -> handleSuccess(result.value)
    is Failure -> {
        logger.warn("Operation failed: ${result.exception.message}")
        handleFailure(result.exception)
    }
}
```

## Common Development Tasks

### Adding New FLPS Component

1. Update `ScoreCalculator.kt` with new calculation logic
2. Add corresponding tests in `ScoreCalculatorTest.kt`
3. Update algorithm documentation in `docs/score-model.md`
4. Verify scores remain in 0.0-1.0 range

### Integrating New External API

1. Create client class in `client/` package
2. Add configuration properties to `SyncProperties.kt`
3. Implement retry and circuit breaker patterns
4. Add comprehensive error handling and logging
5. Create integration tests with MockWebServer

### Adding New Entity

1. Create entity class in `domain/` package
2. Create repository interface in `repository/` package
3. Add Flyway migration in `src/main/resources/db/migration/`
4. Update sync service to handle new data type
5. Add validation and transformation logic

## Troubleshooting

### Common Issues

| Issue | Diagnosis | Solution |
|-------|-----------|----------|
| API 401/403 | Check WoWAudit API key | Verify configuration in `application.yaml` |
| NaN in FLPS | Division by zero in calculations | Add input validation in score components |
| Database connection | Docker service issues | Check `docker-compose logs postgres` |
| Build failures | JDK version mismatch | Ensure JDK 24 is configured |

### Health Checks

```bash
# Application health
curl http://localhost/api/actuator/health

# Service logs
docker-compose logs data-sync --tail=50

# Database connectivity
docker exec -it looter-postgres-1 psql -U lootman -d lootman_db
```

## Success Criteria

Before considering work complete:

- [ ] All tests pass: `./gradlew test`
- [ ] Health checks return green: `/actuator/health`
- [ ] No build warnings or errors
- [ ] Code follows established patterns
- [ ] Relevant documentation updated
- [ ] FLPS calculations produce valid scores (0.0-1.0)

## AI Agent Best Practices

1. **Understand Business Context**: Read domain documentation before making changes
2. **Follow Existing Patterns**: Study similar implementations in the codebase
3. **Test Thoroughly**: Implement both unit and integration tests
4. **Handle Errors Gracefully**: Use Result types and proper logging
5. **Document Changes**: Update relevant documentation files
6. **Validate Assumptions**: Check with stakeholders for business rule changes

## Additional Resources

- **WoW Game Knowledge**: Understanding of raids, loot, and guild dynamics helpful
- **Spring Boot Documentation**: For framework-specific patterns
- **Kotlin Coroutines Guide**: For async programming best practices
- **PostgreSQL Documentation**: For database-related development

---

*This document provides AI agents with the essential context and guidelines needed to contribute effectively to EdgeRush LootMan.*