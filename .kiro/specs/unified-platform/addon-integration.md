# Addon Integration Specification

## Overview

This document specifies the WoW addon development strategy, including the desktop client that bridges in-game data with the web platform. The approach is informed by WoWAudit's successful model and RCLootCouncil's feature set.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     WoW Game Client                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              EdgeRush LootMan Addon                       │   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │   │
│  │  │  FLPS   │  │  Loot   │  │  Gear   │  │ Comms   │      │   │
│  │  │ Display │  │ Council │  │  Sync   │  │ Handler │      │   │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                      │
│                            ▼                                      │
│              SavedVariables/EdgeRushLootMan.lua                   │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ File Watch
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Desktop Client (Tauri)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   File      │  │   Data      │  │    API      │              │
│  │   Watcher   │  │  Parser     │  │   Client    │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ HTTPS/WebSocket
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    EdgeRush LootMan Backend                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Addon      │  │  WebSocket  │  │   Sync      │              │
│  │  Sync API   │  │  Handler    │  │   Service   │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 1: WoW Addon

### Addon Structure

```
EdgeRushLootMan/
├── EdgeRushLootMan.toc           # Addon manifest
├── Core/
│   ├── Core.lua                  # Main initialization
│   ├── Constants.lua             # Shared constants
│   ├── Utils.lua                 # Utility functions
│   └── Comms.lua                 # Addon communication
├── Modules/
│   ├── FLPS/
│   │   ├── Display.lua           # FLPS score display
│   │   └── Leaderboard.lua       # Guild leaderboard
│   ├── LootCouncil/
│   │   ├── LootFrame.lua         # Loot distribution UI
│   │   ├── VotingFrame.lua       # Council voting
│   │   ├── AwardHistory.lua      # Loot history
│   │   └── RCLCCompat.lua        # RCLootCouncil compatibility
│   ├── GearSync/
│   │   ├── GearExport.lua        # Equipment export
│   │   ├── BagExport.lua         # Bag contents export
│   │   └── TalentExport.lua      # Talent export
│   └── Wishlist/
│       ├── Display.lua           # Wishlist display
│       └── Import.lua            # WoWAudit wishlist import
├── UI/
│   ├── MainFrame.lua             # Primary addon window
│   ├── MinimapButton.lua         # Minimap icon
│   └── Options.lua               # Settings panel
├── Libs/                         # Embedded libraries
│   ├── LibStub/
│   ├── AceAddon-3.0/
│   ├── AceDB-3.0/
│   ├── AceComm-3.0/
│   └── LibSerialize/
└── Locales/
    └── enUS.lua                  # Localization
```

### TOC File

```
## Interface: 110002
## Title: EdgeRush LootMan
## Notes: FLPS-based loot distribution and guild management
## Author: EdgeRush
## Version: 1.0.0
## SavedVariables: EdgeRushLootManDB, EdgeRushLootManGuildDB
## SavedVariablesPerCharacter: EdgeRushLootManCharDB
## OptionalDeps: RCLootCouncil
## X-Category: Guild

# Libraries
Libs\LibStub\LibStub.lua
Libs\AceAddon-3.0\AceAddon-3.0.xml
Libs\AceDB-3.0\AceDB-3.0.xml
Libs\AceComm-3.0\AceComm-3.0.xml
Libs\LibSerialize\LibSerialize.lua

# Core
Core\Constants.lua
Core\Utils.lua
Core\Comms.lua
Core\Core.lua

# Modules
Modules\FLPS\Display.lua
Modules\FLPS\Leaderboard.lua
Modules\LootCouncil\LootFrame.lua
Modules\LootCouncil\VotingFrame.lua
Modules\LootCouncil\AwardHistory.lua
Modules\LootCouncil\RCLCCompat.lua
Modules\GearSync\GearExport.lua
Modules\GearSync\BagExport.lua
Modules\GearSync\TalentExport.lua
Modules\Wishlist\Display.lua
Modules\Wishlist\Import.lua

# UI
UI\MainFrame.lua
UI\MinimapButton.lua
UI\Options.lua
```

### Saved Variables Structure

