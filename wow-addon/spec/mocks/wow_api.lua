-- WoW API Mocks for Busted Testing
-- Provides mock implementations of WoW API functions

local MockWowApi = {}

-- Player data
MockWowApi._player = {
    name = "TestPlayer",
    realm = "TestRealm",
    class = "WARRIOR",
    classFile = "WARRIOR",
    level = 80,
    spec = "Arms",
    specId = 1,
    itemLevel = 500.5,
    avgItemLevel = 505.0,
    isGroupLeader = false,
    isGroupAssistant = false,
    isInRaid = false,
}

-- Equipment data
MockWowApi._equipment = {}

-- Group data
MockWowApi._raidRoster = {}

-- Reset all mock data
function MockWowApi:Reset()
    self._player = {
        name = "TestPlayer",
        realm = "TestRealm",
        class = "WARRIOR",
        classFile = "WARRIOR",
        level = 80,
        spec = "Arms",
        specId = 1,
        itemLevel = 500.5,
        avgItemLevel = 505.0,
        isGroupLeader = false,
        isGroupAssistant = false,
        isInRaid = false,
    }
    self._equipment = {}
    self._raidRoster = {}
end

-- Set player data
function MockWowApi:SetPlayer(data)
    for k, v in pairs(data) do
        self._player[k] = v
    end
end

-- Set equipment data
function MockWowApi:SetEquipment(slotId, itemLink)
    self._equipment[slotId] = itemLink
end

-- Set raid roster
function MockWowApi:SetRaidRoster(roster)
    self._raidRoster = roster
end

-- Install mocks into global scope
function MockWowApi:Install()
    -- Basic player functions
    _G.UnitName = function(unit)
        if unit == "player" then
            return self._player.name
        end
        return nil
    end

    _G.GetRealmName = function()
        return self._player.realm
    end

    _G.UnitClass = function(unit)
        if unit == "player" then
            return self._player.class, self._player.classFile
        end
        return nil, nil
    end

    _G.UnitLevel = function(unit)
        if unit == "player" then
            return self._player.level
        end
        return 0
    end

    _G.GetSpecialization = function()
        return self._player.specId
    end

    _G.GetSpecializationInfo = function(specId)
        if specId == self._player.specId then
            return specId, self._player.spec, nil, nil, nil
        end
        return nil
    end

    _G.GetAverageItemLevel = function()
        return self._player.avgItemLevel, self._player.itemLevel
    end

    -- Group/Raid functions
    _G.IsInRaid = function()
        return self._player.isInRaid
    end

    _G.UnitIsGroupLeader = function(unit)
        if unit == "player" then
            return self._player.isGroupLeader
        end
        return false
    end

    _G.UnitIsGroupAssistant = function(unit)
        if unit == "player" then
            return self._player.isGroupAssistant
        end
        return false
    end

    _G.GetNumGroupMembers = function()
        return #self._raidRoster
    end

    _G.GetRaidRosterInfo = function(index)
        local member = self._raidRoster[index]
        if member then
            return member.name, member.rank, member.subgroup,
                   member.level, member.class, member.fileName,
                   member.zone, member.online, member.isDead,
                   member.role, member.isML, member.combatRole
        end
        return nil
    end

    _G.GetLootMethod = function()
        return "master", 0
    end

    _G.GetMasterLootCandidate = function(index)
        if index == 1 then
            return self._player.name
        end
        return nil
    end

    -- Equipment functions
    _G.GetInventoryItemLink = function(unit, slotId)
        if unit == "player" then
            return self._equipment[slotId]
        end
        return nil
    end

    -- Item info functions
    _G.GetItemInfoInstant = function(itemLink)
        if not itemLink then return nil end
        -- Extract item ID from link
        local itemId = itemLink:match("item:(%d+)")
        return tonumber(itemId)
    end

    _G.GetDetailedItemLevelInfo = function(itemLink)
        if not itemLink then return nil end
        -- Extract ilvl from mock data
        local ilvl = itemLink:match("ilvl:(%d+)")
        return tonumber(ilvl) or 500
    end

    _G.GetItemStats = function(itemLink)
        return {}
    end

    -- C_Item namespace
    _G.C_Item = _G.C_Item or {}
    _G.C_Item.GetItemInfo = function(itemLink)
        if not itemLink then return nil end
        -- Return mock item info
        return "Test Item", nil, 4, 500, nil, "Armor", "Plate", nil, "INVTYPE_HEAD", 12345
    end

    -- Timer functions
    _G.C_Timer = _G.C_Timer or {}
    _G.C_Timer.After = function(delay, callback)
        -- In tests, execute callback immediately
        callback()
    end
    _G.C_Timer.NewTimer = function(delay, callback)
        return { Cancel = function() end }
    end

    -- Chat functions
    _G.SendChatMessage = function(msg, channel)
        -- No-op in tests
    end

    -- Sound functions
    _G.PlaySound = function(soundId)
        -- No-op in tests
    end

    _G.SOUNDKIT = { READY_CHECK = 1 }

    -- Raid class colors
    _G.RAID_CLASS_COLORS = {
        WARRIOR = { colorStr = "ffc79c6e" },
        PALADIN = { colorStr = "fff58cba" },
        HUNTER = { colorStr = "ffabd473" },
        ROGUE = { colorStr = "fffff569" },
        PRIEST = { colorStr = "ffffffff" },
        DEATHKNIGHT = { colorStr = "ffc41f3b" },
        SHAMAN = { colorStr = "ff0070de" },
        MAGE = { colorStr = "ff40c7eb" },
        WARLOCK = { colorStr = "ff8787ed" },
        MONK = { colorStr = "ff00ff96" },
        DRUID = { colorStr = "ffff7d0a" },
        DEMONHUNTER = { colorStr = "ffa330c9" },
        EVOKER = { colorStr = "ff33937f" },
    }

    -- String split (WoW built-in)
    _G.strsplit = function(delimiter, str)
        local result = {}
        for match in (str .. delimiter):gmatch("(.-)" .. delimiter) do
            table.insert(result, match)
        end
        return unpack(result)
    end

    -- Date function
    _G.date = os.date

    -- Time function
    _G.time = os.time

    -- Print function (capture for testing)
    _G._printOutput = {}
    _G.print = function(...)
        local args = {...}
        table.insert(_G._printOutput, table.concat(args, " "))
    end
end

return MockWowApi
