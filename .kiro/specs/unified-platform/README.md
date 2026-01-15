# EdgeRush LootMan Unified Platform Specification

## Overview

This specification describes the complete EdgeRush LootMan platform - a unified solution for World of Warcraft raid guild management that consolidates features from RaidPlan.io, Raidbots, WoWAudit, and RCLootCouncil into a single, cohesive platform centered on the FLPS (Final Loot Priority Score) algorithm.

## Vision

**Make raiders' and raid leaders' lives easier by providing an all-in-one platform for raid preparation, execution, and post-raid analysis.**

The platform eliminates the need for raiders to juggle multiple websites and tools by providing:
- **Raid Planning**: Visual encounter planning with assignments (RaidPlan.io parity)
- **Simulation**: Gear optimization and upgrade analysis (Raidbots parity)
- **Guild Management**: Attendance, loot history, wishlists (WoWAudit parity)
- **Loot Distribution**: Fair, transparent FLPS-based loot council (RCLootCouncil parity)
- **Live Integration**: Real-time addon-to-website synchronization
- **Discord Bot**: Notifications and commands
- **Application Portal**: Streamlined recruitment

## Documents

| Document | Description |
|----------|-------------|
| [requirements.md](requirements.md) | Complete functional requirements |
| [design.md](design.md) | System architecture and integration design |
| [tasks.md](tasks.md) | Implementation phases and task breakdown |
| [external-data-strategy.md](external-data-strategy.md) | Strategy for using external APIs and assets |
| [addon-integration.md](addon-integration.md) | WoW addon development and communication |
| [discord-bot-integration.md](discord-bot-integration.md) | Discord bot feature specification |
| [application-portal.md](application-portal.md) | Guild application and recruitment portal |

## Research Summary

### Platforms Analyzed

| Platform | Core Features | Integration Approach |
|----------|---------------|---------------------|
| [RaidPlan.io](https://raidplan.io/) | Visual raid planning, step-based strategy maps, WCL integration | Build native planning editor |
| [Raidbots](https://www.raidbots.com/) | SimC simulations, Droptimizer, Top Gear, Gear Compare | Use local SimC + WoWAudit wishlist data |
| [WoWAudit](https://wowaudit.com/) | Attendance, loot, wishlists, desktop client sync | Already integrated; enhance integration |
| [RCLootCouncil](https://github.com/evil-morfar/RCLootCouncil2) | In-game loot council, voting, history | Build custom addon or fork |

### External Data Sources

| Source | Data Type | Integration Method |
|--------|-----------|-------------------|
| [Blizzard API](https://community.developer.battle.net/documentation/world-of-warcraft/game-data-apis) | Items, spells, instances, talents, media | OAuth2 API calls, cache results |
| [Wowhead](https://www.wowhead.com/tooltips) | Item tooltips, icons, data | Embed tooltip JS, link to pages |
| [Warcraft Logs](https://www.warcraftlogs.com/api/docs) | Combat performance, reports | GraphQL API (already integrated) |
| [Raider.IO](https://raider.io/) | M+ scores, character profiles | API for additional metrics |

## Key Principles

1. **Minimize Local Data Storage**: Use external APIs (Blizzard, Wowhead) for game data rather than maintaining our own item database
2. **Real-Time Synchronization**: Desktop client + addon communication enables live data flow
3. **FLPS-Centric Integration**: All features feed into or display FLPS calculations
4. **Graceful Degradation**: System works without addon, desktop client, or external APIs
5. **Full Feature Parity**: Match capabilities of all analyzed platforms while unifying them

## Technology Decisions

| Component | Technology | Rationale |
|-----------|------------|-----------|
| Web Frontend | Vue 3 + TypeScript + Tailwind | Already in use, SPA capabilities |
| Backend API | Spring Boot Kotlin | Already in use, proven stack |
| Desktop Client | Tauri (Rust + Web) | Cross-platform, lightweight, secure |
| WoW Addon | Lua | Required by WoW addon system |
| Discord Bot | Kotlin + Discord4J | Same language as backend |
| Database | PostgreSQL | Already in use |
| Real-Time | WebSockets + STOMP | Spring native support |

## Phase Summary

| Phase | Focus | Features |
|-------|-------|----------|
| Phase 1 | Foundation | Complete REST API, GraphQL, real-time backend |
| Phase 2 | Web Dashboard | Personal dashboard, leaderboards, loot history |
| Phase 3 | Raid Planning | Visual encounter editor, assignments, sharing |
| Phase 4 | Simulation | Local SimC integration, Droptimizer-like features |
| Phase 5 | Desktop Client | Saved variables sync, real-time communication |
| Phase 6 | WoW Addon | RCLootCouncil fork/integration, FLPS display |
| Phase 7 | Discord Bot | Commands, notifications, loot announcements |
| Phase 8 | Applications | Recruitment portal, automated data pull |
| Phase 9 | Polish | Performance, analytics, mobile optimization |

## Success Metrics

- **Feature Parity**: 100% of core features from analyzed platforms
- **Test Coverage**: 85%+ across all components
- **Performance**: <100ms API responses, <3s page loads
- **Adoption**: Raiders can replace 4 separate tools with this platform
- **Real-Time**: <1s latency for addon-to-web synchronization