```lua
-- EdgeRushLootManDB (global)
EdgeRushLootManDB = {
    -- Synced from server (via desktop client)
    flpsData = {
        lastSync = 1705334400,  -- Unix timestamp
        guildId = "edgerush-twisting-nether",
        raiders = {
            ["PlayerName-Realm"] = {
                flps = 0.847,
                rms = 0.92,
                ipi = 0.88,
                rdf = 0.95,
                eligible = true,
                rdfExpiry = 1705939200,
            }
        }
    },

    -- Loot council decisions (synced)
    lootHistory = {
        {
            itemId = 207788,
            itemLink = "|cffff8000|Hitem:207788::::::::70:64::6:...|h[Item Name]|h|r",
            winner = "PlayerName-Realm",
            flps = 0.847,
            encounter = "Fyrakk",
            date = 1705334400,
            council = {
                { voter = "Officer1", vote = "MainSpec" },
                { voter = "Officer2", vote = "MainSpec" },
            }
        }
    },

    -- Configuration
    settings = {
        minimapButton = { show = true, position = 45 },
        showFlpsTooltip = true,
        showLeaderboard = true,
        announceAwards = true,
        councilChannel = "OFFICER",
    }
}

-- EdgeRushLootManCharDB (per character)
EdgeRushLootManCharDB = {
    -- Data to sync TO server
    syncData = {
        lastExport = 1705334400,
        gear = {
            ["INVSLOT_HEAD"] = { itemId = 207788, itemLink = "...", ilvl = 489 },
            -- ... all slots
        },
        bags = {
            { itemId = 207789, itemLink = "...", ilvl = 486, slot = "bag1:3" },
            -- ... bag items
        },
        talents = {
            specId = 64,  -- Frost Mage
            loadoutCode = "...",  -- Talent string
        },
        vault = {
            -- Weekly vault options if available
        }
    },

    -- Wishlist data (synced from server)
    wishlist = {
        lastSync = 1705334400,
        items = {
            { itemId = 207788, upgradeValue = 8.5, priority = 1 },
            { itemId = 207789, upgradeValue = 4.2, priority = 2 },
        }
    }
}
```

### Core Module

```lua
-- Core/Core.lua
local ADDON_NAME, Private = ...

-- Create main addon object
EdgeRushLootMan = LibStub("AceAddon-3.0"):NewAddon(
    "EdgeRushLootMan",
    "AceEvent-3.0",
    "AceComm-3.0"
)

local ELM = EdgeRushLootMan

-- Default database structure
local defaults = {
    global = {
        flpsData = {},
        lootHistory = {},
        settings = {
            minimapButton = { show = true },
            showFlpsTooltip = true,
        }
    },
    char = {
        syncData = {},
        wishlist = {},
    }
}

function ELM:OnInitialize()
    -- Initialize database
    self.db = LibStub("AceDB-3.0"):New("EdgeRushLootManDB", defaults, true)
    self.charDb = LibStub("AceDB-3.0"):New("EdgeRushLootManCharDB", defaults.char, true)

    -- Register slash commands
    SLASH_ELM1 = "/elm"
    SLASH_ELM2 = "/edgerush"
    SlashCmdList["ELM"] = function(msg)
        self:HandleSlashCommand(msg)
    end

    -- Initialize modules
    self:InitializeModules()
end

function ELM:OnEnable()
    -- Register events
    self:RegisterEvent("PLAYER_LOGOUT", "OnPlayerLogout")
    self:RegisterEvent("ENCOUNTER_END", "OnEncounterEnd")
    self:RegisterEvent("LOOT_OPENED", "OnLootOpened")

    -- Register comms
    self:RegisterComm("EdgeRushLM", "OnCommReceived")

    self:Print("EdgeRush LootMan loaded. Type /elm for commands.")
end

function ELM:OnPlayerLogout()
    -- Export gear data for desktop client
    self:ExportGearData()
    self:ExportBagData()
end

function ELM:HandleSlashCommand(msg)
    local cmd, args = strsplit(" ", msg, 2)
    cmd = strlower(cmd or "")

    if cmd == "" or cmd == "show" then
        self:ShowMainFrame()
    elseif cmd == "flps" then
        self:ShowFLPSFrame()
    elseif cmd == "leaderboard" or cmd == "lb" then
        self:ShowLeaderboard()
    elseif cmd == "loot" then
        self:ShowLootFrame()
    elseif cmd == "sync" then
        self:ExportAllData()
        self:Print("Data exported for sync.")
    elseif cmd == "wishlist" or cmd == "wl" then
        self:ShowWishlist()
    elseif cmd == "config" or cmd == "options" then
        self:ShowOptions()
    else
        self:PrintHelp()
    end
end
```

