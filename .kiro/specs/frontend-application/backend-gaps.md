# Backend Gaps Analysis

## Overview

This document identifies all backend work required before frontend development and integration support for Discord bot and future WoW addon.

---

## Current Backend Status

### What's Complete (44 Controllers)

**Core Domain APIs:**
- ✅ `FlpsController` - FLPS score calculations and reports
- ✅ `FlpsModifierController` - Guild-specific FLPS configuration
- ✅ `RaiderController` - Full CRUD for raiders
- ✅ `LootAwardController` - Loot award management
- ✅ `LootController`, `LootBanController` - Loot and ban management
- ✅ `BehavioralActionController` - Behavioral action management
- ✅ `AttendanceController`, `AttendanceStatController` - Attendance tracking
- ✅ `GuildController`, `GuildConfigurationController` - Guild management
- ✅ `WishlistController`, `WishlistSnapshotController` - Wishlist management
- ✅ `RaidController`, `RaidEncounterController`, `RaidSignupController` - Raid management
- ✅ `SimulationController` - SimulationCraft integration

**Character Data APIs:**
- ✅ `RaiderEntityController` - Core raider data
- ✅ `RaiderGearItemController`, `GearController` - Gear management
- ✅ `RaiderStatisticsController` - Statistics
- ✅ `RaiderWarcraftLogController` - Warcraft Logs data
- ✅ `RaiderVaultSlotController`, `RaiderCrestCountController` - Weekly vault/crests
- ✅ `RaiderRaidProgressController` - Raid progress
- ✅ `RaiderPvpBracketController`, `RaiderRenownController` - PvP/Renown
- ✅ `RaiderTrackItemController` - Track items

**Supporting APIs:**
- ✅ `ApplicationController`, `ApplicationAltController` - Guild applications
- ✅ `ApplicationQuestionController`, `ApplicationQuestionFileController` - Application questions
- ✅ `TeamMetadataController`, `TeamRaidDayController` - Team management
- ✅ `CharacterHistoryController` - Character history
- ✅ `HistoricalActivityController` - Activity logs
- ✅ `SyncRunController` - Data sync tracking
- ✅ `WoWAuditSnapshotController` - WoWAudit snapshots
- ✅ `PeriodSnapshotController` - Period snapshots
- ✅ `GuestController` - Guest management

**GraphQL Layer:**
- ✅ Query resolvers: Raider, Guild, FLPS, Loot, Attendance
- ✅ Mutation resolvers: Raider CRUD, Loot award/revoke
- ✅ Subscription resolvers: Loot events via WebSocket
- ✅ DataLoaders for N+1 prevention
- ✅ Query complexity/depth limiting
- ✅ Error handling with error codes

**Security Infrastructure:**
- ✅ JWT token validation and generation
- ✅ Role-based access control (GUILD_ADMIN, SYSTEM_ADMIN)
- ✅ Admin mode bypass for development
- ✅ Rate limiting (100 reads/sec, 20 writes/sec)

---

## Critical Backend Gaps

### GAP 1: Discord User Linking (Required for Discord Bot + Frontend)

**Problem:** No way to link Discord users to WoW characters

**Database Migration Required:**
```sql
-- V0021__add_discord_user_links.sql

CREATE TABLE discord_user_links (
    id BIGSERIAL PRIMARY KEY,
    discord_user_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    raider_id BIGINT REFERENCES raiders(id),
    is_primary BOOLEAN DEFAULT false,
    linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    linked_by VARCHAR(255),
    UNIQUE(discord_user_id, character_name, character_realm)
);

CREATE INDEX idx_discord_links_user_id ON discord_user_links(discord_user_id);
CREATE INDEX idx_discord_links_raider_id ON discord_user_links(raider_id);
```

**API Endpoints Needed:**
```
POST   /api/v1/discord/links                     # Create link
GET    /api/v1/discord/links/user/{discordUserId} # Get links by Discord user
GET    /api/v1/discord/links/raider/{raiderId}   # Get links by raider
DELETE /api/v1/discord/links/{id}                # Remove link
```

**Domain Objects:**
- `DiscordUserLink` entity
- `DiscordUserLinkRepository` interface
- `JdbcDiscordUserLinkRepository` implementation
- `InMemoryDiscordUserLinkRepository` for tests
- `DiscordLinkingService` service
- `DiscordUserLinkController` REST controller

