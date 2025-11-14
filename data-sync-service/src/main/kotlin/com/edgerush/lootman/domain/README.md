# Domain Layer

This layer contains the core business logic and domain models. It has no dependencies on other layers and is framework-agnostic.

## Structure

```
domain/
├── flps/              # FLPS bounded context
│   ├── model/         # Domain models (entities, value objects, aggregates)
│   ├── service/       # Domain services
│   └── repository/    # Repository interfaces
├── loot/              # Loot bounded context
│   ├── model/
│   ├── service/
│   └── repository/
├── attendance/        # Attendance bounded context
│   ├── model/
│   ├── service/
│   └── repository/
├── raids/             # Raids bounded context
│   ├── model/
│   ├── service/
│   └── repository/
└── shared/            # Shared domain concepts
    ├── model/
    └── events/
```

## Principles

- **Pure Business Logic**: No framework dependencies
- **Rich Domain Models**: Entities contain behavior, not just data
- **Ubiquitous Language**: Use domain terminology consistently
- **Bounded Contexts**: Clear boundaries between different areas
- **Repository Interfaces**: Define data access contracts (implementations in infrastructure layer)

## Guidelines

- Value Objects are immutable and validate in constructor
- Entities have identity and lifecycle methods
- Aggregates enforce consistency boundaries
- Domain Services contain logic that doesn't fit in entities
- Repository interfaces use domain types, not database types
