# Requirements Document - EdgeRush LootMan Unified Platform

## Introduction

This document specifies the complete requirements for the EdgeRush LootMan unified platform, combining features from RaidPlan.io (raid planning), Raidbots (simulation/optimization), WoWAudit (guild management), and RCLootCouncil (loot distribution) into a single, integrated solution.

## Glossary

| Term | Definition |
|------|------------|
| **FLPS** | Final Loot Priority Score - the core algorithm for fair loot distribution |
| **RMS** | Raider Merit Score - behavioral component (attendance, performance, preparation) |
| **IPI** | Item Priority Index - upgrade value component |
| **RDF** | Recency Decay Factor - fairness modifier based on recent loot |
| **Droptimizer** | Feature that analyzes potential drops for upgrade value |
| **Desktop Client** | Local application that bridges WoW addon and web platform |
| **Saved Variables** | WoW's file-based data persistence for addons |

---

# Part 1: Web Dashboard Requirements

## R1: Authentication and Authorization

**User Story:** As a user, I want to authenticate with Discord or Battle.net so I can access my guild's data securely.

### Acceptance Criteria

1. THE System SHALL support OAuth2 authentication via Discord
2. THE System SHALL support OAuth2 authentication via Battle.net
3. THE System SHALL automatically link authenticated users to their WoW characters
4. THE System SHALL support role-based access control:
   - **Guest**: View public guild info only
   - **Applicant**: View own application, public guild info
   - **Raider**: View own data, guild leaderboard, raid signups
   - **Trial**: Same as Raider with trial indicator
   - **Officer**: Behavioral actions, loot bans, applications
   - **Admin**: Full configuration, user management
5. THE System SHALL persist sessions with secure JWT tokens (configurable expiry)
6. THE System SHALL support logout and session revocation
7. WHEN authentication fails, THE System SHALL display actionable error messages

---

## R2: Personal Dashboard

**User Story:** As a raider, I want a comprehensive personal dashboard showing all my FLPS-related data.

### Acceptance Criteria

1. THE System SHALL display current FLPS score with:
   - Numerical value (e.g., 0.847)
   - Percentile rank in guild (e.g., "Top 15%")
   - Color-coded indicator (green/yellow/red based on eligibility)
2. THE System SHALL display RMS breakdown:
   - ACS (Attendance Commitment Score) with percentage
   - MAS (Mechanical Adherence Score) with DPA/ADT metrics
   - EPS (External Preparation Score) with vault/crest/farming status
3. THE System SHALL display IPI breakdown:
   - UV (Upgrade Value) with simulation source
   - Tier completion status (0/2/4 piece)
   - Role multiplier applied
4. THE System SHALL display RDF status:
   - Days since last contested award
   - Current penalty percentage
   - Countdown to full eligibility
5. THE System SHALL display eligibility status with reasons if ineligible
6. THE System SHALL display active behavioral actions with expiration
7. THE System SHALL display recent loot (last 5 items with RDF status)
8. THE System SHALL display upcoming events (raids, RDF expirations)
9. THE System SHALL update in real-time via WebSocket
10. THE System SHALL display FLPS trend chart (last 30 days)

---

## R3: Loot History

**User Story:** As a raider, I want to view my complete loot history with FLPS context.

### Acceptance Criteria

1. THE System SHALL display loot awards with:
   - Item name with Wowhead tooltip integration
   - Item icon from Wowhead/Blizzard API
   - Item level and difficulty (Normal/Heroic/Mythic)
   - Date and time of award
   - Raid encounter where item dropped
   - FLPS score at time of award with breakdown
   - RDF penalty applied and expiration date
   - Other contenders and their FLPS (officer view)
2. THE System SHALL support filtering by:
   - Date range (preset: week, month, tier)
   - Raid tier/season
   - Item slot
   - Difficulty
   - RDF status (active, expired, none)
3. THE System SHALL support sorting by:
   - Date (default: newest first)
   - Item level
   - Upgrade value
4. THE System SHALL highlight items with active RDF
5. THE System SHALL display countdown timer for RDF expiration
6. THE System SHALL support pagination with infinite scroll option
7. THE System SHALL support CSV/PDF export