**Effort:** 2-3 days

---

### GAP 2: OAuth2 Authentication Endpoints (Required for Frontend)

**Problem:** No endpoints for OAuth2 authentication flow

**Current State:**
- JWT service exists for token generation/validation
- No OAuth2 provider integration
- No authentication endpoints

**API Endpoints Needed:**
```
GET    /api/v1/auth/discord/url          # Get Discord OAuth URL
POST   /api/v1/auth/discord/callback     # Exchange Discord code for JWT
GET    /api/v1/auth/battlenet/url        # Get Battle.net OAuth URL
POST   /api/v1/auth/battlenet/callback   # Exchange Battle.net code for JWT
GET    /api/v1/auth/me                   # Get current user profile
POST   /api/v1/auth/refresh              # Refresh JWT token
POST   /api/v1/auth/logout               # Logout (invalidate refresh token)
```

**Database Migration Required:**
```sql
-- V0022__add_users_table.sql

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    discord_id VARCHAR(255) UNIQUE,
    battlenet_id VARCHAR(255) UNIQUE,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    avatar_url VARCHAR(512),
    role VARCHAR(50) NOT NULL DEFAULT 'RAIDER',
    guild_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

CREATE TABLE user_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_discord_id ON users(discord_id);
CREATE INDEX idx_users_battlenet_id ON users(battlenet_id);
CREATE INDEX idx_refresh_tokens_user_id ON user_refresh_tokens(user_id);
```

**Domain Objects:**
- `User` entity
- `UserRepository` interface
- `OAuth2Service` - Discord/Battle.net OAuth2 flows
- `AuthenticationService` - User authentication logic
- `AuthController` REST controller

**Configuration:**
```yaml
oauth2:
  discord:
    client-id: ${DISCORD_CLIENT_ID}
    client-secret: ${DISCORD_CLIENT_SECRET}
    redirect-uri: ${DISCORD_REDIRECT_URI}
    scopes: identify,guilds
  battlenet:
    client-id: ${BATTLENET_CLIENT_ID}
    client-secret: ${BATTLENET_CLIENT_SECRET}
    redirect-uri: ${BATTLENET_REDIRECT_URI}
    region: us
```

**Effort:** 3-4 days

---

### GAP 3: User-Character Mapping (Required for Frontend)

**Problem:** Need to map authenticated users to their WoW characters

**API Endpoints Needed:**
```
GET    /api/v1/users/me/characters        # Get user's linked characters
POST   /api/v1/users/me/characters        # Link new character
DELETE /api/v1/users/me/characters/{id}   # Unlink character
PUT    /api/v1/users/me/characters/{id}/primary  # Set primary character
```

**Database Migration Required:**
```sql
-- V0023__add_user_character_mappings.sql

CREATE TABLE user_character_mappings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    raider_id BIGINT NOT NULL REFERENCES raiders(id),
    is_primary BOOLEAN DEFAULT false,
    linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified BOOLEAN DEFAULT false,
    UNIQUE(user_id, raider_id)
);

CREATE INDEX idx_user_char_user_id ON user_character_mappings(user_id);
CREATE INDEX idx_user_char_raider_id ON user_character_mappings(raider_id);
```

**Effort:** 1-2 days

---

### GAP 4: Discord Notification Configuration (Required for Discord Bot)

**Problem:** No way to configure notification channels per guild

**Database Migration Required:**
```sql
-- V0024__add_discord_notifications.sql

CREATE TABLE discord_notification_configs (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    discord_server_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(guild_id, notification_type)
);

-- notification_type values: LOOT_AWARD, RDF_EXPIRY, PENALTY, LOOT_BAN, SYNC_COMPLETE
```

**API Endpoints Needed:**
```
GET    /api/v1/guilds/{guildId}/discord/config       # Get notification config
PUT    /api/v1/guilds/{guildId}/discord/config       # Update notification config
POST   /api/v1/guilds/{guildId}/discord/config/test  # Send test notification
```

**Effort:** 1-2 days

---

### GAP 5: Real-time WebSocket Events (Enhancement for Frontend)

**Problem:** GraphQL subscriptions exist but REST WebSocket events missing

