# EdgeRush LootMan - WoW Addon

World of Warcraft addon providing FLPS (Final Loot Priority Score) integration and full loot council functionality with RCLootCouncil parity.

## Features

### Standalone Mode (Full RCLC Replacement)
- Master Looter frame for loot distribution decisions
- Response frame for raiders to declare need/greed/pass
- Voting system for loot council members
- Loot history tracking with full session records
- Auto-pass on items not usable by class
- Configurable response timeout with auto-pass
- Award announcements in raid chat
- Export loot history to CSV
- Session management (start, end, cancel)

### FLPS Integration
- Real-time FLPS score display on tooltips
- Guild FLPS leaderboard frame
- FLPS score breakdown (RMS/IPI/RDF components)
- Sort candidates by FLPS score or upgrade value
- Wishlist integration for upgrade recommendations
- Configurable FLPS component weights

### RCLootCouncil Compatibility Mode
- Detect RCLootCouncil presence automatically
- Inject FLPS column into RCLC voting frame
- Add FLPS data to RCLC candidate tooltips
- Import loot history from RCLootCouncil
- Export EdgeRush history to RCLC format
- Toggle between modes via settings

### Data Synchronization
- Export gear data to SavedVariables on logout
- Export bag contents for upgrade tracking
- Sync FLPS data from desktop client
- AceComm protocol for raid-wide communication

## Installation

1. Download the addon
2. Extract to `World of Warcraft/_retail_/Interface/AddOns/EdgeRushLootMan`
3. Restart WoW or type `/reload`

## Commands

- `/elm help` - Show help
- `/elm config` - Open configuration
- `/elm sync` - Export gear data
- `/elm flps` - Show your FLPS score
- `/elm leaderboard` - Show guild FLPS leaderboard
- `/elm loot` - Open loot council frame
- `/elm ml` - Open master loot frame (ML only)
- `/elm history` - Show loot history
- `/elm wishlist` - Show your wishlist
- `/elm minimap` - Toggle minimap button

## Development

### Running Tests

The addon uses [busted](https://olivinelabs.com/busted/) for Lua unit testing.

#### Prerequisites

1. Install Lua and LuaRocks
2. Install busted: `luarocks install busted`

#### Running Tests

```bash
# Linux/Mac
./run_tests.sh

# Windows
run_tests.bat

# Or directly with busted
busted --verbose --pattern="_spec" spec/
```

### Test Structure

```
spec/
├── mocks/
│   ├── init.lua          # Test environment setup
│   ├── wow_api.lua       # WoW API mocks
│   └── ace_libs.lua      # Ace3 library mocks
├── core/
│   ├── constants_spec.lua
│   └── utils_spec.lua
└── modules/
    ├── session_manager_spec.lua
    ├── gear_export_spec.lua
    └── loot_history_spec.lua
```

### Writing Tests

```lua
-- Example test
describe("Utils", function()
    local ELM

    before_each(function()
        MockWowApi, MockAceLibs = TestEnv:Setup()
        ELM = TestEnv:LoadModule("Core/Constants.lua")
        TestEnv:LoadModule("Core/Utils.lua")
    end)

    it("should format FLPS as percentage", function()
        local result = ELM.Utils:FormatFLPS(0.85)
        assert.equals("85.0%", result)
    end)
end)
```

## Dependencies

- Ace3 libraries (included in Libs/)
  - AceAddon-3.0
  - AceConsole-3.0
  - AceEvent-3.0
  - AceComm-3.0
  - AceSerializer-3.0
  - AceDB-3.0
- LibDataBroker-1.1
- LibDBIcon-1.0

## License

Copyright (c) EdgeRush Gaming. All rights reserved.