---

## R4: Guild Leaderboard

**User Story:** As a raider, I want to compare my FLPS to other guild members.

### Acceptance Criteria

1. THE System SHALL display sortable leaderboard with columns:
   - Rank (with movement indicator since last week)
   - Character name, class icon, class color
   - Primary role indicator
   - FLPS score (sortable)
   - RMS (sortable)
   - IPI (sortable)
   - RDF (sortable)
   - Eligibility status icon
   - Last loot date
2. THE System SHALL support filtering by:
   - Role (Tank, Healer, Melee DPS, Ranged DPS)
   - Class
   - Eligibility status
   - Team/roster
   - Character name search
3. THE System SHALL highlight current user's row
4. THE System SHALL show user's rank even if not visible
5. THE System SHALL display tie-breaking explanation on hover
6. THE System SHALL support item-specific views (FLPS for specific item)
7. THE System SHALL update in real-time
8. THE System SHALL support CSV export

---

## R5: Wishlist Management

**User Story:** As a raider, I want to view and manage my wishlist with upgrade values.

### Acceptance Criteria

1. THE System SHALL display wishlist items with:
   - Item name with Wowhead tooltip
   - Item icon
   - Item level and difficulty variants
   - Slot indicator
   - Upgrade value percentage (DPS gain)
   - Simulation source (SimC, WoWAudit, manual)
   - Last simulation date with staleness warning
   - Drop source (boss name, instance)
   - BiS indicator if applicable
2. THE System SHALL support sorting by:
   - Upgrade value (default)
   - Slot
   - Drop source
   - Simulation date
3. THE System SHALL display warning for stale simulations (>7 days)
4. THE System SHALL group by source (raid, M+, crafted, vendor)
5. THE System SHALL support import from:
   - WoWAudit (primary)
   - Raidbots Droptimizer JSON (manual upload)
   - SimC string (manual entry)
6. THE System SHALL calculate expected FLPS for each wishlist item
7. THE System SHALL link to drop source details (boss, encounter)

---

## R6: Performance Metrics

**User Story:** As a raider, I want to view my combat performance metrics.

### Acceptance Criteria

1. THE System SHALL display Warcraft Logs data:
   - Overall performance rating
   - Parse percentiles by encounter (recent tier)
   - DPA (Deaths Per Attempt) with spec average comparison
   - ADT (Avoidable Damage Taken) with spec average comparison
   - MAS calculation breakdown
2. THE System SHALL display performance trend charts:
   - Parse percentile over time
   - DPA trend
   - ADT trend
3. THE System SHALL highlight critical issues:
   - DPA > 1.5x spec average (critical)
   - ADT > 1.5x spec average (critical)
   - Parse < 25th percentile (warning)
4. THE System SHALL link to full Warcraft Logs reports
5. THE System SHALL display MAS impact on FLPS
6. WHERE Warcraft Logs data unavailable, THE System SHALL explain impact

---

## R7: Attendance Tracking

**User Story:** As a raider, I want to view my attendance history and its impact on FLPS.

### Acceptance Criteria

1. THE System SHALL display attendance statistics:
   - Overall attendance percentage
   - Attendance by raid tier
   - Attendance by day of week
   - Streak information (consecutive raids attended)
2. THE System SHALL display attendance calendar view:
   - Color-coded by status (present, excused, absent, bench)
   - Raid names and dates
   - Clickable for details
3. THE System SHALL show ACS calculation breakdown:
   - Base attendance percentage
   - Tardiness penalty (if applicable)
   - No-show penalty (if applicable)
   - Final ACS value
4. THE System SHALL differentiate status types:
   - Present (full credit)
   - Present - Late (partial credit)
   - Absent - Excused (no penalty)
   - Absent - Unexcused (penalty)
   - Bench/Standby (partial credit)
5. THE System SHALL support filtering by date range, raid tier

---

# Part 2: Raid Planning Requirements

## R8: Raid Plan Editor (RaidPlan.io Parity)

**User Story:** As a raid leader, I want to create visual raid strategies with position assignments.

### Acceptance Criteria

1. THE System SHALL provide visual encounter maps for current raids:
   - Midnight encounters
   - Liberation of Undermine encounters
   - Legacy encounters (previous tier)
