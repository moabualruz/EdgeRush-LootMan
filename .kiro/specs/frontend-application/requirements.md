# Requirements Document - Frontend Application

## Introduction

This is a full-featured web application for EdgeRush LootMan, providing complete guild operations management, raider self-service, and administrative tools. Unlike a simple dashboard, this application covers all system functionality including real-time collaboration, analytics, and configuration management.

## Glossary

- **Application**: Full-featured React web application
- **Raider View**: Player-facing interface showing personal FLPS data
- **Admin Panel**: Administrative interface for guild configuration and management
- **Loot Council Interface**: Real-time decision support during raids
- **Analytics**: Historical trends and comparative analysis
- **The System**: EdgeRush LootMan web application

---

## Requirements

### R1: Authentication and Authorization

**User Story:** As a user, I want to securely authenticate with my Discord or Battle.net account so I can access my guild's FLPS data.

#### Acceptance Criteria

1. THE System SHALL support OAuth2 authentication via Discord
2. THE System SHALL support OAuth2 authentication via Battle.net
3. THE System SHALL automatically map authenticated users to their WoW characters
4. THE System SHALL support role-based access control:
   - **Raider**: View own data, guild leaderboard
   - **Officer**: Create behavioral actions, loot bans
   - **Admin**: Full configuration access
5. THE System SHALL persist sessions with secure JWT tokens
6. THE System SHALL support logout and session management
7. WHEN authentication fails, THE System SHALL display clear error messages

---

### R2: Personal Dashboard

**User Story:** As a raider, I want a comprehensive personal dashboard showing all my FLPS-related data at a glance.

#### Acceptance Criteria

1. THE System SHALL display current FLPS score prominently with visual indicator
2. THE System SHALL display detailed breakdown:
   - RMS (Raider Merit Score): ACS, MAS, EPS components
   - IPI (Item Priority Index): UV, Tier Bonus, Role Multiplier
   - RDF (Recency Decay Factor): Days since last award, decay percentage
3. THE System SHALL display eligibility status with reasons if ineligible
4. THE System SHALL display active behavioral actions with expiration dates
5. THE System SHALL display recent loot awards (last 5)
6. THE System SHALL display upcoming RDF expirations
7. THE System SHALL display performance trend chart (last 30 days)
8. THE System SHALL update in real-time when FLPS changes

---

### R3: Loot History

**User Story:** As a raider, I want to view my complete loot history to understand my RDF status and award patterns.

#### Acceptance Criteria

1. THE System SHALL display all loot awards with:
   - Item name, icon, and item level
   - Date and time of award
   - FLPS score at time of award
   - RDF penalty status and expiration
   - Raid encounter where item dropped
2. THE System SHALL support filtering by:
   - Date range
   - Item tier/difficulty
   - Item type/slot
   - RDF status (active, expired)
3. THE System SHALL support sorting by date, item level, slot
4. THE System SHALL highlight items with active RDF penalties
5. THE System SHALL display countdown timer for RDF expiration
6. THE System SHALL support pagination for large histories

---

### R4: Guild Leaderboard

**User Story:** As a raider, I want to see how my FLPS compares to other guild members.

#### Acceptance Criteria

1. THE System SHALL display sortable leaderboard with columns:
   - Rank
   - Character name and class
   - FLPS score
   - RMS, IPI, RDF breakdown
   - Eligibility status
2. THE System SHALL support filtering by:
   - Role (tank, healer, DPS)
   - Class
   - Eligibility status
   - Team/roster
3. THE System SHALL highlight the current user's position
4. THE System SHALL show user's rank even if not in visible range
5. THE System SHALL display tie-breaking criteria when scores match
6. THE System SHALL support export to CSV
7. THE System SHALL update in real-time when scores change

---

### R5: Wishlist Management

**User Story:** As a raider, I want to view and manage my wishlist with upgrade values to prioritize items.

#### Acceptance Criteria

1. THE System SHALL display wishlist items with:
   - Item name, icon, slot
   - Upgrade value percentage
   - Simulation source (SimC, wishlist percentage)
   - Last simulation date
2. THE System SHALL support sorting by upgrade value, slot, source
3. THE System SHALL display warning for stale simulation data
4. THE System SHALL allow viewing item details (stats, source encounters)
5. THE System SHALL highlight BiS (Best in Slot) items
6. THE System SHALL support wishlist import from WoWAudit

---

### R6: Performance Metrics

