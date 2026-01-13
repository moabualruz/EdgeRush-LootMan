# SimulationCraft Integration Spec

## Overview

Integration with local SimulationCraft via Docker to provide accurate gear upgrade simulations for IPI (Item Priority Index) calculation, replacing wishlist percentage estimates with actual simulation data.

## Architecture Change (from Raidbots)

**Previous Approach (Blocked - Raidbots API required API keys):**
```
FlpsComponentCalculator → RaidbotsClient → Raidbots API
```

**Current Approach (Local SimC Docker):**
```
FlpsComponentCalculator → UpgradeValueCalculator → SimulationRepository
                                 ↓
                       SimulationService → DockerSimulationExecutor
                                 ↓
                       Docker (simulationcraftorg/simc)
```

## Key Benefits

- **No API Keys Required**: Runs locally via Docker container
- **Same Accuracy**: Uses same SimulationCraft engine as Raidbots
- **Full Control**: Configure iterations, fight length, and other parameters
- **Cost-Free**: No subscription or API costs
- **Offline-Capable**: Works without internet once Docker image is pulled

## Solution

- Execute SimulationCraft simulations via Docker container
- Generate SimC profiles from character gear data
- Process simulation results for DPS/HPS gains
- Calculate normalized upgrade values from results
- Store results in database for caching
- Fall back to wishlist percentages when unavailable

## Key Features

- Docker-based simulation execution
- Async processing with coroutines
- Guild-specific simulation parameters
- Automatic profile generation from gear data
- Comprehensive result caching
- Fallback to wishlist percentages

## Documents

- **requirements.md** - Updated requirements for local simulation
- **design.md** - Technical design with Docker architecture
- **tasks.md** - Implementation task list (COMPLETED)

## Implementation Status

**Status**: ✅ Implementation Complete

### Completed Components

1. **Domain Layer** (`domain/simulation/`)
   - `SimulationProfile.kt` - Value object for SimC profile
   - `SimulationResult.kt` - Value object for DPS/HPS results
   - `SimulationRequest.kt` - Entity for tracking jobs
   - `SimulationStatus.kt` - Status enum
   - `SimulationRepository.kt` - Port interface

2. **Application Layer** (`application/simulation/`)
   - `UpgradeValueCalculator.kt` - Calculates UV from simulation data
   - `ProfileGeneratorService.kt` - Generates SimC profiles
   - `SimulationService.kt` - Orchestrates simulation workflow

3. **Infrastructure Layer** (`infrastructure/simulation/`)
   - `DockerSimulationExecutor.kt` - Runs SimC via Docker
   - `JdbcSimulationRepository.kt` - JDBC persistence

4. **Database Migration**
   - `V0020__add_simulation_tables.sql`

5. **FLPS Integration**
   - `FlpsComponentCalculator.calculateUVWithSimulation()`
   - Maintains backward compatibility with `calculateUV()`

## Docker Configuration

```yaml
# docker-compose.yml
data-sync:
  volumes:
    - simc_profiles:/home/gradle/project/simc-profiles
    - /var/run/docker.sock:/var/run/docker.sock
  environment:
    SIMULATION_DOCKER_IMAGE: simulationcraftorg/simc
    SIMULATION_DOCKER_PROFILE_DIR: /home/gradle/project/simc-profiles
    SIMULATION_DOCKER_TIMEOUT_MINUTES: 30
```

## Usage

```kotlin
// Calculate UV with simulation data (or fallback to wishlist)
val uv = flpsComponentCalculator.calculateUVWithSimulation(
    guildId = "my-guild",
    characterName = "Testchar",
    characterRealm = "TestRealm",
    itemId = ItemId(12345),
    wishlist = raiderWishlist
)

// Check if simulation data is available
val hasSimData = flpsComponentCalculator.hasSimulationData(
    guildId = "my-guild",
    characterName = "Testchar",
    characterRealm = "TestRealm"
)
```

## Priority

**Critical** - Required for accurate IPI calculations