2. THE System SHALL support plan creation with:
   - Drag-and-drop player/role markers
   - Custom shape drawing (circles, arrows, lines)
   - Text annotations
   - Boss position markers
   - Hazard zone indicators
3. THE System SHALL support multi-step plans:
   - Timeline-based phases
   - Step navigation (next/previous)
   - Step rearrangement
4. THE System SHALL support marker types:
   - Raid markers (skull, X, square, etc.)
   - Role markers (tank, healer, DPS icons)
   - Player name markers (from roster)
   - Class-colored markers
5. THE System SHALL support plan management:
   - Save plans with titles
   - Duplicate existing plans
   - Share via link (public/guild-only)
   - Export as image
6. THE System SHALL integrate with roster:
   - Auto-populate player markers from raid roster
   - Show player availability
   - Highlight missing roles

---

## R9: Cooldown Assignment

**User Story:** As a raid leader, I want to assign raid cooldowns to specific timers.

### Acceptance Criteria

1. THE System SHALL display available cooldowns per encounter:
   - Defensive CDs (Spirit Link, Barrier, etc.)
   - Offensive CDs (Bloodlust, Power Infusion, etc.)
   - External CDs (BOP, Ironbark, etc.)
2. THE System SHALL support assignment to:
   - Boss timers/abilities
   - Custom time markers
   - Phase transitions
3. THE System SHALL validate assignments:
   - Cooldown availability (player in roster)
   - Cooldown recovery time
   - Overlap warnings
4. THE System SHALL display assignment grid:
   - Timeline-based view
   - Player x Ability matrix
5. THE System SHALL export to:
   - Method Raid Tools note format
   - WeakAura string
   - Discord-formatted table
6. THE System SHALL import from:
   - Warcraft Logs (existing assignments)
   - MRT note format

---

## R10: Raid Roster Management

**User Story:** As an officer, I want to manage raid rosters and compositions.

### Acceptance Criteria

1. THE System SHALL display roster with:
   - Character name, class, spec
   - Role assignment
   - Availability status
   - FLPS score
   - Attendance percentage
   - Gear level
2. THE System SHALL support roster actions:
   - Add/remove raiders
   - Change role assignments
   - Set bench priority
   - Create roster templates
3. THE System SHALL calculate composition metrics:
   - Role counts (tank/heal/DPS)
   - Class distribution
   - Buff/utility coverage
   - Average item level
4. THE System SHALL warn about:
   - Missing critical buffs
   - Unbalanced roles
   - Low attendance raiders
   - Gear level outliers
5. THE System SHALL support multiple rosters per raid
6. THE System SHALL integrate with raid signups

---

## R11: Raid Signup and Calendar

**User Story:** As a raider, I want to sign up for raids and view the schedule.

### Acceptance Criteria

1. THE System SHALL display raid calendar with:
   - Upcoming raids (date, time, instance)
   - Signup status (open, closed, in-progress)
   - Current signup counts by role
2. THE System SHALL support signup actions:
   - Sign up as available
   - Sign up as tentative
   - Decline with reason
   - Set character/spec preference
3. THE System SHALL display signup details:
   - Who signed up and when
   - Role distribution
   - FLPS-sorted roster suggestion
4. THE System SHALL support recurring raid schedules
5. THE System SHALL integrate with Discord reminders
6. THE System SHALL sync with WoWAudit calendar
7. THE System SHALL display past raids with attendance

---

# Part 3: Simulation Requirements

## R12: Droptimizer (Raidbots Parity)

**User Story:** As a raider, I want to analyze which raid drops are upgrades for me.

### Acceptance Criteria

1. THE System SHALL support drop analysis for:
   - Current raid (all difficulties)
   - Mythic+ dungeons
   - Weekly vault
   - World bosses
   - Crafted items
2. THE System SHALL display results with:
   - DPS gain percentage per item
   - Expected value per boss/dungeon
   - Best possible drop
   - Priority ranking (which boss to prioritize)
3. THE System SHALL support filtering by:
   - Source (raid, M+, vault)
   - Slot
   - Minimum upgrade value