### FLPS Display Module

```lua
-- Modules/FLPS/Display.lua
local ELM = EdgeRushLootMan

function ELM:GetPlayerFLPS(playerName)
    local data = self.db.global.flpsData.raiders
    if not data then return nil end

    local name = playerName or UnitName("player") .. "-" .. GetRealmName()
    return data[name]
end

function ELM:ShowFLPSFrame()
    if not self.flpsFrame then
        self:CreateFLPSFrame()
    end

    local flps = self:GetPlayerFLPS()
    if flps then
        self.flpsFrame.score:SetText(string.format("%.3f", flps.flps))
        self.flpsFrame.rms:SetText(string.format("RMS: %.2f", flps.rms))
        self.flpsFrame.ipi:SetText(string.format("IPI: %.2f", flps.ipi))
        self.flpsFrame.rdf:SetText(string.format("RDF: %.2f", flps.rdf))

        if flps.eligible then
            self.flpsFrame.status:SetText("|cff00ff00Eligible|r")
        else
            self.flpsFrame.status:SetText("|cffff0000Ineligible|r")
        end

        if flps.rdfExpiry and flps.rdfExpiry > time() then
            local days = math.ceil((flps.rdfExpiry - time()) / 86400)
            self.flpsFrame.rdfNote:SetText(string.format("RDF expires in %d day(s)", days))
        else
            self.flpsFrame.rdfNote:SetText("")
        end
    else
        self.flpsFrame.score:SetText("No Data")
        self.flpsFrame.status:SetText("Sync required")
    end

    self.flpsFrame:Show()
end

-- Tooltip integration
function ELM:AddFLPSToTooltip(tooltip, unit)
    if not self.db.global.settings.showFlpsTooltip then return end
    if not unit or not UnitIsPlayer(unit) then return end

    local name = UnitName(unit) .. "-" .. GetRealmName()
    local flps = self:GetPlayerFLPS(name)

    if flps then
        tooltip:AddLine(" ")
        tooltip:AddDoubleLine("FLPS Score:", string.format("%.3f", flps.flps), 1, 0.82, 0, 1, 1, 1)
        if flps.eligible then
            tooltip:AddDoubleLine("Status:", "Eligible", 1, 0.82, 0, 0, 1, 0)
        else
            tooltip:AddDoubleLine("Status:", "Ineligible", 1, 0.82, 0, 1, 0, 0)
        end
    end
end

-- Hook tooltips
hooksecurefunc("GameTooltip_SetDefaultAnchor", function(tooltip, parent)
    local unit = select(2, tooltip:GetUnit())
    if unit then
        ELM:AddFLPSToTooltip(tooltip, unit)
    end
end)
```

### Loot Council Module

