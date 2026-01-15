# Design Document - EdgeRush LootMan Unified Platform

## System Architecture

### High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              USER LAYER                                   │
├────────────────┬────────────────┬────────────────┬───────────────────────┤
│   Web Browser  │ Discord Client │  WoW Client    │   Desktop Client      │
│   (Vue 3 SPA)  │    (Bot)       │   (Addon)      │     (Tauri)           │
└────────┬───────┴───────┬────────┴────────┬───────┴───────────┬───────────┘
         │               │                 │                   │
         │ HTTPS         │ HTTPS           │ File I/O          │ HTTPS
         │               │                 │                   │
         ▼               ▼                 ▼                   ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY                                    │
│              (Nginx - SSL termination, routing, rate limiting)            │
└───────────────────────────────────┬──────────────────────────────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         │                          │                          │
         ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   GraphQL API   │    │   WebSocket     │
│   /api/*        │    │   /graphql      │    │   /ws/*         │
└────────┬────────┘    └────────┬────────┘    └────────┬────────┘
         │                      │                      │
         └──────────────────────┼──────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │   APPLICATION LAYER    │
                    │   (Spring Boot 3.x)    │
                    │   Use Cases, Services  │
                    └───────────┬───────────┘
                                │
         ┌──────────────────────┼──────────────────────────┐
         │                      │                          │
         ▼                      ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  DOMAIN LAYER   │    │ INFRASTRUCTURE  │    │ EXTERNAL APIs   │
│ FLPS, Loot, etc │    │ Repositories    │    │ Blizzard, WCL   │
└─────────────────┘    └────────┬────────┘    │ Raider.IO       │
                                │             └─────────────────┘
                                ▼
                    ┌───────────────────────┐
                    │      PostgreSQL       │
                    │   Primary Database    │
                    └───────────────────────┘
```

---

## Component Architecture

### Backend Services

```
data-sync-service/
├── api/                          # API Layer
│   ├── rest/                     # REST Controllers
│   │   ├── raider/               # Raider endpoints
│   │   ├── loot/                 # Loot endpoints
│   │   ├── attendance/           # Attendance endpoints
│   │   ├── raid/                 # Raid endpoints
│   │   ├── application/          # Application endpoints
│   │   └── admin/                # Admin endpoints
│   │
│   ├── graphql/                  # GraphQL Layer
│   │   ├── query/                # Query resolvers
│   │   ├── mutation/             # Mutation resolvers
│   │   ├── subscription/         # Subscription resolvers
│   │   └── dataloader/           # DataLoaders
│   │
│   └── websocket/                # WebSocket Layer
│       ├── FlpsUpdateHandler     # FLPS score updates
│       ├── LootAwardHandler      # Loot award notifications
│       └── RaidSignupHandler     # Raid signup updates
│
├── application/                  # Application Layer (Use Cases)
│   ├── flps/                     # FLPS calculation use cases
│   ├── loot/                     # Loot management use cases
│   ├── attendance/               # Attendance use cases
│   ├── simulation/               # Simulation use cases
│   ├── application/              # Guild application use cases
│   └── sync/                     # External data sync use cases
│
├── domain/                       # Domain Layer
│   ├── flps/                     # FLPS domain model
│   ├── raider/                   # Raider domain model
│   ├── loot/                     # Loot domain model
│   ├── raid/                     # Raid domain model
│   └── shared/                   # Shared value objects
│
└── infrastructure/               # Infrastructure Layer
    ├── persistence/              # Database repositories
    ├── blizzard/                 # Blizzard API client
    ├── warcraftlogs/             # Warcraft Logs client
    ├── raiderio/                 # Raider.IO client
    ├── wowaudit/                 # WoWAudit client
    ├── simulation/               # SimC Docker integration
    └── cache/                    # Caching infrastructure
```

### Frontend Architecture

```
web-dashboard/
├── src/
│   ├── views/                    # Page components
│   │   ├── DashboardView.vue     # Personal dashboard
│   │   ├── LeaderboardView.vue   # Guild leaderboard
│   │   ├── LootHistoryView.vue   # Loot history
│   │   ├── PerformanceView.vue   # Performance metrics
│   │   ├── AttendanceView.vue    # Attendance tracking
│   │   ├── WishlistView.vue      # Wishlist management
│   │   ├── RaidPlanView.vue      # Raid planning
│   │   ├── SimulationView.vue    # Simulations
│   │   ├── ApplyView.vue         # Guild application
│   │   └── admin/                # Admin views
│   │
│   ├── components/               # Reusable components
│   │   ├── common/               # Shared components
│   │   ├── dashboard/            # Dashboard widgets
│   │   ├── leaderboard/          # Leaderboard components
│   │   ├── raidplan/             # Raid planning components
│   │   ├── simulation/           # Simulation components
│   │   └── admin/                # Admin components
│   │
│   ├── stores/                   # Pinia stores
│   │   ├── auth.ts               # Authentication state
│   │   ├── user.ts               # Current user state
│   │   ├── flps.ts               # FLPS data state
│   │   ├── raid.ts               # Raid data state
│   │   └── websocket.ts          # WebSocket state
│   │
│   ├── composables/              # Vue composables
│   │   ├── useAuth.ts            # Authentication logic
│   │   ├── useFlps.ts            # FLPS data fetching
│   │   ├── useWebSocket.ts       # WebSocket connection
│   │   └── useWowheadTooltip.ts  # Wowhead integration
│   │
│   ├── services/                 # API services
│   │   ├── api.ts                # Base API client
│   │   ├── flpsService.ts        # FLPS API calls
│   │   ├── raidService.ts        # Raid API calls
│   │   └── simulationService.ts  # Simulation API calls
│   │
│   └── router/                   # Vue Router config
│       ├── index.ts              # Route definitions
│       └── guards/               # Navigation guards
```

---

## Data Flow Diagrams

### FLPS Calculation Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        FLPS CALCULATION FLOW                            │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  WoWAudit   │────▶│ Attendance  │────▶│    ACS      │
│   Sync      │     │   Data      │     │  (0-1.0)    │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
┌─────────────┐     ┌─────────────┐     ┌──────▼──────┐
│  Warcraft   │────▶│ Performance │────▶│    MAS      │
│   Logs      │     │   Data      │     │  (0-1.0)    │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
┌─────────────┐     ┌─────────────┐     ┌──────▼──────┐     ┌─────────────┐
│  WoWAudit   │────▶│ Preparation │────▶│    EPS      │────▶│    RMS      │
│  Vault/M+   │     │   Data      │     │  (0-1.0)    │     │  (0-1.0)    │
└─────────────┘     └─────────────┘     └─────────────┘     └──────┬──────┘
                                                                    │
┌─────────────┐     ┌─────────────┐     ┌─────────────┐            │
│  SimC/      │────▶│  Upgrade    │────▶│     UV      │            │
│  WoWAudit   │     │   Value     │     │  (0-1.0)    │            │
└─────────────┘     └─────────────┘     └──────┬──────┘            │
                                               │                    │
┌─────────────┐     ┌─────────────┐     ┌──────▼──────┐     ┌──────▼──────┐
│  Character  │────▶│  Tier Set   │────▶│    IPI      │────▶│    FLPS     │
│   Data      │     │   Status    │     │  (0-1.0)    │     │  (0-1.0)    │
└─────────────┘     └─────────────┘     └─────────────┘     └──────┬──────┘
                                                                    │
┌─────────────┐     ┌─────────────┐     ┌─────────────┐            │
│   Loot      │────▶│  Recent     │────▶│    RDF      │────────────┘
│  History    │     │  Awards     │     │  (0-1.0)    │
└─────────────┘     └─────────────┘     └─────────────┘
```

### Real-Time Sync Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       ADDON ←→ WEB SYNC FLOW                            │
└─────────────────────────────────────────────────────────────────────────┘

┌───────────────┐          ┌───────────────┐          ┌───────────────┐
│   WoW Addon   │          │Desktop Client │          │    Backend    │
│               │          │    (Tauri)    │          │               │
└───────┬───────┘          └───────┬───────┘          └───────┬───────┘
        │                          │                          │
        │  SavedVariables          │                          │
        │──────────────────▶       │                          │
        │                          │                          │
        │                          │  HTTP POST /api/sync     │
        │                          │─────────────────────────▶│
        │                          │                          │
        │                          │     Process & Store      │
        │                          │        ───────           │
        │                          │                          │
        │                          │     FLPS Data            │
        │                          │◀─────────────────────────│
        │                          │                          │
        │  Write SavedVariables    │                          │
        │◀──────────────────────   │                          │
        │                          │                          │
        │  /reload                 │                          │
        │    ─────                 │                          │
        │                          │                          │
        │  Read FLPS Data          │                          │
        │    ───────────           │                          │
        │                          │                          │
┌───────▼───────┐          ┌───────▼───────┐          ┌───────▼───────┐
│  Display FLPS │          │  Show Status  │          │ Broadcast WS  │
│  in Tooltip   │          │  "Synced ✓"   │          │ to Web Users  │
└───────────────┘          └───────────────┘          └───────────────┘
```

### Application Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      APPLICATION PROCESSING FLOW                        │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Applicant   │     │   OAuth     │     │  Character  │     │    Form     │
│  Opens Form │────▶│  Connect    │────▶│  Selection  │────▶│  Questions  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                          │                    │                    │
                          ▼                    ▼                    │
                    ┌─────────────┐     ┌─────────────┐            │
                    │  Discord ID │     │ Blizzard ID │            │
                    │  Battle.net │     │  + Chars    │            │
                    └─────────────┘     └─────────────┘            │
                                               │                    │
                    ┌──────────────────────────┴────────────────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │   Submit Application │
         └──────────┬──────────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  Warcraft   │ │  Raider.IO  │ │  Blizzard   │
│   Logs      │ │    API      │ │    API      │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │               │               │
       └───────────────┼───────────────┘
                       ▼
             ┌─────────────────────┐
             │  Enriched Application│
             │  with Performance    │
             │  Analysis            │
             └──────────┬──────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   Officer   │ │   Discord   │ │    Email    │
│   Review    │ │   Notify    │ │   Notify    │
└──────┬──────┘ └─────────────┘ └─────────────┘
       │
       ▼
┌─────────────────────┐
│   Approve/Decline   │
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    ▼             ▼
┌────────┐   ┌────────┐
│ Trial  │   │ Notify │
│ Create │   │ Result │
└────────┘   └────────┘
```

---

## Database Schema Extensions

### New Entities for Platform

```sql
-- Raid Plans
CREATE TABLE raid_plans (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    encounter_id BIGINT,
    visibility VARCHAR(20) DEFAULT 'GUILD',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE raid_plan_steps (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT REFERENCES raid_plans(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    title VARCHAR(100),
    data JSONB NOT NULL  -- Canvas state
);

-- Cooldown Assignments
CREATE TABLE cooldown_assignments (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT REFERENCES raid_plans(id) ON DELETE CASCADE,
    raider_id BIGINT REFERENCES raiders(id),
    ability_id BIGINT NOT NULL,
    time_offset_seconds INT NOT NULL,
    notes TEXT
);

-- Simulation Jobs
CREATE TABLE simulation_jobs (
    id BIGSERIAL PRIMARY KEY,
    raider_id BIGINT REFERENCES raiders(id),
    job_type VARCHAR(50) NOT NULL,  -- DROPTIMIZER, TOP_GEAR, GEAR_COMPARE
    status VARCHAR(20) DEFAULT 'PENDING',
    input_data JSONB NOT NULL,
    result_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Applications
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    discord_user_id VARCHAR(50),
    battlenet_id VARCHAR(100),
    character_name VARCHAR(50) NOT NULL,
    realm VARCHAR(100) NOT NULL,
    region VARCHAR(10) NOT NULL,
    character_class VARCHAR(50),
    specialization VARCHAR(50),
    item_level INT,
    mythic_plus_score INT,
    performance_data JSONB,
    name VARCHAR(100),
    age INT,
    timezone VARCHAR(50),
    raid_availability JSONB,
    previous_guild TEXT,
    reason_for_leaving TEXT,
    why_this_guild TEXT,
    what_you_bring TEXT,
    goals TEXT,
    stable_internet BOOLEAN,
    alt_characters JSONB,
    submitted_at TIMESTAMP DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(id),
    decline_reason TEXT
);

CREATE TABLE application_notes (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT REFERENCES applications(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    is_private BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Trials
CREATE TABLE trials (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT REFERENCES applications(id),
    raider_id BIGINT REFERENCES raiders(id),
    guild_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    extension_count INT DEFAULT 0,
    attendance_percent DECIMAL(5,2),
    average_parse DECIMAL(5,2),
    deaths_per_pull DECIMAL(5,2),
    mechanics_score DECIMAL(5,2),
    resolved_at TIMESTAMP,
    resolved_by BIGINT REFERENCES users(id),
    resolution TEXT
);

-- Addon Sync Data
CREATE TABLE addon_sync_data (
    id BIGSERIAL PRIMARY KEY,
    raider_id BIGINT REFERENCES raiders(id),
    gear_data JSONB,
    bag_data JSONB,
    talent_data JSONB,
    synced_at TIMESTAMP DEFAULT NOW()
);

-- API Cache
CREATE TABLE api_cache (
    cache_key VARCHAR(500) PRIMARY KEY,
    data JSONB NOT NULL,
    source VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_api_cache_expires ON api_cache(expires_at);
```

---

## Security Design

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         OAUTH2 AUTHENTICATION                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Browser   │     │   Backend   │     │  Discord/   │     │  Database   │
│             │     │             │     │  Battle.net │     │             │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │
       │  Click Login      │                   │                   │
       │──────────────────▶│                   │                   │
       │                   │                   │                   │
       │  Redirect to OAuth│                   │                   │
       │◀──────────────────│                   │                   │
       │                   │                   │                   │
       │  Authorization Request               │                   │
       │──────────────────────────────────────▶│                   │
       │                   │                   │                   │
       │  User Consents    │                   │                   │
       │◀──────────────────────────────────────│                   │
       │                   │                   │                   │
       │  Callback with Code                  │                   │
       │──────────────────▶│                   │                   │
       │                   │                   │                   │
       │                   │  Exchange Code   │                   │
       │                   │──────────────────▶│                   │
       │                   │                   │                   │
       │                   │  Access Token    │                   │
       │                   │◀──────────────────│                   │
       │                   │                   │                   │
       │                   │  Fetch User Info │                   │
       │                   │──────────────────▶│                   │
       │                   │                   │                   │
       │                   │  User Data       │                   │
       │                   │◀──────────────────│                   │
       │                   │                   │                   │
       │                   │  Create/Update User                  │
       │                   │──────────────────────────────────────▶│
       │                   │                   │                   │
       │                   │  Generate JWT    │                   │
       │                   │                   │                   │
       │  Set JWT Cookie   │                   │                   │
       │◀──────────────────│                   │                   │
       │                   │                   │                   │
       │  Redirect to App  │                   │                   │
       │◀──────────────────│                   │                   │
```

### Authorization Model

```kotlin
// Role hierarchy
enum class Role {
    GUEST,      // Unauthenticated
    APPLICANT,  // Has pending application
    TRIAL,      // In trial period
    RAIDER,     // Full raider
    OFFICER,    // Can manage raiders, applications
    ADMIN       // Full access
}

// Permission definitions
enum class Permission {
    // Read permissions
    VIEW_OWN_FLPS,
    VIEW_GUILD_LEADERBOARD,
    VIEW_GUILD_RAIDS,
    VIEW_LOOT_HISTORY,
    VIEW_APPLICATIONS,

    // Write permissions
    SIGN_UP_FOR_RAIDS,
    MANAGE_WISHLIST,
    CREATE_RAID_PLANS,
    MANAGE_APPLICATIONS,
    CREATE_BEHAVIORAL_ACTIONS,
    CREATE_LOOT_BANS,
    CONFIGURE_GUILD,
    MANAGE_USERS
}

// Role to permission mapping
val rolePermissions = mapOf(
    Role.GUEST to setOf(),
    Role.APPLICANT to setOf(Permission.VIEW_OWN_FLPS),
    Role.TRIAL to setOf(
        Permission.VIEW_OWN_FLPS,
        Permission.VIEW_GUILD_LEADERBOARD,
        Permission.VIEW_GUILD_RAIDS,
        Permission.VIEW_LOOT_HISTORY,
        Permission.SIGN_UP_FOR_RAIDS,
        Permission.MANAGE_WISHLIST
    ),
    Role.RAIDER to setOf(
        // All TRIAL permissions plus:
        Permission.CREATE_RAID_PLANS
    ),
    Role.OFFICER to setOf(
        // All RAIDER permissions plus:
        Permission.VIEW_APPLICATIONS,
        Permission.MANAGE_APPLICATIONS,
        Permission.CREATE_BEHAVIORAL_ACTIONS,
        Permission.CREATE_LOOT_BANS
    ),
    Role.ADMIN to Permission.values().toSet()
)
```

---

## API Design

### REST API Conventions

```yaml
# Base URL: /api/v1

# Standard CRUD pattern
GET    /raiders              # List (paginated)
POST   /raiders              # Create
GET    /raiders/{id}         # Read
PUT    /raiders/{id}         # Update
DELETE /raiders/{id}         # Delete

# Relationships
GET    /raiders/{id}/loot-awards    # Nested resources
GET    /raiders/{id}/attendance

# Actions
POST   /raids/{id}/signups          # Create signup
DELETE /raids/{id}/signups/{sid}    # Remove signup
POST   /loot-awards/{id}/undo       # Special action

# Filtering and pagination
GET    /raiders?role=TANK&eligible=true&page=0&size=20&sort=flps,desc

# Current user endpoints
GET    /me/flps                     # My FLPS score
GET    /me/loot-history             # My loot history
GET    /me/attendance               # My attendance
```

### GraphQL Schema (Excerpt)

```graphql
type Query {
    # Raider queries
    raider(id: ID!): Raider
    raiders(filter: RaiderFilter, page: PageInput): RaiderConnection!
    me: Raider

    # FLPS queries
    flpsReport(guildId: String!, itemId: Long): FlpsReport!
    flpsScore(raiderId: ID!, itemId: Long!): FlpsScore

    # Raid queries
    raids(guildId: String!, upcoming: Boolean): [Raid!]!
    raid(id: ID!): Raid

    # Application queries
    applications(status: ApplicationStatus): [Application!]!
    application(id: ID!): Application
}

type Mutation {
    # Raider mutations
    createRaider(input: CreateRaiderInput!): Raider!
    updateRaider(id: ID!, input: UpdateRaiderInput!): Raider!

    # Loot mutations
    awardLoot(input: AwardLootInput!): LootAward!
    undoLootAward(id: ID!): LootAward!

    # Raid mutations
    signUpForRaid(raidId: ID!, raiderId: ID!, role: Role!): RaidSignup!
    withdrawFromRaid(raidId: ID!, raiderId: ID!): Boolean!

    # Application mutations
    submitApplication(input: ApplicationInput!): Application!
    approveApplication(id: ID!): Application!
    declineApplication(id: ID!, reason: String): Application!

    # Admin mutations
    createBehavioralAction(input: BehavioralActionInput!): BehavioralAction!
    createLootBan(input: LootBanInput!): LootBan!
}

type Subscription {
    flpsUpdated(guildId: String!): FlpsUpdate!
    lootAwarded(guildId: String!): LootAward!
    raidSignupChanged(raidId: ID!): RaidSignup!
}
```

---

## WebSocket Protocol

### Connection

```
WebSocket URL: wss://api.edgerush.gg/ws
Protocol: STOMP over WebSocket
```

### Topics

```
# FLPS updates for a guild
/topic/guild/{guildId}/flps

# Loot awards for a guild
/topic/guild/{guildId}/loot

# Raid signup updates
/topic/raid/{raidId}/signups

# User-specific notifications
/user/queue/notifications
```

### Message Formats

```json
// FLPS Update
{
    "type": "FLPS_UPDATE",
    "timestamp": "2025-01-15T12:00:00Z",
    "data": {
        "raiderId": 123,
        "raiderName": "Playername",
        "oldScore": 0.845,
        "newScore": 0.872,
        "reason": "RDF_EXPIRED"
    }
}

// Loot Award
{
    "type": "LOOT_AWARDED",
    "timestamp": "2025-01-15T12:00:00Z",
    "data": {
        "itemId": 207788,
        "itemName": "Fyrakk's Tainted Rageheart",
        "winner": "Playername",
        "flps": 0.872,
        "encounter": "Fyrakk the Blazing",
        "rationale": "Highest FLPS, tier completion priority"
    }
}
```

---

## Performance Considerations

### Caching Strategy

| Data Type | Cache Layer | TTL | Invalidation |
|-----------|-------------|-----|--------------|
| FLPS scores | Memory | 5 min | On calculation |
| Item data | Redis | 7 days | On patch |
| Character data | Redis | 1 hour | On sync |
| Raid plans | Memory | 10 min | On save |
| Leaderboard | Memory | 1 min | On FLPS change |

### Database Optimization

```sql
-- Indexes for common queries
CREATE INDEX idx_raiders_guild_eligible ON raiders(guild_id, eligible);
CREATE INDEX idx_loot_awards_raider_date ON loot_awards(raider_id, awarded_at DESC);
CREATE INDEX idx_attendance_raider_date ON attendance_records(raider_id, raid_date DESC);

-- Materialized view for leaderboard
CREATE MATERIALIZED VIEW guild_leaderboard AS
SELECT
    r.id,
    r.guild_id,
    r.character_name,
    r.character_class,
    r.role,
    fs.flps_value,
    fs.rms_value,
    fs.ipi_value,
    fs.rdf_value,
    r.eligible
FROM raiders r
JOIN flps_scores fs ON r.id = fs.raider_id
WHERE fs.is_current = true;

CREATE INDEX idx_leaderboard_guild_flps ON guild_leaderboard(guild_id, flps_value DESC);

-- Refresh periodically
REFRESH MATERIALIZED VIEW CONCURRENTLY guild_leaderboard;
```

---

## Error Handling

### Error Response Format

```json
{
    "error": {
        "code": "RAIDER_NOT_FOUND",
        "message": "Raider with ID 123 not found",
        "details": {
            "raiderId": 123
        },
        "timestamp": "2025-01-15T12:00:00Z",
        "traceId": "abc-123-def"
    }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | Invalid request data |
| UNAUTHORIZED | 401 | Authentication required |
| FORBIDDEN | 403 | Insufficient permissions |
| NOT_FOUND | 404 | Resource not found |
| CONFLICT | 409 | Resource conflict |
| RATE_LIMITED | 429 | Too many requests |
| INTERNAL_ERROR | 500 | Server error |
| EXTERNAL_API_ERROR | 502 | External API failure |
