# EdgeRush LootMan - Comprehensive Raid Management Platform Plan

**Version:** 2.0
**Last Updated:** 2026-01-15
**Status:** Planning Phase

---

## Executive Summary

This document outlines the complete implementation plan for transforming EdgeRush LootMan into a comprehensive raid management platform with feature parity to:
- **[RaidPlan.io](https://raidplan.io/)** - Visual raid strategy planning
- **[Raidbots.com](https://www.raidbots.com/)** - Character simulation and optimization
- **[WoWAudit](https://wowaudit.com/)** - Guild management, attendance, and loot tracking
- **[RC Loot Council](https://github.com/evil-morfar/RCLootCouncil2)** - In-game loot distribution

The platform will provide an all-in-one solution for raid leaders and guild officers to manage everything related to raiding: recruitment, roster management, loot distribution, strategy planning, cooldown assignments, performance tracking, and real-time coordination via addon, Discord integration, and desktop companion app.

### Key Goals

1. **Single Platform** - Replace multiple disconnected tools with one unified solution
2. **Live Integration** - Real-time sync between addon, web app, and Discord bot
3. **Data-Driven Decisions** - Auto-pull data from APIs instead of manual entry
4. **Minimal Local Storage** - Leverage external CDNs for game assets
5. **Progressive Enhancement** - Works without addon, better with addon, best with desktop app

---

## Research Summary

### RaidPlan.io Analysis

**Core Features:**
- Visual boss encounter planning with drag-and-drop markers
- Multi-step encounter timelines
- Raid marker icons (skull, X, square, moon, triangle, diamond, circle, star)
- Role markers (tank, healer, DPS)
- Shape tools (circles, arrows, lines, rectangles)
- Text labels on shapes
- Color palette customization
- Step rearrangement
- VOD review integration with WarcraftLogs/FFLogs
- Plan sharing without registration
- Account system for plan management

**Supported Content:**
- WoW Retail & Classic raids
- FFXIV, Lost Ark, New World (multi-game support)

**Technical Notes:**
- Inspired by Exorsus Raid Tools' Visual Notes
- No addon required - browser-based
- Plans shareable via URL

### Raidbots.com Analysis

**Simulation Types:**

| Tool | Purpose | Use Case |
|------|---------|----------|
| **Top Gear** | Find optimal gear/talent combinations | Comprehensive optimization |
| **Droptimizer** | Prioritize raid bosses for upgrades | Loot farming priority |
| **Gear Compare** | Compare specific gear combinations | Quick comparisons |
| **Quick Sim** | Fast simulation with damage breakdown | Verify setup |
| **Advanced** | Custom SimC scripts | Power users |

**Key Metrics from Droptimizer:**
- **Expected Value**: Average DPS increase from item pool
- **Best Drop**: Maximum possible DPS gain
- **Priority**: Aggregate ranking combining value and probability

**Integration:**
- Uses SimulationCraft engine
- Requires Simulationcraft addon for character export
- Cloud-based simulation (no local install required)

### WoWAudit Analysis

**Core Features:**
- Character tracking (item level, specs, weekly progress)
- Loot distribution tracking
- Attendance monitoring
- Gear auditing (enchants, gems, tier pieces, legendaries)
- Wishlist/Droptimizer integration
- Historical data tracking
- Google Sheets integration
- API for custom integrations

**Scale:** 25,000+ guilds, 1,500,000+ characters

**RCLootCouncil Integration:**
- [RCLootCouncil_wowaudit addon](https://curseforge.com/wow/addons/rclootcouncil-wowaudit) automatically exports loot history
- Statistics page for past loot distribution
- API endpoint for raw loot history data

### RC Loot Council Analysis

**Core Features:**
- Automatic loot session with tradable items
- Voting frame for council members
- Response options (Mainspec, Offspec, Minor Upgrade, Pass)
- Real-time vote synchronization
- Award history with export (JSON, TSV)
- Council member management by guild rank
- Notes to council
- Combat minimization
- In-game sync of settings/history

**Addon Architecture:**
- Modular design with companion modules
- Test-driven development with .specs files
- Lua-based with extensive libraries
- 3,614 commits, well-maintained

### WoW Addon Communication Limitations

**Key Constraints:**
- Addons **cannot directly communicate** with external servers (no HTTP requests)
- SavedVariables are only written to disk on logout/reload/UI reload
- SavedVariables are only read from disk on UI loading
- Combat/Chat logs can be parsed in real-time by external apps
- Blizzard's ToS prohibits certain automation methods

**Viable Communication Patterns:**

| Method | Real-Time | Complexity | Reliability |
|--------|-----------|------------|-------------|
| **Desktop Companion App** | Near real-time | Medium | High |
| **Combat Log Parsing** | Real-time | Low | Medium |
| **Manual Export/Import** | No | Low | High |
| **Pixel Manipulation** | Yes | Very High | Low |

**Recommended Solution: Desktop Companion App**

This is how [WoWthing](https://www.curseforge.com/wow/addons/wowthing-collector), [WoWAudit](https://wowaudit.com/desktop), and other tools solve this problem:

1. WoW addon writes data to SavedVariables
2. Desktop app watches the SavedVariables folder
3. When files change (on reload/logout), app syncs to server
4. Server pushes updates back via WebSocket
5. Desktop app can optionally trigger in-game reloads

**Sources:**
- [WoW API Documentation](https://wowpedia.fandom.com/wiki/World_of_Warcraft_API)
- [WoWthing Collector](https://www.curseforge.com/wow/addons/wowthing-collector)
- [MMO-Champion Addon Discussion](https://www.mmo-champion.com/threads/1816688-Addon-that-saves-data-to-external-database)

### External Data Sources (Detailed)

#### Wowhead Integration

[Wowhead Tooltips](https://www.wowhead.com/tooltips) provide free embedded tooltips for items, spells, and more.

**Implementation:**

```html
<!-- Add to index.html -->
<script src="https://wow.zamimg.com/js/tooltips.js" async></script>
<script>
  const whTooltips = {
    colorLinks: true,
    iconizeLinks: true,
    renameLinks: true
  };
</script>
```

**Usage in Vue:**

```html
<a href="https://www.wowhead.com/item=207172" class="wowhead">Item Name</a>
```

**Icon CDN URLs:**

```
Large:  https://wow.zamimg.com/images/wow/icons/large/{iconName}.jpg
Medium: https://wow.zamimg.com/images/wow/icons/medium/{iconName}.jpg
Small:  https://wow.zamimg.com/images/wow/icons/small/{iconName}.jpg
```

#### Blizzard Game Data API

[Official API Documentation](https://develop.battle.net/documentation/world-of-warcraft/game-data-apis)

**Key Endpoints:**

| Endpoint | Purpose | Rate Limit |
| -------- | ------- | ---------- |
| `/profile/wow/character/{realm}/{name}` | Character profile | 100/sec |
| `/profile/wow/character/{realm}/{name}/equipment` | Equipped gear | 100/sec |
| `/data/wow/item/{itemId}` | Item details | 100/sec |
| `/data/wow/media/item/{itemId}` | Item icon | 100/sec |
| `/data/wow/spell/{spellId}` | Spell details | 100/sec |
| `/data/wow/journal-encounter/index` | Raid encounters | 100/sec |

**Rate Limits:** 100 requests/second, 36,000/hour

#### Raider.IO API

[Raider.IO Developer API](https://raider.io/api)

**Character Profile:**

```
GET https://raider.io/api/v1/characters/profile
    ?region={region}
    &realm={realm}
    &name={name}
    &fields=mythic_plus_scores_by_season,raid_progression,gear
```

**Response includes:**

- Mythic+ score (current and all seasons)
- Raid progression (kills per boss/difficulty)
- Gear summary (item level)
- Recent dungeon runs

**Guild Profile:**

```
GET https://raider.io/api/v1/guilds/profile
    ?region={region}
    &realm={realm}
    &name={name}
    &fields=raid_progression
```

#### Warcraft Logs API

Already implemented in our system. Key enhancements needed:

- Character encounter rankings
- Best parse retrieval by encounter
- Recent reports query for guild

---

## Platform Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EdgeRush LootMan Platform                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Web App    │  │ Discord Bot │  │ WoW Addon   │  │ Desktop App │        │
│  │  (Vue 3)    │  │  (JDA 5.x)  │  │   (Lua)     │  │  (Electron) │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
│         │                │                │                │                │
│         └────────────────┴────────────────┴────────────────┘                │
│                                    │                                        │
│                          ┌─────────▼─────────┐                              │
│                          │   REST/GraphQL    │                              │
│                          │       API         │                              │
│                          └─────────┬─────────┘                              │
│                                    │                                        │
│  ┌─────────────────────────────────┴─────────────────────────────────┐     │
│  │                         Backend Services                          │     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │     │
│  │  │   FLPS   │ │   Raid   │ │  Loot    │ │  Roster  │ │ Recruit │ │     │
│  │  │  Engine  │ │ Planning │ │  Mgmt    │ │  Mgmt    │ │  Mgmt   │ │     │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └─────────┘ │     │
│  └─────────────────────────────────┬─────────────────────────────────┘     │
│                                    │                                        │
│  ┌─────────────────────────────────┴─────────────────────────────────┐     │
│  │                        External Integrations                       │     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │     │
│  │  │ Blizzard │ │ Warcraft │ │ Raider   │ │  Wowhead │ │ SimC    │ │     │
│  │  │   API    │ │   Logs   │ │   IO     │ │ Tooltips │ │ Docker  │ │     │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └─────────┘ │     │
│  └───────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Flow Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          Data Flow Diagram                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  WoW Client                    Desktop App                    Server     │
│  ┌─────────┐                  ┌─────────┐                  ┌─────────┐   │
│  │  Addon  │ ──SavedVars──▶  │ Watcher │ ───HTTP POST──▶  │   API   │   │
│  │         │                  │         │                  │         │   │
│  │  RCLC   │ ◀──Commands───  │ Sync    │ ◀──WebSocket───  │  Push   │   │
│  └─────────┘                  └─────────┘                  └─────────┘   │
│       │                            │                            │        │
│       │                            │                            │        │
│       ▼                            ▼                            ▼        │
│  ┌─────────┐                  ┌─────────┐                  ┌─────────┐   │
│  │ Combat  │                  │ Desktop │                  │   DB    │   │
│  │  Log    │ ───realtime───▶ │   UI    │ ◀───queries────  │         │   │
│  └─────────┘                  └─────────┘                  └─────────┘   │
│                                                                           │
└──────────────────────────────────────────────────────────────────────────┘
```

### External Asset Strategy

**Minimize Local Storage - Use External CDNs:**

| Asset Type | Source | Usage |
|------------|--------|-------|
| Item Icons | Wowhead CDN | `wow.zamimg.com/images/wow/icons/` |
| Item Tooltips | Wowhead JS | Embedded tooltips |
| Spell Icons | Blizzard API | `/data/wow/media/spell/{id}` |
| Item Data | Blizzard API | `/data/wow/item/{id}` |
| Raid/Dungeon Info | Blizzard API | Journal endpoints |
| Boss Images | Wowhead | Boss encounter pages |
| Character Portraits | Blizzard API | Character media |

**What We Store Locally:**
- Boss encounter maps (arena images for planning)
- Guild/user configuration
- Loot history and awards
- Attendance records
- Raid plans and assignments
- Application submissions
- FLPS calculations and history

---

## Feature Specification by Module

### Module 1: Raid Planning (RaidPlan.io Parity)

#### 1.1 Plan Canvas Editor
**Status:** 80% Complete (Phase 2 done)

**Existing Features:**
- ✅ Canvas with zoom/pan
- ✅ Raid markers (skull, X, square, moon, etc.)
- ✅ Role markers (tank, healer, DPS)
- ✅ Shape tools (circle, line, arrow, rectangle)
- ✅ Multi-step timeline
- ✅ Undo/redo
- ✅ Plan persistence

**Missing Features:**
- [ ] Boss encounter background images
- [ ] Text labels on shapes
- [ ] Color palette customization
- [ ] Player name labels
- [ ] Import from MRT notes
- [ ] Export to MRT format
- [ ] VOD sync with WarcraftLogs
- [ ] Template library

#### 1.2 Encounter Database
**Status:** Not Started

**Requirements:**
- Fetch encounter data from Blizzard API
- Cache encounter info (boss names, abilities)
- Store arena/room images for planning
- Link abilities to timeline events

**Implementation:**
```typescript
// Backend: EncounterService
interface Encounter {
  id: number
  name: string
  instanceId: number
  instanceName: string
  mapImageUrl: string
  abilities: EncounterAbility[]
}

interface EncounterAbility {
  id: number
  name: string
  spellId: number
  iconUrl: string
  defaultTime: number // seconds into fight
  damageType: 'PHYSICAL' | 'MAGIC' | 'NATURE' | 'FIRE' | 'FROST' | 'SHADOW' | 'ARCANE'
}
```

#### 1.3 MRT Integration
**Status:** Not Started

**Requirements:**
- Parse MRT note format
- Generate MRT notes from plans
- Support spell links `{spell:ID}`
- Support color codes `|cFFRRGGBB|r`
- Support timestamps

**MRT Note Format:**
```
|cff00ff00--- Phase 1 ---|r
|cffff9900{time:0:25} Silken Tomb:|r
  {spell:64843} Healmaster - Divine Hymn
  {spell:740} Treehugger - Tranquility
```

### Module 2: Character Simulation (Raidbots Parity)

#### 2.1 SimulationCraft Integration
**Status:** Complete (Docker-based)

**Existing Features:**
- ✅ Docker-based SimC execution
- ✅ Profile generation from character data
- ✅ Upgrade value calculation
- ✅ FLPS integration

#### 2.2 Top Gear Simulation
**Status:** Not Started

**Requirements:**
- Accept multiple gear/talent combinations
- Run parallel simulations
- Return ranked results by DPS
- Cache simulation results

**Implementation:**
```kotlin
// Backend: TopGearService
data class TopGearRequest(
    val characterId: Long,
    val gearOptions: List<GearOption>,
    val talentOptions: List<String>,
    val fightStyle: FightStyle = FightStyle.PATCHWERK
)

data class TopGearResult(
    val combinations: List<GearCombinationResult>,
    val bestDps: Double,
    val simulationTime: Duration
)
```

#### 2.3 Droptimizer
**Status:** Partial (Wishlist exists)

**Existing Features:**
- ✅ Wishlist with item priorities
- ✅ Upgrade value display

**Missing Features:**
- [ ] Boss-by-boss priority ranking
- [ ] Expected value calculation
- [ ] Drop probability weighting
- [ ] Raid difficulty selection
- [ ] Integration with raid lockout

**Implementation:**
```kotlin
// Backend: DroptimizerService
data class DroptimizerRequest(
    val characterId: Long,
    val raidId: Int,
    val difficulty: RaidDifficulty
)

data class BossDropPriority(
    val bossId: Int,
    val bossName: String,
    val expectedValue: Double,      // Average DPS gain
    val bestDrop: Double,           // Max DPS gain
    val priority: Double,           // Composite ranking
    val upgrades: List<ItemUpgrade>
)
```

### Module 3: Guild Management (WoWAudit Parity)

#### 3.1 Character Auditing
**Status:** Partial

**Existing Features:**
- ✅ Item level tracking
- ✅ Spec tracking
- ✅ Tier piece tracking

**Missing Features:**
- [ ] Enchant audit with warnings
- [ ] Gem audit with warnings
- [ ] Consumable check (flasks, food, augments)
- [ ] Weekly M+ progress
- [ ] Weekly raid progress
- [ ] Great Vault tracking

#### 3.2 Attendance System
**Status:** Complete

**Existing Features:**
- ✅ Attendance tracking
- ✅ ACS (Attendance Composite Score)
- ✅ Calendar view
- ✅ Historical data

#### 3.3 Loot Distribution
**Status:** Complete (FLPS Core)

**Existing Features:**
- ✅ FLPS algorithm
- ✅ Loot history
- ✅ RDF (Recency Decay Factor)
- ✅ Award tracking

### Module 4: Recruitment & Applications

#### 4.1 Application System
**Status:** Partial

**Existing Features:**
- ✅ ApplyPage exists
- ✅ Basic form structure

**Missing Features - Application Form Fields:**

| Section | Field | Type | Auto-Pull |
|---------|-------|------|-----------|
| **Identity** | Battle.net ID | Text | OAuth |
| | Discord ID | Text | OAuth |
| | Character Name | Text | Input |
| | Server/Realm | Select | Input |
| **Auto-Fetched** | Class/Spec | Display | Blizzard API |
| | Item Level | Display | Blizzard API |
| | Raider.IO Score | Display | Raider.IO API |
| | Best Parses | Display | WarcraftLogs API |
| | Raid Progress | Display | Raider.IO API |
| **Availability** | Raid Days Available | Multi-select | Config |
| | Alt Characters | Text[] | Input |
| **Background** | Previous Guilds | Text | Input |
| | Reason for Leaving | Textarea | Input |
| | Why This Guild | Textarea | Input |
| | How Did You Hear | Select | Input |
| **Technical** | Stable Connection | Checkbox | Input |
| | Voice Comms | Checkbox | Input |
| | UI Screenshot | File | Input |
| **Additional** | Comments | Textarea | Input |

**Removed Fields (Auto-pulled instead):**
- Raider.IO link → Auto-fetched
- WarcraftLogs link → Auto-fetched
- Armory link → Auto-fetched via OAuth

#### 4.2 Application Review Dashboard
**Status:** Not Started

**Requirements:**
- List all pending applications
- Display auto-fetched metrics
- Character comparison with roster
- Voting/approval workflow
- Notes and comments
- Status tracking (Pending, Interview, Trial, Accepted, Rejected)

#### 4.3 Trial Management
**Status:** Not Started

**Requirements:**
- Trial period tracking
- Performance comparison
- Attendance during trial
- Trial extension/completion
- Promotion to raider

### Module 5: Cooldown Assignments

#### 5.1 Cooldown Grid
**Status:** Complete

**Existing Features:**
- ✅ Roster display with class colors
- ✅ Available cooldowns per player
- ✅ Boss ability timeline
- ✅ Drag & drop assignment
- ✅ Cooldown recovery validation
- ✅ Overlap warnings
- ✅ MRT export
- ✅ WeakAura export

#### 5.2 Missing Cooldown Features
**Status:** Not Started

**Missing Features:**
- [ ] Save/load cooldown plans
- [ ] Per-boss cooldown presets
- [ ] Import from MRT notes
- [ ] Share cooldown plans
- [ ] Link to raid plan steps

### Module 6: WoW Addon

#### 6.1 Addon Architecture Decision

**Option A: Extend RCLootCouncil**
- Pros: Established user base, tested codebase, loot distribution solved
- Cons: Dependency on third-party updates, limited customization

**Option B: Custom Addon (LootMan Addon)**
- Pros: Full control, integrated experience, all features unified
- Cons: More development effort, need to implement loot features

**Recommendation: Hybrid Approach**
1. Create **LootMan Companion Addon** for sync/integration
2. Support **RCLootCouncil** via module (like wowaudit does)
3. Optionally build **LootMan Full** in future if needed

#### 6.2 LootMan Companion Addon Features

**Data Export (SavedVariables):**
```lua
LootManData = {
    version = 1,
    lastSync = timestamp,
    character = {
        name = "CharName",
        realm = "Realm",
        class = "PRIEST",
        spec = "Holy",
        itemLevel = 489,
        gear = { ... },
        talents = "...",
    },
    attendance = {
        -- Current raid status
    },
    lootHistory = {
        -- Recent awards from RCLC
    },
    cooldownsUsed = {
        -- Combat log tracking
    }
}
```

**In-Game Features:**
- Display FLPS score
- Show loot priority for items
- Raid roster status
- Cooldown assignment alerts
- Integration with RC Loot Council

#### 6.3 Desktop Companion App

**Technology Stack:**

- **Framework:** Electron 28+
- **Language:** TypeScript
- **UI:** React (for settings window)
- **File Watching:** chokidar
- **HTTP Client:** axios
- **WebSocket:** socket.io-client
- **Packaging:** electron-builder

**Core Features:**

| Feature | Description | Priority |
| ------- | ----------- | -------- |
| File Watcher | Monitor SavedVariables folder for changes | P0 |
| Auto-Sync | Upload data when files change | P0 |
| WebSocket | Receive push updates from server | P0 |
| System Tray | Background operation with status icon | P0 |
| Auth | OAuth2 login flow | P0 |
| Settings | Configure WoW path, sync frequency | P1 |
| Combat Log | Parse combat log for real-time data | P2 |
| Auto-Update | Self-updating via electron-updater | P2 |

**Architecture Diagram:**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Desktop Companion App                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐  │
│  │   Main Process  │    │ Renderer Process│    │   Preload   │  │
│  │                 │    │                 │    │             │  │
│  │ - File Watcher  │◄──►│ - Settings UI   │◄──►│ - IPC Bridge│  │
│  │ - Sync Service  │    │ - Status Display│    │             │  │
│  │ - Tray Manager  │    │ - Login Flow    │    │             │  │
│  │ - WebSocket     │    │                 │    │             │  │
│  └────────┬────────┘    └─────────────────┘    └─────────────┘  │
│           │                                                      │
│           ▼                                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Services Layer                        │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │    │
│  │  │ FileWatcher  │  │  SyncService │  │ WebSocketSvc │   │    │
│  │  │              │  │              │  │              │   │    │
│  │  │ Watches:     │  │ Endpoints:   │  │ Events:      │   │    │
│  │  │ - SavedVars/ │  │ POST /sync   │  │ - loot:award │   │    │
│  │  │ - Logs/      │  │ POST /upload │  │ - raid:start │   │    │
│  │  │              │  │ GET /status  │  │ - roster:upd │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    System Tray UI                        │    │
│  │                                                          │    │
│  │  [Icon] LootMan Companion                               │    │
│  │  ├── Status: Connected ✓                                │    │
│  │  ├── Last Sync: 2 minutes ago                           │    │
│  │  ├── ─────────────────                                  │    │
│  │  ├── Sync Now                                           │    │
│  │  ├── Open Dashboard (browser)                           │    │
│  │  ├── Settings...                                        │    │
│  │  ├── ─────────────────                                  │    │
│  │  └── Quit                                               │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**File Watcher Implementation:**

```typescript
// services/FileWatcher.ts
import chokidar from 'chokidar';
import path from 'path';
import { parseLuaTable } from '../utils/luaParser';

export class FileWatcher {
  private watcher: chokidar.FSWatcher | null = null;

  constructor(
    private wowPath: string,
    private onDataChange: (data: AddonData) => void
  ) {}

  start(): void {
    const savedVarsPath = path.join(
      this.wowPath,
      '_retail_/WTF/Account/*/SavedVariables/LootMan.lua'
    );

    this.watcher = chokidar.watch(savedVarsPath, {
      persistent: true,
      ignoreInitial: false,
      awaitWriteFinish: {
        stabilityThreshold: 2000,
        pollInterval: 100
      }
    });

    this.watcher.on('change', async (filePath) => {
      const data = await this.parseAddonData(filePath);
      this.onDataChange(data);
    });
  }

  private async parseAddonData(filePath: string): Promise<AddonData> {
    const content = await fs.readFile(filePath, 'utf-8');
    return parseLuaTable(content);
  }
}
```

**Sync Service Implementation:**

```typescript
// services/SyncService.ts
import axios from 'axios';

export class SyncService {
  constructor(
    private apiUrl: string,
    private authToken: string
  ) {}

  async syncAddonData(data: AddonData): Promise<SyncResult> {
    const response = await axios.post(
      `${this.apiUrl}/api/v1/addon/sync`,
      {
        version: data.version,
        character: data.character,
        lootHistory: data.lootHistory,
        attendance: data.attendance,
        timestamp: Date.now()
      },
      {
        headers: {
          Authorization: `Bearer ${this.authToken}`,
          'Content-Type': 'application/json'
        }
      }
    );

    return response.data;
  }
}
```

**Desktop App Project Structure:**

```
lootman-companion/
├── src/
│   ├── main/
│   │   ├── index.ts           # Main process entry
│   │   ├── tray.ts            # System tray management
│   │   └── ipc.ts             # IPC handlers
│   ├── renderer/
│   │   ├── App.tsx            # Settings UI
│   │   ├── pages/
│   │   │   ├── Settings.tsx
│   │   │   └── Login.tsx
│   │   └── components/
│   ├── services/
│   │   ├── FileWatcher.ts
│   │   ├── SyncService.ts
│   │   ├── WebSocketService.ts
│   │   └── AuthService.ts
│   ├── utils/
│   │   ├── luaParser.ts       # Parse SavedVariables Lua
│   │   └── config.ts
│   └── preload/
│       └── index.ts
├── assets/
│   ├── icon.png
│   └── tray-icons/
├── package.json
├── electron-builder.yml
└── tsconfig.json
```

### Module 7: Discord Bot Enhancements

#### 7.1 Existing Commands
**Status:** Complete

- ✅ `/flps` - Check FLPS score
- ✅ `/flps compare` - Compare raiders
- ✅ `/leaderboard` - Guild rankings
- ✅ `/loot history` - View loot history
- ✅ `/wishlist` - View wishlist
- ✅ `/attendance` - Check attendance
- ✅ `/link` / `/unlink` - Character linking
- ✅ `/help` - Command help

#### 7.2 New Commands Needed

| Command | Description |
|---------|-------------|
| `/apply` | Start application process |
| `/apps list` | List pending applications |
| `/apps review <id>` | Review specific application |
| `/raid create` | Create raid event |
| `/raid signup <id>` | Sign up for raid |
| `/raid roster <id>` | Show raid roster |
| `/plan list` | List raid plans |
| `/plan share <id>` | Share plan to channel |
| `/cooldowns <boss>` | Show cooldown assignments |
| `/sim topgear` | Run Top Gear simulation |
| `/sim droptimizer` | Run Droptimizer |
| `/audit <character>` | Audit character |

#### 7.3 Discord Integration Features

**Automated Notifications:**
- New application received
- Application status change
- Raid signup reminders
- Loot awarded notifications
- Performance alerts (low parses)
- Attendance warnings

**Embeds:**
- Rich application summaries
- Raid plan previews
- Loot history tables
- Character audit cards

---

## Implementation Phases

### Phase 3A: Application & Recruitment System
**Timeline:** 2-3 weeks
**Priority:** High

#### Tasks:

1. **Backend: Application Domain**
   - [ ] Application entity with all fields
   - [ ] ApplicationStatus enum (PENDING, INTERVIEW, TRIAL, ACCEPTED, REJECTED)
   - [ ] ApplicationRepository with queries
   - [ ] ApplicationService with workflows
   - [ ] ApplicationController REST endpoints

2. **Backend: External Data Fetching**
   - [ ] BlizzardCharacterService - fetch character data
   - [ ] RaiderIOService - fetch M+ scores
   - [ ] WarcraftLogsService - fetch parses (enhance existing)
   - [ ] Caching layer for external data

3. **Frontend: Application Form**
   - [ ] Multi-step form wizard
   - [ ] OAuth integration for Battle.net/Discord
   - [ ] Real-time data fetching preview
   - [ ] File upload for screenshots
   - [ ] Form validation

4. **Frontend: Applications Dashboard**
   - [ ] Applications list with filters
   - [ ] Application detail view
   - [ ] Character comparison modal
   - [ ] Voting/decision UI
   - [ ] Notes and comments

5. **Discord: Application Commands**
   - [ ] `/apply` command with modal
   - [ ] `/apps list` with pagination
   - [ ] `/apps review` with embeds
   - [ ] Application notification webhooks

6. **Tests:**
   - [ ] Application domain unit tests
   - [ ] Application controller tests
   - [ ] Frontend form tests
   - [ ] Discord command tests

### Phase 3B: Raid Planning Enhancements
**Timeline:** 2-3 weeks
**Priority:** High

#### Tasks:

1. **Backend: Encounter Database**
   - [ ] Encounter entity and repository
   - [ ] EncounterAbility entity
   - [ ] BlizzardJournalService - fetch encounters
   - [ ] Encounter image storage/CDN

2. **Frontend: Boss Background Images**
   - [ ] Encounter selector with images
   - [ ] Boss arena backgrounds in canvas
   - [ ] Ability timeline from encounter data

3. **Frontend: Enhanced Canvas**
   - [ ] Text labels on shapes
   - [ ] Color palette picker
   - [ ] Player name labels
   - [ ] Template library

4. **MRT Integration**
   - [ ] MRT note parser
   - [ ] MRT note generator
   - [ ] Import/export UI

5. **Tests:**
   - [ ] Encounter service tests
   - [ ] Canvas enhancement tests
   - [ ] MRT parser tests

### Phase 3C: Advanced Simulation Features
**Timeline:** 2-3 weeks
**Priority:** Medium

#### Tasks:

1. **Backend: Top Gear Service**
   - [ ] TopGearRequest/Response DTOs
   - [ ] Combination generator
   - [ ] Parallel simulation executor
   - [ ] Result caching

2. **Backend: Enhanced Droptimizer**
   - [ ] BossDropPriority calculation
   - [ ] Expected value formula
   - [ ] Raid boss loot tables
   - [ ] Priority algorithm

3. **Frontend: Top Gear UI**
   - [ ] Gear selection interface
   - [ ] Talent selection
   - [ ] Simulation progress
   - [ ] Results comparison

4. **Frontend: Enhanced Droptimizer**
   - [ ] Boss priority list
   - [ ] Visual upgrade indicators
   - [ ] Raid difficulty selector

5. **Discord: Simulation Commands**
   - [ ] `/sim topgear` with options
   - [ ] `/sim droptimizer` with results

6. **Tests:**
   - [ ] Top Gear service tests
   - [ ] Droptimizer tests
   - [ ] Frontend simulation tests

### Phase 3D: WoW Addon Development
**Timeline:** 3-4 weeks
**Priority:** Medium

#### Tasks:

1. **LootMan Companion Addon (Lua)**
   - [ ] Core addon structure
   - [ ] SavedVariables data model
   - [ ] Character data collection
   - [ ] Combat log tracking
   - [ ] RC Loot Council integration hook
   - [ ] In-game FLPS display
   - [ ] Slash commands

2. **Desktop Companion App (Electron)**
   - [ ] File watcher for SavedVariables
   - [ ] Sync service to backend
   - [ ] WebSocket client for push updates
   - [ ] System tray interface
   - [ ] Auto-update mechanism

3. **Backend: Addon Sync API**
   - [ ] /api/v1/addon/sync endpoint
   - [ ] Character data merge logic
   - [ ] Conflict resolution
   - [ ] Rate limiting per user

4. **Tests:**
   - [ ] Addon unit tests (Lua)
   - [ ] Desktop app tests
   - [ ] Sync API tests

### Phase 3E: Character Audit & Guild Management
**Timeline:** 2-3 weeks
**Priority:** Medium

#### Tasks:

1. **Backend: Audit Service**
   - [ ] EnchantAuditService
   - [ ] GemAuditService
   - [ ] ConsumableCheckService
   - [ ] AuditReportGenerator

2. **Frontend: Audit Dashboard**
   - [ ] Character audit card
   - [ ] Warning indicators
   - [ ] Guild-wide audit summary
   - [ ] Audit history

3. **Frontend: Enhanced Roster**
   - [ ] Role distribution chart
   - [ ] Gear score trends
   - [ ] Performance trends
   - [ ] Attendance trends

4. **Discord: Audit Commands**
   - [ ] `/audit <character>` with embed
   - [ ] `/audit roster` summary

5. **Tests:**
   - [ ] Audit service tests
   - [ ] Frontend audit tests

### Phase 4: Real-time Features & Polish
**Timeline:** 2-3 weeks
**Priority:** Low

#### Tasks:

1. **WebSocket Integration**
   - [ ] Loot award notifications
   - [ ] Roster updates
   - [ ] Application status changes
   - [ ] Raid plan edits (collaboration)

2. **VOD Review Integration**
   - [ ] WarcraftLogs report linking
   - [ ] Timeline sync with plan steps
   - [ ] Performance overlay

3. **Analytics Dashboard**
   - [ ] Guild performance trends
   - [ ] Loot distribution analytics
   - [ ] Attendance patterns
   - [ ] Recruitment funnel

4. **Mobile Responsiveness**
   - [ ] Responsive layouts
   - [ ] Touch-friendly interactions
   - [ ] Mobile-specific features

---

## Database Schema Additions

### New Tables Required

```sql
-- Phase 3A: Applications
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    applicant_name VARCHAR(255) NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    realm VARCHAR(255) NOT NULL,
    character_class VARCHAR(50),
    specialization VARCHAR(50),
    item_level INTEGER,
    raider_io_score INTEGER,
    best_parse_avg DECIMAL(5,2),
    battle_net_id VARCHAR(255),
    discord_id VARCHAR(255),
    raid_days_available TEXT[], -- Array of available days
    previous_guilds TEXT,
    reason_for_leaving TEXT,
    why_this_guild TEXT,
    referral_source VARCHAR(255),
    stable_connection BOOLEAN DEFAULT TRUE,
    voice_comms BOOLEAN DEFAULT TRUE,
    screenshot_url VARCHAR(500),
    additional_comments TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_notes TEXT,
    trial_start_date TIMESTAMP,
    trial_end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE application_votes (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id),
    voter_id BIGINT NOT NULL,
    vote VARCHAR(20) NOT NULL, -- APPROVE, REJECT, ABSTAIN
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Phase 3B: Encounters
CREATE TABLE encounters (
    id INTEGER PRIMARY KEY, -- Blizzard encounter ID
    instance_id INTEGER NOT NULL,
    instance_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    map_image_url VARCHAR(500),
    difficulty_ids INTEGER[], -- Available difficulties
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE encounter_abilities (
    id BIGSERIAL PRIMARY KEY,
    encounter_id INTEGER NOT NULL REFERENCES encounters(id),
    spell_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_name VARCHAR(255),
    default_time INTEGER, -- Seconds into fight
    damage_type VARCHAR(50),
    requires_cooldown BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Phase 3C: Top Gear Simulations
CREATE TABLE top_gear_simulations (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    request_hash VARCHAR(64) NOT NULL, -- For caching
    gear_options JSONB NOT NULL,
    talent_options JSONB,
    fight_style VARCHAR(50) NOT NULL DEFAULT 'PATCHWERK',
    results JSONB,
    best_dps DECIMAL(12,2),
    simulation_time_ms INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- Phase 3D: Addon Sync
CREATE TABLE addon_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    character_id BIGINT,
    sync_type VARCHAR(50) NOT NULL, -- FULL, DELTA, COMBAT
    data_hash VARCHAR(64),
    records_synced INTEGER,
    errors JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Cooldown plan persistence (enhance existing)
CREATE TABLE cooldown_plans (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(255) NOT NULL,
    encounter_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    assignments JSONB NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## API Endpoints Summary

### New REST Endpoints

#### Applications
```
POST   /api/v1/applications                 - Submit application
GET    /api/v1/applications                 - List applications (filtered)
GET    /api/v1/applications/{id}            - Get application details
PUT    /api/v1/applications/{id}/status     - Update status
POST   /api/v1/applications/{id}/vote       - Submit vote
GET    /api/v1/applications/{id}/votes      - Get votes

GET    /api/v1/applications/character-data  - Fetch external character data
```

#### Encounters
```
GET    /api/v1/encounters                   - List all encounters
GET    /api/v1/encounters/{id}              - Get encounter details
GET    /api/v1/encounters/{id}/abilities    - Get encounter abilities
POST   /api/v1/encounters/sync              - Sync from Blizzard API
```

#### Simulations
```
POST   /api/v1/simulations/top-gear         - Run Top Gear sim
GET    /api/v1/simulations/top-gear/{id}    - Get sim results
POST   /api/v1/simulations/droptimizer      - Run Droptimizer
GET    /api/v1/simulations/droptimizer/{id} - Get results
```

#### Addon Sync
```
POST   /api/v1/addon/sync                   - Sync addon data
GET    /api/v1/addon/status                 - Get sync status
POST   /api/v1/addon/combat-log             - Upload combat log data
```

#### Cooldown Plans
```
POST   /api/v1/cooldown-plans               - Create plan
GET    /api/v1/cooldown-plans/{id}          - Get plan
PUT    /api/v1/cooldown-plans/{id}          - Update plan
DELETE /api/v1/cooldown-plans/{id}          - Delete plan
GET    /api/v1/cooldown-plans/encounter/{id} - Get plans for encounter
```

---

## External Integration Specifications

### Blizzard API Integration

**Required Endpoints:**
```
Character Profile:    /profile/wow/character/{realm}/{name}
Character Equipment:  /profile/wow/character/{realm}/{name}/equipment
Character Media:      /profile/wow/character/{realm}/{name}/character-media
Item Data:           /data/wow/item/{itemId}
Item Media:          /data/wow/media/item/{itemId}
Journal Encounters:  /data/wow/journal-encounter/index
Spell Data:          /data/wow/spell/{spellId}
Spell Media:         /data/wow/media/spell/{spellId}
```

**Rate Limits:** 100 req/sec, 36,000/hour

### Raider.IO API Integration

**Required Endpoints:**
```
Character Profile:   /api/v1/characters/profile
                     ?region={region}&realm={realm}&name={name}
                     &fields=mythic_plus_scores_by_season,raid_progression

Guild Profile:       /api/v1/guilds/profile
                     ?region={region}&realm={realm}&name={name}
                     &fields=raid_progression
```

### Warcraft Logs API Integration

**Existing:** Already implemented

**Enhancements Needed:**
- Character encounter rankings
- Best parse retrieval
- Recent reports query

### Wowhead Integration

**Tooltip Script:**
```html
<script src="https://wow.zamimg.com/js/tooltips.js"></script>
<script>
  const whTooltips = {
    colorLinks: true,
    iconizeLinks: true,
    renameLinks: true
  };
</script>
```

**Icon URLs:**
```
https://wow.zamimg.com/images/wow/icons/large/{iconName}.jpg
https://wow.zamimg.com/images/wow/icons/medium/{iconName}.jpg
https://wow.zamimg.com/images/wow/icons/small/{iconName}.jpg
```

---

## Success Metrics

### Phase 3A Success Criteria
- [ ] Application form fully functional
- [ ] External data auto-fetching working
- [ ] Application review workflow complete
- [ ] Discord integration working
- [ ] 50+ tests for application module

### Phase 3B Success Criteria
- [ ] All current raid encounters loaded
- [ ] Boss backgrounds in plan editor
- [ ] MRT import/export functional
- [ ] 30+ tests for enhancements

### Phase 3C Success Criteria
- [ ] Top Gear simulation working
- [ ] Droptimizer showing boss priorities
- [ ] Results caching effective
- [ ] 40+ tests for simulation

### Phase 3D Success Criteria
- [ ] Addon syncing data successfully
- [ ] Desktop app auto-syncing
- [ ] FLPS visible in-game
- [ ] RC Loot Council integration working

### Phase 3E Success Criteria
- [ ] Audit warnings displaying
- [ ] Guild-wide audit summary
- [ ] Discord audit commands working

### Overall Platform Success
- [ ] 800+ frontend tests
- [ ] 4000+ backend tests
- [ ] All RaidPlan.io features matched
- [ ] All Raidbots features matched
- [ ] All WoWAudit features matched
- [ ] Addon-to-web sync working
- [ ] Discord bot fully integrated

---

## Risk Assessment

### Technical Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Blizzard API rate limits | Medium | Aggressive caching, queue system |
| WoW addon limitations | High | Desktop companion app bridge |
| SimC Docker scaling | Medium | Queue system, parallel execution |
| External API changes | Medium | Version monitoring, fallbacks |

### Product Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Feature creep | High | Strict phase adherence |
| Complexity overwhelming users | Medium | Progressive disclosure UI |
| Competition from existing tools | Medium | Integration, not replacement |

### Operational Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data sync failures | High | Retry logic, manual fallback |
| External service downtime | Medium | Graceful degradation |
| Addon ToS compliance | High | Careful review of Blizzard policies |

---

## Appendix A: WoW Class Cooldowns Reference

```typescript
const classCooldowns = {
  WARRIOR: [
    { id: 'rallying-cry', name: 'Rallying Cry', spellId: 97462, duration: 10, cd: 180 },
  ],
  PALADIN: [
    { id: 'aura-mastery', name: 'Aura Mastery', spellId: 31821, duration: 8, cd: 180 },
    { id: 'divine-toll', name: 'Divine Toll', spellId: 375576, duration: 0, cd: 60 },
    { id: 'blessing-sacrifice', name: 'Blessing of Sacrifice', spellId: 6940, duration: 12, cd: 120 },
  ],
  PRIEST: [
    { id: 'divine-hymn', name: 'Divine Hymn', spellId: 64843, duration: 8, cd: 180 },
    { id: 'power-word-barrier', name: 'Power Word: Barrier', spellId: 62618, duration: 10, cd: 180 },
    { id: 'spirit-link', name: 'Spirit Link Totem', spellId: 98008, duration: 6, cd: 180 }, // Shaman!
    { id: 'pain-suppression', name: 'Pain Suppression', spellId: 33206, duration: 8, cd: 180 },
  ],
  DRUID: [
    { id: 'tranquility', name: 'Tranquility', spellId: 740, duration: 8, cd: 180 },
    { id: 'ironbark', name: 'Ironbark', spellId: 102342, duration: 12, cd: 90 },
  ],
  SHAMAN: [
    { id: 'spirit-link', name: 'Spirit Link Totem', spellId: 98008, duration: 6, cd: 180 },
    { id: 'healing-tide', name: 'Healing Tide Totem', spellId: 108280, duration: 10, cd: 180 },
    { id: 'ancestral-guidance', name: 'Ancestral Guidance', spellId: 108281, duration: 10, cd: 120 },
  ],
  MONK: [
    { id: 'revival', name: 'Revival', spellId: 115310, duration: 0, cd: 180 },
    { id: 'life-cocoon', name: 'Life Cocoon', spellId: 116849, duration: 12, cd: 120 },
  ],
  EVOKER: [
    { id: 'rewind', name: 'Rewind', spellId: 363534, duration: 0, cd: 240 },
    { id: 'stasis', name: 'Stasis', spellId: 370537, duration: 0, cd: 90 },
  ],
  DEMON_HUNTER: [
    { id: 'darkness', name: 'Darkness', spellId: 196718, duration: 8, cd: 300 },
  ],
  DEATH_KNIGHT: [
    { id: 'amz', name: 'Anti-Magic Zone', spellId: 51052, duration: 8, cd: 120 },
  ],
}
```

---

## Appendix B: Application Form Reference

Based on [Department of Death application](https://docs.google.com/forms/d/e/1FAIpQLSfzxwnsufyKsVeSImoxLumIXCc9wNb2OBrVcpAl6ztYXgsouw/viewform), optimized for auto-fetching.

### Original Form Fields (Google Form)

| Field | Type | Required | Our Approach |
| ----- | ---- | -------- | ------------ |
| Email | Email | Yes | OAuth (Discord/Battle.net) |
| Name | Text | Yes | OAuth (Battle.net) |
| Age | Number | Yes | Manual input |
| Location | Text | Yes | Manual input |
| Battle.net/Discord ID | Text | Yes | OAuth auto-fill |
| Character name + spec | Text | Yes | OAuth + API auto-fill |
| Raider.IO link | URL | Yes | **AUTO-FETCH** |
| WarcraftLogs link | URL | Yes | **AUTO-FETCH** |
| Alt Armory link | URL | No | **AUTO-FETCH** |
| Raid schedule available | Yes/No | Yes | Manual select |
| Alt availability | Yes/No | Yes | Manual select |
| Stable connection | Yes/No | Yes | Manual select |
| Reason for leaving | Paragraph | Yes | Manual input |
| Why this guild | Paragraph | Yes | Manual input |
| Additional comments | Paragraph | No | Manual input |

### Optimized Application Form

#### Step 1: Authentication (OAuth)

```typescript
// User clicks "Apply with Battle.net" or "Apply with Discord"
// OAuth flow provides:
interface OAuthData {
  battleNetId: string;
  discordId: string;
  email: string;
  battletag: string;
}
```

#### Step 2: Character Selection

```typescript
// After OAuth, fetch all characters from Battle.net API
interface CharacterSelection {
  mainCharacter: {
    name: string;
    realm: string;
    region: 'us' | 'eu' | 'kr' | 'tw';
  };
  altCharacters: CharacterSelection['mainCharacter'][];
}
```

#### Step 3: Auto-Fetched Data Display (Read-Only)

```typescript
// Fetched automatically - displayed but not editable
interface AutoFetchedData {
  // From Blizzard API
  class: string;
  specialization: string;
  itemLevel: number;
  gearSummary: GearItem[];

  // From Raider.IO API
  raiderIOScore: number;
  mythicPlusRuns: MythicPlusRun[];
  raidProgression: RaidProgression;

  // From WarcraftLogs API
  bestParses: Parse[];
  averagePerformance: number;
  recentReports: Report[];
}
```

#### Step 4: Manual Input Fields

```typescript
interface ApplicationInput {
  // Personal
  age: number;
  location: string;
  timezone: string;

  // Availability
  raidDaysAvailable: ('wednesday' | 'sunday' | 'monday')[];
  altAvailableAtSameLevel: boolean;
  stableConnection: boolean;
  voiceCommsAvailable: boolean;

  // Background (required)
  previousGuilds: string;  // Free text
  reasonForLeaving: string;  // Paragraph
  whyThisGuild: string;  // Paragraph
  howDidYouHear: 'wowprogress' | 'raiderio' | 'friend' | 'discord' | 'other';
  referredBy?: string;  // If friend selected

  // Optional
  additionalComments?: string;
  uiScreenshot?: File;  // Optional upload
}
```

### Application Form UI Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Apply to [Guild Name]                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 1 of 4: Connect Your Accounts                             │
│  ─────────────────────────────────────────────────              │
│                                                                  │
│  [🎮 Connect Battle.net]    [💬 Connect Discord]                │
│                                                                  │
│  We'll use these to verify your identity and fetch your         │
│  character data automatically.                                   │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 2 of 4: Select Your Character                             │
│  ─────────────────────────────────────────────────              │
│                                                                  │
│  Main Character:                                                 │
│  ┌─────────────────────────────────────────────────┐            │
│  │ [Dropdown: Character Name - Realm]              │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  Alt Characters: (optional)                                      │
│  [+ Add Alt Character]                                          │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 3 of 4: Your Character Profile                            │
│  ─────────────────────────────────────────────────              │
│  (Auto-fetched - this is what we see about you)                 │
│                                                                  │
│  ┌──────────────────────┬──────────────────────────┐            │
│  │ Class: Holy Priest   │ Item Level: 489          │            │
│  ├──────────────────────┼──────────────────────────┤            │
│  │ Raider.IO: 2,847     │ M+ Best: +24 Avg        │            │
│  ├──────────────────────┼──────────────────────────┤            │
│  │ Best Parse: 95%      │ Median Parse: 87%        │            │
│  ├──────────────────────┴──────────────────────────┤            │
│  │ Raid Progress: 8/8 H, 3/8 M Nerub-ar Palace    │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  [View Full Raider.IO] [View WarcraftLogs] [View Armory]        │
│                                                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 4 of 4: Tell Us About Yourself                            │
│  ─────────────────────────────────────────────────              │
│                                                                  │
│  Age: [___]  Location: [___________]  Timezone: [▼ Select]      │
│                                                                  │
│  Raid Schedule (Wed/Sun/Mon 22:00-01:00 ST):                    │
│  [✓] I can make all raid days                                   │
│  [ ] I can only make some days (specify below)                  │
│                                                                  │
│  Previous Guild(s):                                              │
│  ┌─────────────────────────────────────────────────┐            │
│  │                                                  │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  Why are you leaving your current guild?                        │
│  ┌─────────────────────────────────────────────────┐            │
│  │                                                  │            │
│  │                                                  │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  Why do you want to join [Guild Name]?                          │
│  ┌─────────────────────────────────────────────────┐            │
│  │                                                  │            │
│  │                                                  │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  How did you hear about us?                                      │
│  [▼ Select: WoWProgress / Raider.IO / Friend / Discord / Other] │
│                                                                  │
│  Confirmations:                                                  │
│  [✓] I have a stable internet connection                        │
│  [✓] I have working microphone and can use voice comms          │
│  [✓] I have an alt at similar performance level                 │
│                                                                  │
│  Additional Comments: (optional)                                 │
│  ┌─────────────────────────────────────────────────┐            │
│  │                                                  │            │
│  └─────────────────────────────────────────────────┘            │
│                                                                  │
│  UI Screenshot: (optional)                                       │
│  [📎 Upload File]                                               │
│                                                                  │
│                            [Submit Application]                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Backend Implementation

```kotlin
// domain/applications/model/Application.kt
data class Application(
    val id: Long?,
    val guildId: String,
    val status: ApplicationStatus,

    // OAuth data
    val battleNetId: String,
    val discordId: String?,
    val email: String,

    // Character data (auto-fetched)
    val mainCharacterName: String,
    val mainCharacterRealm: String,
    val mainCharacterRegion: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Int,
    val raiderIOScore: Int?,
    val bestParseAverage: Double?,
    val raidProgression: String?,

    // User input
    val age: Int,
    val location: String,
    val timezone: String,
    val raidDaysAvailable: List<String>,
    val previousGuilds: String,
    val reasonForLeaving: String,
    val whyThisGuild: String,
    val referralSource: String,
    val stableConnection: Boolean,
    val voiceComms: Boolean,
    val altAvailable: Boolean,
    val additionalComments: String?,
    val screenshotUrl: String?,

    // Alt characters
    val altCharacters: List<AltCharacter>,

    // Timestamps
    val createdAt: Instant,
    val updatedAt: Instant,
    val reviewedAt: Instant?,
    val reviewedBy: Long?
)

enum class ApplicationStatus {
    PENDING,
    UNDER_REVIEW,
    INTERVIEW_SCHEDULED,
    TRIAL,
    ACCEPTED,
    REJECTED,
    WITHDRAWN
}
```

### Data Fetching Service

```kotlin
// domain/applications/service/ApplicationDataService.kt
@Service
class ApplicationDataService(
    private val blizzardClient: BlizzardApiClient,
    private val raiderIOClient: RaiderIOClient,
    private val warcraftLogsClient: WarcraftLogsClient
) {
    suspend fun fetchCharacterData(
        name: String,
        realm: String,
        region: String
    ): CharacterData = coroutineScope {
        val blizzardData = async { blizzardClient.getCharacter(name, realm, region) }
        val raiderIOData = async { raiderIOClient.getCharacterProfile(name, realm, region) }
        val wclData = async { warcraftLogsClient.getCharacterParses(name, realm, region) }

        CharacterData(
            blizzard = blizzardData.await(),
            raiderIO = raiderIOData.await(),
            warcraftLogs = wclData.await()
        )
    }
}
```

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-15 | Claude | Initial comprehensive plan |

**Next Review:** After Phase 3A completion
