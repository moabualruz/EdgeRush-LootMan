# Design Document - Discord Bot

## Overview

Discord bot providing automated notifications and interactive commands for FLPS data access and guild management.

## Technology Stack

**Bot Framework**: Discord.js (Node.js) or JDA (Java/Kotlin)
**Language**: Kotlin (to match existing codebase)
**Library**: JDA (Java Discord API)
**Database**: Shared PostgreSQL with main application

## Architecture

```
┌─────────────────────────────────────────┐
│         Discord Platform                │
└────────────────┬────────────────────────┘
                 │ Gateway WebSocket
┌────────────────▼────────────────────────┐
│         Discord Bot Application         │
│  ┌───────────────────────────────────┐  │
│  │  Command Handlers: FLPS, Loot,    │  │
│  │  Leaderboard, Admin, Link         │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  Services: FLPS, Notification,    │  │
│  │  Character Linking                │  │
│  └───────────────┬───────────────────┘  │
│  ┌───────────────▼───────────────────┐  │
│  │  API Client: REST calls to        │  │
│  │  Spring Boot backend              │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                 │ HTTP/REST
┌────────────────▼────────────────────────┐
│      Spring Boot Backend (API)          │
│  Existing FLPS services and data        │
└─────────────────────────────────────────┘
```

## Key Components

### Command Handlers

```kotlin
// Slash command structure
@SlashCommand(name = "flps", description = "Check FLPS score")
class FlpsCommand : CommandHandler {
    override suspend fun handle(event: SlashCommandInteractionEvent) {
        val user = event.user
        val character = characterLinkingService.getLinkedCharacter(user.id)
        val flps = flpsService.getFlpsForCharacter(character)
        
        event.replyEmbeds(createFlpsEmbed(flps)).queue()
    }
}
```

### Database Entities

```kotlin
@Table("discord_user_links")
data class DiscordUserLinkEntity(
    @Id val id: Long? = null,
    val discordUserId: String,
    val characterName: String,
    val characterRealm: String,
    val isPrimary: Boolean,
    val linkedAt: Instant,
    val linkedBy: String?
)

@Table("discord_notifications")
data class DiscordNotificationEntity(
    @Id val id: Long? = null,
    val guildId: String,
    val notificationType: String,
    val channelId: String,
    val enabled: Boolean
)
```

### Configuration

```yaml
discord:
  bot:
    token: ${DISCORD_BOT_TOKEN}
    application-id: ${DISCORD_APPLICATION_ID}
  
  guilds:
    - guild-id: "123456789"
      discord-server-id: "987654321"
      notification-channels:
        loot-awards: "channel-id-1"
        rdf-expiry: "channel-id-2"
        penalties: "channel-id-3"
  
  commands:
    enabled: true
    admin-role-ids:
      - "admin-role-id"
    officer-role-ids:
      - "officer-role-id"
  
  notifications:
    loot-awards:
      enabled: true
      include-runner-ups: 3
    rdf-expiry:
      enabled: true
      dm-users: true
    penalties:
      enabled: true
      dm-users: true
```

## Database Migration

```sql
-- V0019__add_discord_tables.sql

CREATE TABLE discord_user_links (
    id BIGSERIAL PRIMARY KEY,
    discord_user_id VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    character_realm VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    linked_at TIMESTAMP NOT NULL,
    linked_by VARCHAR(255),
    UNIQUE(discord_user_id, character_name, character_realm)
);

CREATE TABLE discord_notifications (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    UNIQUE(guild_id, notification_type)
);

CREATE INDEX idx_discord_links_user ON discord_user_links(discord_user_id);
CREATE INDEX idx_discord_links_char ON discord_user_links(character_name, character_realm);
```

## Command Examples

### /flps
```
User: /flps
Bot: [Embed]
     Title: FLPS Score - PlayerName
     Color: Green (high score)
     Fields:
       - FLPS: 0.872 (Rank #3)
       - RMS: 0.920 (ACS: 1.0, MAS: 0.85, EPS: 0.90)
       - IPI: 0.850 (UV: 0.75, Tier: 1.1, Role: 1.0)
       - RDF: 1.000 (No recent loot)
       - Eligibility: ✅ Eligible
```

### /loot history
```
User: /loot history
Bot: [Embed]
     Title: Recent Loot - PlayerName
     Fields:
       - Item 1: [Fyr'alath] - 2024-01-10 - FLPS: 0.892 - RDF: Expired
       - Item 2: [Nymue's Unraveling Spindle] - 2024-01-05 - FLPS: 0.845 - RDF: 2 days
```

### /leaderboard
```
User: /leaderboard role:dps
Bot: [Embed]
     Title: FLPS Leaderboard - DPS
     Fields:
       1. PlayerOne - 0.925
       2. PlayerTwo - 0.892
       3. **You** - 0.872
       ...
```

## Notification Examples

### Loot Award
```
[Embed]
Title: 🎁 Loot Awarded
Color: Gold
Fields:
  - Item: [Fyr'alath, the Dream Render]
  - Recipient: PlayerName
  - FLPS: 0.892
  - Rationale: Highest FLPS, 4pc completion priority
  - Runner-ups:
    • PlayerTwo - 0.885
    • PlayerThree - 0.870
```

### RDF Expiry (DM)
```
[Embed]
Title: ✅ RDF Expired
Color: Green
Description: Your RDF penalty for [Nymue's Unraveling Spindle] has expired!
Fields:
  - New FLPS: 0.892 (was 0.803)
  - Status: Back in full contention for loot
```