4. THE System SHALL integrate with wishlist (auto-update)
5. THE System SHALL use local SimC Docker for calculations
6. THE System SHALL cache results with staleness indicator
7. THE System SHALL support batch processing (overnight simulation)
8. THE System SHALL compare to WoWAudit wishlist data

---

## R13: Top Gear (Raidbots Parity)

**User Story:** As a raider, I want to find my optimal gear combination.

### Acceptance Criteria

1. THE System SHALL analyze combinations of:
   - Equipped gear
   - Bag items
   - Bank items (if synced via addon)
   - Vault options
2. THE System SHALL optimize for:
   - Single target DPS
   - AoE DPS (with weight)
   - Custom fight profile
3. THE System SHALL display results:
   - Best overall setup
   - Top 5 alternatives
   - Gem/enchant recommendations
   - Talent recommendations (if variable)
4. THE System SHALL compare current vs optimal
5. THE System SHALL handle tier set optimization
6. THE System SHALL handle embellishment optimization
7. THE System SHALL use local SimC Docker

---

## R14: Gear Compare (Raidbots Parity)

**User Story:** As a raider, I want to compare specific gear pieces.

### Acceptance Criteria

1. THE System SHALL support comparing:
   - Owned items (from addon sync)
   - Items by ID (manual entry)
   - Hypothetical items (crafted, upgraded)
2. THE System SHALL display:
   - DPS difference (absolute and %)
   - Stat changes
   - Secondary stat weights context
3. THE System SHALL support:
   - Multiple item comparison (A vs B vs C)
   - "What if" scenarios (e.g., "if I upgrade this crest")
   - Embellishment combinations
4. THE System SHALL integrate with wishlist

---

## R15: Character Import

**User Story:** As a raider, I want to import my character data for simulation.

### Acceptance Criteria

1. THE System SHALL support import methods:
   - SimC addon string (paste)
   - Blizzard Armory API (auto-fetch)
   - WoWAudit character data
   - Local addon sync (desktop client)
2. THE System SHALL capture:
   - Equipped gear with stats
   - Bag items (for Top Gear)
   - Current talents
   - Gems and enchants
   - Tier set pieces
   - Embellishments
3. THE System SHALL detect outdated imports
4. THE System SHALL store import history
5. THE System SHALL use Blizzard API as fallback

---

# Part 4: Addon Integration Requirements

## R16: Desktop Client

**User Story:** As a raider, I want a desktop client that syncs my addon data with the website.

### Acceptance Criteria

1. THE System SHALL provide desktop client for:
   - Windows (primary)
   - macOS (secondary)
   - Linux (tertiary)
2. THE System SHALL sync data from WoW addon:
   - Saved Variables on logout/reload
   - Real-time via file watching
3. THE System SHALL sync to web platform:
   - Character gear and bags
   - Loot council decisions
   - Attendance confirmations
   - Combat log events
4. THE System SHALL run in system tray
5. THE System SHALL auto-start with Windows (optional)
6. THE System SHALL configure WoW installation path
7. THE System SHALL support multiple WoW installations (retail, PTR)
8. THE System SHALL authenticate with web platform

---

## R17: WoW Addon - FLPS Display

**User Story:** As a raider, I want to see FLPS scores in-game.

### Acceptance Criteria

1. THE System SHALL display FLPS data in-game:
   - Personal FLPS score and breakdown
   - Eligibility status
   - RDF countdown
2. THE System SHALL display guild FLPS leaderboard
3. THE System SHALL show FLPS in:
   - Dedicated addon window
   - Tooltip enhancement (mouseover players)
   - Chat link format
4. THE System SHALL receive updates from desktop client
5. THE System SHALL work without desktop client (cached data)
6. THE System SHALL minimize combat impact (disable during encounters)

---

## R18: WoW Addon - Loot Council

**User Story:** As a loot council member, I want to distribute loot with FLPS integration.

### Acceptance Criteria

1. THE System SHALL provide loot distribution interface:
   - Item dropped display with stats
   - Eligible raiders with FLPS scores
   - Wishlist data per raider
   - Upgrade value per raider
2. THE System SHALL support voting:
   - Council member votes
   - Vote reasons (MainSpec, OffSpec, etc.)
   - Vote visibility settings