```lua
-- Modules/LootCouncil/LootFrame.lua
local ELM = EdgeRushLootMan

function ELM:OnLootOpened()
    -- Only process if player is loot master or in personal loot
    if not self:ShouldHandleLoot() then return end

    local numItems = GetNumLootItems()
    for i = 1, numItems do
        local itemLink = GetLootSlotLink(i)
        if itemLink then
            local itemId = GetItemInfoFromHyperlink(itemLink)
            local quality = select(3, GetItemInfo(itemLink))

            -- Only handle epic+ items
            if quality >= 4 then
                self:ProcessLootItem(itemLink, itemId)
            end
        end
    end
end

function ELM:ProcessLootItem(itemLink, itemId)
    -- Get eligible raiders
    local eligible = self:GetEligibleRaiders(itemId)

    if #eligible > 0 then
        -- Show loot council frame
        self:ShowLootCouncilFrame(itemLink, itemId, eligible)
    end
end

function ELM:GetEligibleRaiders(itemId)
    local eligible = {}
    local flpsData = self.db.global.flpsData.raiders

    if not flpsData then return eligible end

    for name, data in pairs(flpsData) do
        if data.eligible and self:CanUseItem(name, itemId) then
            -- Get wishlist data for upgrade value
            local wishlistItem = self:GetWishlistItem(name, itemId)
            local upgradeValue = wishlistItem and wishlistItem.upgradeValue or 0

            table.insert(eligible, {
                name = name,
                flps = data.flps,
                rms = data.rms,
                ipi = data.ipi,
                rdf = data.rdf,
                upgradeValue = upgradeValue,
                response = nil,  -- Will be set by raider
            })
        end
    end

    -- Sort by FLPS descending
    table.sort(eligible, function(a, b)
        return a.flps > b.flps
    end)

    return eligible
end

function ELM:ShowLootCouncilFrame(itemLink, itemId, eligible)
    if not self.lootCouncilFrame then
        self:CreateLootCouncilFrame()
    end

    local frame = self.lootCouncilFrame

    -- Set item info
    local name, _, quality, ilvl, _, _, _, _, _, icon = GetItemInfo(itemLink)
    frame.itemIcon:SetTexture(icon)
    frame.itemName:SetText(itemLink)
    frame.itemLevel:SetText("ilvl " .. ilvl)

    -- Clear and populate raiders
    for _, row in pairs(frame.rows) do
        row:Hide()
    end

    for i, raider in ipairs(eligible) do
        local row = frame.rows[i] or self:CreateRaiderRow(frame, i)
        self:PopulateRaiderRow(row, raider)
        row:Show()
    end

    frame:Show()
end

function ELM:PopulateRaiderRow(row, raider)
    local shortName = strsplit("-", raider.name)

    row.name:SetText(shortName)
    row.flps:SetText(string.format("%.3f", raider.flps))
    row.upgrade:SetText(string.format("+%.1f%%", raider.upgradeValue))

    -- Color code by upgrade value
    if raider.upgradeValue >= 5 then
        row.upgrade:SetTextColor(0, 1, 0)  -- Green
    elseif raider.upgradeValue >= 2 then
        row.upgrade:SetTextColor(1, 1, 0)  -- Yellow
    else
        row.upgrade:SetTextColor(1, 1, 1)  -- White
    end

    row.raiderData = raider
end

function ELM:AwardItem(itemLink, winner, reason)
    local record = {
        itemId = GetItemInfoFromHyperlink(itemLink),
        itemLink = itemLink,
        winner = winner,
        flps = self:GetPlayerFLPS(winner).flps,
        encounter = self:GetCurrentEncounter(),
        date = time(),
        reason = reason,
    }

    -- Store locally
    table.insert(self.db.global.lootHistory, record)

    -- Mark for sync
    self.charDb.char.pendingSync = self.charDb.char.pendingSync or {}
    table.insert(self.charDb.char.pendingSync, {
        type = "loot_award",
        data = record
    })

    -- Announce
    if self.db.global.settings.announceAwards then
        self:AnnounceLootAward(record)
    end
end
```

### Gear Export Module

