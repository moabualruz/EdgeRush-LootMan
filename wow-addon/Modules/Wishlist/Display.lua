-- EdgeRush LootMan Wishlist Display
-- Shows player's loot wishlist synced from server

local ADDON_NAME, ELM = ...

ELM.Wishlist = {}

local Wishlist = ELM.Wishlist

local ROW_HEIGHT = 25
local MAX_ITEMS = 20

-- Create the wishlist frame
function Wishlist:Create()
    if self.frame then return self.frame end

    local frame = CreateFrame("Frame", "EdgeRushWishlist", UIParent, "BackdropTemplate")
    frame:SetSize(400, 350)
    frame:SetPoint("CENTER")
    frame:SetMovable(true)
    frame:EnableMouse(true)
    frame:RegisterForDrag("LeftButton")
    frame:SetScript("OnDragStart", frame.StartMoving)
    frame:SetScript("OnDragStop", frame.StopMovingOrSizing)
    frame:SetFrameStrata("HIGH")

    frame:SetBackdrop({
        bgFile = "Interface\\DialogFrame\\UI-DialogBox-Background-Dark",
        edgeFile = "Interface\\DialogFrame\\UI-DialogBox-Border",
        tile = true,
        tileSize = 32,
        edgeSize = 32,
        insets = { left = 8, right = 8, top = 8, bottom = 8 }
    })
    frame:SetBackdropColor(0.1, 0.1, 0.15, 0.95)

    -- Title
    local title = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalLarge")
    title:SetPoint("TOP", frame, "TOP", 0, -15)
    title:SetText("Your Wishlist")
    title:SetTextColor(0, 0.8, 1)

    -- Close button
    local closeBtn = CreateFrame("Button", nil, frame, "UIPanelCloseButton")
    closeBtn:SetPoint("TOPRIGHT", frame, "TOPRIGHT", -5, -5)

    -- Subtitle (last sync)
    local subtitle = frame:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    subtitle:SetPoint("TOP", title, "BOTTOM", 0, -5)
    subtitle:SetTextColor(0.5, 0.5, 0.5)
    frame.subtitle = subtitle

    -- Header row
    local header = CreateFrame("Frame", nil, frame)
    header:SetSize(360, 20)
    header:SetPoint("TOP", subtitle, "BOTTOM", 0, -10)

    local prioHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    prioHeader:SetPoint("LEFT", header, "LEFT", 5, 0)
    prioHeader:SetWidth(30)
    prioHeader:SetText("#")

    local itemHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    itemHeader:SetPoint("LEFT", prioHeader, "RIGHT", 5, 0)
    itemHeader:SetWidth(150)
    itemHeader:SetText("Item")

    local slotHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    slotHeader:SetPoint("LEFT", itemHeader, "RIGHT", 5, 0)
    slotHeader:SetWidth(70)
    slotHeader:SetText("Slot")

    local upgradeHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    upgradeHeader:SetPoint("LEFT", slotHeader, "RIGHT", 5, 0)
    upgradeHeader:SetWidth(60)
    upgradeHeader:SetText("Upgrade")

    local sourceHeader = header:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    sourceHeader:SetPoint("LEFT", upgradeHeader, "RIGHT", 5, 0)
    sourceHeader:SetWidth(70)
    sourceHeader:SetText("Source")

    -- Scroll frame
    local scrollFrame = CreateFrame("ScrollFrame", "EdgeRushWishlistScroll", frame, "UIPanelScrollFrameTemplate")
    scrollFrame:SetPoint("TOPLEFT", header, "BOTTOMLEFT", 0, -5)
    scrollFrame:SetPoint("BOTTOMRIGHT", frame, "BOTTOMRIGHT", -30, 15)

    local scrollChild = CreateFrame("Frame")
    scrollFrame:SetScrollChild(scrollChild)
    scrollChild:SetWidth(360)
    scrollChild:SetHeight(1)

    -- Create rows
    self.rows = {}
    for i = 1, MAX_ITEMS do
        local row = self:CreateRow(scrollChild, i)
        row:SetPoint("TOPLEFT", scrollChild, "TOPLEFT", 0, -((i - 1) * ROW_HEIGHT))
        self.rows[i] = row
    end

    frame:Hide()
    self.frame = frame
    self.scrollChild = scrollChild

    return frame
end

