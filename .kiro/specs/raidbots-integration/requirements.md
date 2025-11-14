# Requirements Document - Raidbots Integration

## Introduction

This feature integrates the Raidbots API to provide accurate gear upgrade simulation data for the Item Priority Index (IPI) component of the FLPS algorithm. Currently, upgrade values are estimated using wishlist percentages, which is less accurate than simulation-based calculations. This integration will enable precise upgrade value calculations using Raidbots Droptimizer simulation results.

## Glossary

- **Raidbots**: Third-party service that runs SimulationCraft simulations for World of Warcraft characters
- **Droptimizer**: Raidbots feature that simulates all possible gear upgrades to determine optimal items
- **IPI (Item Priority Index)**: Component of FLPS that measures item upgrade value (60% of FLPS weight)
- **UV (Upgrade Value)**: Normalized DPS/HPS gain from equipping an item (45% of IPI weight)
- **SimulationCraft**: Open-source tool that simulates character performance with different gear configurations
- **Sim Profile**: Character configuration used as input for simulations
- **DPS/HPS**: Damage Per Second / Healing Per Second - primary performance metrics
- **The System**: EdgeRush LootMan FLPS calculation system
- **Guild Configuration**: Customizable settings that control how Raidbots data is processed

## Requirements

### Requirement 1: API Authentication and Configuration

**User Story:** As a guild administrator, I want to configure Raidbots API credentials so that the system can run gear simulations for my raiders.

#### Acceptance Criteria

1. WHERE Raidbots integration is enabled, THE System SHALL store API key in encrypted configuration
2. WHEN the System starts, THE System SHALL validate Raidbots API credentials and log authentication status
3. WHERE multiple guilds are configured, THE System SHALL support guild-specific Raidbots API keys
4. THE System SHALL support both personal and guild-wide Raidbots API keys
5. WHEN API credentials are invalid, THE System SHALL log detailed error messages and continue operation with UV defaulting to wishlist percentages

### Requirement 2: Character Profile Management

**User Story:** As a raider, I want my character profile to be automatically generated so that simulations reflect my current gear and talents.

#### Acceptance Criteria

1. THE System SHALL generate SimulationCraft profiles from WoWAudit character data
2. THE System SHALL include current gear, talents, and covenant configuration in profiles
3. WHERE character data is incomplete, THE System SHALL log warnings and use configurable defaults
4. THE System SHALL support manual profile overrides for advanced users
5. WHEN profile generation fails, THE System SHALL fall back to wishlist-based upgrade values

### Requirement 3: Droptimizer Simulation Execution

**User Story:** As a system operator, I want the system to automatically run Droptimizer simulations so that upgrade values stay current with character progression.

#### Acceptance Criteria

1. THE System SHALL submit Droptimizer simulation requests to Raidbots API on a configurable schedule (default: weekly)
2. WHERE a simulation schedule is configured, THE System SHALL execute simulations at the specified interval
3. THE System SHALL include configurable item sources (raid, dungeon, vault, crafted) in simulations
4. THE System SHALL support configurable simulation parameters (iterations, fight length, fight style)
5. WHEN rate limits are encountered, THE System SHALL implement exponential backoff with configurable retry attempts (default: 3 retries)

### Requirement 4: Simulation Result Processing

**User Story:** As a system operator, I want simulation results to be processed and stored so that upgrade values can be calculated efficiently.

#### Acceptance Criteria

1. THE System SHALL poll Raidbots API for simulation completion with configurable polling interval (default: 30 seconds)
2. THE System SHALL extract DPS/HPS gains for each simulated item
3. THE System SHALL store simulation results in the database with timestamps
4. WHERE simulation fails, THE System SHALL log the error and retry with configurable backoff
5. WHEN simulation completes, THE System SHALL invalidate cached upgrade values for affected characters

### Requirement 5: Upgrade Value Calculation

**User Story:** As a raider, I want my upgrade values to be calculated from actual simulations so that my IPI score accurately reflects item priority.

#### Acceptance Criteria

1. THE System SHALL calculate UV as simulated DPS/HPS gain divided by spec baseline output
2. THE System SHALL normalize UV values to 0.0-1.0 range using configurable percentile (default: 95th percentile)
3. WHERE multiple simulation results exist for an item, THE System SHALL use the most recent result
4. THE System SHALL apply configurable time-based weighting to prioritize recent simulations (default: last 7 days weighted 2x)
5. WHERE no simulation data exists for an item, THE System SHALL fall back to wishlist percentage-based estimation

