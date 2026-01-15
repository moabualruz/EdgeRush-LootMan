-- EdgeRush LootMan Gear Export
-- Exports equipped gear to SavedVariables for desktop client sync

local ADDON_NAME, ELM = ...

ELM.GearExport = {}

local GearExport = ELM.GearExport

-- Export all equipped gear
function GearExport:Export()
    local gear = {}

    for slotID, slotInfo in pairs(ELM.SLOTS) do
        local itemLink = GetInventoryItemLink("player", slotID)

        if itemLink then
            local itemData = self:GetItemData(slotID, itemLink)
            if itemData then
                gear[slotID] = itemData
            end
        end
    end

    -- Store in database
    local db = ELM.Addon.db.char
    db.gear = gear
    db.lastExport = time()

    -- Also store character info
    db.characterName = ELM.Utils:GetCharacterName()
    db.realm = ELM.Utils:GetRealmName()
    db.class = ELM.Utils:GetCharacterClass()
    db.spec = ELM.Utils:GetCharacterSpec()
    db.level = ELM.Utils:GetCharacterLevel()

    local equipped, overall = ELM.Utils:GetItemLevel()
    db.itemLevel = equipped
    db.avgItemLevel = overall

    ELM.Utils:Debug("Exported " .. ELM.Utils:TableCount(gear) .. " gear items")

    return gear
end

-- Get detailed item data for a slot
function GearExport:GetItemData(slotID, itemLink)
    if not itemLink then return nil end

    local itemID = GetItemInfoInstant(itemLink)
    if not itemID then return nil end

    local itemLevel = ELM.Utils:GetItemLevelFromLink(itemLink)
    local name, _, quality, _, _, _, _, _, equipLoc, icon = C_Item.GetItemInfo(itemLink)

    -- Get additional item info
    local enchantID = ELM.Utils:GetEnchantID(itemLink)
    local gemIDs = ELM.Utils:GetGemIDs(itemLink)
    local bonusIDs = ELM.Utils:GetBonusIDs(itemLink)

    -- Check if item has sockets
    local stats = GetItemStats(itemLink)
    local hasSocket = stats and (
        stats["EMPTY_SOCKET_RED"] or
        stats["EMPTY_SOCKET_YELLOW"] or
        stats["EMPTY_SOCKET_BLUE"] or
        stats["EMPTY_SOCKET_PRISMATIC"] or
        stats["EMPTY_SOCKET_META"]
    )

    -- Check if enchantable slot
    local enchantableSlots = {
        [1] = true,   -- Head (Arcanum)
        [3] = true,   -- Shoulder (Inscription)
        [5] = true,   -- Chest
        [6] = true,   -- Waist (Belt Buckle)
        [7] = true,   -- Legs (Spellthread/Armor Kit)
        [8] = true,   -- Feet
        [9] = true,   -- Wrist
        [10] = true,  -- Hands
        [11] = true,  -- Ring 1
        [12] = true,  -- Ring 2
        [15] = true,  -- Back
        [16] = true,  -- Main Hand
        [17] = true,  -- Off Hand
    }

    return {
        slotID = slotID,
        slotName = ELM.SLOTS[slotID] and ELM.SLOTS[slotID].name or "Unknown",
        itemID = itemID,
        itemName = name,
        itemLink = itemLink,
        itemLevel = itemLevel,
        quality = quality,
        equipLoc = equipLoc,
        icon = icon,
        enchantID = enchantID,
        gemIDs = gemIDs,
        bonusIDs = bonusIDs,
        hasSocket = hasSocket or false,
        isEnchanted = enchantID ~= nil,
        needsEnchant = enchantableSlots[slotID] and not enchantID,
        needsGem = hasSocket and #gemIDs == 0,
    }
end

-- Get missing enchants report
function GearExport:GetMissingEnchants()
    local missing = {}

    for slotID, slotInfo in pairs(ELM.SLOTS) do
        local itemLink = GetInventoryItemLink("player", slotID)
        if itemLink then
            local itemData = self:GetItemData(slotID, itemLink)
            if itemData and itemData.needsEnchant then
                table.insert(missing, {
                    slot = slotInfo.name,
                    item = itemData.itemName,
                    itemLink = itemLink,
                })
            end
        end
    end

    return missing
end

-- Get missing gems report
function GearExport:GetMissingGems()
    local missing = {}

    for slotID, slotInfo in pairs(ELM.SLOTS) do
        local itemLink = GetInventoryItemLink("player", slotID)
        if itemLink then
            local itemData = self:GetItemData(slotID, itemLink)
            if itemData and itemData.needsGem then
                table.insert(missing, {
                    slot = slotInfo.name,
                    item = itemData.itemName,
                    itemLink = itemLink,
                })
            end
        end
    end

    return missing
end

-- Print gear report to chat
function GearExport:PrintReport()
    local equipped, overall = ELM.Utils:GetItemLevel()
    ELM.Utils:Print("Gear Report:")
    print("  Equipped iLvl: " .. string.format("%.1f", equipped))
    print("  Average iLvl: " .. string.format("%.1f", overall))

    local missingEnchants = self:GetMissingEnchants()
    if #missingEnchants > 0 then
        print("  Missing Enchants:")
        for _, item in ipairs(missingEnchants) do
            print("    - " .. item.slot .. ": " .. item.itemLink)
        end
    else
        print("  All enchants applied!")
    end

    local missingGems = self:GetMissingGems()
    if #missingGems > 0 then
        print("  Missing Gems:")
        for _, item in ipairs(missingGems) do
            print("    - " .. item.slot .. ": " .. item.itemLink)
        end
    else
        print("  All gems socketed!")
    end
end
