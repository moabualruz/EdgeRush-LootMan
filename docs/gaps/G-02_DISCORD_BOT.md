# G-02: Discord Bot Implementation Plan

**Requirement:** R20-R23 (Discord Bot Core, Notifications, Admin Commands, Character Linking)  
**Priority:** P0 (Highest)  
**Estimated Effort:** 16-24 hours  
**Status:** ⬜ TODO

---

## Current State

- **Folder:** `discord-bot/src/main/kotlin/com/edgerush/`
- **Files:** 15 Kotlin files (basic structure only)
- **Connectivity:** Not wired to backend services

---

## Requirements Checklist

### R20: Bot Core Features
- [ ] `/flps` - Show personal FLPS score
- [ ] `/flps @user` - Show another user's score (officer only)
- [ ] `/leaderboard` - Show guild rankings
- [ ] `/loot history` - Show personal loot history
- [ ] `/wishlist` - Show personal wishlist
- [ ] `/attendance` - Show personal attendance
- [ ] Embed formatting with color-coded eligibility
- [ ] Link to web dashboard

### R21: Bot Notifications
- [ ] Loot award notifications to channel
- [ ] RDF expiry direct messages
- [ ] Behavioral action notifications
- [ ] Raid signup reminders
- [ ] Configurable channels per notification type

### R22: Bot Admin Commands
- [ ] `/admin action create` - Create behavioral action
- [ ] `/admin action remove` - Remove behavioral action
- [ ] `/admin ban create` - Create loot ban
- [ ] `/admin ban remove` - Remove loot ban
- [ ] `/admin sync` - Trigger data sync
- [ ] Permission validation
- [ ] Audit logging

### R23: Character Linking
- [ ] `/link <character> <realm>` - Link character
- [ ] `/link list` - Show linked characters
- [ ] `/unlink <character>` - Remove link
- [ ] `/link primary <character>` - Set primary
- [ ] Character validation against guild roster

---

## Implementation Steps

### Phase 1: Bot Foundation (4h)
1. **Configure JDA in Spring Boot**
   - File: `discord-bot/src/main/kotlin/.../config/BotConfiguration.kt`
   - Add JDA bean with token from `application.yml`
   - Implement reconnect with exponential backoff

2. **Slash Command Registry**
   - File: `.../commands/SlashCommandRegistry.kt`
   - Register all commands on bot startup
   - Use JDA's `SlashCommandData` builders

### Phase 2: Backend Integration (4h)
3. **FLPS Data Client**
   - Create Feign/WebClient to call `data-sync-service` API
   - Endpoints needed: `/api/v1/flps/{characterId}`, `/api/v1/leaderboard`

4. **User Linking Service**
   - File: `.../service/CharacterLinkService.kt`
   - Database: `discord_character_links` table
   - Methods: `linkCharacter()`, `unlinkCharacter()`, `getLinkedCharacters()`

### Phase 3: Core Commands (6h)
5. **Implement `/flps` Command**
   - File: `.../commands/FlpsCommand.kt`
   - Query backend, format as Discord embed
   - Include RMS/IPI/RDF breakdown

6. **Implement `/leaderboard` Command**
   - File: `.../commands/LeaderboardCommand.kt`
   - Paginated display (10 per page)
   - Highlight requester's position

7. **Implement `/link` Commands**
   - File: `.../commands/LinkCommand.kt`
   - Validate character exists in guild
   - Handle already-linked conflicts

### Phase 4: Notifications (4h)
8. **Notification Service**
   - File: `.../service/NotificationService.kt`
   - Listen to backend events (Kafka/RabbitMQ or polling)
   - Route to appropriate channels/DMs

9. **Loot Award Notifications**
   - Embed with item, winner, FLPS, runner-ups

10. **RDF Expiry Notifications**
    - Direct message on expiry

### Phase 5: Admin Commands (4h)
11. **Admin Command Handler**
    - File: `.../commands/AdminCommand.kt`
    - Permission checks via Discord roles
    - Call backend admin endpoints

---

## Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| `config/BotConfiguration.kt` | Modify | Add JDA bean, configure token |
| `config/DiscordProperties.kt` | Create | Configuration properties class |
| `commands/SlashCommandRegistry.kt` | Create | Command registration service |
| `commands/FlpsCommand.kt` | Create | /flps slash command |
| `commands/LeaderboardCommand.kt` | Create | /leaderboard command |
| `commands/LinkCommand.kt` | Create | /link, /unlink commands |
| `commands/AdminCommand.kt` | Create | /admin subcommands |
| `service/CharacterLinkService.kt` | Create | Discord-WoW linking logic |
| `service/NotificationService.kt` | Create | Event-driven notifications |
| `client/DataSyncClient.kt` | Create | Feign client to backend |
| `repository/CharacterLinkRepository.kt` | Create | JPA repository |

---

## Testing Strategy

1. **Unit Tests:** Mock JDA events, verify command parsing
2. **Integration Tests:** Use JDA test utilities
3. **Manual Test:** Deploy to test Discord server

---

## Verification Commands

```bash
# Build
cd discord-bot && ./gradlew build

# Run tests
./gradlew test

# Start bot locally
./gradlew bootRun
```

---

## Dependencies to Add

```kotlin
// build.gradle.kts
implementation("net.dv8tion:JDA:5.0.0-beta.18")
implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
```
