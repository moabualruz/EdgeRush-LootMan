# Technical Constraints

## Architectural Style

**Domain-Driven Design (DDD)** with bounded contexts:
- FLPS (score calculation)
- Loot (loot awards, drops)
- Attendance (raid attendance tracking)
- Raids (raid management, encounters)
- Applications (guild applications)
- Integrations (external API integrations)
- Shared (common models, utilities)

## Allowed Languages

| Component | Language | Version |
|-----------|----------|---------|
| Backend | Kotlin | 1.9+ |
| Build | Gradle Kotlin DSL | 8.10+ |
| Database | PostgreSQL | 15+ |
| Migrations | SQL (Flyway) | - |
| Frontend (future) | TypeScript | 5+ |
| Discord Bot (future) | Kotlin | 1.9+ |

## Technology Stack

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Framework | Spring Boot 3.x | Production-ready, excellent ecosystem |
| Web | Spring WebMVC | Simpler than WebFlux for current needs |
| Data Access | Spring Data JDBC | Clean SQL, aggregate support |
| Database | PostgreSQL 15+ | Complex queries, JSONB, reliability |
| Migrations | Flyway | Version-controlled schema changes |
| Testing | JUnit 5 + MockK + Testcontainers | Modern testing stack |
| API Docs | OpenAPI/Swagger | Industry standard |
| Security | Spring Security + JWT | Mature, comprehensive |
| HTTP Client | WebClient | Non-blocking, modern |
| Build | Gradle 8.10+ | Kotlin DSL, fast builds |
| Container | Docker | JDK 21 base image |
| Proxy | Nginx | SSL termination, routing |

## External Dependencies

| Service | Purpose | Criticality |
|---------|---------|-------------|
| WoWAudit API | Character, attendance, loot data | Critical |
| Warcraft Logs API | Combat performance data (MAS) | Important |
| Raidbots API | Simulation/upgrade values | Nice-to-have (blocked) |
| PostgreSQL | Primary data store | Critical |
| Discord API (future) | Bot notifications | Important |

## Compliance Requirements

### Data Handling
- No personal data stored (only WoW character data)
- API keys stored in environment variables, not code
- Audit trail for all loot decisions
- GDPR not applicable (no EU personal data)

### Security
- All endpoints authenticated (except health checks)
- JWT tokens for API access
- HTTPS required in production
- Rate limiting configured (enable for production)

### Quality
- Minimum 85% test coverage target
- All tests must pass before merge
- Code review required for main branch
- ktlint and detekt for code quality