**User Story:** As a raider, I want to view my combat performance metrics to understand my MAS score.

#### Acceptance Criteria

1. THE System SHALL display Warcraft Logs performance data:
   - DPA (Deaths per Attempt)
   - ADT (Avoidable Damage Taken)
   - Parse percentile per encounter
2. THE System SHALL display spec averages for comparison
3. THE System SHALL display performance trend over time (chart)
4. THE System SHALL highlight critical issues affecting MAS
5. THE System SHALL link to full Warcraft Logs reports
6. WHERE Warcraft Logs data unavailable, THE System SHALL explain impact on MAS

---

### R7: Attendance Tracking

**User Story:** As a raider, I want to view my attendance history and how it affects my FLPS.

#### Acceptance Criteria

1. THE System SHALL display attendance statistics:
   - Overall attendance percentage
   - Attendance by raid tier
   - Attendance trend over time
2. THE System SHALL display attendance history calendar view
3. THE System SHALL show impact of attendance on ACS component
4. THE System SHALL differentiate between:
   - Present
   - Absent (excused)
   - Absent (unexcused)
   - Bench/Standby
5. THE System SHALL support filtering by date range, raid tier

---

### R8: Admin Configuration Panel

**User Story:** As a guild administrator, I want to configure FLPS parameters for my guild.

#### Acceptance Criteria

1. THE System SHALL provide interface for editing:
   - RMS weights (attendance, performance, seniority)
   - IPI weights (upgrade value, tier completion, role)
   - RDF decay parameters (duration, curve)
2. THE System SHALL display current values vs defaults
3. THE System SHALL validate configuration before saving
4. THE System SHALL preview impact of changes on current scores
5. THE System SHALL log all configuration changes with admin identity
6. THE System SHALL support configuration export/import
7. THE System SHALL support reset to defaults

---

### R9: Behavioral Action Management

**User Story:** As a guild officer, I want to manage behavioral actions to apply penalties or bonuses.

#### Acceptance Criteria

1. THE System SHALL provide interface for creating behavioral actions:
   - Select raider
   - Action type (penalty, bonus)
   - Score modifier value
   - Reason/description
   - Duration (permanent or time-limited)
2. THE System SHALL display all active actions with:
   - Raider name
   - Action type and value
   - Reason
   - Created by (officer name)
   - Expiration date
   - FLPS impact
3. THE System SHALL support editing and removing actions
4. THE System SHALL display historical actions
5. THE System SHALL log all changes with officer identity

---

### R10: Loot Ban Management

**User Story:** As a guild officer, I want to manage loot bans to restrict eligibility.

#### Acceptance Criteria

1. THE System SHALL provide interface for creating loot bans:
   - Select raider
   - Ban scope (specific item, slot, all items)
   - Reason/description
   - Duration (permanent or time-limited)
2. THE System SHALL display all active bans with:
   - Raider name
   - Ban scope
   - Reason
   - Created by (officer name)
   - Expiration date
3. THE System SHALL support editing and removing bans
4. THE System SHALL display historical bans
5. THE System SHALL show ban impact on eligibility

---

### R11: Loot Council Decision Interface

**User Story:** As a loot council member, I want real-time FLPS data during raids to make informed decisions.

#### Acceptance Criteria

1. THE System SHALL display eligible raiders for current item:
   - FLPS score
   - Upgrade value for this item
   - RDF status
   - Detailed breakdown on expand
2. THE System SHALL sort by FLPS with configurable weights
3. THE System SHALL highlight ties and tie-breakers
4. THE System SHALL filter by:
   - Can equip item
   - Role
   - Eligibility status
5. THE System SHALL provide recommendation with reasoning
6. THE System SHALL update in real-time
7. THE System SHALL support quick award action
8. THE System SHALL display runner-up comparison

---

### R12: Raid Management

**User Story:** As a raider, I want to view upcoming raids and manage my signups.

#### Acceptance Criteria

1. THE System SHALL display upcoming raids:
   - Raid name, date, time
   - Current signups count
   - Role composition
2. THE System SHALL support signup/withdrawal
3. THE System SHALL display raid calendar view
4. THE System SHALL show past raids with attendance records
5. THE System SHALL allow officers to create/edit raids
6. THE System SHALL display raid encounter progression

---

### R13: Team Management

**User Story:** As an officer, I want to manage team rosters and compositions.

#### Acceptance Criteria