### Requirement 6: IPI Integration

**User Story:** As a raider, I want my Raidbots simulation data to be reflected in my IPI score so that my FLPS is accurate.

#### Acceptance Criteria

1. WHEN Raidbots data is available, THE System SHALL calculate UV using simulation results instead of wishlist percentages
2. WHERE simulation data is stale (older than configurable threshold, default: 30 days), THE System SHALL trigger new simulation
3. THE System SHALL apply guild-specific UV weight in IPI calculation (default: 0.45)
4. WHERE Raidbots integration is disabled, THE System SHALL fall back to wishlist-based UV calculation
5. THE System SHALL expose UV source (simulation vs wishlist) in FLPS breakdown for transparency

### Requirement 7: Data Persistence and Caching

**User Story:** As a system operator, I want simulation data to be cached so that IPI calculations are fast and don't require repeated API calls.

#### Acceptance Criteria

1. THE System SHALL persist simulation requests with status tracking (pending, running, completed, failed)
2. THE System SHALL persist simulation results with item-specific DPS/HPS gains
3. THE System SHALL cache calculated UV values with configurable TTL (default: 24 hours)
4. WHERE cached data exists and is not expired, THE System SHALL use cached UV values
5. WHEN new simulation data is available, THE System SHALL invalidate affected UV caches

### Requirement 8: Error Handling and Resilience

**User Story:** As a system operator, I want the system to handle Raidbots API failures gracefully so that IPI calculations continue even when simulations are unavailable.

#### Acceptance Criteria

1. WHEN Raidbots API is unavailable, THE System SHALL log the error and continue with cached or wishlist-based UV values
2. WHERE API rate limits are exceeded, THE System SHALL implement exponential backoff with configurable maximum delay (default: 10 minutes)
3. IF authentication fails, THEN THE System SHALL log detailed error information and disable Raidbots integration until credentials are updated
4. THE System SHALL expose health check endpoint indicating Raidbots integration status
5. WHEN simulation times out (configurable, default: 10 minutes), THE System SHALL cancel the request and retry later

### Requirement 9: Configuration Management

**User Story:** As a guild administrator, I want to customize how Raidbots simulations are configured so that results align with my guild's raid environment.

#### Acceptance Criteria

1. THE System SHALL support guild-specific configuration for all Raidbots integration parameters
2. WHERE no guild-specific configuration exists, THE System SHALL use system-wide default values
3. THE System SHALL expose REST API endpoints for viewing and updating Raidbots configuration
4. THE System SHALL validate configuration changes and reject invalid values with descriptive error messages
5. WHEN configuration is updated, THE System SHALL apply changes to subsequent simulations without requiring restart

### Requirement 10: Observability and Monitoring

**User Story:** As a system operator, I want to monitor Raidbots integration health so that I can identify and resolve issues quickly.

#### Acceptance Criteria

1. THE System SHALL log all Raidbots API calls with response times and status codes
2. THE System SHALL expose metrics for simulation success rate, API latency, and cache hit rate
3. THE System SHALL track per-character simulation status and log characters with failed simulations
4. WHERE simulation failures occur repeatedly, THE System SHALL emit warning-level logs with actionable guidance
5. THE System SHALL provide admin endpoint showing last simulation time, next simulation time, and simulation status per character

### Requirement 11: Data Privacy and Security

**User Story:** As a guild administrator, I want Raidbots API credentials to be stored securely so that unauthorized access is prevented.

#### Acceptance Criteria

1. THE System SHALL encrypt Raidbots API keys at rest using industry-standard encryption
2. THE System SHALL never log or expose API keys in plain text
3. WHERE credentials are transmitted, THE System SHALL use HTTPS/TLS encryption
4. THE System SHALL support credential rotation without data loss
5. THE System SHALL restrict access to Raidbots configuration to authorized administrators only

### Requirement 12: Performance Optimization

**User Story:** As a system operator, I want Raidbots simulation processing to be efficient so that it doesn't impact system performance.

#### Acceptance Criteria

1. THE System SHALL process simulations asynchronously without blocking IPI calculations
2. THE System SHALL batch simulation requests where possible to minimize API calls
3. WHERE multiple characters need simulations, THE System SHALL queue requests with configurable concurrency limit (default: 3)
4. THE System SHALL prioritize simulations for characters with upcoming loot decisions
5. WHEN simulation queue exceeds configurable threshold (default: 50), THE System SHALL pause new submissions until queue clears