**Current State:**
- GraphQL subscriptions work for loot events
- No dedicated WebSocket endpoint for dashboard updates

**WebSocket Endpoints Needed:**
```
WS     /ws/events                        # Main event stream
       - flpsScoreUpdated(raiderId, newScore)
       - lootAwarded(awardId, itemName, recipientName)
       - rdfExpired(raiderId, itemName)
       - penaltyApplied(raiderId, type, reason)
       - syncCompleted(guildId, syncType)
```

**Implementation:**
- Spring WebSocket with STOMP
- Event publisher service
- Client subscription management

**Effort:** 2-3 days

---

### GAP 6: Leaderboard Filtering (Enhancement for Discord Bot)

**Problem:** Leaderboard needs role/class/eligibility filters

**Current Endpoint:**
```
GET /api/v1/flps/guilds/{guildId}/report
```

**Enhanced Endpoint:**
```
GET /api/v1/flps/guilds/{guildId}/leaderboard
    ?role=dps|healer|tank
    &class=warrior|mage|...
    &eligible=true|false
    &limit=10
    &offset=0
```

**Effort:** 1 day

---

### GAP 7: Configuration Preview (Enhancement for Admin Panel)

**Problem:** Admin panel needs to preview config changes before saving

**API Endpoints Needed:**
```
POST   /api/v1/guilds/{guildId}/config/preview
       Body: { modifiedConfig }
       Response: { previewReport with affected raiders and score changes }
```

**Effort:** 1-2 days

---

## Implementation Priority

### Phase 1: Foundation (Week 1-2)
**Required for both Discord Bot and Frontend**

1. **V0021: Discord User Links** - 2-3 days
   - Migration, entity, repository, service, controller
   - Full test coverage

2. **V0022: Users Table** - 1 day
   - Migration for user accounts
   - Basic CRUD

3. **OAuth2 Authentication** - 3-4 days
   - Discord OAuth2 integration
   - Battle.net OAuth2 integration
   - JWT token generation
   - Auth controller

### Phase 2: User Management (Week 2-3)
**Required for Frontend**

4. **V0023: User-Character Mappings** - 1-2 days
   - Migration, entity, repository
   - Character linking endpoints

5. **V0024: Discord Notification Config** - 1-2 days
   - Migration, entity, repository
   - Configuration endpoints

### Phase 3: Enhancements (Week 3-4)
**Improves UX for both integrations**

6. **Leaderboard Filtering** - 1 day
   - Add query parameters
   - Update service layer

7. **Configuration Preview** - 1-2 days
   - Preview endpoint
   - Score calculation preview

8. **WebSocket Events** - 2-3 days
   - STOMP configuration
   - Event publisher
   - Client management

---

## Backend Work Summary

| Gap | Description | Effort | Priority | Blocks |
|-----|-------------|--------|----------|--------|
| GAP 1 | Discord User Links | 2-3 days | P0 | Discord Bot, Frontend |
| GAP 2 | OAuth2 Authentication | 3-4 days | P0 | Frontend |
| GAP 3 | User-Character Mapping | 1-2 days | P1 | Frontend |
| GAP 4 | Notification Config | 1-2 days | P1 | Discord Bot |
| GAP 5 | WebSocket Events | 2-3 days | P2 | Real-time Dashboard |
| GAP 6 | Leaderboard Filters | 1 day | P2 | Discord Bot Commands |
| GAP 7 | Config Preview | 1-2 days | P3 | Admin Panel UX |

**Total Backend Effort: 2-3 weeks**

---

## WoW Addon Notes

**Current Status:** No specification exists

**Required for WoW Addon:**
- Addon specification document needs to be created
- Would likely need additional APIs for in-game data display
- May require specialized binary protocol for game client
- RC Loot Council export format support
- This is a future consideration, not blocking current work

**Recommendation:** Create WoW addon specification after frontend is complete, as frontend work will validate API design and reveal any missing requirements.

---

## Recommendation

**Start with Backend Gaps Phase 1** before frontend development:

1. Implement Discord User Linking (GAP 1) - shared by Discord bot and Frontend
2. Implement OAuth2 Authentication (GAP 2) - required for Frontend
3. Then proceed with Frontend development
4. Discord Bot can be developed in parallel after GAP 1 is complete
