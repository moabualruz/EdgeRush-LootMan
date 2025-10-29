# GitHub Copilot Context

## Project: EdgeRush LootMan
**World of Warcraft Guild Loot Distribution System**

### Core Algorithm: FLPS (Final Loot Priority Score)
```
FLPS = (RMS × IPI) × RDF
```

- **RMS**: Raider Merit Score (0.0-1.0)
  - Attendance: 40% weight
  - Performance: 40% weight  
  - Preparation: 20% weight

- **IPI**: Item Priority Index (0.0-1.0)
  - Upgrade Value: 45% weight
  - Tier Set Impact: 35% weight
  - Role Multiplier: 20% weight

- **RDF**: Recency Decay Factor (0.0-1.0)
  - Reduces score for recent loot recipients
  - Time-based exponential decay

### Technology Stack
- **Language**: Kotlin with Coroutines
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 18
- **Build**: Gradle 8.10.1 + JDK 24
- **Deployment**: Docker Compose
- **External API**: WoWAudit guild data integration

### Key Patterns
1. **Error Handling**: Use `Result<T>` for all operations that can fail
2. **Async Operations**: Leverage Kotlin coroutines with `suspend` functions
3. **Testing**: Unit tests + integration tests with TestContainers
4. **API Integration**: WebClient with retry mechanisms and circuit breakers
5. **Data Persistence**: JPA entities with Flyway migrations

### Critical Business Rules
- FLPS scores must always be between 0.0 and 1.0
- Character data synchronization is essential for accurate calculations
- All external API calls must include proper error handling and retries
- Database operations should be transactional for data consistency

### File Structure Context
```
data-sync-service/src/main/kotlin/com/edgerush/datasync/
├── api/           # External API integrations (WoWAudit)
├── client/        # HTTP clients with retry logic
├── config/        # Spring configuration classes
├── domain/        # JPA entities and business models
├── repository/    # Data access layer (Spring Data JPA)
├── service/       # Business logic (FLPS calculations)
└── schema/        # API response validation schemas
```

### Common Development Tasks
- **Score Algorithm Updates**: Modify `service/ScoreCalculator.kt`
- **API Integration**: Extend `client/WoWAuditClient.kt`
- **Data Model Changes**: Update `domain/` entities + add migrations
- **Business Logic**: Implement in `service/` package with proper testing

### Quality Requirements
- All public methods must have KDoc documentation
- Business logic requires comprehensive unit tests
- External API integrations need integration tests
- Database changes require Flyway migration scripts
- Error scenarios must be explicitly handled

### Development Commands
```bash
# Start services
docker-compose up -d

# Run tests
./gradlew test

# Build application
./gradlew build

# Health check
curl http://localhost/api/actuator/health
```

This context helps GitHub Copilot understand the domain, architecture, and coding standards for the EdgeRush LootMan project.