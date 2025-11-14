# Infrastructure Layer

This layer contains technical implementations and framework-specific code. It depends on both domain and application layers.

## Structure

```
infrastructure/
├── persistence/       # Database implementations
│   ├── entity/        # JPA/JDBC entities
│   ├── repository/    # Repository implementations
│   └── mapper/        # Entity ↔ Domain mappers
├── external/          # External API clients
│   ├── wowaudit/
│   ├── warcraftlogs/
│   └── raidbots/
└── config/            # Configuration
    ├── DatabaseConfig.kt
    ├── SecurityConfig.kt
    └── WebClientConfig.kt
```

## Principles

- **Implements Domain Interfaces**: Repository implementations live here
- **Framework-Specific**: Spring, JDBC, HTTP clients, etc.
- **Adapters**: Adapt external systems to domain interfaces
- **Mappers**: Convert between persistence and domain models
- **Configuration**: All framework configuration

## Guidelines

- Repository implementations use domain types in signatures
- Mappers handle conversion between layers
- External API clients implement domain interfaces
- Configuration classes are framework-specific
- No business logic in this layer