```lua
-- Modules/GearSync/GearExport.lua
local ELM = EdgeRushLootMan

local EQUIPMENT_SLOTS = {
    "INVSLOT_HEAD", "INVSLOT_NECK", "INVSLOT_SHOULDER", "INVSLOT_BACK",
    "INVSLOT_CHEST", "INVSLOT_WRIST", "INVSLOT_HAND", "INVSLOT_WAIST",
    "INVSLOT_LEGS", "INVSLOT_FEET", "INVSLOT_FINGER1", "INVSLOT_FINGER2",
    "INVSLOT_TRINKET1", "INVSLOT_TRINKET2", "INVSLOT_MAINHAND", "INVSLOT_OFFHAND"
}

function ELM:ExportGearData()
    local gear = {}

    for _, slotName in ipairs(EQUIPMENT_SLOTS) do
        local slotId = GetInventorySlotInfo(slotName)
        local itemId = GetInventoryItemID("player", slotId)

        if itemId then
            local itemLink = GetInventoryItemLink("player", slotId)
            local ilvl = GetDetailedItemLevelInfo(itemLink)
            local _, _, _, _, _, _, _, _, equipLoc = GetItemInfo(itemLink)

            gear[slotName] = {
                itemId = itemId,
                itemLink = itemLink,
                ilvl = ilvl,
                equipLoc = equipLoc,
                gems = self:GetItemGems(slotId),
                enchant = self:GetItemEnchant(itemLink),
            }
        end
    end

    self.charDb.char.syncData.gear = gear
    self.charDb.char.syncData.lastExport = time()
end

function ELM:ExportBagData()
    local bags = {}

    for bag = 0, 4 do
        local numSlots = C_Container.GetContainerNumSlots(bag)
        for slot = 1, numSlots do
            local info = C_Container.GetContainerItemInfo(bag, slot)
            if info and info.hyperlink then
                local ilvl = GetDetailedItemLevelInfo(info.hyperlink)
                if ilvl and ilvl >= 400 then  -- Only track relevant gear
                    local _, _, _, _, _, _, _, _, equipLoc = GetItemInfo(info.hyperlink)
                    if equipLoc and equipLoc ~= "" then
                        table.insert(bags, {
                            itemId = info.itemID,
                            itemLink = info.hyperlink,
                            ilvl = ilvl,
                            slot = string.format("bag%d:%d", bag, slot),
                            equipLoc = equipLoc,
                        })
                    end
                end
            end
        end
    end

    self.charDb.char.syncData.bags = bags
end

function ELM:ExportAllData()
    self:ExportGearData()
    self:ExportBagData()

    -- Export talents
    local specId = GetSpecialization()
    local specInfo = GetSpecializationInfo(specId)
    self.charDb.char.syncData.talents = {
        specId = specInfo,
        loadoutCode = C_Traits.GenerateImportString(C_ClassTalents.GetActiveConfigID()),
    }

    self:Print("All data exported successfully.")
end

function ELM:GetItemGems(slotId)
    local gems = {}
    local link = GetInventoryItemLink("player", slotId)
    if not link then return gems end

    for i = 1, 3 do
        local gemLink = select(2, GetItemGem(link, i))
        if gemLink then
            local gemId = GetItemInfoFromHyperlink(gemLink)
            gems[i] = gemId
        end
    end

    return gems
end

function ELM:GetItemEnchant(itemLink)
    if not itemLink then return nil end

    local _, _, enchant = string.find(itemLink, "item:%d+:(%d+):")
    return enchant and tonumber(enchant) or nil
end
```

### RCLootCouncil Compatibility

```lua
-- Modules/LootCouncil/RCLCCompat.lua
local ELM = EdgeRushLootMan

-- Detect RCLootCouncil
function ELM:HasRCLootCouncil()
    return IsAddOnLoaded("RCLootCouncil")
end

-- Hook into RCLC if present
function ELM:SetupRCLCIntegration()
    if not self:HasRCLootCouncil() then return end

    local RCLootCouncil = _G.RCLootCouncil

    -- Add FLPS data to voting frame
    hooksecurefunc(RCLootCouncil.votingFrame, "Update", function(frame)
        ELM:EnhanceRCLCVotingFrame(frame)
    end)

    -- Export RCLC awards to our system
    RCLootCouncil:RegisterCallback("OnAwardReceived", function(data)
        ELM:OnRCLCAward(data)
    end)
end

function ELM:EnhanceRCLCVotingFrame(frame)
    -- Add FLPS column to voting frame
    for _, row in pairs(frame.rows) do
        if row.entry and row.entry.name then
            local flps = self:GetPlayerFLPS(row.entry.name)
            if flps then
                -- Add FLPS score display
                if not row.flpsText then
                    row.flpsText = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
                    row.flpsText:SetPoint("LEFT", row.response, "RIGHT", 10, 0)
                end
                row.flpsText:SetText(string.format("FLPS: %.3f", flps.flps))
            end
        end
    end
end

function ELM:OnRCLCAward(data)
    -- Convert RCLC award to our format and sync
    local record = {
        itemId = data.lootId,
        itemLink = data.link,
        winner = data.winner,
        flps = self:GetPlayerFLPS(data.winner)?.flps or 0,
        encounter = data.boss or "Unknown",
        date = time(),
        source = "RCLootCouncil",
    }

    table.insert(self.db.global.lootHistory, record)

    -- Queue for sync
    self.charDb.char.pendingSync = self.charDb.char.pendingSync or {}
    table.insert(self.charDb.char.pendingSync, {
        type = "loot_award",
        data = record
    })
end
```

