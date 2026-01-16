-- EdgeRush LootMan Utilities
-- Common utility functions used throughout the addon

local ADDON_NAME, ELM = ...

ELM.Utils = {}

-- Get character identifier (Name-Realm)
function ELM.Utils:GetCharacterID()
    local name = UnitName("player")
    local realm = GetRealmName()
    return name .. "-" .. realm
end

-- Get character name
function ELM.Utils:GetCharacterName()
    return UnitName("player")
end

-- Get realm name
function ELM.Utils:GetRealmName()
    return GetRealmName()
end

-- Get character class
function ELM.Utils:GetCharacterClass()
    local _, classFile = UnitClass("player")
    return classFile
end

-- Get character spec
function ELM.Utils:GetCharacterSpec()
    local specID = GetSpecialization()
    if specID then
        local _, name = GetSpecializationInfo(specID)
        return name
    end
    return "Unknown"
end

-- Get character level
function ELM.Utils:GetCharacterLevel()
    return UnitLevel("player")
end

-- Get character race (file format)
function ELM.Utils:GetCharacterRace()
    local _, raceFile = UnitRace("player")
    return raceFile
end

-- Get talent export string
function ELM.Utils:GetTalentString()
    local configID = C_ClassTalents.GetActiveConfigID()
    if not configID then return nil end
    return C_Traits.GenerateImportString(configID)
end

-- Get average item level
function ELM.Utils:GetItemLevel()
    local overall, equipped = GetAverageItemLevel()
    return equipped, overall
end

-- Get item info from item link
function ELM.Utils:GetItemInfo(itemLink)
    if not itemLink then return nil end

    local itemID = GetItemInfoInstant(itemLink)
    if not itemID then return nil end

    local name, _, quality, ilvl, _, _, _, _, equipLoc, icon = C_Item.GetItemInfo(itemLink)

    return {
        id = itemID,
        name = name,
        quality = quality,
        itemLevel = ilvl,
        equipLoc = equipLoc,
        icon = icon,
        link = itemLink,
    }
end

-- Get item level from item link
function ELM.Utils:GetItemLevelFromLink(itemLink)
    if not itemLink then return 0 end

    -- Try to get effective item level
    local ilvl = GetDetailedItemLevelInfo(itemLink)
    if ilvl then return ilvl end

    -- Fall back to base item level
    local _, _, _, baseIlvl = C_Item.GetItemInfo(itemLink)
    return baseIlvl or 0
end

-- Get bonus IDs from item link
function ELM.Utils:GetBonusIDs(itemLink)
    if not itemLink then return {} end

    local bonusIDs = {}
    local itemString = itemLink:match("item[%-?%d:]+")

    if itemString then
        local parts = { strsplit(":", itemString) }
        -- Bonus IDs start at position 14 in the item string
        local numBonusIDs = tonumber(parts[14]) or 0
        for i = 1, numBonusIDs do
            local bonusID = tonumber(parts[14 + i])
            if bonusID then
                table.insert(bonusIDs, bonusID)
            end
        end
    end

    return bonusIDs
end

-- Get gem IDs from item link
function ELM.Utils:GetGemIDs(itemLink)
    if not itemLink then return {} end

    local gemIDs = {}
    local itemString = itemLink:match("item[%-?%d:]+")

    if itemString then
        local parts = { strsplit(":", itemString) }
        -- Gem IDs are at positions 4, 5, 6, 7
        for i = 4, 7 do
            local gemID = tonumber(parts[i])
            if gemID and gemID > 0 then
                table.insert(gemIDs, gemID)
            end
        end
    end

    return gemIDs
end

-- Get enchant ID from item link
function ELM.Utils:GetEnchantID(itemLink)
    if not itemLink then return nil end

    local itemString = itemLink:match("item[%-?%d:]+")
    if itemString then
        local parts = { strsplit(":", itemString) }
        local enchantID = tonumber(parts[3])
        if enchantID and enchantID > 0 then
            return enchantID
        end
    end

    return nil
end

-- Check if player is in a raid
function ELM.Utils:IsInRaid()
    return IsInRaid()
end

-- Check if player is raid leader or assistant
function ELM.Utils:IsRaidLeader()
    return UnitIsGroupLeader("player") or UnitIsGroupAssistant("player")
end

-- Get raid roster
function ELM.Utils:GetRaidRoster()
    local roster = {}

    if not IsInRaid() then
        return roster
    end

    for i = 1, GetNumGroupMembers() do
        local name, rank, subgroup, level, class, fileName, zone, online, isDead, role, isML, combatRole = GetRaidRosterInfo(i)
        if name then
            table.insert(roster, {
                name = name,
                rank = rank,
                subgroup = subgroup,
                class = fileName,
                online = online,
                role = combatRole,
                isMasterLooter = isML,
            })
        end
    end

    return roster
end

-- Color text by quality
function ELM.Utils:ColorByQuality(text, quality)
    local color = ELM.QUALITY_COLORS[quality] or ELM.QUALITY_COLORS[1]
    return string.format("|cff%02x%02x%02x%s|r",
        color.r * 255, color.g * 255, color.b * 255, text)
end

-- Color text by FLPS score
function ELM.Utils:ColorByFLPS(text, score)
    local color
    if score >= ELM.FLPS_THRESHOLDS.HIGH then
        color = ELM.FLPS_COLORS.HIGH
    elseif score >= ELM.FLPS_THRESHOLDS.MEDIUM then
        color = ELM.FLPS_COLORS.MEDIUM
    else
        color = ELM.FLPS_COLORS.LOW
    end

    return string.format("|cff%02x%02x%02x%s|r",
        color.r * 255, color.g * 255, color.b * 255, text)
end

-- Format FLPS score for display
function ELM.Utils:FormatFLPS(score)
    return string.format("%.1f%%", score * 100)
end

-- Format timestamp
function ELM.Utils:FormatTimestamp(timestamp)
    if not timestamp then return "Never" end
    return date("%Y-%m-%d %H:%M:%S", timestamp)
end

-- Print to chat
function ELM.Utils:Print(msg)
    print("|cff00ccff[EdgeRush]|r " .. tostring(msg))
end

-- Debug print (only in debug mode)
function ELM.Utils:Debug(msg)
    if ELM.DEBUG then
        print("|cffff9900[EdgeRush Debug]|r " .. tostring(msg))
    end
end

-- Table deep copy
function ELM.Utils:DeepCopy(orig)
    local copy
    if type(orig) == 'table' then
        copy = {}
        for k, v in pairs(orig) do
            copy[k] = self:DeepCopy(v)
        end
    else
        copy = orig
    end
    return copy
end

-- Check if table is empty
function ELM.Utils:IsTableEmpty(t)
    return next(t) == nil
end

-- Get table count
function ELM.Utils:TableCount(t)
    local count = 0
    for _ in pairs(t) do
        count = count + 1
    end
    return count
end