1. THE System SHALL display team roster with:
   - Member list
   - Role assignments
   - Attendance statistics
   - FLPS averages
2. THE System SHALL support roster modifications
3. THE System SHALL display team FLPS distribution chart
4. THE System SHALL show bench/standby raiders
5. THE System SHALL support team comparison

---

### R14: Application Management

**User Story:** As an officer, I want to manage guild applications.

#### Acceptance Criteria

1. THE System SHALL display pending applications
2. THE System SHALL display application details:
   - Character info
   - Answers to questions
   - Warcraft Logs profile
   - Previous guild history
3. THE System SHALL support approve/reject actions
4. THE System SHALL support application notes
5. THE System SHALL log all application decisions

---

### R15: Analytics and Reporting

**User Story:** As an admin, I want analytics and reports to understand guild trends.

#### Acceptance Criteria

1. THE System SHALL provide FLPS distribution charts
2. THE System SHALL provide loot distribution analytics:
   - Items awarded per raider
   - Items by role/class
   - Items by tier
3. THE System SHALL provide attendance trends
4. THE System SHALL provide performance trends
5. THE System SHALL support date range filtering
6. THE System SHALL support export to PDF/CSV
7. THE System SHALL provide comparison to previous periods

---

### R16: Character Linking

**User Story:** As a user, I want to link my Discord account to my WoW characters.

#### Acceptance Criteria

1. THE System SHALL display linked characters
2. THE System SHALL support linking additional characters (alts)
3. THE System SHALL support unlinking characters
4. THE System SHALL support setting primary character
5. THE System SHALL validate character exists in guild roster
6. WHERE character already linked to another user, THE System SHALL require admin approval

---

### R17: Notification Preferences

**User Story:** As a user, I want to configure my notification preferences.

#### Acceptance Criteria

1. THE System SHALL support configuring notifications:
   - RDF expiry reminders
   - FLPS score changes
   - Loot council decisions
   - Penalty/ban notifications
2. THE System SHALL support notification channels:
   - In-app notifications
   - Email (optional)
   - Discord DM (via bot)
3. THE System SHALL allow disabling notifications by type

---

### R18: Responsive Design

**User Story:** As a user, I want to access the application on mobile devices.

#### Acceptance Criteria

1. THE System SHALL be responsive for:
   - Desktop (1920px+)
   - Laptop (1024px-1920px)
   - Tablet (768px-1024px)
   - Mobile (320px-768px)
2. THE System SHALL optimize layout for each breakpoint
3. THE System SHALL maintain full functionality on touch devices
4. THE System SHALL load within 3 seconds on mobile networks
5. THE System SHALL support offline viewing of cached data

---

### R19: Real-Time Updates

**User Story:** As a user, I want real-time updates when data changes.

#### Acceptance Criteria

1. THE System SHALL connect via WebSocket for live updates
2. WHEN FLPS scores change, THE System SHALL update displayed values
3. WHEN loot is awarded, THE System SHALL update histories
4. THE System SHALL display notification for updates
5. WHERE WebSocket fails, THE System SHALL fallback to polling (30s interval)
6. THE System SHALL indicate connection status

---

### R20: Search and Navigation

**User Story:** As a user, I want quick search and intuitive navigation.

#### Acceptance Criteria

1. THE System SHALL provide global search:
   - Search raiders by name
   - Search items by name
   - Search raids by name/date
2. THE System SHALL provide keyboard shortcuts
3. THE System SHALL support browser history navigation
4. THE System SHALL remember user preferences (filters, sorting)
5. THE System SHALL provide breadcrumb navigation

---

### R21: Accessibility

**User Story:** As a user, I want the application to be accessible.

#### Acceptance Criteria

1. THE System SHALL meet WCAG 2.1 AA standards
2. THE System SHALL support keyboard navigation
3. THE System SHALL support screen readers
4. THE System SHALL provide sufficient color contrast
5. THE System SHALL not rely solely on color for information
6. THE System SHALL support text scaling

---

### R22: Error Handling and Recovery

**User Story:** As a user, I want clear feedback when errors occur.

#### Acceptance Criteria

1. WHEN API errors occur, THE System SHALL display user-friendly messages
2. THE System SHALL provide recovery suggestions
3. THE System SHALL support retry for transient failures
4. THE System SHALL gracefully degrade when features unavailable
5. THE System SHALL log errors for debugging
6. THE System SHALL maintain state during errors when possible
