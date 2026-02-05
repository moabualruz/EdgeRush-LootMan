# G-06: WoW Addon Implementation Plan

**Requirement:** R17-R19 (FLPS Display, Loot Council, Data Export)  
**Priority:** P2  
**Estimated Effort:** 8-12 hours  
**Status:** ⬜ TODO

---

## Current State

- **Folder:** `wow-addon/`
- **TOC:** `EdgeRushLootMan.toc` exists
- **Modules:** 4 folders exist (FLPS, GearSync, LootCouncil, Wishlist)
- **Status:** Basic skeleton, features incomplete

---

## Requirements Checklist

### R17: FLPS Display
- [ ] Personal FLPS score display
- [ ] FLPS breakdown (RMS/IPI/RDF)
- [ ] Eligibility status
- [ ] RDF countdown
- [ ] Guild FLPS leaderboard
- [ ] Dedicated addon window
- [ ] Tooltip enhancement (mouseover players)
- [ ] Chat link format for FLPS
- [ ] Disable during encounters

### R18: Loot Council Integration
- [ ] Loot distribution interface
- [ ] Eligible raiders with FLPS scores
- [ ] Wishlist data per raider
- [ ] Upgrade value per raider
- [ ] Council voting system
- [ ] FLPS-based recommendations
- [ ] Decision recording
- [ ] Sync to web platform
- [ ] RCLootCouncil compatibility mode
- [ ] Raid chat announcements

### R19: Data Export
- [ ] Export character gear (all slots)
- [ ] Export bag contents
- [ ] Export bank contents (optional)
- [ ] Export talents and spec
- [ ] Export consumable/flask status
- [ ] Export combat log events
- [ ] Export on logout
- [ ] Export on UI reload
- [ ] Export via `/elm sync` command
- [ ] Export on combat end
- [ ] Data compression for large sets

---

## Implementation Steps

### Phase 1: Core Framework (2h)
1. **Addon Core**
   - File: `Core/Main.lua`
   - Ace3 framework setup
   - Event registration
   - SavedVariables initialization

2. **Data Store**
   - File: `Core/DataStore.lua`
   - FLPS data caching
   - Sync status tracking

### Phase 2: FLPS Module (3h)
3. **FLPS Display Frame**
   - File: `Modules/FLPS/Display.lua`
   - Draggable/resizable minimap button + frame
   - Score display with color coding
   - Breakdown view

4. **Leaderboard Frame**
   - File: `Modules/FLPS/Leaderboard.lua`
   - Sortable list of guild members
   - Highlight current player

5. **Tooltips**
   - File: `Modules/FLPS/Tooltips.lua`
   - Hook player tooltips
   - Display FLPS inline

### Phase 3: Loot Council (3h)
6. **Loot Frame**
   - File: `Modules/LootCouncil/LootFrame.lua`
   - Item display with stats
   - Eligible raider list
   - FLPS + upgrade value columns

7. **Voting System**
   - File: `Modules/LootCouncil/Voting.lua`
   - Council member votes
   - Vote aggregation
   - Decision recording

8. **Announcements**
   - File: `Modules/LootCouncil/Announce.lua`
   - Raid warning format
   - Optional whispers to winner

### Phase 4: Data Export (2h)
9. **Gear Sync**
   - File: `Modules/GearSync/Export.lua`
   - Scan all equipment slots
   - Scan bags (configurable)
   - Format for desktop client

10. **Combat Exporter**
    - File: `Modules/GearSync/Combat.lua`
    - Hook COMBAT_LOG_EVENT_UNFILTERED
    - Track deaths, damage taken

---

## Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| `Core/Main.lua` | Modify | Ace3 setup, event handling |
| `Core/DataStore.lua` | Create | FLPS data cache |
| `Modules/FLPS/Display.lua` | Create | Main FLPS frame |
| `Modules/FLPS/Leaderboard.lua` | Create | Guild leaderboard |
| `Modules/FLPS/Tooltips.lua` | Create | Tooltip hooks |
| `Modules/LootCouncil/LootFrame.lua` | Create | Loot distribution UI |
| `Modules/LootCouncil/Voting.lua` | Create | Voting system |
| `Modules/LootCouncil/Announce.lua` | Create | Raid announcements |
| `Modules/GearSync/Export.lua` | Modify | Gear export logic |
| `Modules/GearSync/Combat.lua` | Create | Combat log export |
| `EdgeRushLootMan.toc` | Modify | Add new files |

---

## Dependencies

```lua
-- Embeds (already in TOC structure)
-- Ace3 libraries
Libs/LibStub/LibStub.lua
Libs/AceAddon-3.0/AceAddon-3.0.lua
Libs/AceEvent-3.0/AceEvent-3.0.lua
Libs/AceGUI-3.0/AceGUI-3.0.lua
Libs/AceDB-3.0/AceDB-3.0.lua
```

---

## SavedVariables Structure

```lua
EdgeRushLootManDB = {
  flps = {
    score = 0.847,
    breakdown = { rms = 0.30, ipi = 0.45, rdf = 0.12 },
    lastUpdate = 1707139200,
  },
  guildData = {
    -- Other raiders' FLPS (synced from server)
  },
  gearData = {
    -- Current equipment + bags
  },
  lootHistory = {
    -- Recent loot decisions
  },
}
```

---

## Testing Strategy

1. **Busted Tests:** `spec/` folder already exists
2. **Manual:** Load in WoW, verify frames render
3. **Integration:** Verify SavedVariables written correctly

---

## Verification Commands

```bash
cd wow-addon

# Run Lua tests
busted spec/

# Or using the script
./run_tests.bat  # Windows
./run_tests.sh   # Unix
```
