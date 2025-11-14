# Requirements Document - Web Dashboard

## Introduction

This feature provides a web-based user interface for raiders and administrators to view FLPS scores, loot history, and guild configuration. Currently, there is no user-facing interface for transparency, requiring manual communication of scores and decisions. This dashboard will enable self-service access to all FLPS data and provide administrative tools for guild management.

## Glossary

- **Dashboard**: Web application providing visual interface to FLPS data
- **Raider View**: Player-facing interface showing personal FLPS data
- **Admin Panel**: Administrative interface for guild configuration and management
- **FLPS Breakdown**: Detailed view showing all score components (RMS, IPI, RDF)
- **Loot History**: Historical record of all loot awards with FLPS context
- **The System**: EdgeRush LootMan web dashboard application
- **Frontend**: React/Vue/Flutter web application
- **Backend**: Existing Spring Boot REST API

## Requirements

### Requirement 1: User Authentication and Authorization

**User Story:** As a raider, I want to log in with my Discord or Battle.net account so that I can view my personal FLPS data securely.

#### Acceptance Criteria

1. THE System SHALL support OAuth2 authentication via Discord
2. THE System SHALL support OAuth2 authentication via Battle.net
3. WHERE a user authenticates, THE System SHALL map their account to WoWAudit character data
4. THE System SHALL support role-based access control (Raider, Officer, Admin)
5. WHEN authentication fails, THE System SHALL display clear error messages with recovery instructions

### Requirement 2: Raider Dashboard

**User Story:** As a raider, I want to view my current FLPS score and breakdown so that I understand my loot priority.

#### Acceptance Criteria

1. THE System SHALL display current FLPS score with visual indicator (color-coded by percentile)
2. THE System SHALL display detailed breakdown of RMS, IPI, and RDF components
3. THE System SHALL display sub-component scores (ACS, MAS, EPS, UV, Tier Bonus, Role Multiplier)
4. THE System SHALL display eligibility status with reasons if ineligible
5. WHERE behavioral actions exist, THE System SHALL display active penalties and expiration dates

### Requirement 3: Loot History View

**User Story:** As a raider, I want to view my loot history so that I can see what I've received and when my RDF expires.

#### Acceptance Criteria

1. THE System SHALL display all loot awards for the character with dates and item details
2. THE System SHALL display FLPS score at time of award
3. THE System SHALL display RDF penalty status and expiration date for each award
4. THE System SHALL support filtering by date range, tier, and item type
5. WHERE RDF is active, THE System SHALL highlight affected items and show countdown to expiry

### Requirement 4: Guild Leaderboard

**User Story:** As a raider, I want to see how my FLPS compares to other guild members so that I understand my relative priority.

#### Acceptance Criteria

1. THE System SHALL display sortable leaderboard of all raiders with FLPS scores
2. THE System SHALL support filtering by role, eligibility status, and class
3. THE System SHALL display key metrics (FLPS, RMS, IPI, RDF) in table format
4. THE System SHALL highlight the current user's position
5. WHERE users have equal FLPS, THE System SHALL display tie-breaking criteria

### Requirement 5: Item Wishlist View

**User Story:** As a raider, I want to view my wishlist with upgrade values so that I can prioritize which items to request.

#### Acceptance Criteria

1. THE System SHALL display all wishlist items with calculated upgrade values
2. THE System SHALL display simulation source (Raidbots vs wishlist percentage)
3. THE System SHALL display item details (name, slot, ilvl, source)
4. THE System SHALL support sorting by upgrade value, slot, and source
5. WHERE simulation data is stale, THE System SHALL display warning and last simulation date

### Requirement 6: Performance Metrics View

**User Story:** As a raider, I want to view my combat performance metrics so that I understand my MAS score.

#### Acceptance Criteria

1. THE System SHALL display recent Warcraft Logs performance data
2. THE System SHALL display DPA and ADT metrics with spec averages for comparison
3. THE System SHALL display performance trend over time (last 30 days)
4. THE System SHALL highlight critical performance issues affecting MAS
5. WHERE Warcraft Logs data is unavailable, THE System SHALL display message explaining MAS calculation

### Requirement 7: Admin Configuration Panel

**User Story:** As a guild administrator, I want to configure FLPS parameters so that scoring aligns with guild priorities.

#### Acceptance Criteria

1. THE System SHALL provide interface for editing guild-specific FLPS configuration
2. THE System SHALL display all configurable parameters with current values and defaults
3. THE System SHALL validate configuration changes before saving
4. THE System SHALL display preview of how changes affect current FLPS scores
5. WHEN configuration is saved, THE System SHALL log the change with administrator identity

### Requirement 8: Behavioral Action Management

**User Story:** As a guild officer, I want to manage behavioral actions so that I can apply penalties or bonuses to raiders.

#### Acceptance Criteria

1. THE System SHALL provide interface for creating behavioral actions
2. THE System SHALL support time-limited actions with automatic expiration
3. THE System SHALL display all active and historical actions per character
4. THE System SHALL show impact of actions on FLPS scores
5. WHEN actions are created or modified, THE System SHALL log the change with officer identity

### Requirement 9: Loot Ban Management

**User Story:** As a guild officer, I want to manage loot bans so that I can temporarily restrict loot eligibility.

#### Acceptance Criteria

1. THE System SHALL provide interface for creating and managing loot bans
2. THE System SHALL support time-limited bans with automatic expiration
3. THE System SHALL display all active bans with reasons and expiration dates
4. THE System SHALL show ban impact on eligibility in FLPS calculations
5. WHEN bans are created or modified, THE System SHALL log the change with officer identity

### Requirement 10: Loot Council Decision Interface

**User Story:** As a loot council member, I want to view FLPS scores during loot distribution so that I can make informed decisions.

#### Acceptance Criteria

1. THE System SHALL display real-time FLPS scores for all eligible raiders
2. THE System SHALL display detailed breakdowns on hover or click
3. THE System SHALL support filtering by role and eligibility
4. THE System SHALL highlight tie-breaking scenarios
5. WHERE multiple raiders have similar scores, THE System SHALL display recommendation with reasoning

### Requirement 11: Responsive Design

**User Story:** As a raider, I want to access the dashboard on mobile devices so that I can check my FLPS during raid breaks.

#### Acceptance Criteria

1. THE System SHALL provide responsive design supporting desktop, tablet, and mobile
2. THE System SHALL optimize layout for screen sizes from 320px to 4K
3. THE System SHALL maintain functionality on touch devices
4. THE System SHALL load within configurable time threshold (default: 3 seconds) on mobile networks
5. WHERE screen size is constrained, THE System SHALL prioritize critical information

### Requirement 12: Real-Time Updates

**User Story:** As a raider, I want to see real-time updates when my FLPS changes so that I always have current information.

#### Acceptance Criteria

1. THE System SHALL support WebSocket connections for real-time updates
2. WHEN FLPS scores change, THE System SHALL push updates to connected clients
3. WHEN loot is awarded, THE System SHALL update affected raiders' dashboards immediately
4. THE System SHALL display notification when data is updated
5. WHERE WebSocket connection fails, THE System SHALL fall back to polling with configurable interval (default: 30 seconds)