---

## Part 2: Desktop Client

### Technology Choice: Tauri

**Rationale**:
- Lightweight (~10MB vs Electron's 150MB+)
- Rust backend for file operations (secure, fast)
- WebView for UI (reuse Vue components)
- Cross-platform (Windows, macOS, Linux)
- No Node.js runtime required

### Project Structure

```
desktop-client/
├── src-tauri/
│   ├── src/
│   │   ├── main.rs                 # Entry point
│   │   ├── config.rs               # Configuration handling
│   │   ├── watcher.rs              # SavedVariables file watcher
│   │   ├── parser.rs               # Lua SavedVariables parser
│   │   ├── api.rs                  # Backend API client
│   │   ├── sync.rs                 # Sync logic
│   │   └── tray.rs                 # System tray
│   ├── Cargo.toml
│   └── tauri.conf.json
├── src/                            # Vue frontend
│   ├── App.vue
│   ├── components/
│   │   ├── StatusPanel.vue
│   │   ├── SyncHistory.vue
│   │   └── Settings.vue
│   └── main.ts
├── package.json
└── vite.config.ts
```

### Core Functionality

#### File Watcher (Rust)

```rust
// src-tauri/src/watcher.rs
use notify::{Watcher, RecursiveMode, watcher};
use std::sync::mpsc::channel;
use std::time::Duration;
use std::path::Path;

pub struct SavedVariablesWatcher {
    wow_path: String,
    account_name: String,
}

impl SavedVariablesWatcher {
    pub fn new(wow_path: &str, account_name: &str) -> Self {
        SavedVariablesWatcher {
            wow_path: wow_path.to_string(),
            account_name: account_name.to_string(),
        }
    }

    pub fn get_saved_variables_path(&self) -> String {
        format!(
            "{}/WTF/Account/{}/SavedVariables/EdgeRushLootMan.lua",
            self.wow_path, self.account_name
        )
    }

    pub fn start_watching<F>(&self, callback: F) -> notify::Result<()>
    where
        F: Fn(&str) + Send + 'static,
    {
        let (tx, rx) = channel();
        let mut watcher = watcher(tx, Duration::from_secs(2))?;

        let sv_path = self.get_saved_variables_path();
        watcher.watch(Path::new(&sv_path), RecursiveMode::NonRecursive)?;

        // Watch for changes
        loop {
            match rx.recv() {
                Ok(event) => {
                    if let notify::DebouncedEvent::Write(_) = event {
                        // Read and parse the file
                        if let Ok(content) = std::fs::read_to_string(&sv_path) {
                            callback(&content);
                        }
                    }
                }
                Err(e) => println!("Watch error: {:?}", e),
            }
        }
    }
}
```

#### Lua Parser (Rust)

```rust
// src-tauri/src/parser.rs
use serde::{Deserialize, Serialize};
use std::collections::HashMap;

#[derive(Debug, Serialize, Deserialize)]
pub struct GearItem {
    pub item_id: i64,
    pub item_link: String,
    pub ilvl: i32,
    pub gems: Vec<i64>,
    pub enchant: Option<i64>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SyncData {
    pub last_export: i64,
    pub gear: HashMap<String, GearItem>,
    pub bags: Vec<GearItem>,
    pub talents: TalentData,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TalentData {
    pub spec_id: i32,
    pub loadout_code: String,
}

pub fn parse_saved_variables(content: &str) -> Result<SyncData, Box<dyn std::error::Error>> {
    // Use a Lua parser library or regex-based parsing
    // For simplicity, we'll use the `full_moon` crate for Lua parsing

    // Parse the Lua table structure
    // This is a simplified example
    let lua = full_moon::parse(content)?;

    // Extract EdgeRushLootManCharDB.syncData
    // (Implementation details omitted for brevity)

    Ok(SyncData {
        last_export: extract_timestamp(&lua, "lastExport")?,
        gear: extract_gear(&lua)?,
        bags: extract_bags(&lua)?,
        talents: extract_talents(&lua)?,
    })
}
```

#### API Client (Rust)

```rust
// src-tauri/src/api.rs
use reqwest::Client;
use serde::{Deserialize, Serialize};

pub struct EdgeRushApiClient {
    client: Client,
    base_url: String,
    api_key: String,
}

#[derive(Debug, Serialize)]
pub struct SyncPayload {
    pub character_name: String,
    pub realm: String,
    pub gear: serde_json::Value,
    pub bags: serde_json::Value,
    pub talents: serde_json::Value,
    pub loot_awards: Vec<serde_json::Value>,
}

impl EdgeRushApiClient {
    pub fn new(base_url: &str, api_key: &str) -> Self {
        EdgeRushApiClient {
            client: Client::new(),
            base_url: base_url.to_string(),
            api_key: api_key.to_string(),
        }
    }

    pub async fn sync_character(&self, data: SyncPayload) -> Result<(), reqwest::Error> {
        self.client
            .post(&format!("{}/api/addon/sync", self.base_url))
            .header("Authorization", format!("Bearer {}", self.api_key))
            .json(&data)
            .send()
            .await?;

        Ok(())
    }

    pub async fn get_flps_data(&self, guild_id: &str) -> Result<serde_json::Value, reqwest::Error> {
        let response = self.client
            .get(&format!("{}/api/addon/flps/{}", self.base_url, guild_id))
            .header("Authorization", format!("Bearer {}", self.api_key))
            .send()
            .await?;

        response.json().await
    }

    pub async fn get_wishlist(&self, character: &str, realm: &str) -> Result<serde_json::Value, reqwest::Error> {
        let response = self.client
            .get(&format!("{}/api/addon/wishlist/{}/{}", self.base_url, realm, character))
            .header("Authorization", format!("Bearer {}", self.api_key))
            .send()
            .await?;

        response.json().await
    }
}
```

#### Sync Service (Rust)

```rust
// src-tauri/src/sync.rs
use crate::{api::EdgeRushApiClient, parser, watcher::SavedVariablesWatcher};
use std::sync::Arc;
use tokio::sync::Mutex;

pub struct SyncService {
    api_client: EdgeRushApiClient,
    watcher: SavedVariablesWatcher,
    last_sync: Arc<Mutex<i64>>,
}

impl SyncService {
    pub fn new(
        wow_path: &str,
        account_name: &str,
        api_url: &str,
        api_key: &str,
    ) -> Self {
        SyncService {
            api_client: EdgeRushApiClient::new(api_url, api_key),
            watcher: SavedVariablesWatcher::new(wow_path, account_name),
            last_sync: Arc::new(Mutex::new(0)),
        }
    }

    pub async fn start(&self) {
        let api = self.api_client.clone();
        let last_sync = self.last_sync.clone();

        self.watcher.start_watching(move |content| {
            // Parse saved variables
            if let Ok(data) = parser::parse_saved_variables(content) {
                // Check if data is newer than last sync
                let mut sync_time = last_sync.blocking_lock();
                if data.last_export > *sync_time {
                    // Sync to backend
                    let payload = api::SyncPayload {
                        character_name: data.character_name.clone(),
                        realm: data.realm.clone(),
                        gear: serde_json::to_value(&data.gear).unwrap(),
                        bags: serde_json::to_value(&data.bags).unwrap(),
                        talents: serde_json::to_value(&data.talents).unwrap(),
                        loot_awards: vec![],  // Would include pending awards
                    };

                    // Async sync
                    tokio::spawn(async move {
                        if let Err(e) = api.sync_character(payload).await {
                            eprintln!("Sync failed: {:?}", e);
                        }
                    });

                    *sync_time = data.last_export;
                }
            }
        });
    }

    pub async fn fetch_and_write_flps(&self, guild_id: &str) -> Result<(), Box<dyn std::error::Error>> {
        // Fetch FLPS data from server
        let flps_data = self.api_client.get_flps_data(guild_id).await?;

        // Write to SavedVariables (for addon to read on /reload)
        let sv_path = self.watcher.get_saved_variables_path();
        let existing = std::fs::read_to_string(&sv_path)?;

        // Update flpsData section
        let updated = update_lua_table(&existing, "flpsData", &flps_data)?;
        std::fs::write(&sv_path, updated)?;

        Ok(())
    }
}
```

### System Tray (Rust)

```rust
// src-tauri/src/tray.rs
use tauri::{CustomMenuItem, SystemTray, SystemTrayMenu, SystemTrayEvent};

pub fn create_system_tray() -> SystemTray {
    let sync_now = CustomMenuItem::new("sync", "Sync Now");
    let settings = CustomMenuItem::new("settings", "Settings");
    let quit = CustomMenuItem::new("quit", "Quit");

    let tray_menu = SystemTrayMenu::new()
        .add_item(sync_now)
        .add_native_item(tauri::SystemTrayMenuItem::Separator)
        .add_item(settings)
        .add_native_item(tauri::SystemTrayMenuItem::Separator)
        .add_item(quit);

    SystemTray::new().with_menu(tray_menu)
}

pub fn handle_tray_event(app: &tauri::AppHandle, event: SystemTrayEvent) {
    match event {
        SystemTrayEvent::MenuItemClick { id, .. } => match id.as_str() {
            "sync" => {
                // Trigger manual sync
                app.emit_all("sync-requested", ()).unwrap();
            }
            "settings" => {
                // Show settings window
                if let Some(window) = app.get_window("main") {
                    window.show().unwrap();
                }
            }
            "quit" => {
                std::process::exit(0);
            }
            _ => {}
        },
        _ => {}
    }
}
```

---

## Part 3: Backend API Endpoints

### Addon Sync Endpoints

```kotlin
// AddonSyncController.kt
@RestController
@RequestMapping("/api/addon")
class AddonSyncController(
    private val addonSyncService: AddonSyncService,
    private val currentUserService: CurrentUserService
) {
    @PostMapping("/sync")
    fun syncCharacterData(
        @RequestBody payload: CharacterSyncPayload
    ): ResponseEntity<SyncResponse> {
        val user = currentUserService.getCurrentUser()
        val result = addonSyncService.processSync(user.guildId, payload)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/flps/{guildId}")
    fun getFlpsDataForAddon(
        @PathVariable guildId: String
    ): ResponseEntity<AddonFlpsData> {
        val data = addonSyncService.getFlpsDataForAddon(guildId)
        return ResponseEntity.ok(data)
    }

    @GetMapping("/wishlist/{realm}/{character}")
    fun getWishlistForAddon(
        @PathVariable realm: String,
        @PathVariable character: String
    ): ResponseEntity<AddonWishlistData> {
        val data = addonSyncService.getWishlist(realm, character)
        return ResponseEntity.ok(data)
    }

    @PostMapping("/loot-award")
    fun recordLootAward(
        @RequestBody payload: LootAwardPayload
    ): ResponseEntity<LootAwardResponse> {
        val result = addonSyncService.recordLootAward(payload)
        return ResponseEntity.ok(result)
    }
}

// Data classes
data class CharacterSyncPayload(
    val characterName: String,
    val realm: String,
    val gear: Map<String, GearItemDto>,
    val bags: List<GearItemDto>,
    val talents: TalentDataDto,
    val lootAwards: List<LootAwardPayload>?
)

data class AddonFlpsData(
    val lastSync: Long,
    val guildId: String,
    val raiders: Map<String, RaiderFlpsDto>
)

data class RaiderFlpsDto(
    val flps: Double,
    val rms: Double,
    val ipi: Double,
    val rdf: Double,
    val eligible: Boolean,
    val rdfExpiry: Long?
)
```

---

## Implementation Phases

### Phase 1: Addon Foundation
1. Create addon project structure
2. Implement gear/bag export
3. Implement SavedVariables format
4. Add slash commands and basic UI
5. Test data export on /reload and logout

### Phase 2: Desktop Client Foundation
1. Create Tauri project
2. Implement file watcher
3. Implement Lua parser
4. Add system tray
5. Test sync to backend

### Phase 3: Backend Integration
1. Add addon sync endpoints
2. Implement FLPS data format for addon
3. Add wishlist data format
4. Test end-to-end sync

### Phase 4: FLPS Display
1. Implement FLPS frame in addon
2. Add tooltip integration
3. Implement leaderboard view
4. Add desktop client → addon data flow

### Phase 5: Loot Council
1. Implement loot frame UI
2. Add voting functionality
3. Implement award recording
4. Add RCLootCouncil compatibility layer

### Phase 6: Polish
1. Add configuration UI
2. Implement error handling
3. Add offline mode
4. Performance optimization
5. Documentation
