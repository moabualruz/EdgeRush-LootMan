# Requirements Document - SimulationCraft Integration

## Introduction

This feature integrates local SimulationCraft execution via Docker to provide accurate gear upgrade simulation data for the Item Priority Index (IPI) component of the FLPS algorithm. This replaces the original Raidbots API approach (which required API keys) with a self-hosted solution using the same underlying SimulationCraft engine.

## Glossary

- **SimulationCraft (SimC)**: Open-source tool that simulates character performance with different gear configurations
- **Docker**: Container platform used to run SimC in isolation
- **IPI (Item Priority Index)**: Component of FLPS that measures item upgrade value (60% of FLPS weight)
- **UV (Upgrade Value)**: Normalized DPS/HPS gain from equipping an item (45% of IPI weight)
- **Sim Profile**: Character configuration used as input for simulations
- **DPS/HPS**: Damage Per Second / Healing Per Second - primary performance metrics
- **The System**: EdgeRush LootMan FLPS calculation system
- **Guild Configuration**: Customizable settings that control how simulation data is processed

## Requirements

### Requirement 1: Docker-Based Simulation Execution

**User Story:** As a system operator, I want to run SimulationCraft locally via Docker so that gear simulations don't require external API keys.

#### Acceptance Criteria

1. THE System SHALL execute SimulationCraft simulations using the `simulationcraftorg/simc` Docker image
2. THE System SHALL support configurable Docker image version via environment variable
3. THE System SHALL mount profile directories as volumes for input/output
4. THE System SHALL support configurable simulation timeout (default: 30 minutes)
5. WHEN Docker is unavailable, THE System SHALL log error and fall back to wishlist-based UV calculation

### Requirement 2: Character Profile Generation

**User Story:** As a raider, I want my character profile to be automatically generated so that simulations reflect my current gear and talents.

#### Acceptance Criteria

1. THE System SHALL generate SimulationCraft profiles from character gear data
2. THE System SHALL include character class, spec, level, and race in profiles
3. THE System SHALL include all equipped gear with item IDs in profiles
4. WHERE character data is incomplete, THE System SHALL log warnings and use defaults
5. WHEN profile generation fails, THE System SHALL fall back to wishlist-based upgrade values

### Requirement 3: Simulation Request Management

**User Story:** As a system operator, I want simulation requests to be tracked so that I can monitor processing status.

#### Acceptance Criteria

1. THE System SHALL persist simulation requests with status tracking (PENDING, RUNNING, COMPLETED, FAILED)
2. THE System SHALL record submission timestamp for each request
3. THE System SHALL record completion timestamp when simulation finishes
4. THE System SHALL support configurable simulation parameters (iterations, fight length)
5. WHEN simulation fails, THE System SHALL record error message for debugging

### Requirement 4: Simulation Result Processing

**User Story:** As a system operator, I want simulation results to be processed and stored so that upgrade values can be calculated efficiently.

#### Acceptance Criteria

1. THE System SHALL parse SimC JSON output for DPS/HPS results
2. THE System SHALL extract per-item DPS gains from simulation results
3. THE System SHALL store simulation results in the database with timestamps
4. WHERE simulation output is invalid, THE System SHALL log error and mark request as failed
5. WHEN simulation completes, THE System SHALL make results available for UV calculation

### Requirement 5: Upgrade Value Calculation

**User Story:** As a raider, I want my upgrade values to be calculated from actual simulations so that my IPI score accurately reflects item priority.

#### Acceptance Criteria

1. THE System SHALL calculate UV from simulated DPS/HPS gain percentage
2. THE System SHALL normalize UV values to 0.0-1.0 range using configurable max percent gain (default: 10%)
3. WHERE multiple simulation results exist for an item, THE System SHALL use the most recent result
4. WHERE no simulation data exists for an item, THE System SHALL fall back to wishlist percentage-based estimation
5. THE System SHALL expose method to check if simulation data exists for a character

### Requirement 6: FLPS Integration

**User Story:** As a raider, I want my simulation data to be reflected in my IPI score so that my FLPS is accurate.

#### Acceptance Criteria

1. WHEN simulation data is available, THE System SHALL calculate UV using simulation results instead of wishlist percentages
2. THE System SHALL maintain backward compatibility with existing calculateUV() method
3. THE System SHALL provide calculateUVWithSimulation() method for simulation-aware calculation
4. WHERE simulation integration is unavailable, THE System SHALL fall back to wishlist-based UV calculation
5. THE System SHALL support optional injection of UpgradeValueCalculator

### Requirement 7: Data Persistence

**User Story:** As a system operator, I want simulation data to be persisted so that UV calculations don't require repeated simulations.

#### Acceptance Criteria

1. THE System SHALL persist simulation profiles with guild, character, and realm identifiers
2. THE System SHALL persist simulation requests with profile references
3. THE System SHALL persist simulation results with item-specific DPS/HPS gains
4. THE System SHALL support unique constraint on profile (guild, character, realm)
5. THE System SHALL support upsert for profile updates

### Requirement 8: Error Handling and Resilience

**User Story:** As a system operator, I want the system to handle simulation failures gracefully so that IPI calculations continue even when simulations fail.

#### Acceptance Criteria

1. WHEN Docker execution fails, THE System SHALL log error and return failure result
2. WHEN simulation times out, THE System SHALL terminate process and mark request as failed
3. WHEN JSON parsing fails, THE System SHALL log error with details
4. THE System SHALL support configurable timeout for Docker execution
5. WHERE any error occurs, THE System SHALL fall back to wishlist-based UV calculation

### Requirement 9: Configuration Management

**User Story:** As a system operator, I want to configure simulation parameters so that results align with raid environment.

#### Acceptance Criteria

1. THE System SHALL support configurable Docker image via `simulation.docker.image`
2. THE System SHALL support configurable profile directory via `simulation.docker.profile-directory`
3. THE System SHALL support configurable timeout via `simulation.docker.timeout-minutes`
4. THE System SHALL support configurable Docker command for testing
5. WHERE no configuration is provided, THE System SHALL use sensible defaults

### Requirement 10: Testing Support

**User Story:** As a developer, I want the simulation system to be testable so that I can verify behavior without running actual simulations.

#### Acceptance Criteria

1. THE System SHALL define SimulationExecutor interface for dependency injection
2. THE System SHALL support mock implementations of SimulationExecutor
3. THE System SHALL support mock implementations of SimulationRepository
4. THE System SHALL use TDD for all components
5. THE System SHALL maintain test coverage standards (96%+ instruction, 92%+ branch)