3. THE System SHALL calculate recommendations:
   - Highest FLPS eligible raider
   - Tie-breaker display
   - Warning for unusual decisions
4. THE System SHALL record decisions:
   - Winner and reason
   - Other contenders
   - Council votes
   - Timestamp
5. THE System SHALL sync decisions to web platform
6. THE System SHALL support RCLootCouncil compatibility mode
7. THE System SHALL announce awards to raid chat

---

## R19: WoW Addon - Data Export

**User Story:** As a user, I want my addon data synchronized to the web platform.

### Acceptance Criteria

1. THE System SHALL export:
   - Character gear (all slots)
   - Bag contents (for Top Gear)
   - Bank contents (optional)
   - Talents and spec
   - Consumables/flask status
   - Combat log events (deaths, damage taken)
2. THE System SHALL export on:
   - Logout
   - UI reload
   - Manual command (/elm sync)
   - Combat end (combat log)
3. THE System SHALL format data for desktop client reading
4. THE System SHALL compress large datasets
5. THE System SHALL handle multi-character data

---

# Part 5: Discord Bot Requirements

## R20: Bot Core Features

**User Story:** As a guild member, I want to interact with FLPS data via Discord.

### Acceptance Criteria

1. THE System SHALL provide slash commands:
   - `/flps` - Show personal FLPS score
   - `/flps @user` - Show another user's score (officer)
   - `/leaderboard` - Show guild rankings
   - `/loot history` - Show personal loot history
   - `/wishlist` - Show personal wishlist
   - `/attendance` - Show personal attendance
   - `/sim droptimizer` - Request droptimizer run
2. THE System SHALL display results in embeds:
   - Color-coded by eligibility
   - Formatted breakdown tables
   - Item icons where applicable
3. THE System SHALL link to web dashboard for details
4. THE System SHALL support ephemeral responses (private)

---

## R21: Bot Notifications

**User Story:** As a guild member, I want automated notifications about FLPS events.

### Acceptance Criteria

1. THE System SHALL notify on loot awards:
   - Item awarded
   - Winner and FLPS score
   - Brief rationale
   - Runner-up comparison
2. THE System SHALL notify on RDF expiry:
   - Direct message to user
   - Item name and new FLPS
3. THE System SHALL notify on behavioral actions:
   - Direct message to affected user
   - Action type and reason
   - Appeal instructions
4. THE System SHALL notify on raid signups:
   - Signup reminders
   - Raid starting notifications
5. THE System SHALL support configurable channels per notification type
6. THE System SHALL support user notification preferences

---

## R22: Bot Admin Commands

**User Story:** As an officer, I want to manage FLPS via Discord commands.

### Acceptance Criteria

1. THE System SHALL provide admin commands:
   - `/admin action create` - Create behavioral action
   - `/admin action remove` - Remove behavioral action
   - `/admin ban create` - Create loot ban
   - `/admin ban remove` - Remove loot ban
   - `/admin sync` - Trigger data sync
   - `/admin config` - View/edit configuration
2. THE System SHALL validate permissions before execution
3. THE System SHALL log all admin actions
4. THE System SHALL confirm destructive actions

---

## R23: Bot Character Linking

**User Story:** As a raider, I want to link my Discord account to my WoW character.

### Acceptance Criteria

1. THE System SHALL provide linking commands:
   - `/link <character> <realm>` - Link character
   - `/link list` - Show linked characters
   - `/unlink <character>` - Remove link
   - `/link primary <character>` - Set primary character
2. THE System SHALL validate character exists in guild
3. THE System SHALL support multiple character links
4. THE System SHALL handle character name conflicts
5. WHERE character already linked, THE System SHALL require admin approval

---

# Part 6: Application Portal Requirements

## R24: Application Form

**User Story:** As a prospective raider, I want to apply to the guild with a comprehensive application.

### Acceptance Criteria

