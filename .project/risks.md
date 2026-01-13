# Risk Register

## Unacceptable Failures

1. **Incorrect FLPS calculations** - Would undermine trust in the system
2. **Data loss** - Loot history must be preserved for audit
3. **Unauthorized access** - Guild data must remain private
4. **System unavailable during raids** - Must work when needed most
5. **Silent failures** - All errors must be logged and visible

## Risk Assessment

| ID | Risk | Probability | Impact | Mitigation |
|----|------|-------------|--------|------------|
| R-001 | WoWAudit API becomes unavailable | Low | High | Implement data caching, graceful degradation |
| R-002 | Warcraft Logs API changes | Medium | Medium | Version-pin API, monitor changelogs |
| R-003 | Raidbots API never accessible | High | Medium | Use wishlist percentages as fallback (current) |
| R-004 | Test coverage below target | Medium | Medium | Dedicated test coverage sprint (current priority) |
| R-005 | Database corruption | Low | Critical | Regular backups, Flyway migrations |
| R-006 | Security vulnerabilities | Medium | High | Spring Security, dependency scanning |
| R-007 | Performance degradation at scale | Low | Medium | Load testing, query optimization |
| R-008 | Team member unavailability | Medium | Medium | Documentation, knowledge sharing |
| R-009 | WoW expansion changes data models | Medium | Medium | Abstraction layers, configurable mappings |
| R-010 | Docker/infra issues | Low | Medium | Documented setup, container health checks |

## Assumptions

1. WoWAudit will continue providing API access for guild data
2. Warcraft Logs GraphQL API remains stable
3. Guild will continue using WoWAudit for raid management
4. PostgreSQL is sufficient for data storage needs
5. Spring Boot 3.x will receive long-term support
6. Kotlin remains a viable JVM language choice
7. JWT remains acceptable for API authentication

## Dependencies

| Dependency | Risk | Contingency |
|------------|------|-------------|
| WoWAudit API | Service discontinuation | Cache data locally, build direct Blizzard integration |
| Warcraft Logs API | API changes/deprecation | Pin version, implement adapter pattern |
| Raidbots API | Access denied | Use wishlist percentages (current fallback) |
| PostgreSQL | Database failure | Regular backups, point-in-time recovery |
| Spring Boot | Security vulnerabilities | Monitor advisories, rapid patching |
| Docker | Container runtime issues | Documented manual deployment fallback |
| Gradle | Build system issues | Version lock, reproducible builds |
