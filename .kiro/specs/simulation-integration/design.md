# Design Document - SimulationCraft Integration

## Overview

This design implements local SimulationCraft execution via Docker to provide accurate gear upgrade simulations for IPI (Item Priority Index) calculation. This replaces the Raidbots API approach with a self-hosted solution that requires no external API keys.

### Design Goals

1. **Accuracy**: Replace wishlist percentages with actual simulation data
2. **Self-Hosted**: No external API keys or subscriptions required
3. **Async Processing**: Non-blocking simulation execution with coroutines
4. **Resilience**: Graceful fallback to wishlist-based calculations
5. **Testability**: Interface-based design for easy mocking

## Architecture

### High-Level Component Diagram

```text
┌─────────────────────────────────────────────────────────────┐
│                    FLPS Calculation Layer                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  FlpsComponentCalculator.calculateUVWithSimulation() │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Application Layer                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  UpgradeValueCalculator                              │  │
│  │  - calculateUpgradeValue()                           │  │
│  │  - hasSimulationData()                               │  │
│  └────────────────────┬─────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  SimulationService                                   │  │
│  │  - submitSimulation()                                │  │
│  │  - processResults()                                  │  │
│  └────────────────────┬─────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ProfileGeneratorService                             │  │
│  │  - generateProfile()                                 │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│              Infrastructure Layer                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  DockerSimulationExecutor                            │  │
│  │  - execute(request)                                  │  │
│  │  - buildDockerCommand()                              │  │
│  │  - parseSimulationResults()                          │  │
│  └────────────────────┬─────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  JdbcSimulationRepository                            │  │
│  │  - saveProfile() / findProfileByCharacter()          │  │
│  │  - saveRequest() / findPendingRequests()             │  │
│  │  - saveResult() / findLatestResultForItem()          │  │
│  └────────────────────┬─────────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                 Docker (simulationcraftorg/simc)            │
│                  (Local Container Execution)                │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Domain Models

```kotlin
// SimulationProfile - Value object for SimC profile content
@ConsistentCopyVisibility
data class SimulationProfile private constructor(
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val profileContent: String,
    val createdAt: Instant
) {
    val characterIdentifier: String
        get() = "$characterName-$characterRealm"

    companion object {
        fun create(...): SimulationProfile
    }
}

// SimulationResult - Value object for DPS/HPS results
@ConsistentCopyVisibility
data class SimulationResult private constructor(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val simulatedAt: Instant
) {
    val isUpgrade: Boolean get() = dpsGain > 0
    fun normalizedUpgradeValue(maxPercentGain: Double = 10.0): Double
}

// SimulationRequest - Entity for tracking simulation jobs
@ConsistentCopyVisibility
data class SimulationRequest private constructor(
    val id: Long? = null,
    val profile: SimulationProfile,
    val iterations: Int,
    val fightLengthSeconds: Int,
    val status: SimulationStatus,
    val submittedAt: Instant,
    val completedAt: Instant?,
    val results: List<SimulationResult>,
    val errorMessage: String?
) {
    fun withId(id: Long): SimulationRequest
    fun markRunning(): SimulationRequest
    fun markCompleted(results: List<SimulationResult>): SimulationRequest
    fun markFailed(errorMessage: String): SimulationRequest
}

enum class SimulationStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}
```

### 2. Repository Interface

```kotlin
interface SimulationRepository {
    fun saveProfile(profile: SimulationProfile): Pair<Long, SimulationProfile>
    fun findProfileById(id: Long): SimulationProfile?
    fun findProfileByCharacter(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): SimulationProfile?

    fun saveRequest(request: SimulationRequest): SimulationRequest
    fun findRequestById(id: Long): SimulationRequest?
    fun findPendingRequests(): List<SimulationRequest>

    fun saveResult(profileId: Long, result: SimulationResult)
    fun findLatestResultForItem(profileId: Long, itemId: Long): SimulationResult?
    fun findResultsByProfile(profileId: Long): List<SimulationResult>
}
```

### 3. Simulation Executor Interface

```kotlin
interface SimulationExecutor {
    suspend fun execute(request: SimulationRequest): Result<List<SimulationResult>>
}
```

### 4. Docker Execution

**Docker command pattern:**
```bash
docker run --rm \
  -v /path/to/profiles:/simc/profiles \
  simulationcraftorg/simc \
  /simc/profiles/character.simc \
  iterations=10000 \
  max_time=300 \
  json2=/simc/profiles/results.json