1. THE System SHALL provide application form with:
   - **Personal Info**:
     - Name (real or preferred)
     - Age
     - Location/timezone
   - **Character Info** (auto-populated from API):
     - Character name
     - Realm
     - Class/spec
     - Current item level
     - Mythic+ score (Raider.IO)
   - **Raid Info**:
     - Raid availability (with schedule display)
     - Previous raid experience
     - Best mythic kills
     - Current/previous guild and reason for leaving
   - **Performance Links** (auto-validated):
     - Warcraft Logs URL (validated, data pulled)
     - Raider.IO URL (validated, data pulled)
   - **About You**:
     - Why this guild
     - What you bring
     - Goals for this tier
   - **Technical**:
     - Stable hardware/internet
     - Discord availability
     - Battle.net tag
   - **Alts**:
     - Alt characters (optional)
     - Alt swap willingness
2. THE System SHALL auto-fill from Battle.net OAuth
3. THE System SHALL validate URLs and fetch data
4. THE System SHALL display fetched data inline
5. THE System SHALL require all mandatory fields

---

## R25: Application Processing

**User Story:** As an officer, I want to efficiently review and process applications.

### Acceptance Criteria

1. THE System SHALL display application list:
   - Pending applications (newest first)
   - Application date
   - Character class/spec
   - Quick stats (ilvl, M+ score, best parse)
2. THE System SHALL display application detail:
   - All form responses
   - Auto-fetched data (logs, raider.io)
   - Performance summary chart
   - Red flags (low attendance, guild hopping)
3. THE System SHALL support actions:
   - Approve (move to trial)
   - Decline (with optional reason)
   - Request more info
   - Add internal notes
4. THE System SHALL notify applicants:
   - Application received
   - Decision made
   - Interview scheduled
5. THE System SHALL integrate with Discord:
   - Post new applications to channel
   - Create applicant role/channel
6. THE System SHALL track application history

---

## R26: Trial Management

**User Story:** As an officer, I want to track trial raiders and their progress.

### Acceptance Criteria

1. THE System SHALL display trial dashboard:
   - Active trials with start date
   - Trial duration remaining
   - Performance metrics
   - Attendance during trial
   - Loot received during trial
2. THE System SHALL support trial actions:
   - Promote to raider
   - Extend trial
   - End trial (failed)
   - Add trial notes
3. THE System SHALL track trial criteria:
   - Minimum attendance
   - Performance benchmarks
   - Behavior notes
4. THE System SHALL notify on trial milestones
5. THE System SHALL integrate with FLPS (trial modifier)

---

# Part 7: External Data Integration Requirements

## R27: Blizzard API Integration

**User Story:** As a system, I want to fetch game data from Blizzard's API to minimize local storage.

### Acceptance Criteria

1. THE System SHALL fetch from Blizzard API:
   - Item data (name, stats, icon URL)
   - Spell data (name, description, icon)
   - Instance data (name, bosses, map)
   - Character data (gear, talents, achievements)
   - Guild data (roster, achievements)
   - Talent data (trees, nodes)
2. THE System SHALL cache responses:
   - Items: 7 days
   - Instances: 30 days (or until patch)
   - Characters: 1 hour
   - Talent trees: 7 days
3. THE System SHALL handle API failures gracefully
4. THE System SHALL use OAuth2 client credentials flow
5. THE System SHALL respect rate limits (36,000/hour)

---

## R28: Wowhead Integration

**User Story:** As a user, I want rich item tooltips without downloading all item data.

### Acceptance Criteria

1. THE System SHALL embed Wowhead tooltip script
2. THE System SHALL display tooltips for:
   - Items (with gems, enchants, ilvl)
   - Spells (with rank, description)
   - Achievements
   - NPCs (bosses)
3. THE System SHALL link to Wowhead pages for details
4. THE System SHALL use Wowhead icons as fallback
5. THE System SHALL configure tooltip options:
   - Color links by rarity
   - Show icons
   - Hide certain info (sell price, etc.)

---

## R29: Warcraft Logs Integration (Enhanced)

**User Story:** As a system, I want comprehensive Warcraft Logs data integration.

### Acceptance Criteria

1. THE System SHALL fetch from Warcraft Logs:
   - Character rankings by encounter
   - Parse percentiles
   - Deaths and death causes
   - Damage taken by ability
   - Buff/debuff uptime
   - Full report data for analysis
