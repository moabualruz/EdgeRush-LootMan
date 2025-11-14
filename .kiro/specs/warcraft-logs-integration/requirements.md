# Requirements Document - Warcraft Logs Integration

## Introduction

This feature integrates the Warcraft Logs API to provide accurate combat performance data for the Mechanical Adherence Score (MAS) component of the FLPS algorithm. Currently, MAS returns 0.0 due to missing performance data. This integration will enable accurate scoring based on actual raid performance metrics including deaths per attempt, avoidable damage, and spec-specific performance benchmarks.

## Glossary

- **Warcraft Logs**: Third-party service that parses World of Warcraft combat logs and provides detailed performance analytics
- **MAS (Mechanical Adherence Score)**: Component of RMS that measures a raider's mechanical execution quality (40% of RMS weight)
- **DPA (Deaths Per Attempt)**: Average number of deaths per boss attempt for a character
- **ADT (Avoidable Damage Taken)**: Percentage of damage taken that could have been avoided through proper mechanics
- **Combat Log**: Game-generated record of all combat events during encounters
- **Report**: A Warcraft Logs report containing one or more raid encounters
- **Fight**: A single boss encounter attempt within a report
- **The System**: EdgeRush LootMan FLPS calculation system
- **Guild Configuration**: Customizable settings that control how Warcraft Logs data is processed and weighted

## Requirements

### Requirement 1: API Authentication and Configuration

**User Story:** As a guild administrator, I want to configure Warcraft Logs API credentials so that the system can fetch combat log data for my guild.

#### Acceptance Criteria

1. WHERE Warcraft Logs integration is enabled, THE System SHALL store API client ID and client secret in encrypted configuration
2. WHEN the System starts, THE System SHALL validate Warcraft Logs API credentials and log authentication status
3. WHERE multiple guilds are configured, THE System SHALL support guild-specific Warcraft Logs API credentials
4. THE System SHALL support OAuth2 authentication flow for Warcraft Logs API v2
5. WHEN API credentials are invalid, THE System SHALL log detailed error messages and continue operation with MAS defaulting to 0.0

### Requirement 2: Guild and Character Mapping

**User Story:** As a guild administrator, I want to map my WoW guild to Warcraft Logs reports so that the system can find relevant combat data.

#### Acceptance Criteria

1. THE System SHALL store configurable guild identifiers for Warcraft Logs (guild name, realm, region)
2. THE System SHALL map WoWAudit character names to Warcraft Logs character names with configurable aliases
3. WHERE character names differ between systems, THE System SHALL support manual character name mapping via configuration
4. THE System SHALL support multiple guild configurations for merged raid teams
5. WHEN a character cannot be mapped, THE System SHALL log a warning and exclude that character from Warcraft Logs-based scoring

### Requirement 3: Report Discovery and Synchronization

**User Story:** As a system operator, I want the system to automatically discover and sync recent Warcraft Logs reports so that performance data stays current.

#### Acceptance Criteria

1. THE System SHALL fetch reports for configured guilds on a configurable schedule (default: every 6 hours)
2. WHERE a sync schedule is configured, THE System SHALL execute Warcraft Logs sync at the specified interval
3. THE System SHALL fetch reports from a configurable time window (default: last 30 days)
4. THE System SHALL store raw Warcraft Logs report metadata in the database for audit purposes
5. WHEN rate limits are encountered, THE System SHALL implement exponential backoff with configurable retry attempts (default: 3 retries)

### Requirement 4: Fight Data Extraction

**User Story:** As a system operator, I want the system to extract relevant fight data from reports so that performance metrics can be calculated.

#### Acceptance Criteria

1. THE System SHALL extract fight data including encounter ID, difficulty, kill status, and duration
2. THE System SHALL extract per-character metrics including deaths, damage taken, and avoidable damage
3. WHERE multiple attempts exist for an encounter, THE System SHALL process all attempts within the configurable time window
4. THE System SHALL filter fights by configurable difficulty levels (default: Mythic, Heroic)
5. WHEN fight data is incomplete, THE System SHALL log a warning and skip that fight

### Requirement 5: Performance Metric Calculation

**User Story:** As a raider, I want my mechanical performance to be accurately measured so that my FLPS score reflects my actual execution quality.

#### Acceptance Criteria

1. THE System SHALL calculate Deaths Per Attempt (DPA) as total deaths divided by total attempts for each character
2. THE System SHALL calculate Avoidable Damage Taken (ADT) percentage from Warcraft Logs damage events
3. THE System SHALL calculate spec-specific average DPA and ADT for normalization with configurable percentile (default: 50th percentile)
4. WHERE insufficient data exists for a spec, THE System SHALL use configurable fallback values (default: DPA=0.5, ADT=10%)
5. THE System SHALL apply configurable time-based weighting to prioritize recent performance (default: last 14 days weighted 2x)

