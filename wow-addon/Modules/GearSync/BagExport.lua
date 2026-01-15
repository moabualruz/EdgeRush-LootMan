-- EdgeRush LootMan Bag Export
-- Exports bag contents for upgrade detection

local ADDON_NAME, ELM = ...

ELM.BagExport = {}

local BagExport = ELM.BagExport

-- Equipment-related equipment locations
local EQUIPMENT_LOCS = {
    "INVTYPE_HEAD",
    "INVTYPE_NECK",
    "INVTYPE_SHOULDER",
    "INVTYPE_CHEST",
    "INVTYPE_ROBE",
    "INVTYPE_WAIST",
    "INVTYPE_LEGS",
    "INVTYPE_FEET",
    "INVTYPE_WRIST",
    "INVTYPE_HAND",
    "INVTYPE_FINGER",
    "INVTYPE_TRINKET",
    "INVTYPE_CLOAK",
    "INVTYPE_WEAPON",
    "INVTYPE_SHIELD",
    "INVTYPE_2HWEAPON",
    "INVTYPE_WEAPONMAINHAND",
    "INVTYPE_WEAPONOFFHAND",
    "INVTYPE_HOLDABLE",
    "INVTYPE_RANGED",
    "INVTYPE_RANGEDRIGHT",
}

-- Export all equippable items in bags
function BagExport:Export()
    local items = {}

    for bag = 0, 4 do
        local numSlots = C_Container.GetContainerNumSlots(bag)
        for slot = 1, numSlots do
            local itemInfo = C_Container.GetContainerItemInfo(bag, slot)
            if itemInfo and itemInfo.hyperlink then
                local itemData = self:GetItemData(itemInfo.hyperlink)
                if itemData and itemData.isEquipment then
                    table.insert(items, itemData)
                end
            end
        end
    end

    -- Store in database
    ELM.Addon.db.char.bags = items

    ELM.Utils:Debug("Exported " .. #items .. " equippable items from bags")

    return items
end

-- Get item data for a bag item
function BagExport:GetItemData(itemLink)
    if not itemLink then return nil end

    local itemID = GetItemInfoInstant(itemLink)
    if not itemID then return nil end

    local name, _, quality, _, _, itemType, itemSubType, _, equipLoc, icon = C_Item.GetItemInfo(itemLink)

    -- Check if this is equipment
    local isEquipment = false
    for _, loc in ipairs(EQUIPMENT_LOCS) do
        if equipLoc == loc then
            isEquipment = true
            break
        end
    end

    if not isEquipment then
        return nil
    end

    -- Check if usable by player's class/armor type
    if not self:CanUse(itemLink) then
        return nil
    end

    local itemLevel = ELM.Utils:GetItemLevelFromLink(itemLink)

    return {
        itemID = itemID,
        itemName = name,
        itemLink = itemLink,
        itemLevel = itemLevel,
        quality = quality,
        equipLoc = equipLoc,
        itemType = itemType,
        itemSubType = itemSubType,
        icon = icon,
        isEquipment = true,
    }
end

-- Check if player can use an item
function BagExport:CanUse(itemLink)
    if not itemLink then return false end

    -- Get item info
    local _, _, _, _, _, itemType, itemSubType = C_Item.GetItemInfo(itemLink)

    if itemType ~= "Armor" and itemType ~= "Weapon" then
        return true -- Not armor or weapon, probably usable
    end

    -- Check armor type for armor items
    if itemType == "Armor" then
        local playerClass = ELM.Utils:GetCharacterClass()

        -- Find what armor type this class can wear
        local maxArmorType
        for armorType, classes in pairs(ELM.ARMOR_TYPES) do
            for _, class in ipairs(classes) do
                if class == playerClass then
                    maxArmorType = armorType
                    break
                end
            end
            if maxArmorType then break end
        end

        -- Check if item armor type matches (simplified check)
        local armorOrder = { CLOTH = 1, LEATHER = 2, MAIL = 3, PLATE = 4 }
        local itemArmorType = itemSubType:upper()

        -- Allow cloth for everyone, then restrict by class
        if itemArmorType == "CLOTH" then return true end
        if maxArmorType == "CLOTH" then return itemArmorType == "CLOTH" end
        if maxArmorType == "LEATHER" then return armorOrder[itemArmorType] and armorOrder[itemArmorType] <= 2 end
        if maxArmorType == "MAIL" then return armorOrder[itemArmorType] and armorOrder[itemArmorType] <= 3 end
        if maxArmorType == "PLATE" then return true end
    end

    return true
end

-- Find potential upgrades in bags
function BagExport:FindUpgrades()
    local upgrades = {}
    local bagItems = self:Export()
    local equippedGear = ELM.Addon.db.char.gear

    for _, bagItem in ipairs(bagItems) do
        -- Find corresponding equipped slot
        local slotID = self:GetSlotForEquipLoc(bagItem.equipLoc)
        if slotID then
            local equippedItem = equippedGear[slotID]
            if equippedItem then
                local ilevelDiff = bagItem.itemLevel - equippedItem.itemLevel
                if ilevelDiff > 0 then
                    table.insert(upgrades, {
                        bagItem = bagItem,
                        equippedItem = equippedItem,
                        slotID = slotID,
                        ilevelGain = ilevelDiff,
                    })
                end
            else
                -- Empty slot, bag item is definitely an upgrade
                table.insert(upgrades, {
                    bagItem = bagItem,
                    equippedItem = nil,
                    slotID = slotID,
                    ilevelGain = bagItem.itemLevel,
                })
            end
        end
    end

    -- Sort by ilevel gain
    table.sort(upgrades, function(a, b)
        return a.ilevelGain > b.ilevelGain
    end)

    return upgrades
end

-- Map equipment location to slot ID
function BagExport:GetSlotForEquipLoc(equipLoc)
    local locToSlot = {
        INVTYPE_HEAD = 1,
        INVTYPE_NECK = 2,
        INVTYPE_SHOULDER = 3,
        INVTYPE_CHEST = 5,
        INVTYPE_ROBE = 5,
        INVTYPE_WAIST = 6,
        INVTYPE_LEGS = 7,
        INVTYPE_FEET = 8,
        INVTYPE_WRIST = 9,
        INVTYPE_HAND = 10,
        INVTYPE_FINGER = 11, -- Will check both ring slots
        INVTYPE_TRINKET = 13, -- Will check both trinket slots
        INVTYPE_CLOAK = 15,
        INVTYPE_WEAPON = 16,
        INVTYPE_SHIELD = 17,
        INVTYPE_2HWEAPON = 16,
        INVTYPE_WEAPONMAINHAND = 16,
        INVTYPE_WEAPONOFFHAND = 17,
        INVTYPE_HOLDABLE = 17,
        INVTYPE_RANGED = 16,
        INVTYPE_RANGEDRIGHT = 16,
    }

    return locToSlot[equipLoc]
end

-- Print upgrades to chat
function BagExport:PrintUpgrades()
    local upgrades = self:FindUpgrades()

    if #upgrades == 0 then
        ELM.Utils:Print("No upgrades found in bags")
        return
    end

    ELM.Utils:Print("Potential upgrades in bags:")
    for i, upgrade in ipairs(upgrades) do
        if i > 5 then break end -- Show top 5

        local slotName = ELM.SLOTS[upgrade.slotID] and ELM.SLOTS[upgrade.slotID].name or "Unknown"
        local currentIlvl = upgrade.equippedItem and upgrade.equippedItem.itemLevel or 0

        print(string.format("  %s: %s (+%d ilvl)",
            slotName,
            upgrade.bagItem.itemLink,
            upgrade.ilevelGain
        ))
    end
end