2. THE System SHALL calculate MAS from logs:
   - DPA (deaths per attempt)
   - ADT (avoidable damage taken)
   - Spec-normalized metrics
3. THE System SHALL display on dashboard:
   - Performance charts
   - Encounter breakdowns
   - Trend analysis
4. THE System SHALL cache appropriately
5. THE System SHALL support private logs (with user auth)

---

## R30: Raider.IO Integration

**User Story:** As a system, I want Raider.IO data for M+ metrics.

### Acceptance Criteria

1. THE System SHALL fetch from Raider.IO:
   - Overall M+ score
   - Dungeon-specific scores
   - Best runs
   - Seasonal rankings
2. THE System SHALL display in:
   - Character profiles
   - Application reviews
   - Roster views
3. THE System SHALL use for EPS calculation (weekly M+ completion)
4. THE System SHALL cache with reasonable TTL

---

# Part 8: Administration Requirements

## R31: Configuration Management

**User Story:** As an admin, I want to configure all FLPS parameters for my guild.

### Acceptance Criteria

1. THE System SHALL provide configuration for:
   - FLPS weights (RMS, IPI, RDF)
   - RMS component weights (ACS, MAS, EPS)
   - IPI component weights (UV, tier, role)
   - RDF decay parameters
   - Eligibility thresholds
   - Role multipliers
2. THE System SHALL display:
   - Current values vs defaults
   - Value descriptions
   - Impact preview
3. THE System SHALL validate before saving
4. THE System SHALL log all changes
5. THE System SHALL support export/import
6. THE System SHALL support reset to defaults

---

## R32: User Management

**User Story:** As an admin, I want to manage user accounts and permissions.

### Acceptance Criteria

1. THE System SHALL display user list:
   - Username
   - Linked characters
   - Role
   - Last active
2. THE System SHALL support user actions:
   - Change role
   - Link/unlink characters
   - Disable account
   - View activity log
3. THE System SHALL support bulk actions
4. THE System SHALL log all user management actions

---

## R33: Audit Log

**User Story:** As an admin, I want to view a complete audit trail.

### Acceptance Criteria

1. THE System SHALL log:
   - Loot decisions (who, what, why)
   - Behavioral actions (created, removed)
   - Configuration changes
   - User management actions
   - Application decisions
   - Admin command usage
2. THE System SHALL display:
   - Actor (who performed action)
   - Action type
   - Target (affected user/item)
   - Timestamp
   - Details/reason
3. THE System SHALL support filtering by:
   - Date range
   - Action type
   - Actor
   - Target
4. THE System SHALL support export

---

# Part 9: Non-Functional Requirements

## NFR1: Performance

1. API response time SHALL be <100ms for standard queries
2. Page load time SHALL be <3 seconds on desktop, <5 seconds on mobile
3. WebSocket latency SHALL be <1 second for real-time updates
4. Simulation jobs SHALL complete within configured timeout (default: 30 minutes)
5. Database queries SHALL complete in <50ms

## NFR2: Reliability

1. System SHALL maintain 99.9% uptime for web services
2. System SHALL gracefully degrade when external APIs unavailable
3. System SHALL retry failed API calls with exponential backoff
4. System SHALL persist data across component restarts
5. System SHALL support database backups

## NFR3: Security

1. All API endpoints SHALL require authentication (except health)
2. All web traffic SHALL use HTTPS
3. JWT tokens SHALL expire (configurable, default: 24 hours)
4. Sensitive configuration SHALL use encrypted storage
5. Rate limiting SHALL prevent abuse (configurable thresholds)

## NFR4: Scalability

1. System SHALL support 100+ concurrent web users
2. System SHALL support 40+ concurrent addon connections
3. System SHALL queue simulation jobs appropriately
4. System SHALL use connection pooling for databases

## NFR5: Maintainability

1. Test coverage SHALL be ≥85%
2. Code SHALL follow established style guides
3. All changes SHALL be logged in audit trail
4. Documentation SHALL be kept current

## NFR6: Accessibility

1. Web application SHALL meet WCAG 2.1 AA standards
2. Web application SHALL support keyboard navigation
3. Web application SHALL support screen readers
4. Color SHALL not be sole indicator of status
