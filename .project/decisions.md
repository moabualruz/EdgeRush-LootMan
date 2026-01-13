# Decision Log

## Architecture Choices

| Decision | Alternatives Considered | Rationale |
|----------|------------------------|-----------|
| Kotlin + Spring Boot | Java, Node.js, Go | Kotlin conciseness, Spring ecosystem maturity, team familiarity |
| Domain-Driven Design | Layered architecture | Better organization of complex business logic, clear bounded contexts |
| Spring Data JDBC | JPA/Hibernate, raw JDBC | Simpler than JPA, type-safe, good aggregate support |
| PostgreSQL | MySQL, MongoDB | Complex queries, JSONB support, reliability |
| Flyway migrations | Liquibase, manual SQL | Simple, version-controlled, widely supported |
| REST-first API | GraphQL-first | Faster MVP, GraphQL as Phase 2 enhancement |
| WoWAudit as primary data source | Direct Blizzard API | Already aggregates multiple sources, guild already uses it |

## Rejected Approaches

| Approach | Reason for Rejection |
|----------|---------------------|
| Direct Blizzard API integration | WoWAudit already provides aggregated data with better UX |
| Real-time FLPS calculation | Unnecessary complexity; batch calculation sufficient |
| MongoDB for data storage | Relational queries needed for FLPS calculations |
| Microservices architecture | Over-engineering for current scale; monolith simpler |
| Custom authentication | Spring Security sufficient; JWT standard |
| Manual test runners | JUnit 5 + Testcontainers industry standard |

## Tradeoffs Accepted

| Tradeoff | Benefit | Cost |
|----------|---------|------|
| Wishlist % as upgrade proxy | Works without Raidbots API | Less accurate than simulation |
| REST before GraphQL | Faster MVP delivery | Need to maintain two APIs |
| Monolith architecture | Simpler deployment, debugging | Future scaling challenges |
| Spring JDBC over JPA | Simpler, more control | More manual mapping code |
| Scheduled sync (6h) | Reduces API load | Data slightly stale |

## Future Considerations

- **GraphQL Federation**: If multiple services emerge, consider Apollo Federation
- **Event Sourcing**: May benefit audit trail if complexity grows
- **Redis Caching**: Consider for high-traffic production deployment
- **Kubernetes**: For horizontal scaling when user base grows
- **Feature Flags**: For gradual rollout of new features
- **Multi-Region**: If guild support expands geographically
