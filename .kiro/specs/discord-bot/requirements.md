# Requirements Document - Discord Bot

## Introduction

This feature provides a Discord bot for automated notifications and interactive commands related to FLPS scores, loot distribution, and guild operations. Currently, all communication is manual, requiring officers to copy/paste data. This bot will automate notifications and provide self-service commands for raiders.

## Glossary

- **Discord Bot**: Automated application that interacts with Discord servers
- **Slash Command**: Discord's native command system (e.g., `/flps`)
- **Embed**: Rich message format with formatting, colors, and fields
- **Webhook**: HTTP callback for sending messages to Discord channels
- **The System**: EdgeRush LootMan Discord bot application
- **Bot Token**: Authentication credential for Discord bot

## Requirements

### Requirement 1: Bot Setup and Configuration

**User Story:** As a guild administrator, I want to configure the Discord bot so that it can interact with my guild's Discord server.

#### Acceptance Criteria

1. THE System SHALL support Discord bot token configuration
2. THE System SHALL connect to Discord on startup and log connection status
3. WHERE multiple guilds are configured, THE System SHALL support guild-specific Discord server mappings
4. THE System SHALL support configurable notification channels per event type
5. WHEN bot loses connection, THE System SHALL automatically reconnect with exponential backoff

### Requirement 2: FLPS Score Commands

**User Story:** As a raider, I want to check my FLPS score via Discord command so that I can quickly see my loot priority.

#### Acceptance Criteria

1. THE System SHALL provide `/flps` command to display user's current FLPS score
2. THE System SHALL display detailed breakdown in embed format (RMS, IPI, RDF)
3. THE System SHALL support `/flps @user` to check another raider's score (officer only)
4. THE System SHALL display eligibility status and reasons if ineligible
5. WHERE user has no linked character, THE System SHALL provide instructions for linking

### Requirement 3: Loot History Commands

**User Story:** As a raider, I want to check my loot history via Discord so that I can see recent awards and RDF status.

#### Acceptance Criteria

1. THE System SHALL provide `/loot history` command to display recent loot awards
2. THE System SHALL display up to configurable number of recent awards (default: 10)
3. THE System SHALL show RDF status and expiration for each award
4. THE System SHALL support filtering by date range
5. WHERE user has no loot history, THE System SHALL display appropriate message

### Requirement 4: Leaderboard Commands

**User Story:** As a raider, I want to view the FLPS leaderboard via Discord so that I can see my relative standing.

#### Acceptance Criteria

1. THE System SHALL provide `/leaderboard` command to display top raiders by FLPS
2. THE System SHALL display configurable number of top raiders (default: 10)
3. THE System SHALL support filtering by role (tank, healer, dps)
4. THE System SHALL highlight the requesting user's position
5. WHERE user is not in top N, THE System SHALL show their rank separately

### Requirement 5: Wishlist Commands

**User Story:** As a raider, I want to check my wishlist priorities via Discord so that I can see which items have highest upgrade value.

#### Acceptance Criteria

1. THE System SHALL provide `/wishlist` command to display user's top wishlist items
2. THE System SHALL display upgrade values and simulation source
3. THE System SHALL show configurable number of top items (default: 5)
4. THE System SHALL support filtering by slot
5. WHERE simulation data is stale, THE System SHALL display warning

### Requirement 6: Loot Award Notifications

**User Story:** As a guild member, I want to receive notifications when loot is awarded so that I can see who received items and why.

#### Acceptance Criteria

1. WHEN loot is awarded, THE System SHALL post notification to configured channel
2. THE System SHALL display recipient, item, and FLPS score in embed format
3. THE System SHALL include brief rationale (e.g., "Highest FLPS", "Tier completion priority")
4. THE System SHALL display runner-up scores for transparency
5. WHERE multiple items are awarded simultaneously, THE System SHALL batch notifications

### Requirement 7: RDF Expiry Notifications

**User Story:** As a raider, I want to be notified when my RDF expires so that I know when I'm back in full contention for loot.

#### Acceptance Criteria

1. WHEN RDF expires for a character, THE System SHALL send direct message to linked user
2. THE System SHALL include which item's RDF expired
3. THE System SHALL display updated FLPS score
4. THE System SHALL support configurable notification timing (default: on expiry)
5. WHERE user has DMs disabled, THE System SHALL log warning and skip notification

### Requirement 8: Penalty and Ban Notifications

**User Story:** As a raider, I want to be notified when behavioral actions or loot bans are applied so that I understand impacts on my eligibility.

#### Acceptance Criteria

1. WHEN behavioral action is applied, THE System SHALL send direct message to affected user
2. WHEN loot ban is applied, THE System SHALL send direct message to affected user
3. THE System SHALL include reason, duration, and FLPS impact
4. THE System SHALL include appeal instructions
5. WHERE action expires, THE System SHALL send notification of restoration

### Requirement 9: Admin Commands

**User Story:** As a guild officer, I want to manage FLPS data via Discord commands so that I can make quick adjustments without accessing the web dashboard.

#### Acceptance Criteria

1. THE System SHALL provide `/admin behavioral-action` command to create behavioral actions (officer only)
2. THE System SHALL provide `/admin loot-ban` command to create loot bans (officer only)
3. THE System SHALL provide `/admin sync` command to trigger manual data sync (admin only)
4. THE System SHALL validate permissions before executing admin commands
5. WHEN admin commands are executed, THE System SHALL log actions with user identity

### Requirement 10: Character Linking

**User Story:** As a raider, I want to link my Discord account to my WoW character so that bot commands work for me.

#### Acceptance Criteria

1. THE System SHALL provide `/link character-name realm` command to link Discord user to character
2. THE System SHALL validate character exists in guild roster
3. THE System SHALL support linking multiple characters (alts)
4. THE System SHALL provide `/unlink` command to remove character links
5. WHERE character is already linked to another user, THE System SHALL require admin approval

### Requirement 11: Help and Information Commands

**User Story:** As a raider, I want to access help information via Discord so that I can learn how to use bot commands.

#### Acceptance Criteria

1. THE System SHALL provide `/help` command displaying all available commands
2. THE System SHALL provide `/help <command>` for detailed command help
3. THE System SHALL provide `/about` command with bot version and status
4. THE System SHALL display command syntax and examples
5. WHERE user lacks permissions for a command, THE System SHALL indicate permission requirements

### Requirement 12: Error Handling and User Feedback

**User Story:** As a raider, I want clear error messages when commands fail so that I can understand what went wrong.

#### Acceptance Criteria

1. WHEN command fails, THE System SHALL display user-friendly error message
2. WHERE data is unavailable, THE System SHALL explain why and suggest alternatives
3. IF permissions are insufficient, THEN THE System SHALL display required role
4. THE System SHALL provide actionable guidance for resolving errors
5. WHEN bot is experiencing issues, THE System SHALL display status message