```

**DockerSimulationExecutor implementation:**
```kotlin
@Component
class DockerSimulationExecutor(
    @Value("\${simulation.docker.image:simulationcraftorg/simc}")
    private val dockerImage: String,
    @Value("\${simulation.docker.profile-directory:./simc-profiles}")
    private val profileDirectory: String,
    @Value("\${simulation.docker.timeout-minutes:30}")
    private val timeoutMinutes: Long,
    @Value("\${simulation.docker.command:docker}")
    private val dockerCommand: String
) : SimulationExecutor {

    override suspend fun execute(request: SimulationRequest): Result<List<SimulationResult>> {
        // 1. Write profile to file
        // 2. Build Docker command
        // 3. Execute with ProcessBuilder
        // 4. Parse JSON results
        // 5. Return results or failure
    }
}
```

## Database Schema

```sql
-- V0020__add_simulation_tables.sql

CREATE TABLE simulation_profiles (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    profile_content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(guild_id, character_name, character_realm)
);

CREATE TABLE simulation_requests (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id),
    iterations INT NOT NULL DEFAULT 10000,
    fight_length_seconds INT NOT NULL DEFAULT 300,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT
);

CREATE TABLE simulation_results (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES simulation_profiles(id),
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    slot VARCHAR(50) NOT NULL,
    dps_gain DOUBLE PRECISION NOT NULL,
    percent_gain DOUBLE PRECISION NOT NULL,
    simulated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_sim_profiles_char ON simulation_profiles(guild_id, character_name, character_realm);
CREATE INDEX idx_sim_requests_status ON simulation_requests(status, submitted_at);
CREATE INDEX idx_sim_results_profile ON simulation_results(profile_id, item_id);
CREATE INDEX idx_sim_results_item ON simulation_results(item_id, simulated_at DESC);
```

## Configuration

```yaml
# application.yml
simulation:
  docker:
    image: simulationcraftorg/simc
    profile-directory: ./simc-profiles
    timeout-minutes: 30
    command: docker  # Can be overridden for testing
```

```yaml
# docker-compose.yml
data-sync:
  volumes:
    - simc_profiles:/home/gradle/project/simc-profiles
    - /var/run/docker.sock:/var/run/docker.sock  # For Docker-in-Docker
  environment:
    SIMULATION_DOCKER_IMAGE: ${SIMULATION_DOCKER_IMAGE:-simulationcraftorg/simc}
    SIMULATION_DOCKER_PROFILE_DIR: /home/gradle/project/simc-profiles
    SIMULATION_DOCKER_TIMEOUT_MINUTES: ${SIMULATION_DOCKER_TIMEOUT_MINUTES:-30}
```

## UV Calculation Flow

```text
1. FlpsComponentCalculator.calculateUVWithSimulation(guildId, characterName, realm, itemId, wishlist)
   │
   ├─► Check if UpgradeValueCalculator is injected
   │   │
   │   ├─► Yes: Call upgradeValueCalculator.calculateUpgradeValue(...)
   │   │       │
   │   │       ├─► Find profile by character
   │   │       │   │
   │   │       │   ├─► Found: Look up latest simulation result for item
   │   │       │   │       │
   │   │       │   │       ├─► Found: Return normalized UV from simulation
   │   │       │   │       │
   │   │       │   │       └─► Not Found: Fall back to wishlist percentage
   │   │       │   │
   │   │       │   └─► Not Found: Fall back to wishlist percentage
   │   │       │
   │   │       └─► Return UpgradeValue(0.0-1.0)
   │   │
   │   └─► No: Fall back to calculateUV(wishlist, itemId)
   │
   └─► Return UpgradeValue
```

## Testing Strategy

### Unit Tests
- `SimulationProfileTest` - Value object validation
- `SimulationResultTest` - DPS gain calculations, normalization
- `SimulationRequestTest` - State machine transitions
- `ProfileGeneratorServiceTest` - SimC profile generation
- `UpgradeValueCalculatorTest` - UV calculation with mocked repository

### Integration Tests
- `JdbcSimulationRepositoryTest` - Database operations with mocked JdbcTemplate
- `DockerSimulationExecutorTest` - Docker command building, JSON parsing

### Test Coverage Target
- 96%+ instruction coverage
- 92%+ branch coverage

## Error Handling

| Error | Handling |
|-------|----------|
| Docker unavailable | Log error, return failure, fall back to wishlist |
| Simulation timeout | Terminate process, mark FAILED, fall back to wishlist |
| Invalid JSON output | Log parse error, mark FAILED, fall back to wishlist |
| Profile not found | Fall back to wishlist percentage |
| Repository error | Log error, fall back to wishlist |

## Security Considerations

- Docker socket mounted read-write (required for container execution)
- Profile files stored in isolated volume
- No sensitive data in SimC profiles (only gear IDs)
- Timeout prevents runaway processes
