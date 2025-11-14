# Application Layer

This layer contains use cases and application services that orchestrate domain logic. It depends on the domain layer but not on infrastructure.

## Structure

```
application/
├── flps/              # FLPS use cases
│   ├── CalculateFlpsScoreUseCase.kt
│   ├── UpdateModifiersUseCase.kt
│   └── GetFlpsReportUseCase.kt
├── loot/              # Loot use cases
│   ├── AwardLootUseCase.kt
│   ├── GetLootHistoryUseCase.kt
│   └── ManageLootBansUseCase.kt
├── attendance/        # Attendance use cases
│   ├── TrackAttendanceUseCase.kt
│   └── GetAttendanceReportUseCase.kt
├── raids/             # Raid use cases
│   ├── ScheduleRaidUseCase.kt
│   ├── ManageSignupsUseCase.kt
│   └── RecordRaidResultsUseCase.kt
└── common/            # Shared application services
    └── ValidationService.kt
```

## Principles

- **Use Case Per Operation**: One use case class per business operation
- **Transaction Boundaries**: Use cases define transaction boundaries
- **Orchestration**: Coordinate domain services and repositories
- **Result Types**: Return `Result<T>` for error handling
- **Domain Events**: Publish events for cross-context communication

## Guidelines

- Use cases should be thin orchestration layers
- Business logic belongs in the domain layer
- Use `@Transactional` for database operations
- Validate inputs early
- Return domain objects, not DTOs