### Requirement 6: MAS Score Integration

**User Story:** As a raider, I want my Warcraft Logs performance to be reflected in my MAS score so that my FLPS is accurate.

#### Acceptance Criteria

1. WHEN Warcraft Logs data is available, THE System SHALL calculate MAS using the formula: `1 - ((dpaRatio - 1) * dpaWeight + (adtRatio - 1) * adtWeight)`
2. WHERE dpaRatio or adtRatio exceeds the configurable critical threshold (default: 1.5), THE System SHALL set MAS to 0.0
3. THE System SHALL apply configurable weights to DPA and ADT components (default: 0.25 each)
4. WHERE Warcraft Logs data is unavailable, THE System SHALL use configurable fallback MAS value (default: 0.0)
5. THE System SHALL clamp MAS values to the range 0.0 to 1.0

### Requirement 7: Data Persistence and Caching

**User Story:** As a system operator, I want performance data to be cached so that FLPS calculations are fast and don't require repeated API calls.

#### Acceptance Criteria

1. THE System SHALL persist Warcraft Logs report metadata in the database with sync timestamps
2. THE System SHALL persist per-character performance metrics with encounter and date associations
3. THE System SHALL cache calculated MAS scores with configurable TTL (default: 1 hour)
4. WHERE cached data exists and is not expired, THE System SHALL use cached MAS scores
5. WHEN new Warcraft Logs data is synced, THE System SHALL invalidate affected MAS caches

### Requirement 8: Error Handling and Resilience

**User Story:** As a system operator, I want the system to handle Warcraft Logs API failures gracefully so that FLPS calculations continue even when external services are unavailable.

#### Acceptance Criteria

1. WHEN Warcraft Logs API is unavailable, THE System SHALL log the error and continue with cached or fallback MAS values
2. WHERE API rate limits are exceeded, THE System SHALL implement exponential backoff with configurable maximum delay (default: 5 minutes)
3. IF authentication fails, THEN THE System SHALL log detailed error information and disable Warcraft Logs integration until credentials are updated
4. THE System SHALL expose health check endpoint indicating Warcraft Logs integration status
5. WHEN parsing errors occur, THE System SHALL log the error with report context and skip the problematic data

### Requirement 9: Configuration Management

**User Story:** As a guild administrator, I want to customize how Warcraft Logs data is processed so that scoring aligns with my guild's priorities.

#### Acceptance Criteria

1. THE System SHALL support guild-specific configuration for all Warcraft Logs integration parameters
2. WHERE no guild-specific configuration exists, THE System SHALL use system-wide default values
3. THE System SHALL expose REST API endpoints for viewing and updating Warcraft Logs configuration
4. THE System SHALL validate configuration changes and reject invalid values with descriptive error messages
5. WHEN configuration is updated, THE System SHALL apply changes to subsequent calculations without requiring restart

### Requirement 10: Observability and Monitoring

**User Story:** As a system operator, I want to monitor Warcraft Logs integration health so that I can identify and resolve issues quickly.

#### Acceptance Criteria

1. THE System SHALL log all Warcraft Logs API calls with response times and status codes
2. THE System SHALL expose metrics for sync success rate, API latency, and cache hit rate
3. THE System SHALL track per-character data availability and log characters with insufficient data
4. WHERE sync failures occur repeatedly, THE System SHALL emit warning-level logs with actionable guidance
5. THE System SHALL provide admin endpoint showing last sync time, next sync time, and sync status per guild

### Requirement 11: Data Privacy and Security

**User Story:** As a guild administrator, I want Warcraft Logs API credentials to be stored securely so that unauthorized access is prevented.

#### Acceptance Criteria

1. THE System SHALL encrypt Warcraft Logs API credentials at rest using industry-standard encryption
2. THE System SHALL never log or expose API credentials in plain text
3. WHERE credentials are transmitted, THE System SHALL use HTTPS/TLS encryption
4. THE System SHALL support credential rotation without data loss
5. THE System SHALL restrict access to Warcraft Logs configuration to authorized administrators only

### Requirement 12: Performance Optimization

**User Story:** As a system operator, I want Warcraft Logs data processing to be efficient so that it doesn't impact system performance.

#### Acceptance Criteria

1. THE System SHALL process Warcraft Logs reports asynchronously without blocking FLPS calculations
2. THE System SHALL batch API requests where possible to minimize API calls
3. WHERE large reports are processed, THE System SHALL implement pagination with configurable page size (default: 100 fights)
4. THE System SHALL limit concurrent Warcraft Logs API requests to configurable maximum (default: 5)
5. WHEN processing takes longer than configurable timeout (default: 30 seconds), THE System SHALL cancel the operation and retry later
