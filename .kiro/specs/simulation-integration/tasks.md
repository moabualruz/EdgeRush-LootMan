# Implementation Plan - SimulationCraft Integration

## Status: ✅ COMPLETE

All tasks have been implemented following TDD and DDD standards.

## Task List

- [x] 1. Create domain layer models (TDD)
  - Created `SimulationProfile.kt` value object
  - Created `SimulationResult.kt` value object
  - Created `SimulationRequest.kt` entity with state machine
  - Created `SimulationStatus.kt` enum
  - Created `SimulationRepository.kt` port interface
  - Added `@ConsistentCopyVisibility` annotation for Kotlin compatibility
  - _Requirements: 3.1, 3.2, 3.3, 7.1, 7.2_

- [x] 2. Create domain layer tests
  - `SimulationProfileTest.kt` - value object validation
  - `SimulationResultTest.kt` - DPS calculations, normalization
  - `SimulationRequestTest.kt` - state transitions
  - _Requirements: 10.4_

- [x] 3. Create application layer services (TDD)
  - Created `UpgradeValueCalculator.kt` - UV from simulation data
  - Created `ProfileGeneratorService.kt` - SimC profile generation
  - Created `SimulationService.kt` with `SimulationExecutor` interface
  - _Requirements: 2.1, 2.2, 5.1, 5.2, 5.3, 5.4_

- [x] 4. Create application layer tests
  - `UpgradeValueCalculatorTest.kt` - UV calculation with mocks
  - `ProfileGeneratorServiceTest.kt` - profile generation
  - `SimulationServiceTest.kt` - service orchestration
  - _Requirements: 10.1, 10.2, 10.4_

- [x] 5. Create infrastructure layer (TDD)
  - Created `DockerSimulationExecutor.kt` - Docker execution via ProcessBuilder
  - Created `JdbcSimulationRepository.kt` - JDBC persistence
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 7.1, 7.2, 7.3_

- [x] 6. Create infrastructure layer tests
  - `DockerSimulationExecutorTest.kt` - command building, JSON parsing
  - `JdbcSimulationRepositoryTest.kt` - database operations
  - _Requirements: 10.3, 10.4_

- [x] 7. Create database migration
  - Created `V0020__add_simulation_tables.sql`
  - Tables: simulation_profiles, simulation_requests, simulation_results
  - Indexes for efficient queries
  - _Requirements: 7.1, 7.2, 7.4, 7.5_

- [x] 8. Integrate with FlpsComponentCalculator
  - Added `calculateUVWithSimulation()` method
  - Added `hasSimulationData()` method
  - Maintained backward compatibility with `calculateUV()`
  - Optional injection of `UpgradeValueCalculator`
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 9. Implement SOPS + age secrets management
  - Created `.sops.yaml` configuration
  - Created `scripts/setup-secrets.sh`
  - Created `scripts/decrypt-secrets.sh`
  - Updated `.gitignore` for secrets
  - Updated `.env.example` with SOPS configuration
  - _Requirements: (secrets management)_

- [x] 10. Update Docker configuration
  - Updated `docker-compose.yml` with SimC volumes
  - Added Docker socket mount for container execution
  - Added simulation environment variables
  - _Requirements: 1.1, 1.2, 1.3, 9.1, 9.2, 9.3_

- [x] 11. Update documentation
  - Updated simulation-integration README.md
  - Updated requirements.md for local SimC approach
  - Updated design.md with Docker architecture
  - Updated tasks.md (this file)
  - _Requirements: 9.5_

## Files Created

### Domain Layer
- `domain/simulation/model/SimulationProfile.kt`
- `domain/simulation/model/SimulationResult.kt`
- `domain/simulation/model/SimulationRequest.kt`
- `domain/simulation/model/SimulationStatus.kt`
- `domain/simulation/repository/SimulationRepository.kt`

### Application Layer
- `application/simulation/UpgradeValueCalculator.kt`
- `application/simulation/ProfileGeneratorService.kt`
- `application/simulation/SimulationService.kt`

### Infrastructure Layer
- `infrastructure/simulation/DockerSimulationExecutor.kt`
- `infrastructure/simulation/JdbcSimulationRepository.kt`

### Database
- `db/migration/V0020__add_simulation_tables.sql`

### Configuration
- `.sops.yaml`
- `scripts/setup-secrets.sh`
- `scripts/decrypt-secrets.sh`

### Tests
- `domain/simulation/model/SimulationProfileTest.kt`
- `domain/simulation/model/SimulationResultTest.kt`
- `domain/simulation/model/SimulationRequestTest.kt`
- `application/simulation/UpgradeValueCalculatorTest.kt`
- `application/simulation/ProfileGeneratorServiceTest.kt`
- `application/simulation/SimulationServiceTest.kt`
- `infrastructure/simulation/DockerSimulationExecutorTest.kt`
- `infrastructure/simulation/JdbcSimulationRepositoryTest.kt`

## Files Modified

- `application/flps/FlpsComponentCalculator.kt` - Added simulation integration
- `docker-compose.yml` - Added SimC configuration
- `.env.example` - Added simulation and SOPS variables
- `.gitignore` - Added secrets patterns

## Migration from Raidbots Spec

This specification replaces the original `raidbots-integration` spec which required external API keys. The key changes:

| Aspect | Raidbots (Old) | SimulationCraft (New) |
| ------ | -------------- | --------------------- |
| Execution | External API | Local Docker |
| API Keys | Required | Not needed |
| Cost | Subscription | Free |
| Engine | SimC (via Raidbots) | SimC (direct) |
| Control | Limited | Full |