-- Create a wishlist row
function Wishlist:CreateRow(parent, index)
    local row = CreateFrame("Frame", nil, parent)
    row:SetSize(360, ROW_HEIGHT)

    -- Hover highlight
    row:EnableMouse(true)
    row:SetScript("OnEnter", function(f)
        if f.itemLink then
            GameTooltip:SetOwner(f, "ANCHOR_RIGHT")
            GameTooltip:SetHyperlink(f.itemLink)
            GameTooltip:Show()
        end
        f.highlight:Show()
    end)
    row:SetScript("OnLeave", function(f)
        GameTooltip:Hide()
        f.highlight:Hide()
    end)

    local highlight = row:CreateTexture(nil, "BACKGROUND")
    highlight:SetAllPoints()
    highlight:SetColorTexture(1, 1, 1, 0.05)
    highlight:Hide()
    row.highlight = highlight

    -- Priority
    local prio = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    prio:SetPoint("LEFT", row, "LEFT", 5, 0)
    prio:SetWidth(30)
    prio:SetJustifyH("CENTER")
    row.prio = prio

    -- Icon
    local icon = row:CreateTexture(nil, "ARTWORK")
    icon:SetSize(20, 20)
    icon:SetPoint("LEFT", prio, "RIGHT", 5, 0)
    row.icon = icon

    -- Item name
    local itemName = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    itemName:SetPoint("LEFT", icon, "RIGHT", 5, 0)
    itemName:SetWidth(120)
    itemName:SetJustifyH("LEFT")
    row.itemName = itemName

    -- Slot
    local slot = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    slot:SetPoint("LEFT", itemName, "RIGHT", 5, 0)
    slot:SetWidth(70)
    slot:SetJustifyH("LEFT")
    row.slot = slot

    -- Upgrade value
    local upgrade = row:CreateFontString(nil, "OVERLAY", "GameFontNormal")
    upgrade:SetPoint("LEFT", slot, "RIGHT", 5, 0)
    upgrade:SetWidth(60)
    upgrade:SetJustifyH("RIGHT")
    row.upgrade = upgrade

    -- Source
    local source = row:CreateFontString(nil, "OVERLAY", "GameFontNormalSmall")
    source:SetPoint("LEFT", upgrade, "RIGHT", 5, 0)
    source:SetWidth(70)
    source:SetJustifyH("LEFT")
    source:SetTextColor(0.7, 0.7, 0.7)
    row.source = source

    row:Hide()
    return row
end

-- Update wishlist display
function Wishlist:Update()
    if not self.frame then
        self:Create()
    end

    local wishlist = ELM.Addon.db.char.wishlist or {}

    -- Update subtitle
    local lastSync = ELM.Addon.db.char.wishlistLastSync
    if lastSync then
        self.frame.subtitle:SetText("Last synced: " .. ELM.Utils:FormatTimestamp(lastSync))
    else
        self.frame.subtitle:SetText("Not synced - Use desktop client to sync")
    end

    -- Sort by priority
    table.sort(wishlist, function(a, b)
        return (a.priority or 999) < (b.priority or 999)
    end)

    -- Update rows
    for i, row in ipairs(self.rows) do
        local item = wishlist[i]
        if item then
            row.prio:SetText("#" .. i)

            -- Item icon
            if item.icon then
                row.icon:SetTexture(item.icon)
            else
                row.icon:SetTexture("Interface\\Icons\\INV_Misc_QuestionMark")
            end

            -- Item name with quality color
            local nameText = item.itemName or "Unknown"
            if item.quality then
                nameText = ELM.Utils:ColorByQuality(nameText, item.quality)
            end
            row.itemName:SetText(nameText)

            -- Slot
            row.slot:SetText(item.slot or "")

            -- Upgrade value
            if item.upgradeValue and item.upgradeValue > 0 then
                row.upgrade:SetText("|cff00ff00+" .. string.format("%.1f%%", item.upgradeValue) .. "|r")
            else
                row.upgrade:SetText("-")
            end

            -- Source (boss/dungeon)
            row.source:SetText(item.source or "")

            -- Store item link for tooltip
            row.itemLink = item.itemLink

            row:Show()
        else
            row:Hide()
        end
    end

    -- Update scroll height
    self.scrollChild:SetHeight(math.max(#wishlist * ROW_HEIGHT, 1))
end

-- Import wishlist from SavedVariables (synced by desktop client)
function Wishlist:ImportFromSavedVariables()
    -- This is called when the desktop client writes FLPS data
    -- The wishlist should already be in ELM.Addon.db.char.wishlist
    self:Update()
end

-- Get wishlist items
function Wishlist:GetItems()
    return ELM.Addon.db.char.wishlist or {}
end

-- Check if an item is on wishlist
function Wishlist:IsOnWishlist(itemID)
    local wishlist = self:GetItems()
    for _, item in ipairs(wishlist) do
        if item.itemId == itemID then
            return true, item
        end
    end
    return false, nil
end

-- Get upgrade value for an item from wishlist
function Wishlist:GetUpgradeValue(itemID)
    local isOnList, item = self:IsOnWishlist(itemID)
    if isOnList and item then
        return item.upgradeValue or 0
    end
    return 0
end

-- Show the wishlist
function Wishlist:Show()
    if not self.frame then
        self:Create()
    end
    self:Update()
    self.frame:Show()
end

-- Hide the wishlist
function Wishlist:Hide()
    if self.frame then
        self.frame:Hide()
    end
end

-- Toggle the wishlist
function Wishlist:Toggle()
    if self.frame and self.frame:IsShown() then
        self:Hide()
    else
        self:Show()
    end
end
