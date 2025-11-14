# GraphQL + TDD Refactoring Spec

## Overview

This spec defines a comprehensive refactoring of the EdgeRush LootMan codebase in two phases:

**Phase 1 (Current Priority):** Establish test-driven development (TDD) standards and reorganize the project structure using domain-driven design (DDD) principles.

**Phase 2 (Future):** Implement GraphQL APIs alongside existing REST endpoints.

## Status

- **Requirements:** ✅ Complete
- **Design:** ✅ Complete  
- **Tasks:** ✅ Complete (32 tasks, all required)
- **Implementation:** ⏳ Not Started

## Goals

### Phase 1: TDD Standards & Project Refactoring

1. **Establish TDD Standards**
   - Configure testing infrastructure (JUnit 5, MockK, Testcontainers, JaCoCo)
   - Document testing standards and best practices
   - Achieve 85% code coverage minimum
   - Implement code quality tools (ktlint, detekt)

2. **Reorganize Project Structure**
   - Apply domain-driven design principles
   - Organize code into bounded contexts (FLPS, Loot, Attendance, Raids, etc.)
   - Separate concerns: API → Application → Domain → Infrastructure
   - Create clear package structure with documentation

3. **Refactor Codebase**
   - Migrate existing code to new architecture
   - One bounded context at a time (13-week phased approach)
   - Maintain backward compatibility with existing REST APIs
   - Improve code quality, testability, and maintainability

### Phase 2: GraphQL Implementation (Future)

- Implement GraphQL schema for all entities
- Create resolvers with DataLoader for efficient queries
- Add GraphQL subscriptions for real-time updates
- Maintain coexistence with REST APIs

## Key Architectural Decisions

### Domain-Driven Design

The codebase will be organized into bounded contexts:

- **FLPS:** Score calculation and modifiers
- **Loot:** Loot distribution and bans
- **Attendance:** Attendance tracking and reporting
- **Raids:** Raid scheduling and management
- **Applications:** Guild application management
- **Integrations:** External API integrations (WoWAudit, Warcraft Logs, Raidbots)
- **Shared:** Common entities (Raider, Guild)

### Layered Architecture

Each bounded context follows a layered architecture:

```
API Layer (REST/GraphQL)
    ↓
Application Layer (Use Cases)
    ↓
Domain Layer (Business Logic)
    ↓
Infrastructure Layer (Database, External APIs)
```

### Testing Strategy

Test pyramid approach:
- **70% Unit Tests:** Domain and application logic
- **25% Integration Tests:** API endpoints and database
- **5% E2E Tests:** Full user scenarios

## Implementation Timeline

### Phase 1: 13 Weeks

- **Weeks 1-2:** Foundation (testing infrastructure, documentation, package structure)
- **Weeks 3-4:** FLPS bounded context
- **Weeks 5-6:** Loot bounded context
- **Weeks 7-8:** Attendance bounded context
- **Weeks 9-10:** Raids bounded context
- **Weeks 11-12:** Remaining bounded contexts (Applications, Integrations, Shared)
- **Week 13:** Cleanup, optimization, final verification

### Phase 2: TBD

GraphQL implementation will be planned after Phase 1 completion.

## Success Criteria

### Per-Phase Criteria

- ✅ All tests pass (unit + integration)
- ✅ Code coverage ≥ 85%
- ✅ No ktlint or detekt violations
- ✅ All existing REST endpoints still functional
- ✅ Performance benchmarks met

### Overall Success Criteria

- ✅ Complete test suite with 85%+ coverage
- ✅ All bounded contexts refactored
- ✅ Documentation complete and up-to-date
- ✅ Zero regression in existing functionality
- ✅ Performance equal or better than before
- ✅ Team trained on new architecture

## Getting Started

To begin implementation:

1. Open `tasks.md` in this directory
2. Click "Start task" next to Task 1
3. Follow the TDD workflow: Write tests first, then implementation
4. Verify tests pass and coverage meets standards before moving to next task

## Documentation

- **requirements.md:** Detailed requirements with EARS patterns
- **design.md:** Architecture, patterns, and migration strategy
- **tasks.md:** 32 implementation tasks organized by phase

## Related Specs

- **rest-api-layer:** Existing REST API implementation (partially complete)
- **raidbots-integration:** Raidbots API integration (40% complete)
- **warcraft-logs-integration:** Warcraft Logs integration (100% complete)
- **web-dashboard:** Frontend dashboard (planned)
- **discord-bot:** Discord bot integration (planned)

## Questions?

Refer to the design document for detailed patterns and examples, or consult the testing/code standards documentation once created.
