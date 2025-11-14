# Design Document - Raidbots Integration

## Overview

This design implements Raidbots API integration to provide accurate gear upgrade simulations for IPI (Item Priority Index) calculation. The design follows the existing architecture patterns and emphasizes configurability, async processing, and resilience.

### Design Goals

1. **Accuracy**: Replace wishlist percentages with actual simulation data
2. **Configurability**: All parameters configurable per guild
3. **Async Processing**: Non-blocking simulation execution
4. **Resilience**: Graceful fallback to wishlist-based calculations
5. **Performance**: Caching and queue management for efficiency

## Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    FLPS Calculation Layer                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ScoreCalculator.calculateUpgradeValue()             │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Raidbots Service Layer                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  RaidbotsUpgradeService                              │  │
│  │  - getUVForCharacterAndItem()                        │  │
│  │  - getSimulationResults()                            │  │
│  │  - calculateNormalizedUV()                           │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Raidbots Simulation Layer                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  RaidbotsSimulationService                           │  │
│  │  - submitSimulation()                                │  │
│  │  - pollSimulationStatus()                            │  │
│  │  - processSimulationResults()                        │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Raidbots Client Layer                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  RaidbotsClient                                      │  │
│  │  - submitDroptimizer()                               │  │
│  │  - getSimulationStatus()                             │  │
│  │  - getSimulationResults()                            │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                 Raidbots API                                │
│                  (External Service)                         │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. RaidbotsClient

**Purpose**: HTTP client for Raidbots API

**Configuration**:
```kotlin
@ConfigurationProperties("raidbots")
data class RaidbotsProperties(
    val enabled: Boolean = false,
    val apiKey: String,
    val baseUrl: String = "https://www.raidbots.com/api/v1",
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 2000,
    val maxConcurrentSimulations: Int = 3,
    val simulationTimeoutMinutes: Long = 10,
    val pollIntervalSeconds: Long = 30
)
```

**Key Methods**:
```kotlin
interface RaidbotsClient {
    suspend fun submitDroptimizer(
        profile: String,
        itemSources: List<String>,
        simOptions: SimulationOptions
    ): SimulationSubmission
    
    suspend fun getSimulationStatus(simId: String): SimulationStatus
    
    suspend fun getSimulationResults(simId: String): SimulationResults
}
```



### 2. Database Entities

```kotlin
@Table("raidbots_simulations")
data class RaidbotsSimulationEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val simId: String,
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED
    val submittedAt: Instant,
    val completedAt: Instant?,
    val profile: String,
    val simOptions: String // JSON
)

@Table("raidbots_results")
data class RaidbotsResultEntity(
    @Id val id: Long? = null,
    val simulationId: Long,
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val calculatedAt: Instant
)

@Table("raidbots_config")
data class RaidbotsConfigEntity(
    @Id val guildId: String,
    val enabled: Boolean,
    val encryptedApiKey: String?,
    val configJson: String,
    val updatedAt: Instant
)
```

### 3. Configuration Model

```kotlin
data class RaidbotsGuildConfig(
    val guildId: String,
    val enabled: Boolean = true,
    val apiKey: String? = null,
    
    // Simulation schedule
    val simulationIntervalDays: Int = 7,
    val autoSimulateOnGearChange: Boolean = true,
    
    // Simulation parameters
    val iterations: Int = 10000,
    val fightLengthSeconds: Int = 300,
    val fightStyle: String = "Patchwerk",
    val itemSources: List<String> = listOf("raid", "dungeon", "vault"),
    
    // UV calculation
    val uvNormalizationPercentile: Int = 95,
    val recentSimWeightMultiplier: Double = 2.0,
    val recentSimDays: Int = 7,
    val staleSimThresholdDays: Int = 30,
    
    // Cache configuration
    val uvCacheTTLHours: Int = 24,
    
    // Queue management
    val maxQueueSize: Int = 50,
    val priorityThresholdDays: Int = 3
)
```

## Database Migration

```sql
-- V0017__add_raidbots_tables.sql

CREATE TABLE raidbots_config (
    guild_id VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT true,
    encrypted_api_key TEXT,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (guild_id) REFERENCES guild_configuration(guild_id)
);

CREATE TABLE raidbots_simulations (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    sim_id VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    profile TEXT NOT NULL,
    sim_options TEXT NOT NULL,
    FOREIGN KEY (guild_id) REFERENCES raidbots_config(guild_id)
);

CREATE TABLE raidbots_results (
    id BIGSERIAL PRIMARY KEY,
    simulation_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    dps_gain DOUBLE PRECISION NOT NULL,
    percent_gain DOUBLE PRECISION NOT NULL,
    calculated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (simulation_id) REFERENCES raidbots_simulations(id) ON DELETE CASCADE
);

CREATE INDEX idx_rb_sims_char ON raidbots_simulations(character_name, character_realm, status);
CREATE INDEX idx_rb_sims_status ON raidbots_simulations(status, submitted_at);
CREATE INDEX idx_rb_results_sim ON raidbots_results(simulation_id, item_id);
CREATE INDEX idx_rb_results_item ON raidbots_results(item_id, calculated_at DESC);
```

## API Endpoints

```
# Configuration
GET    /api/raidbots/config/{guildId}
PUT    /api/raidbots/config/{guildId}

# Simulations
POST   /api/raidbots/simulate/{guildId}/{characterName}
GET    /api/raidbots/simulations/{guildId}
GET    /api/raidbots/simulations/{simId}/status
GET    /api/raidbots/simulations/{simId}/results

# Upgrade Values
GET    /api/raidbots/upgrades/{guildId}/{characterName}
GET    /api/raidbots/upgrades/{guildId}/{characterName}/{itemId}

# Health
GET    /actuator/health/raidbots
GET    /actuator/metrics/rb.*
```

## Testing Strategy

- Unit tests for profile generation, UV calculation, normalization
- Integration tests with MockWebServer for API client
- End-to-end tests for simulation workflow
- Performance tests for queue management

## Configuration Example

```yaml
raidbots:
  enabled: true
  api-key: ${RAIDBOTS_API_KEY}
  
  simulation:
    interval-days: 7
    auto-simulate-on-gear-change: true
    iterations: 10000
    fight-length-seconds: 300
    fight-style: Patchwerk
    item-sources:
      - raid
      - dungeon
      - vault
  
  uv:
    normalization-percentile: 95
    recent-sim-weight-multiplier: 2.0
    recent-sim-days: 7
    stale-sim-threshold-days: 30
  
  cache:
    uv-ttl-hours: 24
  
  queue:
    max-queue-size: 50
    max-concurrent-simulations: 3
    priority-threshold-days: 3
```
