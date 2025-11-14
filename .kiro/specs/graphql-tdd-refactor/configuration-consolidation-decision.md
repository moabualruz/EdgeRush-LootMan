# Configuration and Client Code Consolidation Decision

## Decision: KEEP IN DATASYNC PACKAGE ✅

After thorough analysis, the configuration, client, and security code should **remain in the `datasync` package**.

## Rationale

### Package Structure Philosophy

The current structure follows **Domain-Driven Design** principles:

```
com.edgerush/
├── datasync/              # APPLICATION ROOT (System-wide concerns)
│   ├── api/              # REST API layer
│   ├── application/      # Use cases (orchestration)
│   ├── client/           # External API clients (WoWAudit, WarcraftLogs, Raidbots)
│   ├── config/           # Application-wide configuration
│   ├── domain/           # Domain models and business logic
│   ├── infrastructure/   # Technical implementations
│   ├── model/            # Shared models
│   └── security/         # Application-wide security
│
└── lootman/              # BOUNDED CONTEXT (Loot-specific concerns)
    ├── api/              # Loot/Attendance/FLPS REST endpoints
    ├── application/      # Loot/Attendance/FLPS use cases
    ├── domain/           # Loot/Attendance/FLPS domain models
    └── infrastructure/   # Loot/Attendance/FLPS persistence
```

### Why Configuration Stays in Datasync

**Configuration files are APPLICATION-WIDE concerns:**

1. **FlpsConfigProperties** - Used across multiple bounded contexts
2. **OpenApiConfig** - Applies to entire API surface
3. **RateLimitConfig** - System-wide rate limiting
4. **SyncProperties** - Integration configuration for all external APIs
5. **WebClientConfig** - HTTP client configuration for all external calls
6. **WoWAuditProperties** - External API configuration
7. **Raidbots/WarcraftLogs config** - External integration settings

These configurations are **not specific to the loot bounded context** - they apply to the entire application.

### Why Client Code Stays in Datasync

**Client implementations are INFRASTRUCTURE concerns:**

1. **WoWAuditClient** - Used by multiple bounded contexts (raids, attendance, applications, etc.)
2. **WarcraftLogsClient** - Used for performance data across contexts
3. **RaidbotsClient** - Used for simulation data

These clients are **shared infrastructure** that multiple bounded contexts depend on. They belong at the application root, not within a specific bounded context.

### Why Security Stays in Datasync

**Security is an APPLICATION-WIDE concern:**

1. **SecurityConfig** - Applies to all endpoints
2. **JwtAuthenticationFilter** - Filters all requests
3. **JwtService** - Used across all controllers
4. **AdminModeConfig** - System-wide security mode
5. **AuthenticatedUser** - Used throughout the application

Security is a **cross-cutting concern** that applies to the entire application, not just one bounded context.

## DDD Bounded Context Pattern

This structure follows the **Bounded Context** pattern from Domain-Driven Design:

- **Application Root** (`datasync`): Contains system-wide concerns and shared infrastructure
- **Bounded Context** (`lootman`): Contains domain-specific logic for loot distribution, attendance, and FLPS

### Analogy

Think of it like a building:
- `datasync` = The building's foundation, utilities, and shared facilities (electricity, plumbing, security)
- `lootman` = A specific department/floor in the building with its own specialized equipment

You wouldn't move the building's electrical system into one department - it serves the whole building.

## Alternative Considered: Move to Lootman

**Why this was rejected:**

1. **Scope Mismatch**: Configuration and clients serve multiple bounded contexts, not just loot
2. **Coupling**: Would create artificial coupling between lootman and other contexts
3. **Naming Confusion**: "lootman" implies loot-specific, but these are system-wide
4. **Maintenance**: Would require moving code back out when adding new bounded contexts

## Conclusion

**Status**: ✅ No changes needed

**Action**: Keep configuration, client, and security code in `datasync` package

**Reasoning**: These are application-wide concerns that serve multiple bounded contexts and belong at the application root, not within a specific bounded context.

This is a **correct and intentional** DDD structure, not technical debt.
