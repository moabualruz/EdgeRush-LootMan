# ADR-003: Bounded Context Boundaries

## Status
Accepted

## Date
2025-01-14

## Context
As the EdgeRush LootMan system grew, we needed to establish clear boundaries between different functional areas. Without explicit boundaries, we risked:
- Tight coupling between unrelated features
- Unclear ownership of functionality
- Difficulty understanding system scope
- Challenges in parallel development
- Inconsistent modeling of business concepts

We needed to identify and define bounded contexts that reflect the business domains and enable independent development and evolution.

## Decision
We will organize the system into seven bounded contexts with explicit boundaries and responsibilities:

### 1. FLPS (Final Loot Priority Score)
**Responsibility**: Calculate and manage FLPS scores and modifiers

**Core Concepts**:
- FlpsScore (Value Object)
- RaiderMeritScore, ItemPriorityIndex, RecencyDecayFactor (Value Objects)
- FlpsModifiers (Entity)
- FlpsCalculationService (Domain Service)

**Boundaries**:
- Owns FLPS algorithm and calculation logic
- Manages guild-specific modifiers and weights
- Provides score calculations to Loot context
- Does NOT manage loot distribution decisions

### 2. Loot
**Responsibility**: Manage loot distribution, awards, and bans

**Core Concepts**:
- LootAward (Aggregate Root)
- LootBan (Entity)
- LootTier (Value Object)
- LootDistributionService (Domain Service)

**Boundaries**:
- Owns loot award records and history
- Manages loot bans and restrictions
- Uses FLPS scores but doesn't calculate them
- Does NOT manage raid scheduling or attendance

### 3. Attendance
**Responsibility**: Track and report raid attendance

**Core Concepts**:
- AttendanceRecord (Entity)
- AttendanceStats (Value Object)
- AttendanceCalculationService (Domain Service)

**Boundaries**:
- Owns attendance tracking and statistics
- Provides attendance data to FLPS context
- Does NOT manage raid scheduling or loot distribution

### 4. Raids
**Responsibility**: Schedule and manage raids, signups, and encounters

**Core Concepts**:
- Raid (Aggregate Root)
- RaidSignup, RaidEncounter (Entities)
- RaidSchedulingService (Domain Service)

**Boundaries**:
- Owns raid lifecycle (scheduled → in progress → completed)
- Manages raid signups and roster
- Tracks raid encounters and results
- Does NOT manage loot distribution or attendance calculation

### 5. Applications
**Responsibility**: Process guild applications and reviews

**Core Concepts**:
- Application (Aggregate Root)
- ApplicationQuestion, ApplicationFile (Entities)
- ApplicationReviewService (Domain Service)

**Boundaries**:
- Owns application submission and review workflow
- Manages application questions and responses
- Does NOT manage guild roster or raider data

### 6. Integrations
**Responsibility**: Synchronize data from external APIs

**Core Concepts**:
- SyncOperation (Entity)
- SyncResult (Value Object)
- ExternalDataSource (Enum)
- SyncOrchestrationService (Domain Service)

**Boundaries**:
- Owns sync operations and status tracking
- Manages WoWAudit, Warcraft Logs, Raidbots integrations
- Provides data to other contexts
- Does NOT own the synced data (owned by respective contexts)

### 7. Shared
**Responsibility**: Provide cross-cutting domain concepts

**Core Concepts**:
- Raider (Entity)
- Guild (Entity)
- RaiderId, GuildId, ItemId (Value Objects)

**Boundaries**:
- Provides shared entities used across contexts
- Manages core guild and raider information
- Does NOT contain business logic specific to other contexts

## Context Relationships

```
┌─────────────┐
│   Shared    │ ← Used by all contexts
└─────────────┘

┌─────────────┐     ┌─────────────┐
│    FLPS     │ ←── │    Loot     │
└─────────────┘     └─────────────┘
      ↑                     ↑
      │                     │
┌─────────────┐     ┌─────────────┐
│ Attendance  │     │    Raids    │
└─────────────┘     └─────────────┘

┌─────────────┐     ┌─────────────┐
│Applications │     │Integrations │
└─────────────┘     └─────────────┘
```

## Communication Between Contexts

### Direct Dependencies (Allowed)
- Loot → FLPS: Get FLPS scores for loot decisions
- FLPS → Attendance: Get attendance data for score calculation
- All → Shared: Access Raider and Guild entities

### Anti-Corruption Layer
- Integrations context provides data through well-defined interfaces
- Each context transforms external data to its own domain model
- No direct database access across context boundaries

### Shared Kernel
- Shared context provides common value objects (RaiderId, GuildId, ItemId)
- Shared entities (Raider, Guild) are used but not modified by other contexts

## Consequences

### Positive
- **Clear Ownership**: Each context has a clear owner and responsibility
- **Independent Evolution**: Contexts can evolve independently
- **Parallel Development**: Teams can work on different contexts simultaneously
- **Reduced Coupling**: Changes in one context have minimal impact on others
- **Focused Testing**: Tests can focus on single context boundaries
- **Scalability**: System can scale by context based on load

### Negative
- **Duplication**: Some concepts may be duplicated across contexts
- **Coordination Overhead**: Changes affecting multiple contexts require coordination
- **Learning Curve**: Developers need to understand context boundaries
- **Potential Over-Separation**: Very small contexts may feel over-engineered

### Neutral
- **Context Mapping**: Relationships between contexts must be explicitly defined
- **Shared Kernel**: Shared context requires careful management to avoid coupling
- **Integration Points**: Clear interfaces needed between contexts

## Implementation Notes
- Each context has its own package structure (api, application, domain, infrastructure)
- Repository interfaces are defined within each context's domain layer
- Use cases orchestrate operations within a single context
- Cross-context operations use application layer services
- Shared context is read-only for most contexts

## Future Considerations
- **Domain Events**: Consider event-driven communication between contexts
- **CQRS**: Separate read and write models if query complexity increases
- **Microservices**: Contexts could be extracted to separate services if needed
- **API Gateway**: GraphQL layer could provide unified API across contexts

## References
- [Bounded Context Pattern](https://martinfowler.com/bliki/BoundedContext.html)
- [Context Mapping](https://www.infoq.com/articles/ddd-contextmapping/)
- [Strategic Domain-Driven Design](https://vaadin.com/blog/ddd-part-1-strategic-domain-driven-design)
